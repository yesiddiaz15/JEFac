package com.yediaz.jefac.feature.cafe

import com.yediaz.jefac.core.models.Product

// ─────────────────────────────────────────────
// PANTALLA DE ORDEN
// ─────────────────────────────────────────────

data class OrderUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val tableNumber: Int = 0,
    val tableId: String = "",
    val appointmentId: String? = null,
    val clientName: String = "",
    val orderId: String? = null,
    val items: List<OrderItemUi> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val activeAppointments: List<ActiveAppointmentUi> = emptyList(),
    val showProductSelector: Boolean = false,
    val pendingCourtesyItemIndex: Int? = null,  // índice del item esperando asignación de cita
    val total: Double = 0.0,
    val error: String? = null
)

data class OrderItemUi(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val isCourtesy: Boolean = false,
    val linkedAppointmentId: String? = null,
    val linkedClientName: String? = null
)

sealed class OrderIntent {
    object LoadOrder : OrderIntent()
    object ShowProductSelector : OrderIntent()
    object HideProductSelector : OrderIntent()
    data class AddProduct(val product: Product, val isCourtesy: Boolean = false) : OrderIntent()
    data class RemoveItem(val itemIndex: Int) : OrderIntent()
    data class LinkCourtesyToAppointment(val itemIndex: Int, val appointment: ActiveAppointmentUi) : OrderIntent()
    data class ShowAppointmentSelector(val itemIndex: Int) : OrderIntent()
    object HideAppointmentSelector : OrderIntent()
    object CloseOrder : OrderIntent()
    object NavigateBack : OrderIntent()
}

sealed class OrderEffect {
    object NavigateBack : OrderEffect()
    object OrderClosed : OrderEffect()
}
