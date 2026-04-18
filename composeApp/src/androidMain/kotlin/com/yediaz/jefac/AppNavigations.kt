package com.yediaz.jefac

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.ui.AppColors
import com.yediaz.jefac.ui.appointments.AppointmentDetailScreen
import com.yediaz.jefac.ui.appointments.AppointmentsScreen
import com.yediaz.jefac.ui.appointments.NewAppointmentScreen
import com.yediaz.jefac.ui.home.HomeScreen

data class NavItem(
    val label: String,
    val icon: ImageVector
)

// ─────────────────────────────────────────────
// ADMIN — acceso total
// ─────────────────────────────────────────────
@Composable
fun AdminNavigation(user: AppUser, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showNewAppointment by remember { mutableStateOf(false) }
    var detailAppointmentId by remember { mutableStateOf<String?>(null) }
    var cafeOrderState by remember { mutableStateOf<Triple<String, Int, String?>?>(null) }

    // Nueva cita
    if (showNewAppointment) {
        NewAppointmentScreen(
            user = user,
            onNavigateBack = { showNewAppointment = false },
            onAppointmentCreated = { showNewAppointment = false }
        )
        return
    }

    // Detalle de cita
    detailAppointmentId?.let { id ->
        AppointmentDetailScreen(
            user = user,
            appointmentId = id,
            onNavigateBack = { detailAppointmentId = null }
        )
        return
    }

    // Orden de cafetería
    cafeOrderState?.let { (tableId, tableNumber, orderId) ->
        com.yediaz.jefac.ui.cafe.OrderScreen(
            user = user,
            tableId = tableId,
            tableNumber = tableNumber,
            existingOrderId = orderId,
            onNavigateBack = { cafeOrderState = null }
        )
        return
    }

    val tabs = listOf(
        NavItem("Inicio", Icons.Filled.Home),
        NavItem("Servicios", Icons.Filled.Person),
        NavItem("Cafetería", Icons.Filled.ShoppingCart),
        NavItem("Finanzas", Icons.Filled.Star)
    )

    Scaffold(
        containerColor = AppColors.BgMain,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.Primary,
                            selectedTextColor = AppColors.Primary,
                            unselectedIconColor = AppColors.TextMuted,
                            unselectedTextColor = AppColors.TextMuted,
                            indicatorColor = Color(0xFFF5EDD6)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    user = user,
                    onNavigateToNewAppointment = { showNewAppointment = true },
                    onNavigateToNewOrder = { /* módulo cafetería - próximo paso */ }
                )

                1 -> AppointmentsScreen(
                    user = user,
                    onNavigateToNewAppointment = { showNewAppointment = true },
                    onNavigateToDetail = { id -> detailAppointmentId = id }
                )

                2 -> com.yediaz.jefac.ui.cafe.CafeScreen(
                    user = user,
                    onNavigateToOrder = { tableId, tableNumber, orderId ->
                        cafeOrderState = Triple(tableId, tableNumber, orderId)
                    }
                )
                3 -> PlaceholderScreen("Finanzas", user, onSignOut)
            }
        }
    }
}

// ─────────────────────────────────────────────
// PROFESSIONAL — solo su agenda
// ─────────────────────────────────────────────
@Composable
fun ProfessionalNavigation(user: AppUser, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        NavItem("Inicio", Icons.Filled.Home),
        NavItem("Mi agenda", Icons.Filled.DateRange)
    )

    Scaffold(
        containerColor = AppColors.BgMain,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.Primary,
                            selectedTextColor = AppColors.Primary,
                            unselectedIconColor = AppColors.TextMuted,
                            unselectedTextColor = AppColors.TextMuted,
                            indicatorColor = Color(0xFFF5EDD6)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AppointmentsScreen(
                    user = user,
                    onNavigateToNewAppointment = { },
                    onNavigateToDetail = { }
                )

                1 -> AppointmentsScreen(
                    user = user,
                    onNavigateToNewAppointment = { },
                    onNavigateToDetail = { }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// CAFE — mesas y pedidos
// ─────────────────────────────────────────────
@Composable
fun CafeNavigation(user: AppUser, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        NavItem("Mesas", Icons.Filled.Home),
        NavItem("Inventario", Icons.Filled.List)
    )

    Scaffold(
        containerColor = AppColors.BgMain,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.Primary,
                            selectedTextColor = AppColors.Primary,
                            unselectedIconColor = AppColors.TextMuted,
                            unselectedTextColor = AppColors.TextMuted,
                            indicatorColor = Color(0xFFF5EDD6)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> PlaceholderScreen("Mesas & Pedidos", user, onSignOut)
                1 -> PlaceholderScreen("Inventario", user, onSignOut)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Placeholder temporal — se reemplaza módulo a módulo
// ─────────────────────────────────────────────
@Composable
fun PlaceholderScreen(
    title: String,
    user: AppUser,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 22.sp, color = AppColors.TextDark)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Hola, ${user.name}", fontSize = 16.sp, color = AppColors.TextMuted)
        Text(text = "Rol: ${user.role}", fontSize = 14.sp, color = AppColors.TextMuted)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
    }
}
