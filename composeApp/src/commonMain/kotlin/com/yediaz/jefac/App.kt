package com.yediaz.jefac

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yediaz.jefac.core.models.AppUser
import com.yediaz.jefac.feature.auth.LoginScreen

@Composable
fun App() {
    var currentUser by remember { mutableStateOf<AppUser?>(null) }

    if (currentUser == null) {
        LoginScreen(onNavigateByRole = { user -> currentUser = user })
    } else {
        when (currentUser!!.role) {
            "admin" -> AdminNavigation(user = currentUser!!, onSignOut = { currentUser = null })
            "professional" -> ProfessionalNavigation(user = currentUser!!, onSignOut = { currentUser = null })
            "cafe" -> CafeNavigation(user = currentUser!!, onSignOut = { currentUser = null })
            else -> LoginScreen(onNavigateByRole = { user -> currentUser = user })
        }
    }
}
