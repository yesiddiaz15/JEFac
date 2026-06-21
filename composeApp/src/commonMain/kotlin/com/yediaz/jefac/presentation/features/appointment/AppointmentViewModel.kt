package com.yediaz.jefac.presentation.features.appointment

import androidx.lifecycle.viewModelScope
import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.AppointmentStatus
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.usecase.appointment.CreateAppointmentUseCase
import com.yediaz.jefac.domain.usecase.appointment.GetAppointmentsByDateRangeUseCase
import com.yediaz.jefac.domain.usecase.appointment.UpdateAppointmentStatusUseCase
import com.yediaz.jefac.domain.usecase.employee.GetActiveEmployeesUseCase
import com.yediaz.jefac.domain.usecase.service.GetActiveServicesUseCase
import com.yediaz.jefac.presentation.components.base.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AppointmentViewModel(
    private val getAppointmentsByDateRangeUseCase: GetAppointmentsByDateRangeUseCase,
    private val createAppointmentUseCase: CreateAppointmentUseCase,
    private val updateAppointmentStatusUseCase: UpdateAppointmentStatusUseCase,
    private val getActiveEmployeesUseCase: GetActiveEmployeesUseCase,
    private val getActiveServicesUseCase: GetActiveServicesUseCase
) : BaseViewModel<AppointmentState, AppointmentIntent, AppointmentEffect>(
    initialState = AppointmentState()
) {

    init {
        emitIntent(AppointmentIntent.LoadAppointments)
        loadCatalogs()
    }

    override fun handleIntent(intent: AppointmentIntent) {
        when (intent) {
            AppointmentIntent.LoadAppointments -> loadTodayAppointments()
            AppointmentIntent.OnCreateClicked -> showForm()
            AppointmentIntent.OnDismissForm -> hideForm()

            is AppointmentIntent.OnEmployeeSelected ->
                updateState { copy(selectedEmployeeId = intent.employeeId) }

            is AppointmentIntent.OnServiceSelected -> onServiceSelected(intent.serviceId)

            is AppointmentIntent.OnClientNameChanged ->
                updateState { copy(clientName = intent.value) }

            is AppointmentIntent.OnClientPhoneChanged ->
                updateState { copy(clientPhone = intent.value) }

            is AppointmentIntent.OnDateSelected ->
                updateState { copy(selectedDateMillis = intent.millis) }

            is AppointmentIntent.OnDurationChanged ->
                updateState { copy(durationMinutes = intent.minutes) }

            AppointmentIntent.OnSaveAppointment -> saveAppointment()

            is AppointmentIntent.OnStatusChanged ->
                updateStatus(intent.appointmentId, intent.newStatus)
        }
    }

    private fun loadCatalogs() {
        getActiveEmployeesUseCase()
            .onEach { result ->
                when (result) {
                    is DomainResult.Success ->
                        updateState { copy(employees = result.data) }

                    is DomainResult.Error ->
                        showSnackbar(result.message)
                }
            }
            .launchIn(viewModelScope)

        getActiveServicesUseCase()
            .onEach { result ->
                when (result) {
                    is DomainResult.Success ->
                        updateState { copy(services = result.data) }

                    is DomainResult.Error ->
                        showSnackbar(result.message)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadTodayAppointments() {
        val (start, end) = todayRangeMillis()

        getAppointmentsByDateRangeUseCase(start, end)
            .onEach { result ->
                when (result) {
                    is DomainResult.Success -> updateState {
                        copy(isLoading = false, appointments = result.data)
                    }

                    is DomainResult.Error -> {
                        updateState { copy(isLoading = false) }
                        showSnackbar(result.message)
                    }
                }
            }
            .launchIn(viewModelScope)

        updateState { copy(isLoading = true) }
    }

    private fun showForm() {
        updateState { copy(isFormVisible = true, formError = null) }
    }

    private fun hideForm() {
        updateState {
            copy(
                isFormVisible = false,
                selectedEmployeeId = "",
                selectedServiceId = "",
                clientName = "",
                clientPhone = "",
                selectedDateMillis = 0L,
                durationMinutes = 30,
                formError = null
            )
        }
    }

    private fun onServiceSelected(serviceId: String) {
        val selectedService = uiState.value.services.firstOrNull { it.id == serviceId }
        updateState {
            copy(
                selectedServiceId = serviceId,
                durationMinutes = selectedService?.durationMinutes ?: durationMinutes
            )
        }
    }

    private fun saveAppointment() {
        val currentState = uiState.value

        val employee =
            currentState.employees.firstOrNull { it.id == currentState.selectedEmployeeId }
        val service = currentState.services.firstOrNull { it.id == currentState.selectedServiceId }

        val appointment = Appointment(
            employeeId = currentState.selectedEmployeeId,
            employeeName = employee?.name.orEmpty(),
            serviceId = currentState.selectedServiceId,
            serviceName = service?.name.orEmpty(),
            servicePrice = service?.price ?: 0.0,
            clientName = currentState.clientName,
            clientPhone = currentState.clientPhone,
            date = currentState.selectedDateMillis,
            durationMinutes = currentState.durationMinutes,
            status = AppointmentStatus.PENDING
        )

        viewModelScope.launch {
            updateState { copy(isSaving = true, formError = null) }

            when (val result = createAppointmentUseCase(appointment)) {
                is DomainResult.Success -> {
                    updateState { copy(isSaving = false) }
                    hideForm()
                    showSnackbar("Cita agendada correctamente")
                    emitEffect(AppointmentEffect.AppointmentSaved)
                }

                is DomainResult.Error -> {
                    updateState { copy(isSaving = false, formError = result.message) }
                }
            }
        }
    }

    private fun updateStatus(appointmentId: String, newStatus: AppointmentStatus) {
        viewModelScope.launch {
            when (val result = updateAppointmentStatusUseCase(appointmentId, newStatus)) {
                is DomainResult.Success -> showSnackbar("Cita actualizada")
                is DomainResult.Error -> showSnackbar(result.message)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun todayRangeMillis(): Pair<Long, Long> {
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date

        val startOfDay = today.atStartOfDayIn(timeZone).toEpochMilliseconds()
        val endOfDay = today.plus(DatePeriod(days = 1))
            .atStartOfDayIn(timeZone)
            .toEpochMilliseconds()

        return startOfDay to endOfDay
    }
}