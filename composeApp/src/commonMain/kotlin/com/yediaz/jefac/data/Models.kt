package com.yediaz.jefac.data

import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val id: String,
    val name: String,
    val owner_email: String
)

@Serializable
data class AppUser(
    val id: String,
    val business_id: String,
    val email: String,
    val name: String,
    val role: String,
    val created_at: String
)

@Serializable
data class Professional(
    val id: String,
    val business_id: String,
    val name: String,
    val role: String,
    val default_commission: Double,
    val is_active: Boolean = true
)

@Serializable
data class Service(
    val id: String,
    val business_id: String,
    val name: String,
    val category: String,
    val base_price: Double,
    val duration_minutes: Int,
    val is_active: Boolean = true
)

@Serializable
data class Client(
    val id: String,
    val business_id: String,
    val name: String,
    val phone: String? = null,
    val notes: String? = null
)

@Serializable
data class Appointment(
    val id: String,
    val business_id: String,
    val client_id: String,
    val service_id: String,
    val professional_id: String? = null,
    val scheduled_at: String,
    val status: String,
    val base_price: Double,
    val discount_type: String? = null,
    val discount_value: Double = 0.0,
    val final_price: Double,
    val commission_pct: Double = 0.0,
    val professional_earn: Double = 0.0,
    val business_earn: Double,
    val has_courtesy_drink: Boolean = false,
    val courtesy_drink_id: String? = null,
    val courtesy_cost: Double = 0.0,
    val notes: String? = null,
    val deposit: Double = 0.0
)

@Serializable
data class AppointmentService(
    val id: String = "",
    val appointment_id: String,
    val service_id: String,
    val base_price: Double
)

@Serializable
data class Product(
    val id: String,
    val business_id: String,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val min_stock: Int,
    val is_available: Boolean = true
)

@Serializable
data class CafeTable(
    val id: String,
    val business_id: String,
    val table_number: Int,
    val status: String
)

@Serializable
data class Order(
    val id: String,
    val business_id: String,
    val table_id: String? = null,
    val appointment_id: String? = null,
    val status: String,
    val total: Double,
    val created_at: String
)

@Serializable
data class OrderItem(
    val id: String,
    val order_id: String,
    val product_id: String,
    val quantity: Int,
    val unit_price: Double,
    val is_courtesy: Boolean = false
)

@Serializable
data class Transaction(
    val id: String,
    val business_id: String,
    val appointment_id: String? = null,
    val order_id: String? = null,
    val type: String,
    val category: String,
    val amount: Double,
    val description: String? = null,
    val date: String
)