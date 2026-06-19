package com.yediaz.jefac.domain.usecase

import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.repository.AuthRepository
import com.yediaz.jefac.domain.util.Validator
import dev.gitlive.firebase.auth.FirebaseUser

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): DomainResult<FirebaseUser> {
        if (!Validator.isValidEmail(email)) {
            return DomainResult.Error("Formato de correo electrónico inválido")
        }
        
        if (!Validator.isValidPassword(password)) {
            return DomainResult.Error("La contraseña debe tener al menos 6 caracteres")
        }

        return try {
            val user = authRepository.signIn(email, password)
            if (user != null) {
                DomainResult.Success(user)
            } else {
                DomainResult.Error("No se pudo iniciar sesión. Por favor, verifica tus credenciales.")
            }
        } catch (e: Exception) {
            DomainResult.Error(mapFirebaseError(e))
        }
    }

    private fun mapFirebaseError(e: Exception): String {
        val message = e.message ?: ""
        return when {
            message.contains("user-not-found") -> "El usuario no existe."
            message.contains("wrong-password") -> "La contraseña es incorrecta."
            message.contains("invalid-email") -> "El formato del correo electrónico es inválido."
            message.contains("user-disabled") -> "Esta cuenta ha sido deshabilitada."
            message.contains("too-many-requests") -> "Demasiados intentos. Por favor, intenta más tarde."
            else -> "Ocurrió un error inesperado. Por favor, intenta de nuevo."
        }
    }
}
