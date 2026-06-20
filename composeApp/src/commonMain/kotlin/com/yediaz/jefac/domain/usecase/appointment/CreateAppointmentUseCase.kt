package com.yediaz.jefac.domain.usecase.appointment

import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.repository.AppointmentRepository

class CreateAppointmentUseCase(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(appointment: Appointment): DomainResult<String> {
        if (appointment.employeeId.isBlank()) {
            return DomainResult.Error("Debes seleccionar un profesional")
        }
        if (appointment.serviceId.isBlank()) {
            return DomainResult.Error("Debes seleccionar un servicio")
        }
        if (appointment.clientName.isBlank()) {
            return DomainResult.Error("El nombre del cliente es obligatorio")
        }
        if (appointment.clientPhone.isBlank()) {
            return DomainResult.Error("El teléfono del cliente es obligatorio")
        }
        if (appointment.date <= 0L) {
            return DomainResult.Error("Debes seleccionar fecha y hora")
        }
        if (appointment.durationMinutes <= 0) {
            return DomainResult.Error("La duración debe ser mayor a cero")
        }
        return repository.createAppointment(appointment)
    }
}