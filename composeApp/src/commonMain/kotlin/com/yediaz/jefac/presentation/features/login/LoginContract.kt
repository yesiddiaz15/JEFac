package com.yediaz.jefac.presentation.features.login

import com.yediaz.jefac.presentation.model.UiEffect
import com.yediaz.jefac.presentation.model.UiIntent
import com.yediaz.jefac.presentation.model.UiState

data class LoginState(
    override val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface LoginIntent : UiIntent {
    data object OnGetStartedClicked : LoginIntent
}

sealed interface LoginEffect : UiEffect {
    data object NavigateToFirstPeriod : LoginEffect
}
