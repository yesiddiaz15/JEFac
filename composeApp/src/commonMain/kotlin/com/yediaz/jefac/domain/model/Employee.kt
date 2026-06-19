package com.yediaz.jefac.domain.model

data class Employee(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val specialties: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = 0L      // epoch millis, sin dependencia de Firebase
)


