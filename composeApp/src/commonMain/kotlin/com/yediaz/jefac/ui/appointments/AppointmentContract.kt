package com.yediaz.jefac.ui.appointments

import com.yediaz.jefac.data.Client
import com.yediaz.jefac.data.Product
import com.yediaz.jefac.data.Professional
import com.yediaz.jefac.data.Service
import com.yediaz.jefac.domain.PricingResult

// ─────────────────────────────────────────────
// LISTA DE CITAS
// ─────────────────────────────────────────────

data class AppointmentListUiState(
    val isLoading: Boolean = false,
    val appointments: List<AppointmentItemUi> = emptyList(),
    val selectedFilter: AppointmentFilter = AppointmentFilter.TODAY,
    val error: String? = null
)

data class AppointmentItemUi(
    val id: String,
    val clientName: String,
    val serviceName: String,
    val professionalName: String,
    val scheduledAt: String,
    val status: String,
    val finalPrice: Double,
    val hasCourtesyDrink: Boolean = false
)

enum class AppointmentFilter { TODAY, WEEK, ALL }

sealed class AppointmentListIntent {
    object LoadAppointments : AppointmentListIntent()
    data class FilterChanged(val filter: AppointmentFilter) : AppointmentListIntent()
    data class OpenAppointment(val id: String) : AppointmentListIntent()
    object NavigateToNewAppointment : AppointmentListIntent()
}

sealed class AppointmentListEffect {
    data class NavigateToDetail(val id: String) : AppointmentListEffect()
    object NavigateToNewAppointment : AppointmentListEffect()
}

// ─────────────────────────────────────────────
// NUEVA CITA / EDITAR CITA
// ─────────────────────────────────────────────

data class NewAppointmentUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    // Datos del formulario
    val selectedClient: Client? = null,
    val selectedService: Service? = null,
    val selectedProfessional: Professional? = null,
    val hasProfessional: Boolean = true,
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val discountType: String? = null,       // "percentage" | "fixed" | null
    val discountValue: Double = 0.0,
    val notes: String = "",

    // Bebida cortesía
    val hasCourtesyDrink: Boolean = false,
    val selectedDrink: Product? = null,

    // Desglose de precios (calculado automáticamente)
    val pricing: PricingResult? = null,

    // Listas para los selectores
    val availableClients: List<Client> = emptyList(),
    val availableServices: List<Service> = emptyList(),
    val availableProfessionals: List<Professional> = emptyList(),
    val availableDrinks: List<Product> = emptyList(),

    // Errores por campo
    val clientError: String? = null,
    val serviceError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val generalError: String? = null
)

sealed class NewAppointmentIntent {
    // Cargar datos iniciales
    object LoadInitialData : NewAppointmentIntent()

    // Selecciones del formulario
    data class SelectClient(val client: Client) : NewAppointmentIntent()
    data class SelectService(val service: Service) : NewAppointmentIntent()
    data class SelectProfessional(val professional: Professional) : NewAppointmentIntent()
    object ToggleProfessional : NewAppointmentIntent()
    data class SetDate(val date: String) : NewAppointmentIntent()
    data class SetTime(val time: String) : NewAppointmentIntent()
    data class SetDiscountType(val type: String?) : NewAppointmentIntent()
    data class SetDiscountValue(val value: Double) : NewAppointmentIntent()
    data class SetNotes(val notes: String) : NewAppointmentIntent()

    // Bebida cortesía
    object ToggleCourtesyDrink : NewAppointmentIntent()
    data class SelectDrink(val drink: Product?) : NewAppointmentIntent()

    // Confirmar
    object ConfirmAppointment : NewAppointmentIntent()
    object ClearErrors : NewAppointmentIntent()
}

sealed class NewAppointmentEffect {
    object AppointmentCreated : NewAppointmentEffect()
    object NavigateBack : NewAppointmentEffect()
    data class ShowError(val message: String) : NewAppointmentEffect()
}

// ─────────────────────────────────────────────
// DETALLE DE CITA ACTIVA
// ─────────────────────────────────────────────

data class AppointmentDetailUiState(
    val isLoading: Boolean = false,
    val appointmentId: String = "",
    val clientName: String = "",
    val serviceName: String = "",
    val professionalName: String = "",
    val scheduledAt: String = "",
    val status: String = "",
    val pricing: PricingResult? = null,
    val hasCourtesyDrink: Boolean = false,
    val selectedDrink: Product? = null,
    val availableDrinks: List<Product> = emptyList(),
    val notes: String = "",
    val error: String? = null
)

sealed class AppointmentDetailIntent {
    data class LoadAppointment(val id: String) : AppointmentDetailIntent()
    object ToggleCourtesyDrink : AppointmentDetailIntent()
    data class SelectDrink(val drink: Product?) : AppointmentDetailIntent()
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
