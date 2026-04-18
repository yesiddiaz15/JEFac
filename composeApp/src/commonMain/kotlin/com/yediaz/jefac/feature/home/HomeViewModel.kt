package com.yediaz.jefac.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yediaz.jefac.core.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val businessId: String,
    private val homeRepository: HomeRepository = HomeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        handleIntent(HomeIntent.LoadDashboard)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadDashboard -> loadDashboard()
            is HomeIntent.RefreshDashboard -> loadDashboard()
            is HomeIntent.NavigateToNewAppointment -> navigateToNewAppointment()
            is HomeIntent.NavigateToNewOrder -> navigateToNewOrder()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val appointmentsDeferred = async { homeRepository.getTodayAppointments(businessId) }
            val incomeDeferred = async { homeRepository.getTodayIncome(businessId) }
            val expensesDeferred = async { homeRepository.getTodayExpenses(businessId) }
            val tablesDeferred = async { homeRepository.getCafeTables(businessId) }
            val weeklyDeferred = async { homeRepository.getWeeklySummary(businessId) }

            val appointments = appointmentsDeferred.await()
            val income = incomeDeferred.await()
            val expenses = expensesDeferred.await()
            val tables = tablesDeferred.await()
            val weekly = weeklyDeferred.await()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    todayAppointments = when (appointments) {
                        is Result.Success -> appointments.data
                        is Result.Error -> state.todayAppointments
                    },
                    todayAppointmentsCount = when (appointments) {
                        is Result.Success -> appointments.data.size
                        is Result.Error -> 0
                    },
                    pendingAppointmentsCount = when (appointments) {
                        is Result.Success -> appointments.data.count {
                            it.status == "pending"
                        }

                        is Result.Error -> 0
                    },
                    todayIncome = when (income) {
                        is Result.Success -> income.data
                        is Result.Error -> 0.0
                    },
                    todayExpenses = when (expenses) {
                        is Result.Success -> expenses.data
                        is Result.Error -> 0.0
                    },
                    cafeTables = when (tables) {
                        is Result.Success -> tables.data
                        is Result.Error -> state.cafeTables
                    },
                    weeklyIncome = when (weekly) {
                        is Result.Success -> weekly.data
                        is Result.Error -> state.weeklyIncome
                    },
                    error = listOf(appointments, income, expenses, tables, weekly)
                        .filterIsInstance<Result.Error>()
                        .firstOrNull()?.message
                )
            }
        }
    }

    private fun navigateToNewAppointment() {
        viewModelScope.launch {
            _effects.send(HomeEffect.NavigateToNewAppointment)
        }
    }

    private fun navigateToNewOrder() {
        viewModelScope.launch {
            _effects.send(HomeEffect.NavigateToNewOrder)
        }
    }
}
