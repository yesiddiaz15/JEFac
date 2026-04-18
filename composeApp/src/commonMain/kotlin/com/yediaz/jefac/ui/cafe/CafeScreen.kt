package com.yediaz.jefac.ui.cafe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.data.CafeTable
import com.yediaz.jefac.data.Order
import com.yediaz.jefac.ui.AppColors
import com.yediaz.jefac.viewmodel.CafeViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CafeScreen(
    user: AppUser,
    refreshKey: Int = 0,
    onNavigateToOrder: (tableId: String?, tableNumber: Int, orderId: String?, appointmentId: String?, clientName: String) -> Unit = { _, _, _, _, _ -> }
) {
    val viewModel: CafeViewModel = viewModel(factory = CafeViewModel.Factory(user.business_id))
    val uiState by viewModel.uiState.collectAsState()

    // Recargar cuando volvemos de una orden
    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) viewModel.handleIntent(CafeIntent.LoadTables)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CafeEffect.NavigateToOrder ->
                    onNavigateToOrder(effect.tableId, effect.tableNumber, effect.orderId, effect.appointmentId, effect.clientName)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgMain)
    ) {
        // Header
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 16.dp)) {
            Text("Cafetería", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
            Text("Mesas", fontSize = 12.sp, color = AppColors.TextMuted, modifier = Modifier.padding(top = 2.dp))
        }

        // Leyenda de estados
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(Color(0xFFD4F0D4), AppColors.CourtesyGreen, "Libre")
            LegendItem(Color(0xFFF5E8E8), AppColors.Expense, "Ocupada")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("Error al cargar mesas", fontSize = 15.sp, color = AppColors.Expense)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.error ?: "", fontSize = 12.sp, color = AppColors.TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.handleIntent(CafeIntent.LoadTables) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) { Text("Reintentar", color = AppColors.OnPrimary) }
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // ── Mesas ──────────────────────
                item {
                    val chunked = uiState.tables.chunked(3)
                    chunked.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { table ->
                                val order = uiState.activeOrders[table.id]
                                Box(modifier = Modifier.weight(1f)) {
                                    TableCard(
                                        table = table,
                                        order = order,
                                        onClick = { viewModel.handleIntent(CafeIntent.SelectTable(table)) }
                                    )
                                }
                            }
                            // Relleno si la fila tiene menos de 3
                            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }

                // ── Citas activas ───────────────
                if (uiState.activeAppointments.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp)) {
                            Text(
                                "CITAS EN CURSO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextMuted,
                                letterSpacing = 0.7.sp
                            )
                            Text("Toca para agregar productos a su cuenta", fontSize = 11.sp, color = AppColors.TextLight)
                        }
                    }
                    items(uiState.activeAppointments) { appt ->
                        AppointmentOrderCard(
                            appointment = appt,
                            onClick = { viewModel.handleIntent(CafeIntent.SelectAppointment(appt)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(bg: Color, textColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(bg)
                .border(0.5.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
        )
        Text(label, fontSize = 11.sp, color = AppColors.TextMuted)
    }
}

@Composable
private fun TableCard(table: CafeTable, order: Order?, onClick: () -> Unit) {
    val isOccupied = table.status == "occupied"
    val isCourtesy = table.status == "courtesy"

    val bgColor = when {
        isCourtesy -> Color(0xFFFFF8E6)
        isOccupied -> Color(0xFFFDF2F2)
        else       -> Color(0xFFF2FBF2)
    }
    val borderColor = when {
        isCourtesy -> AppColors.PrimaryLight
        isOccupied -> AppColors.Expense.copy(alpha = 0.4f)
        else       -> AppColors.CourtesyGreen.copy(alpha = 0.5f)
    }
    val numberColor = when {
        isCourtesy -> AppColors.PrimaryDark
        isOccupied -> AppColors.Expense
        else       -> AppColors.CourtesyGreen
    }

    Card(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Número de mesa
            Text(
                text = "${table.table_number}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor
            )

            Column {
                // Estado
                Text(
                    text = when {
                        isCourtesy -> "Cortesía"
                        isOccupied -> "Ocupada"
                        else       -> "Libre"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = numberColor
                )
                // Total si está ocupada
                if (order != null && order.total > 0) {
                    Text(
                        text = "$${order.total.toLong()}",
                        fontSize = 11.sp,
                        color = AppColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentOrderCard(appointment: ActiveAppointmentUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(androidx.compose.foundation.shape.CircleShape)
                    .background(AppColors.BgSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(appointment.clientName.take(2).uppercase(), fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, color = AppColors.PrimaryDark)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.clientName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
                Text(appointment.serviceName, fontSize = 11.sp, color = AppColors.TextMuted)
            }
            Text("+ Agregar", fontSize = 12.sp, color = AppColors.Primary, fontWeight = FontWeight.Medium)
        }
    }
}
