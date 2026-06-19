package com.yediaz.jefac.domain.model

data class Sale(
    val id: String = "",
    val items: List<SaleItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val clientId: String? = null,
    val clientName: String? = null,
    val appointmentId: String? = null,
    val createdAt: Long = 0L
)