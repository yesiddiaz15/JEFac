package com.yediaz.jefac.feature.appointments

import com.yediaz.jefac.core.models.Client
import com.yediaz.jefac.core.models.Product
import com.yediaz.jefac.core.models.Professional
import com.yediaz.jefac.core.models.Service
import com.yediaz.jefac.feature.finance.PricingResult

data class NewAppointmentUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    // Datos del formulario
    val selectedClient: Client? = null,
    val selectedServices: List<Service> = emptyList(),
    val totalBasePrice: Double = 0.0,
    val selectedProfessional: Professional? = null,
    val hasProfessional: Boolean = true,
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val discountType: String? = null,       // "percentage" | "fixed" | null
    val discountValue: Double = 0.0,
    val depositAmount: Double = 0.0,
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
    val generalError: String? = null,
    val showCreateClient: Boolean = false,
    val newClientName: String = "",
    val newClientPhone: String = "",
    val newClientError: String? = null,
    val isCreatingClient: Boolean = false
)

sealed class NewAppointmentIntent {
    // Cargar datos iniciales
    object LoadInitialData : NewAppointmentIntent()

    // Selecciones del formulario
    data class SelectClient(val client: Client) : NewAppointmentIntent()
    data class ToggleService(val service: Service) : NewAppointmentIntent()
    data class SelectProfessional(val professional: Professional) : NewAppointmentIntent()
    object ToggleProfessional : NewAppointmentIntent()
    data class SetDate(val date: String) : NewAppointmentIntent()
    data class SetTime(val time: String) : NewAppointmentIntent()
    data class SetDiscountType(val type: String?) : NewAppointmentIntent()
    data class SetDiscountValue(val value: Double) : NewAppointmentIntent()
    data class SetDeposit(val amount: Double) : NewAppointmentIntent()
    data class SetNotes(val notes: String) : NewAppointmentIntent()

    // Bebida cortesía
    object ToggleCourtesyDrink : NewAppointmentIntent()
    data class SelectDrink(val drink: Product?) : NewAppointmentIntent()

    // Confirmar
    object ConfirmAppointment : NewAppointmentIntent()
    object ClearErrors : NewAppointmentIntent()
    object ShowCreateClient : NewAppointmentIntent()
    object HideCreateClient : NewAppointmentIntent()
    data class NewClientNameChanged(val value: String) : NewAppointmentIntent()
    data class NewClientPhoneChanged(val value: String) : NewAppointmentIntent()
    object ConfirmCreateClient : NewAppointmentIntent()
}

sealed class NewAppointmentEffect {
    object AppointmentCreated : NewAppointmentEffect()
    object NavigateBack : NewAppointmentEffect()
    data class ShowError(val message: String) : NewAppointmentEffect()
}
