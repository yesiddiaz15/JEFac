package com.yediaz.jefac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.repository.AuthRepository
import com.yediaz.jefac.ui.login.AuthEffect
import com.yediaz.jefac.ui.login.AuthIntent
import com.yediaz.jefac.ui.login.AuthUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        checkExistingSession()
    }

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged -> onEmailChanged(intent.value)
            is AuthIntent.PasswordChanged -> onPasswordChanged(intent.value)
            is AuthIntent.SignIn -> signIn()
            is AuthIntent.ClearErrors -> clearErrors()
        }
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> _effects.send(AuthEffect.NavigateByRole(result.data))
                is Result.Error -> {}
            }
        }
    }

    private fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = null, generalError = null)
        }
    }

    private fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = null, generalError = null)
        }
    }

    private fun signIn() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Ingresa tu correo") }
            return
        }
        if (!state.email.contains("@")) {
            _uiState.update { it.copy(emailError = "Correo inválido") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Ingresa tu contraseña") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Mínimo 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            when (val result = authRepository.signIn(
                email = state.email.trim(),
                password = state.password
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.send(AuthEffect.NavigateByRole(result.data))
                }

                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, generalError = result.message) }
                }
            }
        }
    }

    private fun clearErrors() {
        _uiState.update {
            it.copy(emailError = null, passwordError = null, generalError = null)
        }
    }
}