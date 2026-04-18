package com.yediaz.jefac.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    @SerialName("business_id") val businessId: String,
    @SerialName("appointment_id") val appointmentId: String? = null,
    @SerialName("order_id") val orderId: String? = null,
    val type: String,
    val category: String,
    val amount: Double,
    val description: String? = null,
    val date: String
)
