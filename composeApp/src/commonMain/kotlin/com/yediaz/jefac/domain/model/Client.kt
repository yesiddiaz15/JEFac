package com.yediaz.jefac.domain.model

data class Client(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String? = null,
    val isRegistered: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = 0L
)