package com.yediaz.jefac.feature.auth

import com.yediaz.jefac.core.models.AppUser

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed class AuthIntent {
    data class EmailChanged(val value: String) : AuthIntent()
    data class PasswordChanged(val value: String) : AuthIntent()
    object SignIn : AuthIntent()
    object ClearErrors : AuthIntent()
}

sealed class AuthEffect {
    data class NavigateByRole(val user: AppUser) : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
}
