package com.yediaz.jefac.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yediaz.jefac.presentation.features.appointment.AppointmentScreen
import com.yediaz.jefac.presentation.features.dashboard.DashboardScreen
import com.yediaz.jefac.presentation.features.login.LoginScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Dashboard) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Dashboard> {
            DashboardScreen(
                onNavigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Dashboard) { inclusive = true }
                    }
                },
                onNavigateToAppointments = {
                    navController.navigate(Appointment)
                }
            )
        }

        composable<Appointment> {
            AppointmentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}