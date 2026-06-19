package com.yediaz.jefac.domain.model

enum class ProductCategory {
    DRINK, FOOD, OTHER;

    companion object {
        fun fromString(value: String): ProductCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}