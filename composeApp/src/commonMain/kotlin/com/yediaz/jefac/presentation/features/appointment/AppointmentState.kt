package com.yediaz.jefac.presentation.features.appointment

import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.AppointmentStatus
import com.yediaz.jefac.domain.model.Employee
import com.yediaz.jefac.domain.model.Service
import com.yediaz.jefac.presentation.model.UiEffect
import com.yediaz.jefac.presentation.model.UiIntent
import com.yediaz.jefac.presentation.model.UiState

data class AppointmentState(
    override val isLoading: Boolean = false,
    val appointments: List<Appointment> = emptyList(),
    val employees: List<Employee> = emptyList(),
    val services: List<Service> = emptyList(),
    val isFormVisible: Boolean = false,
    val selectedEmployeeId: String = "",
    val selectedServiceId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val selectedDateMillis: Long = 0L,
    val durationMinutes: Int = 30,
    val isSaving: Boolean = false,
    val formError: String? = null
) : UiState

sealed interface AppointmentIntent : UiIntent {
    data object LoadAppointments : AppointmentIntent
    data object OnCreateClicked : AppointmentIntent
    data object OnDismissForm : AppointmentIntent

    data class OnEmployeeSelected(val employeeId: String) : AppointmentIntent
    data class OnServiceSelected(val serviceId: String) : AppointmentIntent
    data class OnClientNameChanged(val value: String) : AppointmentIntent
    data class OnClientPhoneChanged(val value: String) : AppointmentIntent
    data class OnDateSelected(val millis: Long) : AppointmentIntent
    data class OnDurationChanged(val minutes: Int) : AppointmentIntent

    data object OnSaveAppointment : AppointmentIntent
    data class OnStatusChanged(
        val appointmentId: String,
        val newStatus: AppointmentStatus
    ) : AppointmentIntent
}

sealed interface AppointmentEffect : UiEffect {
    data object AppointmentSaved : AppointmentEffect
}