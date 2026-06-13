package com.yediaz.jefac.presentation.features.login

import com.yediaz.jefac.presentation.model.UiEffect
import com.yediaz.jefac.presentation.model.UiIntent
import com.yediaz.jefac.presentation.model.UiState

data class LoginState(
    override val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val error: String? = null
) : UiState

sealed interface LoginIntent : UiIntent {
    data class OnEmailChanged(val email: String) : LoginIntent
    data class OnPasswordChanged(val password: String) : LoginIntent
    data object OnLoginClicked : LoginIntent
}

sealed interface LoginEffect : UiEffect {
    data object NavigateToFirstPeriod : LoginEffect
}
