package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.Employee
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeDto(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val specialties: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = 0L
)

fun EmployeeDto.toDomain() = Employee(
    id = id,
    name = name,
    phone = phone,
    email = email,
    specialties = specialties,
    isActive = isActive,
    createdAt = createdAt
)

fun Employee.toDto() = EmployeeDto(
    id = id,
    name = name,
    phone = phone,
    email = email,
    specialties = specialties,
    isActive = isActive,
    createdAt = createdAt
)