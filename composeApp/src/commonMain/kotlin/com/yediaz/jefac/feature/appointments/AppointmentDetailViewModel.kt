package com.yediaz.jefac.feature.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.core.Result
import com.yediaz.jefac.core.models.Product
import com.yediaz.jefac.feature.cafe.CafeRepository
import com.yediaz.jefac.feature.finance.PricingCalculator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class AppointmentDetailViewModel(
    private val appointmentId: String,
    private val repository: AppointmentRepository = AppointmentRepository(),
    private val cafeRepository: CafeRepository = CafeRepository(),
    private val pricing: PricingCalculator = PricingCalculator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<AppointmentDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        handleIntent(AppointmentDetailIntent.LoadAppointment(appointmentId))
    }

    fun handleIntent(intent: AppointmentDetailIntent) {
        when (intent) {
            is AppointmentDetailIntent.LoadAppointment -> loadAppointment(intent.id)
            is AppointmentDetailIntent.ToggleCourtesyDrink -> toggleCourtesyDrink()
            is AppointmentDetailIntent.SelectDrink -> selectDrink(intent.drink)
            is AppointmentDetailIntent.UpdateStatus -> updateStatus(intent.status)
            is AppointmentDetailIntent.CompleteAppointment -> completeAppointment()
            is AppointmentDetailIntent.CancelAppointment -> cancelAppointment()
            is AppointmentDetailIntent.ShowCafeSelector -> _uiState.update { it.copy(showCafeSelector = true) }
            is AppointmentDetailIntent.HideCafeSelector -> _uiState.update { it.copy(showCafeSelector = false) }
            is AppointmentDetailIntent.AddCafeProduct -> addCafeProduct(intent.product, intent.isCourtesy)
            is AppointmentDetailIntent.RemoveCafeItem -> removeCafeItem(intent.index)
        }
    }

    private fun loadAppointment(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = repository.getAppointmentById(id)) {
                is Result.Success -> {
                    val appt = result.data

                    // Calcular el desglose de precios
                    val pricingResult = pricing.calculate(
                        basePrice = appt.basePrice,
                        discountType = appt.discountType,
                        discountValue = appt.discountValue,
                        commissionPct = appt.commissionPct
                    )

                    // Cargar productos (bebida cortesía + menú cafetería)
                    val drinks = cafeRepository.getProducts(appt.businessId)

                    // Cargar nombres de cliente, servicio y profesional
                    val clientName = getClientName(appt.clientId, appt.businessId)
                    val apptServices = repository.getAppointmentServices(appt.id, appt.businessId)
                    val serviceNames = when (apptServices) {
                        is Result.Success -> apptServices.data.map { it.name }
                        is Result.Error   -> listOf(getServiceName(appt.serviceId, appt.businessId))
                    }
                    val proName = appt.professionalId?.let {
                        getProfessionalName(it, appt.businessId)
                    } ?: ""

                    // Bebida cortesía seleccionada
                    val selectedDrink =
                        if (appt.hasCourtesyDrink && appt.courtesyDrinkId != null) {
                            when (drinks) {
                                is Result.Success -> drinks.data.find { it.id == appt.courtesyDrinkId }
                                else -> null
                            }
                        } else null

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            appointmentId = appt.id,
                            clientName = clientName,
                            serviceNames = serviceNames,
                            professionalName = proName,
                            scheduledAt = appt.scheduledAt,
                            status = appt.status,
                            pricing = pricingResult,
                            deposit = appt.deposit,
                            hasCourtesyDrink = appt.hasCourtesyDrink,
                            selectedDrink = selectedDrink,
                            availableDrinks = when (drinks) {
                                is Result.Success -> drinks.data
                                else -> emptyList()
                            },
                            notes = appt.notes ?: ""
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun toggleCourtesyDrink() {
        val current = _uiState.value
        viewModelScope.launch {
            val newDrink = if (current.hasCourtesyDrink) null else current.selectedDrink
            _uiState.update { it.copy(hasCourtesyDrink = !it.hasCourtesyDrink) }
            repository.setCourtesyDrink(appointmentId, newDrink)
        }
    }

    private fun selectDrink(drink: Product?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedDrink = drink) }
            repository.setCourtesyDrink(appointmentId, drink)
        }
    }

    private fun updateStatus(status: String) {
        viewModelScope.launch {
            when (repository.updateAppointmentStatus(appointmentId, status)) {
                is Result.Success -> _uiState.update { it.copy(status = status) }
                is Result.Error -> {}
            }
        }
    }

    private fun completeAppointment() {
        viewModelScope.launch {
            when (repository.updateAppointmentStatus(appointmentId, "completed")) {
                is Result.Success -> {
                    _uiState.update { it.copy(status = "completed") }
                    _effects.send(AppointmentDetailEffect.AppointmentCompleted)
                }

                is Result.Error -> {}
            }
        }
    }

    private fun cancelAppointment() {
        viewModelScope.launch {
            when (repository.updateAppointmentStatus(appointmentId, "cancelled")) {
                is Result.Success -> {
                    _uiState.update { it.copy(status = "cancelled") }
                    _effects.send(AppointmentDetailEffect.AppointmentCancelled)
                }

                is Result.Error -> {}
            }
        }
    }

    // ─────────────────────────────────────────
    // Helpers para obtener nombres
    // ─────────────────────────────────────────
    private suspend fun getClientName(clientId: String, businessId: String): String {
        return when (val result = repository.getClients(businessId)) {
            is Result.Success -> result.data.find { it.id == clientId }?.name ?: "Cliente"
            is Result.Error -> "Cliente"
        }
    }

    private suspend fun getServiceName(serviceId: String, businessId: String): String {
        return when (val result = repository.getServices(businessId)) {
            is Result.Success -> result.data.find { it.id == serviceId }?.name ?: "Servicio"
            is Result.Error -> "Servicio"
        }
    }

    private suspend fun getProfessionalName(professionalId: String, businessId: String): String {
        return when (val result = repository.getProfessionals(businessId)) {
            is Result.Success -> result.data.find { it.id == professionalId }?.name ?: ""
            is Result.Error -> ""
        }
    }

    private fun addCafeProduct(product: Product, isCourtesy: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            // businessId from the appointment — get or create order linked to this appointment
            val appt = repository.getAppointmentById(appointmentId)
            if (appt is Result.Error) return@launch
            val businessId = (appt as Result.Success).data.businessId

            val orderId = state.cafeOrderId ?: run {
                val orderResult = cafeRepository.getOrCreateOrder(businessId, null, appointmentId)
                if (orderResult is Result.Error) return@launch
                val id = (orderResult as Result.Success).data.id
                _uiState.update { it.copy(cafeOrderId = id) }
                id
            }

            val itemResult = cafeRepository.addItemToOrder(orderId, product, isCourtesy,
                if (isCourtesy) appointmentId else null)
            if (itemResult is Result.Success) {
                val newItems = state.cafeItems + itemResult.data
                _uiState.update { it.copy(cafeItems = newItems, showCafeSelector = false) }
            }
        }
    }

    private fun removeCafeItem(index: Int) {
        val items = _uiState.value.cafeItems
        if (index !in items.indices) return
        viewModelScope.launch {
            cafeRepository.removeOrderItem(items[index].id)
            _uiState.update { it.copy(cafeItems = items.toMutableList().also { l -> l.removeAt(index) }) }
        }
    }

    class Factory(private val appointmentId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return AppointmentDetailViewModel(appointmentId = appointmentId) as T
        }
    }
}
