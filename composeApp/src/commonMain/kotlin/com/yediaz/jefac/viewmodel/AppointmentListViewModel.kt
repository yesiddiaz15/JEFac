package com.yediaz.jefac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.repository.AppointmentRepository
import com.yediaz.jefac.ui.appointments.AppointmentFilter
import com.yediaz.jefac.ui.appointments.AppointmentListEffect
import com.yediaz.jefac.ui.appointments.AppointmentListIntent
import com.yediaz.jefac.ui.appointments.AppointmentListUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        handleIntent(AppointmentListIntent.LoadAppointments)
    }

    fun handleIntent(intent: AppointmentListIntent) {
        when (intent) {
            is AppointmentListIntent.LoadAppointments -> loadAppointments()
            is AppointmentListIntent.FilterChanged -> onFilterChanged(intent.filter)
            is AppointmentListIntent.OpenAppointment -> openAppointment(intent.id)
            is AppointmentListIntent.NavigateToNewAppointment -> navigateToNew()
        }
    }

    private fun loadAppointments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = when (_uiState.value.selectedFilter) {
                AppointmentFilter.TODAY -> repository.getTodayAppointments(businessId)
                AppointmentFilter.WEEK -> repository.getWeekAppointments(businessId)
                AppointmentFilter.ALL -> repository.getWeekAppointments(businessId)
            }

            _uiState.update { state ->
                when (result) {
                    is Result.Success -> state.copy(
                        isLoading = false,
                        appointments = result.data
                    )

                    is Result.Error -> state.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    private fun onFilterChanged(filter: AppointmentFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadAppointments()
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
