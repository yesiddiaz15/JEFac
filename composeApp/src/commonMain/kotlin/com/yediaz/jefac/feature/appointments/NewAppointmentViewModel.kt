package com.yediaz.jefac.feature.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.core.Result
import com.yediaz.jefac.core.models.Appointment
import com.yediaz.jefac.core.models.Client
import com.yediaz.jefac.core.models.Professional
import com.yediaz.jefac.core.models.Service
import com.yediaz.jefac.feature.finance.PricingCalculator
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
            is NewAppointmentIntent.ToggleService -> onServiceToggled(intent.service)
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
            is NewAppointmentIntent.SetDeposit -> _uiState.update { it.copy(depositAmount = intent.amount) }
            is NewAppointmentIntent.SetNotes -> _uiState.update { it.copy(notes = intent.notes) }
            is NewAppointmentIntent.ToggleCourtesyDrink -> toggleCourtesyDrink()
            is NewAppointmentIntent.SelectDrink -> _uiState.update { it.copy(selectedDrink = intent.drink) }
            is NewAppointmentIntent.ConfirmAppointment -> confirmAppointment()
            is NewAppointmentIntent.ClearErrors -> clearErrors()
            is NewAppointmentIntent.ShowCreateClient -> _uiState.update { it.copy(showCreateClient = true) }
            is NewAppointmentIntent.HideCreateClient -> _uiState.update {
                it.copy(
                    showCreateClient = false,
                    newClientName = "",
                    newClientPhone = "",
                    newClientError = null
                )
            }

            is NewAppointmentIntent.NewClientNameChanged -> _uiState.update { it.copy(newClientName = intent.value) }
            is NewAppointmentIntent.NewClientPhoneChanged -> _uiState.update {
                it.copy(
                    newClientPhone = intent.value
                )
            }

            is NewAppointmentIntent.ConfirmCreateClient -> createClient()
        }
    }

    private fun createClient() {
        val state = _uiState.value

        if (state.newClientName.isBlank()) {
            _uiState.update { it.copy(newClientError = "Ingresa el nombre") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingClient = true) }

            val newClient = Client(
                id = "",  // Supabase genera el UUID
                businessId = businessId,
                name = state.newClientName.trim(),
                phone = state.newClientPhone.trim().ifBlank { null }
            )

            when (val result = repository.createClient(newClient)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isCreatingClient = false,
                            showCreateClient = false,
                            newClientName = "",
                            newClientPhone = "",
                            selectedClient = result.data,
                            clientError = null,
                            // Agregar el nuevo cliente a la lista
                            availableClients = it.availableClients + result.data
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingClient = false,
                            newClientError = result.message
                        )
                    }
                }
            }
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

    private fun onServiceToggled(service: Service) {
        val current = _uiState.value.selectedServices
        val updated = if (current.any { it.id == service.id }) {
            current.filter { it.id != service.id }  // deseleccionar
        } else {
            current + service  // seleccionar
        }
        _uiState.update { it.copy(selectedServices = updated) }
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
        if (state.selectedServices.isEmpty()) return

        // Total base = suma de todos los servicios
        val totalBase = state.selectedServices.sumOf { it.basePrice }

        val commissionPct = if (state.hasProfessional) {
            state.selectedProfessional?.defaultCommission ?: 0.0
        } else 0.0

        val result = pricing.calculate(
            basePrice = totalBase,
            discountType = state.discountType,
            discountValue = state.discountValue,
            commissionPct = commissionPct
        )

        _uiState.update {
            it.copy(
                totalBasePrice = totalBase,
                pricing = result
            )
        }
    }

    // ─────────────────────────────────────────
    // Validar y confirmar la cita
    // ─────────────────────────────────────────
    private fun confirmAppointment() {
        val state = _uiState.value

        if (state.selectedClient == null) {
            _uiState.update { it.copy(clientError = "Selecciona un cliente") }
            return
        }
        if (state.selectedServices.isEmpty()) {
            _uiState.update { it.copy(serviceError = "Selecciona al menos un servicio") }
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
            basePrice = state.totalBasePrice
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null) }

            // Usamos el primer servicio como serviceId principal
            // y guardamos todos en appointment_services
            val appointment = Appointment(
                id = "",
                businessId = businessId,
                clientId = state.selectedClient.id,
                serviceId = state.selectedServices.first().id,
                professionalId = state.selectedProfessional?.id,
                scheduledAt = "${state.scheduledDate}T${state.scheduledTime}:00.000Z",
                status = "pending",
                basePrice = pricingResult.basePrice,
                discountType = state.discountType,
                discountValue = pricingResult.discountAmount,
                finalPrice = pricingResult.finalPrice,
                commissionPct = pricingResult.commissionPct,
                professionalEarn = pricingResult.professionalEarn,
                businessEarn = pricingResult.businessEarn,
                hasCourtesyDrink = state.hasCourtesyDrink,
                courtesyDrinkId = state.selectedDrink?.id,
                courtesyCost = state.selectedDrink?.price ?: 0.0,
                notes = state.notes.ifBlank { null },
                deposit = state.depositAmount
            )

            when (val result = repository.createAppointment(
                appointment = appointment,
                services = state.selectedServices
            )) {
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
