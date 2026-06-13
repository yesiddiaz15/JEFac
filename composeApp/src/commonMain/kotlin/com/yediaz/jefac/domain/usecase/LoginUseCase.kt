package com.yediaz.jefac.domain.usecase

import com.yediaz.jefac.domain.repository.AuthRepository
import dev.gitlive.firebase.auth.FirebaseUser

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): FirebaseUser? {
        return authRepository.signIn(email, password)
    }
}
