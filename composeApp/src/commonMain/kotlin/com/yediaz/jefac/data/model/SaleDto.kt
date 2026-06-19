package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.Sale
import kotlinx.serialization.Serializable

@Serializable
data class SaleDto(
    val id: String = "",
    val items: List<SaleItemDto> = emptyList(),
    val totalAmount: Double = 0.0,
    val clientId: String? = null,
    val clientName: String? = null,
    val appointmentId: String? = null,
    val createdAt: Long = 0L
)

fun SaleDto.toDomain() = Sale(
    id = id,
    items = items.map { it.toDomain() },
    totalAmount = totalAmount,
    clientId = clientId,
    clientName = clientName,
    appointmentId = appointmentId,
    createdAt = createdAt
)

fun Sale.toDto() = SaleDto(
    id = id,
    items = items.map { it.toDto() },
    totalAmount = totalAmount,
    clientId = clientId,
    clientName = clientName,
    appointmentId = appointmentId,
    createdAt = createdAt
)