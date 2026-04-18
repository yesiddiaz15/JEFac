package com.yediaz.jefac.feature.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.core.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KClass

class AppointmentListViewModel(
    private val businessId: String,
    private val repository: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentListUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<AppointmentListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        _uiState.update { it.copy(calendarYear = today.year, calendarMonth = today.month.ordinal + 1) }
        handleIntent(AppointmentListIntent.LoadAppointments)
    }

    fun handleIntent(intent: AppointmentListIntent) {
        when (intent) {
            is AppointmentListIntent.LoadAppointments -> loadAppointments()
            is AppointmentListIntent.FilterChanged -> onFilterChanged(intent.filter)
            is AppointmentListIntent.OpenAppointment -> openAppointment(intent.id)
            is AppointmentListIntent.NavigateToNewAppointment -> navigateToNew()
            is AppointmentListIntent.SelectCalendarDay -> onCalendarDaySelected(intent.date)
            is AppointmentListIntent.PrevMonth -> navigateMonth(-1)
            is AppointmentListIntent.NextMonth -> navigateMonth(1)
        }
    }

    private fun loadAppointments() {
        val currentFilter = _uiState.value.selectedFilter
        val currentDate = _uiState.value.selectedDate
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = when {
                currentDate != null -> repository.getAppointmentsForDate(currentDate, businessId)
                currentFilter == AppointmentFilter.TODAY -> repository.getTodayAppointments(businessId)
                currentFilter == AppointmentFilter.WEEK -> repository.getWeekAppointments(businessId)
                else -> repository.getAllAppointments(businessId)
            }

            _uiState.update { state ->
                when (result) {
                    is Result.Success -> state.copy(isLoading = false, appointments = result.data)
                    is Result.Error -> state.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    private fun onFilterChanged(filter: AppointmentFilter) {
        _uiState.update { it.copy(selectedFilter = filter, selectedDate = null) }
        loadAppointments()
    }

    private fun onCalendarDaySelected(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadAppointments()
    }

    private fun navigateMonth(delta: Int) {
        val state = _uiState.value
        val currentFirst = LocalDate(state.calendarYear, state.calendarMonth, 1)
        val newFirst = if (delta > 0) {
            currentFirst.plus(1, DateTimeUnit.MONTH)
        } else {
            currentFirst.minus(1, DateTimeUnit.MONTH)
        }
        _uiState.update { it.copy(calendarYear = newFirst.year, calendarMonth = newFirst.month.ordinal + 1) }
    }

    private fun openAppointment(id: String) {
        viewModelScope.launch {
            _effects.send(AppointmentListEffect.NavigateToDetail(id))
        }
    }

    private fun navigateToNew() {
        viewModelScope.launch {
            _effects.send(AppointmentListEffect.NavigateToNewAppointment)
        }
    }

    class Factory(private val businessId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return AppointmentListViewModel(businessId = businessId) as T
        }
    }
}
