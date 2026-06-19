package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.Client
import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String? = null,
    val isRegistered: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = 0L
)

fun ClientDto.toDomain() = Client(
    id = id,
    name = name,
    phone = phone,
    email = email,
    isRegistered = isRegistered,
    notes = notes,
    createdAt = createdAt
)

fun Client.toDto() = ClientDto(
    id = id,
    name = name,
    phone = phone,
    email = email,
    isRegistered = isRegistered,
    notes = notes,
    createdAt = createdAt
)