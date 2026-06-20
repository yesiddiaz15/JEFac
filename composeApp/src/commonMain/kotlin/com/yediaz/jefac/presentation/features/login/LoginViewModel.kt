package com.yediaz.jefac.presentation.features.login

import androidx.lifecycle.viewModelScope
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.usecase.auth.LoginUseCase
import com.yediaz.jefac.presentation.components.base.BaseViewModel
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginState, LoginIntent, LoginEffect>(
    initialState = LoginState()
) {
    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnEmailChanged -> updateState { copy(email = intent.email, error = null) }
            is LoginIntent.OnPasswordChanged -> updateState { copy(password = intent.password, error = null) }
            is LoginIntent.OnLoginClicked -> login()
        }
    }

    private fun login() {
        val email = uiState.value.email
        val password = uiState.value.password

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            
            when (val result = loginUseCase(email, password)) {
                is DomainResult.Success -> {
                    emitEffect(LoginEffect.NavigateToDashboard)
                }
                is DomainResult.Error -> {
                    updateState { copy(error = result.message) }
                    showSnackbar(result.message)
                }
            }
            
            updateState { copy(isLoading = false) }
        }
    }
}
