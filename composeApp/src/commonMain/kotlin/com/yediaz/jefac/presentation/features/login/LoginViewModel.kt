package com.yediaz.jefac.presentation.features.login

import androidx.lifecycle.viewModelScope
import com.yediaz.jefac.domain.usecase.LoginUseCase
import com.yediaz.jefac.presentation.components.base.BaseViewModel
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginState, LoginIntent, LoginEffect>(
    initialState = LoginState()
) {
    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnEmailChanged -> updateState { copy(email = intent.email) }
            is LoginIntent.OnPasswordChanged -> updateState { copy(password = intent.password) }
            is LoginIntent.OnLoginClicked -> login()
        }
    }

    private fun login() {
        val email = uiState.value.email
        val password = uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            showSnackbar("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val user = loginUseCase(email, password)
                if (user != null) {
                    emitEffect(LoginEffect.NavigateToFirstPeriod)
                } else {
                    showSnackbar("Authentication failed")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred"
                updateState { copy(error = errorMessage) }
                showSnackbar(errorMessage)
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }
}
