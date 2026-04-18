package com.yediaz.jefac.feature.appointments

data class AppointmentListUiState(
    val isLoading: Boolean = false,
    val appointments: List<AppointmentItemUi> = emptyList(),
    val selectedFilter: AppointmentFilter = AppointmentFilter.TODAY,
    val selectedDate: String? = null,
    val calendarYear: Int = 0,
    val calendarMonth: Int = 0,
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
    data class SelectCalendarDay(val date: String) : AppointmentListIntent()
    object PrevMonth : AppointmentListIntent()
    object NextMonth : AppointmentListIntent()
}

sealed class AppointmentListEffect {
    data class NavigateToDetail(val id: String) : AppointmentListEffect()
    object NavigateToNewAppointment : AppointmentListEffect()
}
