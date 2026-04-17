package com.yediaz.jefac.repository

import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json

class AuthRepository {
    suspend fun signIn(email: String, password: String): Result<AppUser> {
        return try {
            // Paso 1: autenticar
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = supabase.auth.currentUserOrNull()?.id
            println("DEBUG userId: $userId")

            if (userId == null) {
                return Result.Error("userId es null")
            }

            // Paso 2: leer tabla users con deserialización manual
            val responseText = supabase.postgrest["users"]
                .select { filter { eq("id", userId) } }
                .data

            println("DEBUG raw response: $responseText")

            val users = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.decodeFromString<List<AppUser>>(responseText)

            val user = users.firstOrNull()
                ?: return Result.Error("Usuario no encontrado en la tabla")

            println("DEBUG user: $user")

            Result.Success(user)

        } catch (e: Exception) {
            println("DEBUG error: ${e.cause}: ${e.message}")
            Result.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cerrar sesión")
        }
    }

    suspend fun getCurrentUser(): Result<AppUser> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.Error("Sin sesión activa")

            val user = supabase.postgrest["users"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<AppUser>()

            Result.Success(user)

        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener usuario")
        }
    }

    private fun mapError(message: String?): String {
        return when {
            message == null -> "Error desconocido"
            message.contains("Invalid login") -> "Correo o contraseña incorrectos"
            message.contains("Email not confirmed") -> "Confirma tu correo antes de ingresar"
            message.contains("network", ignoreCase = true) -> "Sin conexión a internet"
            else -> "Error al iniciar sesión"
        }
    }
}