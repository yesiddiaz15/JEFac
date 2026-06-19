package com.yediaz.jefac.domain.model

data class Service(
    val id: String = "",
    val name: String = "",
    val category: ServiceCategory = ServiceCategory.NAILS,
    val durationMinutes: Int = 30,
    val price: Double = 0.0,
    val allowsCustomDuration: Boolean = false,
    val isActive: Boolean = true
)