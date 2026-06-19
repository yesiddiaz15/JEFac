package com.yediaz.jefac.domain.model

enum class TransactionType {
    INCOME, EXPENSE;

    companion object {
        fun fromString(value: String): TransactionType =
            entries.firstOrNull { it.name == value } ?: INCOME
    }
}