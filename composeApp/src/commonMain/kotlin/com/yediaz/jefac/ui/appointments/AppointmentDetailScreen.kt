package com.yediaz.jefac.ui.appointments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.data.Product
import com.yediaz.jefac.domain.PricingResult
import com.yediaz.jefac.ui.AppColors
import com.yediaz.jefac.viewmodel.AppointmentDetailViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppointmentDetailScreen(
    user: AppUser,
    appointmentId: String,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: AppointmentDetailViewModel = viewModel(
        key = appointmentId,
        factory = AppointmentDetailViewModel.Factory(appointmentId)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AppointmentDetailEffect.NavigateBack -> onNavigateBack()
                is AppointmentDetailEffect.AppointmentCompleted -> onNavigateBack()
                is AppointmentDetailEffect.AppointmentCancelled -> onNavigateBack()
                is AppointmentDetailEffect.ShowError -> {}
            }
        }
    }

    Scaffold(
        containerColor = AppColors.BgMain,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(AppColors.BgCard)
                            .border(0.5.dp, AppColors.Border, RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Volver",
                            tint = AppColors.TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Cita de ${uiState.clientName.ifBlank { "..." }}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                // Badge de estado
                StatusBadgeDetail(uiState.status)
            }
        },
        bottomBar = {
            if (uiState.status !in listOf("completed", "cancelled")) {
                BottomActions(uiState.status, viewModel)
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Info del cliente ─────────
                item { ClientInfoCard(uiState) }

                // ── Info de la cita ──────────
                item { AppointmentInfoCard(uiState) }

                // ── Desglose de precios ──────
                uiState.pricing?.let { pricing ->
                    item {
                        SectionLabel("Desglose de pago")
                        PricingCard(pricing)
                    }
                }

                // ── Bebida cortesía ──────────
                item {
                    SectionLabel("Bebida cortesía")
                    CourtesyDrinkCard(
                        hasCourtesyDrink = uiState.hasCourtesyDrink,
                        selectedDrink = uiState.selectedDrink,
                        availableDrinks = uiState.availableDrinks,
                        isCompleted = uiState.status in listOf("completed", "cancelled"),
                        onToggle = { viewModel.handleIntent(AppointmentDetailIntent.ToggleCourtesyDrink) },
                        onSelectDrink = {
                            viewModel.handleIntent(
                                AppointmentDetailIntent.SelectDrink(
                                    it
                                )
                            )
                        }
                    )
                }

                // ── Notas ────────────────────
                if (uiState.notes.isNotBlank()) {
                    item {
                        SectionLabel("Notas")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
                            border = BorderStroke(0.5.dp, AppColors.Border)
                        ) {
                            Text(
                                text = uiState.notes,
                                fontSize = 13.sp,
                                color = AppColors.TextMuted,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // ── Error ────────────────────
                uiState.error?.let {
                    item {
                        Text(text = it, color = AppColors.Expense, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Componentes internos
// ─────────────────────────────────────────────

@Composable
private fun ClientInfoCard(uiState: AppointmentDetailUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con iniciales
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColors.BgSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.clientName.take(2).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.PrimaryDark
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = uiState.clientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                Text(
                    text = formatTime(uiState.scheduledAt),
                    fontSize = 12.sp,
                    color = AppColors.TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AppointmentInfoCard(uiState: AppointmentDetailUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (uiState.serviceNames.size > 1) {
                // Múltiples servicios: label arriba, chips abajo
                Text(
                    text = "Servicio",
                    fontSize = 12.sp,
                    color = AppColors.TextMuted
                )
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.serviceNames.forEach { name ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.BgSecondary)
                                .border(0.5.dp, AppColors.Border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextDark
                            )
                        }
                    }
                }
            } else {
                InfoRow(
                    label = "Servicio",
                    value = uiState.serviceNames.firstOrNull() ?: ""
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppColors.Border,
                thickness = 0.5.dp
            )
            InfoRow(
                label = "Profesional",
                value = uiState.professionalName.ifBlank { "Negocio propio" }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppColors.Border,
                thickness = 0.5.dp
            )
            InfoRow("Fecha", formatDate(uiState.scheduledAt))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppColors.Border,
                thickness = 0.5.dp
            )
            InfoRow("Hora", formatTime(uiState.scheduledAt))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = AppColors.TextMuted)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextDark
        )
    }
}

@Composable
private fun PricingCard(pricing: PricingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            PricingRow("Precio base", formatCurrency(pricing.basePrice), AppColors.TextDark)
            if (pricing.discountAmount > 0) {
                PricingRow(
                    "Descuento",
                    "- ${formatCurrency(pricing.discountAmount)}",
                    AppColors.DiscountColor
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppColors.Border,
                thickness = 0.5.dp
            )
            PricingRow(
                label = "Total cliente",
                value = formatCurrency(pricing.finalPrice),
                valueColor = AppColors.Primary,
                isBold = true,
                valueFontSize = 16
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (pricing.professionalEarn > 0) {
                PricingRow(
                    "Comisión (${pricing.commissionPct.toInt()}%)",
                    formatCurrency(pricing.professionalEarn),
                    AppColors.Commission
                )
            }
            PricingRow(
                "Ganancia negocio",
                formatCurrency(pricing.businessEarn),
                AppColors.BusinessEarn
            )
        }
    }
}

@Composable
private fun PricingRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    isBold: Boolean = false,
    valueFontSize: Int = 13
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = AppColors.TextMuted)
        Text(
            text = value,
            fontSize = valueFontSize.sp,
            fontWeight = if (isBold) FontWeight.Medium else FontWeight.Normal,
            color = valueColor
        )
    }
}

@Composable
private fun CourtesyDrinkCard(
    hasCourtesyDrink: Boolean,
    selectedDrink: Product?,
    availableDrinks: List<Product>,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onSelectDrink: (Product?) -> Unit
) {
    var showDrinkSelector by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasCourtesyDrink) AppColors.CourtesyBg else AppColors.BgCard
        ),
        border = BorderStroke(
            0.5.dp,
            if (hasCourtesyDrink) AppColors.CourtesyBorder else AppColors.Border
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (hasCourtesyDrink) AppColors.CourtesyBorder
                        else AppColors.BgSecondary
                    ),
                contentAlignment = Alignment.Center
            ) { Text("☕", fontSize = 16.sp) }

            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    text = if (hasCourtesyDrink && selectedDrink != null)
                        selectedDrink.name
                    else if (hasCourtesyDrink) "Seleccionar bebida"
                    else "Sin bebida cortesía",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasCourtesyDrink) AppColors.CourtesyGreen else AppColors.TextDark
                )
                Text(
                    text = if (hasCourtesyDrink && selectedDrink != null)
                        "Costo interno: ${formatCurrency(selectedDrink.price)} · Sin cobro"
                    else if (hasCourtesyDrink) "Toca para seleccionar"
                    else "Sin costo para el cliente",
                    fontSize = 11.sp,
                    color = if (hasCourtesyDrink) AppColors.CourtesyGreen else AppColors.TextMuted,
                    modifier = if (hasCourtesyDrink && selectedDrink == null)
                        Modifier.clickable { showDrinkSelector = true }
                    else Modifier
                )
            }

            if (!isCompleted) {
                Switch(
                    checked = hasCourtesyDrink,
                    onCheckedChange = {
                        onToggle()
                        if (!hasCourtesyDrink) showDrinkSelector = true
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.OnPrimary,
                        checkedTrackColor = AppColors.CourtesyGreen,
                        uncheckedThumbColor = AppColors.OnPrimary,
                        uncheckedTrackColor = AppColors.Border
                    )
                )
            }
        }
    }

    // Selector de bebida como bottom sheet simulado
    if (showDrinkSelector && availableDrinks.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
            border = BorderStroke(0.5.dp, AppColors.Border)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Selecciona la bebida",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                availableDrinks.forEach { drink ->
                    val isSelected = drink.id == selectedDrink?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) AppColors.CourtesyBg
                                else AppColors.BgMain
                            )
                            .clickable {
                                onSelectDrink(drink)
                                showDrinkSelector = false
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = drink.name,
                            fontSize = 13.sp,
                            color = AppColors.TextDark,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(drink.price),
                            fontSize = 12.sp,
                            color = AppColors.TextMuted
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✓", fontSize = 12.sp, color = AppColors.CourtesyGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadgeDetail(status: String) {
    val (bg, textColor, label) = when (status) {
        "confirmed" -> Triple(AppColors.CourtesyBg, AppColors.CourtesyGreen, "Confirmada")
        "in_progress" -> Triple(AppColors.BgSecondary, AppColors.Primary, "En curso")
        "pending" -> Triple(AppColors.BgSecondary, AppColors.PrimaryDark, "Pendiente")
        "completed" -> Triple(AppColors.CourtesyBg, AppColors.CourtesyGreen, "Completada")
        "cancelled" -> Triple(AppColors.BgSecondary, AppColors.Expense, "Cancelada")
        else -> Triple(AppColors.Border, AppColors.TextMuted, status)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BottomActions(
    status: String,
    viewModel: AppointmentDetailViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.BgCard)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        when (status) {
            "pending", "confirmed" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.handleIntent(AppointmentDetailIntent.CancelAppointment) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.5.dp, AppColors.Border)
                    ) {
                        Text("Cancelar", color = AppColors.TextMuted, fontSize = 14.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.handleIntent(
                                AppointmentDetailIntent.UpdateStatus("in_progress")
                            )
                        },
                        modifier = Modifier.weight(2f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            contentColor = AppColors.OnPrimary
                        )
                    ) {
                        Text("Iniciar cita", fontSize = 14.sp)
                    }
                }
            }

            "in_progress" -> {
                Button(
                    onClick = { viewModel.handleIntent(AppointmentDetailIntent.CompleteAppointment) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = AppColors.OnPrimary
                    )
                ) {
                    Text("Completar y cobrar", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.TextMuted,
        letterSpacing = 0.7.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

private fun formatCurrency(amount: Double): String = "$${amount.toLong()}"

private fun formatTime(scheduledAt: String): String =
    try {
        scheduledAt.substring(11, 16)
    } catch (e: Exception) {
        "--:--"
    }

private fun formatDate(scheduledAt: String): String =
    try {
        scheduledAt.substring(0, 10)
    } catch (e: Exception) {
        "---"
    }
