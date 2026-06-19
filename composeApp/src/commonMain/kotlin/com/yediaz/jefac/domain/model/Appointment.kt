package com.yediaz.jefac.domain.model

data class Appointment(
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
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String? = null,
    val createdAt: Long = 0L
)