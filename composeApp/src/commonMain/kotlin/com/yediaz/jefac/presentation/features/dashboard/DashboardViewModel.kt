package com.yediaz.jefac.presentation.features.dashboard

import com.yediaz.jefac.domain.repository.AuthRepository
import com.yediaz.jefac.presentation.components.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel<DashboardState, DashboardIntent, DashboardEffect>(
    initialState = DashboardState()
) {
    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = authRepository.currentUser
        updateState { copy(userName = user?.email ?: "User") }
    }

    override fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.OnLogoutClicked -> logout()
            DashboardIntent.OnNavToAppointments -> onNavToAppointments()
        }
    }

    private fun onNavToAppointments() {
        viewModelScope.launch {
            emitEffect(DashboardEffect.NavigateToAppointments)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            emitEffect(DashboardEffect.NavigateToLogin)
        }
    }
}
