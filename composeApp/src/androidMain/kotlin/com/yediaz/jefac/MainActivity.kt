package com.yediaz.jefac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.ui.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppNavigation() }
    }
}

@Composable
fun AppNavigation() {
    var currentUser by remember { mutableStateOf<AppUser?>(null) }

    if (currentUser == null) {
        LoginScreen(
            onNavigateByRole = { user -> currentUser = user }
        )
    } else {
        when (currentUser!!.role) {
            "admin" -> AdminNavigation(user = currentUser!!, onSignOut = { currentUser = null })
            "professional" -> ProfessionalNavigation(
                user = currentUser!!,
                onSignOut = { currentUser = null })

            "cafe" -> CafeNavigation(user = currentUser!!, onSignOut = { currentUser = null })
        }
    }
}