package com.yediaz.jefac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.data.Product
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.domain.PricingCalculator
import com.yediaz.jefac.repository.AppointmentRepository
import com.yediaz.jefac.ui.appointments.AppointmentDetailEffect
import com.yediaz.jefac.ui.appointments.AppointmentDetailIntent
import com.yediaz.jefac.ui.appointments.AppointmentDetailUiState
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
                        basePrice = appt.base_price,
                        discountType = appt.discount_type,
                        discountValue = appt.discount_value,
                        commissionPct = appt.commission_pct
                    )

                    // Cargar productos disponibles para la bebida cortesía
                    val drinks = repository.getAvailableProducts(appt.business_id)

                    // Cargar nombres de cliente, servicio y profesional
                    val clientName = getClientName(appt.client_id, appt.business_id)
                    val apptServices = repository.getAppointmentServices(appt.id, appt.business_id)
                    val serviceNames = when (apptServices) {
                        is Result.Success -> apptServices.data.map { it.name }
                        is Result.Error   -> listOf(getServiceName(appt.service_id, appt.business_id))
                    }
                    val proName = appt.professional_id?.let {
                        getProfessionalName(it, appt.business_id)
                    } ?: ""

                    // Bebida cortesía seleccionada
                    val selectedDrink =
                        if (appt.has_courtesy_drink && appt.courtesy_drink_id != null) {
                            when (drinks) {
                                is Result.Success -> drinks.data.find { it.id == appt.courtesy_drink_id }
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
                            scheduledAt = appt.scheduled_at,
                            status = appt.status,
                            pricing = pricingResult,
                            hasCourtesyDrink = appt.has_courtesy_drink,
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

    class Factory(private val appointmentId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return AppointmentDetailViewModel(appointmentId = appointmentId) as T
        }
    }
}
