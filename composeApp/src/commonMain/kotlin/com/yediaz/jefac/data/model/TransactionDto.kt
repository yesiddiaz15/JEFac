package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.Transaction
import com.yediaz.jefac.domain.model.TransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(

    val id: String = "",
    val type: String = "INCOME",
    val category: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val sourceId: String? = null,
    val sourceType: String? = null,
    val date: Long = 0L,
    val createdAt: Long = 0L
)

fun TransactionDto.toDomain() = Transaction(
    id = id,
    type = TransactionType.fromString(type),
    category = category,
    amount = amount,
    description = description,
    sourceId = sourceId,
    sourceType = sourceType,
    date = date,
    createdAt = createdAt
)

fun Transaction.toDto() = TransactionDto(
    id = id,
    type = type.name,
    category = category,
    amount = amount,
    description = description,
    sourceId = sourceId,
    sourceType = sourceType,
    date = date,
    createdAt = createdAt
)