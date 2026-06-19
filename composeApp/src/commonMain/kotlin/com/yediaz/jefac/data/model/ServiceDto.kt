package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.Service
import com.yediaz.jefac.domain.model.ServiceCategory
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDto(
    val id: String = "",
    val name: String = "",
    val category: String = "NAILS",
    val durationMinutes: Int = 30,
    val price: Double = 0.0,
    val allowsCustomDuration: Boolean = false,
    val isActive: Boolean = true
)

fun ServiceDto.toDomain() = Service(
    id = id,
    name = name,
    category = ServiceCategory.fromString(category),
    durationMinutes = durationMinutes,
    price = price,
    allowsCustomDuration = allowsCustomDuration,
    isActive = isActive
)

fun Service.toDto() = ServiceDto(
    id = id,
    name = name,
    category = category.name,
    durationMinutes = durationMinutes,
    price = price,
    allowsCustomDuration = allowsCustomDuration,
    isActive = isActive
)