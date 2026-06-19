package com.yediaz.jefac.domain.model

enum class ServiceCategory {
    NAILS, MASSAGE, OTHER;

    companion object {
        fun fromString(value: String): ServiceCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}