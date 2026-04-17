package com.yediaz.jefac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.data.Appointment
import com.yediaz.jefac.data.Client
import com.yediaz.jefac.data.Professional
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.data.Service
import com.yediaz.jefac.domain.PricingCalculator
import com.yediaz.jefac.repository.AppointmentRepository
import com.yediaz.jefac.ui.appointments.NewAppointmentEffect
import com.yediaz.jefac.ui.appointments.NewAppointmentIntent
import com.yediaz.jefac.ui.appointments.NewAppointmentUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class NewAppointmentViewModel(
    private val businessId: String,
    private val repository: AppointmentRepository = AppointmentRepository(),
    private val pricing: PricingCalculator = PricingCalculator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewAppointmentUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<NewAppointmentEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        handleIntent(NewAppointmentIntent.LoadInitialData)
    }

    fun handleIntent(intent: NewAppointmentIntent) {
        when (intent) {
            is NewAppointmentIntent.LoadInitialData -> loadInitialData()
            is NewAppointmentIntent.SelectClient -> onClientSelected(intent.client)
            is NewAppointmentIntent.SelectService -> onServiceSelected(intent.service)
            is NewAppointmentIntent.SelectProfessional -> onProfessionalSelected(intent.professional)
            is NewAppointmentIntent.ToggleProfessional -> toggleProfessional()
            is NewAppointmentIntent.SetDate -> _uiState.update {
                it.copy(
                    scheduledDate = intent.date,
                    dateError = null
                )
            }

            is NewAppointmentIntent.SetTime -> _uiState.update {
                it.copy(
                    scheduledTime = intent.time,
                    timeError = null
                )
            }

            is NewAppointmentIntent.SetDiscountType -> onDiscountTypeChanged(intent.type)
            is NewAppointmentIntent.SetDiscountValue -> onDiscountValueChanged(intent.value)
            is NewAppointmentIntent.SetNotes -> _uiState.update { it.copy(notes = intent.notes) }
            is NewAppointmentIntent.ToggleCourtesyDrink -> toggleCourtesyDrink()
            is NewAppointmentIntent.SelectDrink -> _uiState.update { it.copy(selectedDrink = intent.drink) }
            is NewAppointmentIntent.ConfirmAppointment -> confirmAppointment()
            is NewAppointmentIntent.ClearErrors -> clearErrors()
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val clients = repository.getClients(businessId)
            val services = repository.getServices(businessId)
            val professionals = repository.getProfessionals(businessId)
            val drinks = repository.getAvailableProducts(businessId)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    availableClients = when (clients) {
                        is Result.Success -> clients.data
                        is Result.Error -> emptyList()
                    },
                    availableServices = when (services) {
                        is Result.Success -> services.data
                        is Result.Error -> emptyList()
                    },
                    availableProfessionals = when (professionals) {
                        is Result.Success -> professionals.data
                        is Result.Error -> emptyList()
                    },
                    availableDrinks = when (drinks) {
                        is Result.Success -> drinks.data
                        is Result.Error -> emptyList()
                    }
                )
            }
        }
    }

    private fun onClientSelected(client: Client) {
        _uiState.update { it.copy(selectedClient = client, clientError = null) }
    }

    private fun onServiceSelected(service: Service) {
        _uiState.update { it.copy(selectedService = service, serviceError = null) }
        recalculatePricing()
    }

    private fun onProfessionalSelected(professional: Professional) {
        _uiState.update { it.copy(selectedProfessional = professional) }
        recalculatePricing()
    }

    private fun toggleProfessional() {
        _uiState.update { state ->
            state.copy(
                hasProfessional = !state.hasProfessional,
                selectedProfessional = null
            )
        }
        recalculatePricing()
    }

    private fun onDiscountTypeChanged(type: String?) {
        _uiState.update { it.copy(discountType = type, discountValue = 0.0) }
        recalculatePricing()
    }

    private fun onDiscountValueChanged(value: Double) {
        _uiState.update { it.copy(discountValue = value) }
        recalculatePricing()
    }

    private fun toggleCourtesyDrink() {
        _uiState.update { state ->
            state.copy(
                hasCourtesyDrink = !state.hasCourtesyDrink,
                selectedDrink = if (state.hasCourtesyDrink) null else state.selectedDrink
            )
        }
    }

    // ─────────────────────────────────────────
    // Recalcular precios cada vez que cambia
    // el servicio, profesional o descuento
    // ─────────────────────────────────────────
    private fun recalculatePricing() {
        val state = _uiState.value
        val basePrice = state.selectedService?.base_price ?: return

        val commissionPct = if (state.hasProfessional) {
            state.selectedProfessional?.default_commission ?: 0.0
        } else 0.0

        val result = pricing.calculate(
            basePrice = basePrice,
            discountType = state.discountType,
            discountValue = state.discountValue,
            commissionPct = commissionPct
        )

        _uiState.update { it.copy(pricing = result) }
    }

    // ─────────────────────────────────────────
    // Validar y confirmar la cita
    // ─────────────────────────────────────────
    private fun confirmAppointment() {
        val state = _uiState.value

        // Validaciones
        if (state.selectedClient == null) {
            _uiState.update { it.copy(clientError = "Selecciona una clienta") }
            return
        }
        if (state.selectedService == null) {
            _uiState.update { it.copy(serviceError = "Selecciona un servicio") }
            return
        }
        if (state.scheduledDate.isBlank()) {
            _uiState.update { it.copy(dateError = "Selecciona una fecha") }
            return
        }
        if (state.scheduledTime.isBlank()) {
            _uiState.update { it.copy(timeError = "Selecciona una hora") }
            return
        }

        val pricingResult = state.pricing ?: pricing.calculate(
            basePrice = state.selectedService.base_price
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null) }

            val appointment = Appointment(
                id = "",  // Supabase genera el UUID
                business_id = businessId,
                client_id = state.selectedClient.id,
                service_id = state.selectedService.id,
                professional_id = state.selectedProfessional?.id,
                scheduled_at = "${state.scheduledDate}T${state.scheduledTime}:00.000Z",
                status = "pending",
                base_price = pricingResult.basePrice,
                discount_type = state.discountType,
                discount_value = pricingResult.discountAmount,
                final_price = pricingResult.finalPrice,
                commission_pct = pricingResult.commissionPct,
                professional_earn = pricingResult.professionalEarn,
                business_earn = pricingResult.businessEarn,
                has_courtesy_drink = state.hasCourtesyDrink,
                courtesy_drink_id = state.selectedDrink?.id,
                courtesy_cost = state.selectedDrink?.price ?: 0.0,
                notes = state.notes.ifBlank { null }
            )

            when (val result = repository.createAppointment(appointment)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _effects.send(NewAppointmentEffect.AppointmentCreated)
                }

                is Result.Error -> {
                    _uiState.update { it.copy(isSaving = false, generalError = result.message) }
                }
            }
        }
    }

    private fun clearErrors() {
        _uiState.update {
            it.copy(
                clientError = null,
                serviceError = null,
                dateError = null,
                timeError = null,
                generalError = null
            )
        }
    }

    // Factory
    class Factory(private val businessId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return NewAppointmentViewModel(businessId = businessId) as T
        }
    }
}
