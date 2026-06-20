package com.yediaz.jefac.domain.usecase.appointment

import com.yediaz.jefac.domain.model.AppointmentStatus
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.repository.AppointmentRepository

class UpdateAppointmentStatusUseCase(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(
        appointmentId: String,
        newStatus: AppointmentStatus
    ): DomainResult<Unit> {
        if (appointmentId.isBlank()) {
            return DomainResult.Error("ID de cita inválido")
        }
        return repository.updateAppointmentStatus(appointmentId, newStatus)
    }
}