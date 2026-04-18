package com.yediaz.jefac.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val id: String,
    val name: String,
    @SerialName("owner_email") val ownerEmail: String
)

@Serializable
data class AppUser(
    val id: String,
    @SerialName("business_id") val businessId: String,
    val email: String,
    val name: String,
    val role: String,
    @SerialName("created_at") val createdAt: String
)
