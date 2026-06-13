package com.yediaz.jefac.presentation.model

interface UiState {
    val isLoading: Boolean
}

interface UiIntent

interface UiEffect {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEffect
}
