package com.yediaz.jefac.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

private val Primary = Color(0xFFD4756A)
private val BgMain = Color(0xFFFDF8F5)
private val TextDark = Color(0xFF3D2E27)
private val TextMuted = Color(0xFFB09080)
private val CardBg = Color(0xFFFFFFFF)
private val AccentPink = Color(0xFFE8A89C)
private val AccentBlue = Color(0xFF9FC8E0)
private val AccentGreen = Color(0xFFA3C99A)
private val BorderColor = Color(0xFFF0E6DE)

@Composable
fun HomeScreen(
    user: AppUser,
    onNavigateToNewAppointment: () -> Unit = {},
    onNavigateToNewOrder: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(user.business_id)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is HomeEffect.NavigateToNewAppointment -> onNavigateToNewAppointment()
                is HomeEffect.NavigateToNewOrder -> onNavigateToNewOrder()
                is HomeEffect.ShowError -> {}
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgMain
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── Header ───────────────────
                item { HeaderSection(user, viewModel) }

                // ── Métricas del día ─────────
                item { MetricsSection(uiState) }

                // ── Acciones rápidas ─────────
                item { QuickActionsSection(viewModel) }

                // ── Gráfica semanal ──────────
                item { WeeklyChartSection(uiState) }

                // ── Citas del día ────────────
                item {
                    SectionTitle("Citas de hoy")
                }
                if (uiState.todayAppointments.isEmpty()) {
                    item { EmptyState("No hay citas programadas para hoy") }
                } else {
                    items(uiState.todayAppointments) { appointment ->
                        AppointmentRow(appointment)
                    }
                }

                // ── Mesas de cafetería ───────
                item {
                    SectionTitle("Mesas cafetería")
                }
                item { TablesSection(uiState) }
            }
        }
    }
}

@Composable
private fun HeaderSection(user: AppUser, viewModel: HomeViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola, ${user.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Text(
                text = "Resumen de hoy",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        IconButton(
            onClick = { viewModel.handleIntent(HomeIntent.RefreshDashboard) }
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Actualizar", tint = TextMuted)
        }
    }
}

@Composable
private fun MetricsSection(uiState: HomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Ingresos — tarjeta acento
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Ingresos",
            value = formatCurrency(uiState.todayIncome),
            subtext = "hoy",
            isAccent = true
        )
        // Citas
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Citas",
            value = uiState.todayAppointmentsCount.toString(),
            subtext = "${uiState.pendingAppointmentsCount} pendientes"
        )
        // Mesas
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Mesas",
            value = "${uiState.cafeTables.count { it.status == "occupied" }}/${uiState.cafeTables.size}",
            subtext = "ocupadas"
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String,
    isAccent: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAccent) Primary else CardBg
        ),
        border = if (!isAccent) androidx.compose.foundation.BorderStroke(
            0.5.dp, BorderColor
        ) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isAccent) Color.White.copy(alpha = 0.8f) else TextMuted
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isAccent) Color.White else TextDark,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = if (isAccent) Color.White.copy(alpha = 0.6f) else TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(viewModel: HomeViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Nueva cita
        Button(
            onClick = { viewModel.handleIntent(HomeIntent.NavigateToNewAppointment) },
            modifier = Modifier.weight(2f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Nueva cita", fontSize = 13.sp)
        }
        // Nueva orden
        OutlinedButton(
            onClick = { viewModel.handleIntent(HomeIntent.NavigateToNewOrder) },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor)
        ) {
            Text("Orden", fontSize = 13.sp, color = TextDark)
        }
    }
}

@Composable
private fun WeeklyChartSection(uiState: HomeUiState) {
    if (uiState.weeklyIncome.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ingresos vs Gastos",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(14.dp))

            val maxValue = uiState.weeklyIncome.maxOf { it.income }.coerceAtLeast(1.0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                uiState.weeklyIncome.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Barras
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Barra de ingresos
                            val incomeHeight = ((day.income / maxValue) * 60).coerceAtLeast(4.0)
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(incomeHeight.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(AccentPink)
                            )
                            // Barra de gastos
                            val expenseHeight = ((day.expenses / maxValue) * 60).coerceAtLeast(4.0)
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(expenseHeight.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(BorderColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayLabel,
                            fontSize = 10.sp,
                            color = if (day.dayLabel == "Hoy") Primary else TextMuted
                        )
                    }
                }
            }

            // Leyenda
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = AccentPink, label = "Ingresos")
                LegendItem(color = BorderColor, label = "Gastos")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = TextMuted,
        letterSpacing = 0.7.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun AppointmentRow(appointment: AppointmentSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hora
            Text(
                text = formatTime(appointment.scheduledAt),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                modifier = Modifier.width(40.dp)
            )

            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor(appointment.status))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.clientName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
                Text(
                    text = "${appointment.serviceName} · ${appointment.professionalName}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Precio
            Text(
                text = formatCurrency(appointment.finalPrice),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }
    }
}

@Composable
private fun TablesSection(uiState: HomeUiState) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items(uiState.cafeTables) { table ->
            val isOccupied = table.status == "occupied"
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOccupied) Color(0xFFFDE8E0) else CardBg
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isOccupied) AccentPink else BorderColor
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = "Mesa ${table.tableNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOccupied) Color(0xFF8A3A2E) else TextDark
                    )
                    Text(
                        text = if (isOccupied) formatCurrency(table.currentTotal) else "Libre",
                        fontSize = 11.sp,
                        color = if (isOccupied) Primary else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, fontSize = 13.sp, color = TextMuted)
    }
}

// ─────────────────────────────────────────────
// Helpers de formato
// ─────────────────────────────────────────────
private fun formatCurrency(amount: Double): String {
    val rounded = amount.toLong()
    return "$$rounded"
}

private fun formatTime(scheduledAt: String): String {
    return try {
        // Extraer HH:mm del timestamp ISO
        scheduledAt.substring(11, 16)
    } catch (e: Exception) {
        "--:--"
    }
}

private fun statusColor(status: String): Color {
    return when (status) {
        "confirmed" -> Color(0xFFA3C99A)
        "in_progress" -> Color(0xFFE8A89C)
        "pending" -> Color(0xFFF4C88A)
        "completed" -> Color(0xFF9FC8E0)
        else -> Color(0xFFD8CEC8)
    }
}
