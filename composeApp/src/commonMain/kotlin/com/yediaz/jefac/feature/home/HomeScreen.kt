package com.yediaz.jefac.feature.home

import androidx.compose.foundation.BorderStroke
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
import com.yediaz.jefac.core.models.AppUser
import com.yediaz.jefac.core.ui.AppColors
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    user: AppUser,
    refreshKey: Int = 0,
    onNavigateToNewAppointment: () -> Unit = {},
    onNavigateToNewOrder: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(user.businessId)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) viewModel.handleIntent(HomeIntent.LoadDashboard)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is HomeEffect.NavigateToNewAppointment -> onNavigateToNewAppointment()
                is HomeEffect.NavigateToNewOrder -> onNavigateToNewOrder()
                is HomeEffect.ShowError -> {}
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.BgMain) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { HeaderSection(user, viewModel) }
                item { MetricsSection(uiState) }
                item { QuickActionsSection(viewModel) }
                item { WeeklyChartSection(uiState) }
                item { SectionTitle("Citas de hoy") }
                if (uiState.todayAppointments.isEmpty()) {
                    item { EmptyState("No hay citas programadas para hoy") }
                } else {
                    items(uiState.todayAppointments) { AppointmentRow(it) }
                }
                item { SectionTitle("Mesas cafetería") }
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
                color = AppColors.TextDark
            )
            Text(
                text = "Resumen de hoy",
                fontSize = 12.sp,
                color = AppColors.TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        IconButton(onClick = { viewModel.handleIntent(HomeIntent.RefreshDashboard) }) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Actualizar",
                tint = AppColors.TextMuted
            )
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
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Ingresos",
            value = formatCurrency(uiState.todayIncome),
            subtext = "hoy",
            isAccent = true
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Citas",
            value = uiState.todayAppointmentsCount.toString(),
            subtext = "${uiState.pendingAppointmentsCount} pendientes"
        )
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
            containerColor = if (isAccent) AppColors.Primary else AppColors.BgCard
        ),
        border = if (!isAccent) BorderStroke(0.5.dp, AppColors.Border) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isAccent) AppColors.OnPrimary.copy(alpha = 0.8f) else AppColors.TextMuted
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isAccent) AppColors.OnPrimary else AppColors.TextDark,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = if (isAccent) AppColors.OnPrimary.copy(alpha = 0.6f) else AppColors.TextMuted,
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
        Button(
            onClick = { viewModel.handleIntent(HomeIntent.NavigateToNewAppointment) },
            modifier = Modifier.weight(2f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary,
                contentColor = AppColors.OnPrimary
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Nueva cita", fontSize = 13.sp)
        }
        OutlinedButton(
            onClick = { viewModel.handleIntent(HomeIntent.NavigateToNewOrder) },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.5.dp, AppColors.Border)
        ) {
            Text("Orden", fontSize = 13.sp, color = AppColors.TextDark)
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
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ingresos vs Gastos",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextDark
            )
            Spacer(modifier = Modifier.height(14.dp))

            val maxValue = uiState.weeklyIncome.maxOf { it.income }.coerceAtLeast(1.0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                uiState.weeklyIncome.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val incomeHeight = ((day.income / maxValue) * 60).coerceAtLeast(4.0)
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(incomeHeight.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(AppColors.Primary)
                            )
                            val expenseHeight = ((day.expenses / maxValue) * 60).coerceAtLeast(4.0)
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(expenseHeight.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(AppColors.Border)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayLabel,
                            fontSize = 10.sp,
                            color = if (day.dayLabel == "Hoy") AppColors.Primary else AppColors.TextMuted
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = AppColors.Primary, label = "Ingresos")
                LegendItem(color = AppColors.Border, label = "Gastos")
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
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, fontSize = 11.sp, color = AppColors.TextMuted)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.TextMuted,
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
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(appointment.scheduledAt),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextMuted,
                modifier = Modifier.width(40.dp)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor(appointment.status))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.clientName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                Text(
                    text = "${appointment.serviceName} · ${appointment.professionalName}",
                    fontSize = 11.sp,
                    color = AppColors.TextMuted
                )
            }
            Text(
                text = formatCurrency(appointment.finalPrice),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Primary
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
                    containerColor = if (isOccupied) AppColors.BgSecondary else AppColors.BgCard
                ),
                border = BorderStroke(
                    0.5.dp,
                    if (isOccupied) AppColors.PrimaryLight else AppColors.Border
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = "Mesa ${table.tableNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOccupied) AppColors.PrimaryDark else AppColors.TextDark
                    )
                    Text(
                        text = if (isOccupied) formatCurrency(table.currentTotal) else "Libre",
                        fontSize = 11.sp,
                        color = if (isOccupied) AppColors.Primary else AppColors.TextMuted
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
        Text(text = message, fontSize = 13.sp, color = AppColors.TextMuted)
    }
}

private fun formatCurrency(amount: Double): String = "$${amount.toLong()}"
private fun formatTime(scheduledAt: String): String =
    try {
        scheduledAt.substring(11, 16)
    } catch (e: Exception) {
        "--:--"
    }

private fun statusColor(status: String): Color = when (status) {
    "confirmed" -> AppColors.StatusConfirmed
    "in_progress" -> AppColors.StatusInProgress
    "pending" -> AppColors.StatusPending
    "completed" -> AppColors.StatusCompleted
    else -> AppColors.Border
}
