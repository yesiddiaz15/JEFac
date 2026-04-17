package com.yediaz.jefac.ui.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val todayIncome: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayAppointmentsCount: Int = 0,
    val pendingAppointmentsCount: Int = 0,
    val todayAppointments: List<AppointmentSummary> = emptyList(),
    val cafeTables: List<CafeTableSummary> = emptyList(),
    val weeklyIncome: List<DailySummary> = emptyList(),
    val error: String? = null
)

data class AppointmentSummary(
    val id: String,
    val clientName: String,
    val serviceName: String,
    val professionalName: String,
    val scheduledAt: String,
    val status: String,
    val finalPrice: Double
)

data class CafeTableSummary(
    val id: String,
    val tableNumber: Int,
    val status: String,
    val currentTotal: Double = 0.0
)

data class DailySummary(
    val dayLabel: String,
    val income: Double,
    val expenses: Double
)

sealed class HomeIntent {
    object LoadDashboard : HomeIntent()
    object RefreshDashboard : HomeIntent()
    object NavigateToNewAppointment : HomeIntent()
    object NavigateToNewOrder : HomeIntent()
}

sealed class HomeEffect {
    object NavigateToNewAppointment : HomeEffect()
    object NavigateToNewOrder : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
}