package com.yediaz.jefac.domain.repository

import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.AppointmentStatus
import com.yediaz.jefac.domain.model.DomainResult
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAppointmentsByDateRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<DomainResult<List<Appointment>>>

    suspend fun createAppointment(appointment: Appointment): DomainResult<String>

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): DomainResult<Unit>
}