package com.yediaz.jefac.domain.model

data class Transaction(
    val id: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val category: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val sourceId: String? = null,
    val sourceType: String? = null,
    val date: Long = 0L,
    val createdAt: Long = 0L
)