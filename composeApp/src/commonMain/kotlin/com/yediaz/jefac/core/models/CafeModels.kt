package com.yediaz.jefac.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    @SerialName("business_id") val businessId: String,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    @SerialName("min_stock") val minStock: Int,
    @SerialName("is_available") val isAvailable: Boolean = true
)

@Serializable
data class CafeTable(
    val id: String,
    @SerialName("business_id") val businessId: String,
    @SerialName("table_number") val tableNumber: Int,
    val status: String
)

@Serializable
data class Order(
    val id: String,
    @SerialName("business_id") val businessId: String,
    @SerialName("table_id") val tableId: String? = null,
    @SerialName("appointment_id") val appointmentId: String? = null,
    val status: String,
    val total: Double,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class OrderItem(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("is_courtesy") val isCourtesy: Boolean = false
)
