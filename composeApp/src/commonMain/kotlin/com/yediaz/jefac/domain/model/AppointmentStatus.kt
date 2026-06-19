package com.yediaz.jefac.domain.model

enum class AppointmentStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW;

    companion object {
        fun fromString(value: String): AppointmentStatus =
            entries.firstOrNull { it.name == value } ?: PENDING
    }
}