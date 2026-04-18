package com.yediaz.jefac.feature.appointments

import com.yediaz.jefac.core.models.Product
import com.yediaz.jefac.feature.cafe.OrderItemUi
import com.yediaz.jefac.feature.finance.PricingResult

data class AppointmentDetailUiState(
    val isLoading: Boolean = false,
    val appointmentId: String = "",
    val clientName: String = "",
    val serviceName: String = "",
    val professionalName: String = "",
    val scheduledAt: String = "",
    val status: String = "",
    val pricing: PricingResult? = null,
    val deposit: Double = 0.0,
    val hasCourtesyDrink: Boolean = false,
    val selectedDrink: Product? = null,
    val availableDrinks: List<Product> = emptyList(),
    val notes: String = "",
    val error: String? = null,
    val serviceNames: List<String> = emptyList(),
    // Pedido cafetería
    val cafeItems: List<OrderItemUi> = emptyList(),
    val showCafeSelector: Boolean = false,
    val cafeOrderId: String? = null
)

sealed class AppointmentDetailIntent {
    data class LoadAppointment(val id: String) : AppointmentDetailIntent()
    object ToggleCourtesyDrink : AppointmentDetailIntent()
    data class SelectDrink(val drink: Product?) : AppointmentDetailIntent()
    object ShowCafeSelector : AppointmentDetailIntent()
    object HideCafeSelector : AppointmentDetailIntent()
    data class AddCafeProduct(val product: Product, val isCourtesy: Boolean) : AppointmentDetailIntent()
    data class RemoveCafeItem(val index: Int) : AppointmentDetailIntent()
    data class UpdateStatus(val status: String) : AppointmentDetailIntent()
    object CompleteAppointment : AppointmentDetailIntent()
    object CancelAppointment : AppointmentDetailIntent()
}

sealed class AppointmentDetailEffect {
    object AppointmentCompleted : AppointmentDetailEffect()
    object AppointmentCancelled : AppointmentDetailEffect()
    object NavigateBack : AppointmentDetailEffect()
    data class ShowError(val message: String) : AppointmentDetailEffect()
}
