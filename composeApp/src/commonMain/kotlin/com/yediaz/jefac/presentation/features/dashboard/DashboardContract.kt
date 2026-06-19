package com.yediaz.jefac.presentation.features.dashboard

import com.yediaz.jefac.presentation.model.UiEffect
import com.yediaz.jefac.presentation.model.UiIntent
import com.yediaz.jefac.presentation.model.UiState

data class DashboardState(
    override val isLoading: Boolean = false,
    val userName: String = ""
) : UiState

sealed interface DashboardIntent : UiIntent {
    data object OnLogoutClicked : DashboardIntent
}

sealed interface DashboardEffect : UiEffect {
    data object NavigateToLogin : DashboardEffect
}
