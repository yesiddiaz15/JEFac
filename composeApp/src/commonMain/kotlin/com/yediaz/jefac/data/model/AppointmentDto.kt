package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.AppointmentStatus
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDto(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val servicePrice: Double = 0.0,
    val clientId: String? = null,
    val clientName: String = "",
    val clientPhone: String = "",
    val date: Long = 0L,
    val durationMinutes: Int = 30,
    val status: String = "PENDING",
    val notes: String? = null,
    val createdAt: Long = 0L
)

fun AppointmentDto.toDomain() = Appointment(
    id = id,
    employeeId = employeeId,
    employeeName = employeeName,
    serviceId = serviceId,
    serviceName = serviceName,
    servicePrice = servicePrice,
    clientId = clientId,
    clientName = clientName,
    clientPhone = clientPhone,
    date = date,
    durationMinutes = durationMinutes,
    status = AppointmentStatus.fromString(status),
    notes = notes,
    createdAt = createdAt
)

fun Appointment.toDto() = AppointmentDto(
    id = id,
    employeeId = employeeId,
    employeeName = employeeName,
    serviceId = serviceId,
    serviceName = serviceName,
    servicePrice = servicePrice,
    clientId = clientId,
    clientName = clientName,
    clientPhone = clientPhone,
    date = date,
    durationMinutes = durationMinutes,
    status = status.name,
    notes = notes,
    createdAt = createdAt
)