package com.yediaz.jefac

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.ui.home.HomeScreen

data class NavItem(
    val label: String,
    val icon: ImageVector
)

val primaryColor = Color(0xFFD4756A)

@Composable
fun AdminNavigation(user: AppUser, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        NavItem("Inicio", Icons.Filled.Home),
        NavItem("Servicios", Icons.Filled.Person),
        NavItem("Cafetería", Icons.Filled.ShoppingCart),
        NavItem("Finanzas", Icons.Filled.Star)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = Color(0xFFFDF0EB)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(user = user)
                1 -> PlaceholderScreen("Servicios & Citas", user, onSignOut)
                2 -> PlaceholderScreen("Cafetería", user, onSignOut)
                3 -> PlaceholderScreen("Finanzas", user, onSignOut)
            }
        }
    }
}

@Composable
fun ProfessionalNavigation(user: AppUser, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        NavItem("Inicio", Icons.Filled.Home),
        NavItem("Mi agenda", Icons.Filled.DateRange)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = Color(0xFFFDF0EB)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> PlaceholderScreen("Mis citas de hoy", user, onSignOut)
                1 -> PlaceholderScreen("Mi agenda", user, onSignOut)
            }
        }
    }
}

@Composable
fun CafeNavigation(user: AppUser, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        NavItem("Mesas", Icons.Filled.Home),
        NavItem("Inventario", Icons.Filled.List)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = Color(0xFFFDF0EB)
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

@Composable
private fun PlaceholderScreen(
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
        Text(
            text = title,
            fontSize = 22.sp,
            color = Color(0xFF3D2E27)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Hola, ${user.name}",
            fontSize = 16.sp,
            color = Color(0xFFB09080)
        )
        Text(
            text = "Rol: ${user.role}",
            fontSize = 14.sp,
            color = Color(0xFFB09080)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4756A))
        ) {
            Text("Cerrar sesión")
        }
    }
}