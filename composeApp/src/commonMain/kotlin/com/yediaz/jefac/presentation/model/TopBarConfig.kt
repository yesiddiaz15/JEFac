package com.yediaz.jefac.presentation.model

data class TopBarConfig(
    val title: String,
    val showBackButton: Boolean = false,
    val onBackClick: () -> Unit = {}
)