package com.yediaz.jefac.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Professional(
    val id: String,
    @SerialName("business_id") val businessId: String,
    val name: String,
    val role: String,
    @SerialName("default_commission") val defaultCommission: Double,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Service(
    val id: String,
    @SerialName("business_id") val businessId: String,
    val name: String,
    val category: String,
    @SerialName("base_price") val basePrice: Double,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Client(
    val id: String,
    @SerialName("business_id") val businessId: String,
    val name: String,
    val phone: String? = null,
    val notes: String? = null
)

@Serializable
data class Appointment(
    val id: String,
    @SerialName("business_id") val businessId: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("professional_id") val professionalId: String? = null,
    @SerialName("scheduled_at") val scheduledAt: String,
    val status: String,
    @SerialName("base_price") val basePrice: Double,
    @SerialName("discount_type") val discountType: String? = null,
    @SerialName("discount_value") val discountValue: Double = 0.0,
    @SerialName("final_price") val finalPrice: Double,
    @SerialName("commission_pct") val commissionPct: Double = 0.0,
    @SerialName("professional_earn") val professionalEarn: Double = 0.0,
    @SerialName("business_earn") val businessEarn: Double,
    @SerialName("has_courtesy_drink") val hasCourtesyDrink: Boolean = false,
    @SerialName("courtesy_drink_id") val courtesyDrinkId: String? = null,
    @SerialName("courtesy_cost") val courtesyCost: Double = 0.0,
    val notes: String? = null,
    val deposit: Double = 0.0
)

@Serializable
data class AppointmentService(
    val id: String = "",
    @SerialName("appointment_id") val appointmentId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("base_price") val basePrice: Double
)
