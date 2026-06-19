package com.yediaz.jefac.domain.util

object Validator {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun isValidEmail(email: String): Boolean {
        return email.matches(emailRegex)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
