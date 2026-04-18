package com.yediaz.jefac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.data.Product
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.repository.CafeRepository
import com.yediaz.jefac.ui.cafe.ActiveAppointmentUi
import com.yediaz.jefac.ui.cafe.OrderEffect
import com.yediaz.jefac.ui.cafe.OrderIntent
import com.yediaz.jefac.ui.cafe.OrderUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class OrderViewModel(
    private val businessId: String,
    private val tableId: String?,
    private val tableNumber: Int,
    private val existingOrderId: String?,
    private val appointmentId: String? = null,
    private val clientName: String = "",
    private val repository: CafeRepository = CafeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OrderUiState(
            tableNumber   = tableNumber,
            tableId       = tableId ?: "",
            appointmentId = appointmentId,
            clientName    = clientName
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<OrderEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { handleIntent(OrderIntent.LoadOrder) }

    fun handleIntent(intent: OrderIntent) {
        when (intent) {
            is OrderIntent.LoadOrder              -> loadOrder()
            is OrderIntent.ShowProductSelector    -> _uiState.update { it.copy(showProductSelector = true) }
            is OrderIntent.HideProductSelector    -> _uiState.update { it.copy(showProductSelector = false) }
            is OrderIntent.AddProduct             -> addProduct(intent.product, intent.isCourtesy)
            is OrderIntent.RemoveItem             -> removeItem(intent.itemIndex)
            is OrderIntent.ShowAppointmentSelector -> _uiState.update { it.copy(pendingCourtesyItemIndex = intent.itemIndex) }
            is OrderIntent.HideAppointmentSelector -> _uiState.update { it.copy(pendingCourtesyItemIndex = null) }
            is OrderIntent.LinkCourtesyToAppointment -> linkToAppointment(intent.itemIndex, intent.appointment)
            is OrderIntent.CloseOrder             -> closeOrder()
            is OrderIntent.NavigateBack           -> viewModelScope.launch { _effects.send(OrderEffect.NavigateBack) }
        }
    }

    private fun loadOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val productsResult     = repository.getProducts(businessId)
            val products           = if (productsResult is Result.Success) productsResult.data else emptyList()
            val appointmentsResult = repository.getActiveAppointments(businessId)
            val appointments       = if (appointmentsResult is Result.Success) appointmentsResult.data else emptyList()

            if (existingOrderId != null) {
                val itemsResult = repository.getOrderItems(existingOrderId, products)
                val items       = if (itemsResult is Result.Success) itemsResult.data else emptyList()
                val total       = items.filter { !it.isCourtesy }.sumOf { it.unitPrice * it.quantity }
                _uiState.update {
                    it.copy(isLoading = false, orderId = existingOrderId, items = items,
                        availableProducts = products, activeAppointments = appointments, total = total)
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, availableProducts = products, activeAppointments = appointments)
                }
            }
        }
    }

    private fun addProduct(product: Product, isCourtesy: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showProductSelector = false) }

            val orderId = _uiState.value.orderId ?: run {
                val orderResult = if (tableId != null) {
                    repository.getOrCreateOrder(businessId, tableId, null)
                } else {
                    repository.getOrCreateOrder(businessId, null, appointmentId)
                }
                if (orderResult is Result.Error) {
                    _uiState.update { it.copy(isSaving = false, error = orderResult.message) }
                    return@launch
                }
                val order = (orderResult as Result.Success).data
                _uiState.update { it.copy(orderId = order.id) }
                order.id
            }

            // Para órdenes de cita, vincular automáticamente el ítem a la cita
            val linkedApptId = if (appointmentId != null && isCourtesy) appointmentId else null

            val itemResult = repository.addItemToOrder(orderId, product, isCourtesy, linkedApptId)
            if (itemResult is Result.Success) {
                val newItems = _uiState.value.items + itemResult.data
                val total    = newItems.filter { !it.isCourtesy }.sumOf { it.unitPrice * it.quantity }
                repository.updateOrderTotal(orderId, total)
                _uiState.update { it.copy(isSaving = false, items = newItems, total = total) }
            } else {
                _uiState.update { it.copy(isSaving = false, error = (itemResult as Result.Error).message) }
            }
        }
    }

    private fun removeItem(index: Int) {
        val items = _uiState.value.items
        if (index !in items.indices) return
        val item = items[index]

        viewModelScope.launch {
            repository.removeOrderItem(item.id)
            val newItems = items.toMutableList().also { it.removeAt(index) }
            val total    = newItems.filter { !it.isCourtesy }.sumOf { it.unitPrice * it.quantity }
            val orderId  = _uiState.value.orderId
            if (orderId != null) repository.updateOrderTotal(orderId, total)
            _uiState.update { it.copy(items = newItems, total = total) }
        }
    }

    private fun linkToAppointment(index: Int, appointment: ActiveAppointmentUi) {
        val items = _uiState.value.items
        if (index !in items.indices) return
        val item = items[index]

        viewModelScope.launch {
            repository.linkItemToAppointment(item.id, appointment.id)
            val updated = items.toMutableList()
            updated[index] = item.copy(
                isCourtesy          = true,
                linkedAppointmentId = appointment.id,
                linkedClientName    = appointment.clientName
            )
            _uiState.update { it.copy(items = updated, pendingCourtesyItemIndex = null) }
        }
    }

    private fun closeOrder() {
        val state   = _uiState.value
        val orderId = state.orderId ?: run {
            // Orden vacía — solo navegar atrás
            viewModelScope.launch { _effects.send(OrderEffect.NavigateBack) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = repository.closeOrder(orderId, tableId, state.total, businessId)
            if (result is Result.Success) {
                _effects.send(OrderEffect.OrderClosed)
            } else {
                _uiState.update { it.copy(isSaving = false, error = (result as Result.Error).message) }
            }
        }
    }

    class Factory(
        private val businessId: String,
        private val tableId: String?,
        private val tableNumber: Int,
        private val existingOrderId: String?,
        private val appointmentId: String? = null,
        private val clientName: String = ""
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(businessId, tableId, tableNumber, existingOrderId, appointmentId, clientName) as T
        }
    }
}
