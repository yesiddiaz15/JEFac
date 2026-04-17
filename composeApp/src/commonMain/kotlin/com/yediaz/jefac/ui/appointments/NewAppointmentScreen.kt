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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.data.Client
import com.yediaz.jefac.data.Product
import com.yediaz.jefac.data.Professional
import com.yediaz.jefac.data.Service
import com.yediaz.jefac.domain.PricingResult
import com.yediaz.jefac.ui.AppColors
import com.yediaz.jefac.viewmodel.NewAppointmentViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NewAppointmentScreen(
    user: AppUser,
    onNavigateBack: () -> Unit = {},
    onAppointmentCreated: () -> Unit = {}
) {
    val viewModel: NewAppointmentViewModel = viewModel(
        factory = NewAppointmentViewModel.Factory(user.business_id)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is NewAppointmentEffect.AppointmentCreated -> onAppointmentCreated()
                is NewAppointmentEffect.NavigateBack -> onNavigateBack()
                is NewAppointmentEffect.ShowError -> {}
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
                verticalAlignment = Alignment.CenterVertically
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
                    text = "Nueva cita",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.BgCard)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { viewModel.handleIntent(NewAppointmentIntent.ConfirmAppointment) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = AppColors.OnPrimary
                    ),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = AppColors.OnPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirmar cita", fontSize = 16.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FieldLabel("Cliente")
                ClientSelector(
                    clients = uiState.availableClients,
                    selected = uiState.selectedClient,
                    error = uiState.clientError,
                    onSelect = { viewModel.handleIntent(NewAppointmentIntent.SelectClient(it)) }
                )
            }
            item {
                FieldLabel("Servicio")
                ServiceGrid(
                    services = uiState.availableServices,
                    selected = uiState.selectedService,
                    error = uiState.serviceError,
                    onSelect = { viewModel.handleIntent(NewAppointmentIntent.SelectService(it)) }
                )
            }
            item {
                ProfessionalSection(
                    professionals = uiState.availableProfessionals,
                    selected = uiState.selectedProfessional,
                    hasProfessional = uiState.hasProfessional,
                    onToggle = { viewModel.handleIntent(NewAppointmentIntent.ToggleProfessional) },
                    onSelect = { viewModel.handleIntent(NewAppointmentIntent.SelectProfessional(it)) }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Fecha")
                        OutlinedTextField(
                            value = uiState.scheduledDate,
                            onValueChange = { viewModel.handleIntent(NewAppointmentIntent.SetDate(it)) },
                            placeholder = {
                                Text(
                                    "2026-04-17",
                                    color = AppColors.TextLight,
                                    fontSize = 13.sp
                                )
                            },
                            isError = uiState.dateError != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors(),
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Hora")
                        OutlinedTextField(
                            value = uiState.scheduledTime,
                            onValueChange = { viewModel.handleIntent(NewAppointmentIntent.SetTime(it)) },
                            placeholder = {
                                Text(
                                    "10:00",
                                    color = AppColors.TextLight,
                                    fontSize = 13.sp
                                )
                            },
                            isError = uiState.timeError != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors(),
                            singleLine = true
                        )
                    }
                }
            }
            item { HorizontalDivider(color = AppColors.Border, thickness = 0.5.dp) }
            item {
                FieldLabel("Descuento")
                DiscountSection(
                    discountType = uiState.discountType,
                    discountValue = uiState.discountValue,
                    onTypeChange = { viewModel.handleIntent(NewAppointmentIntent.SetDiscountType(it)) },
                    onValueChange = {
                        viewModel.handleIntent(
                            NewAppointmentIntent.SetDiscountValue(
                                it
                            )
                        )
                    }
                )
            }
            uiState.pricing?.let { pricing ->
                item {
                    FieldLabel("Desglose de pago")
                    PricingBreakdown(pricing = pricing)
                }
            }
            item { HorizontalDivider(color = AppColors.Border, thickness = 0.5.dp) }
            item {
                CourtesyDrinkSection(
                    hasCourtesyDrink = uiState.hasCourtesyDrink,
                    selectedDrink = uiState.selectedDrink,
                    availableDrinks = uiState.availableDrinks,
                    onToggle = { viewModel.handleIntent(NewAppointmentIntent.ToggleCourtesyDrink) },
                    onSelectDrink = { viewModel.handleIntent(NewAppointmentIntent.SelectDrink(it)) }
                )
            }
            item {
                FieldLabel("Notas")
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.handleIntent(NewAppointmentIntent.SetNotes(it)) },
                    placeholder = {
                        Text(
                            "Agregar nota...",
                            color = AppColors.TextLight,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors()
                )
            }
            uiState.generalError?.let {
                item { Text(text = it, color = AppColors.Expense, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.TextMuted,
        letterSpacing = 0.7.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun ClientSelector(
    clients: List<Client>,
    selected: Client?,
    error: String?,
    onSelect: (Client) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = { },
            placeholder = {
                Text(
                    "Buscar clienta...",
                    color = AppColors.TextLight,
                    fontSize = 13.sp
                )
            },
            readOnly = true,
            isError = error != null,
            supportingText = { error?.let { Text(it, color = AppColors.Expense) } },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (clients.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay clientes registrados", color = AppColors.TextMuted) },
                    onClick = { expanded = false }
                )
            }
            clients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.name, color = AppColors.TextDark) },
                    onClick = { onSelect(client); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ServiceGrid(
    services: List<Service>,
    selected: Service?,
    error: String?,
    onSelect: (Service) -> Unit
) {
    if (services.isEmpty()) {
        Text("No hay servicios configurados", fontSize = 13.sp, color = AppColors.TextMuted)
        return
    }
    services.chunked(2).forEach { rowServices ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowServices.forEach { service ->
                val isSelected = service.id == selected?.id
                Card(
                    modifier = Modifier.weight(1f).clickable { onSelect(service) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AppColors.BgSecondary else AppColors.BgCard
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 0.5.dp,
                        if (isSelected) AppColors.Primary else AppColors.Border
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when (service.category) {
                                "nail_spa" -> "💅"
                                "access_bars" -> "✨"
                                else -> "·"
                            },
                            fontSize = 18.sp
                        )
                        Text(
                            text = service.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = formatCurrency(service.base_price),
                            fontSize = 11.sp,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }
            if (rowServices.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
    error?.let { Text(text = it, color = AppColors.Expense, fontSize = 12.sp) }
}

@Composable
private fun ProfessionalSection(
    professionals: List<Professional>,
    selected: Professional?,
    hasProfessional: Boolean,
    onToggle: () -> Unit,
    onSelect: (Professional) -> Unit
) {
    FieldLabel("Profesional")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Asignar profesional",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                Text(
                    "Desactiva si lo hace el negocio",
                    fontSize = 11.sp,
                    color = AppColors.TextMuted
                )
            }
            Switch(
                checked = hasProfessional,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.OnPrimary,
                    checkedTrackColor = AppColors.Primary,
                    uncheckedThumbColor = AppColors.OnPrimary,
                    uncheckedTrackColor = AppColors.Border
                )
            )
        }
    }

    if (hasProfessional) {
        Spacer(modifier = Modifier.height(8.dp))
        professionals.forEach { professional ->
            val isSelected = professional.id == selected?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { onSelect(professional) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) AppColors.BgSecondary else AppColors.BgCard
                ),
                border = BorderStroke(
                    if (isSelected) 1.5.dp else 0.5.dp,
                    if (isSelected) AppColors.Primary else AppColors.Border
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(34.dp).clip(CircleShape)
                            .background(AppColors.BgSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = professional.name.take(2).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.PrimaryDark
                        )
                    }
                    Text(
                        text = professional.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextDark,
                        modifier = Modifier.weight(1f).padding(start = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppColors.BgSecondary)
                            .border(0.5.dp, AppColors.Border, RoundedCornerShape(20.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${professional.default_commission.toInt()}%",
                            fontSize = 11.sp,
                            color = AppColors.PrimaryDark
                        )
                    }
                }
            }
        }
    } else {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.BgSecondary),
            border = BorderStroke(0.5.dp, AppColors.Border)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(AppColors.Border),
                    contentAlignment = Alignment.Center
                ) { Text("🏠", fontSize = 16.sp) }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        "Negocio propio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextDark
                    )
                    Text("Sin comisión externa", fontSize = 11.sp, color = AppColors.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun DiscountSection(
    discountType: String?,
    discountValue: Double,
    onTypeChange: (String?) -> Unit,
    onValueChange: (Double) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("percentage" to "% Porcentaje", "fixed" to "$ Valor fijo").forEach { (type, label) ->
            val isSelected = discountType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) AppColors.BgSecondary else AppColors.BgCard)
                    .border(
                        if (isSelected) 1.5.dp else 0.5.dp,
                        if (isSelected) AppColors.Primary else AppColors.Border,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onTypeChange(if (isSelected) null else type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = if (isSelected) AppColors.PrimaryDark else AppColors.TextMuted,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
    if (discountType != null) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = if (discountValue == 0.0) "" else discountValue.toString(),
            onValueChange = { onValueChange(it.toDoubleOrNull() ?: 0.0) },
            placeholder = { Text("0", color = AppColors.TextLight) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}

@Composable
private fun PricingBreakdown(pricing: PricingResult) {
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
                "Total cliente",
                formatCurrency(pricing.finalPrice),
                AppColors.Primary,
                isBold = true,
                valueFontSize = 15
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (pricing.professionalEarn > 0) {
                PricingRow(
                    "Comisión profesional (${pricing.commissionPct.toInt()}%)",
                    formatCurrency(pricing.professionalEarn),
                    AppColors.Commission
                )
            }
            PricingRow(
                "Ganancia negocio",
                formatCurrency(pricing.businessEarn),
                AppColors.BusinessEarn
            )
            if (pricing.discountAmount > 0) {
                Text(
                    text = "* Comisión sobre precio base. Descuento asumido por el negocio.",
                    fontSize = 10.sp,
                    color = AppColors.TextMuted,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
private fun CourtesyDrinkSection(
    hasCourtesyDrink: Boolean,
    selectedDrink: Product?,
    availableDrinks: List<Product>,
    onToggle: () -> Unit,
    onSelectDrink: (Product?) -> Unit
) {
    FieldLabel("Bebida cortesía")
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
                    .background(if (hasCourtesyDrink) AppColors.CourtesyBorder else AppColors.BgSecondary),
                contentAlignment = Alignment.Center
            ) { Text("☕", fontSize = 16.sp) }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    text = if (hasCourtesyDrink && selectedDrink != null) selectedDrink.name else "Agregar bebida cortesía",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasCourtesyDrink) AppColors.CourtesyGreen else AppColors.TextDark
                )
                Text(
                    text = if (hasCourtesyDrink && selectedDrink != null)
                        "Costo interno: ${formatCurrency(selectedDrink.price)} · Sin cobro"
                    else "Sin costo para la clienta",
                    fontSize = 11.sp,
                    color = if (hasCourtesyDrink) AppColors.CourtesyGreen else AppColors.TextMuted
                )
            }
            Switch(
                checked = hasCourtesyDrink,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.OnPrimary,
                    checkedTrackColor = AppColors.CourtesyGreen,
                    uncheckedThumbColor = AppColors.OnPrimary,
                    uncheckedTrackColor = AppColors.Border
                )
            )
        }
    }
    if (hasCourtesyDrink && availableDrinks.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        availableDrinks.forEach { drink ->
            val isSelected = drink.id == selectedDrink?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { onSelectDrink(if (isSelected) null else drink) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) AppColors.CourtesyBg else AppColors.BgCard
                ),
                border = BorderStroke(
                    if (isSelected) 1.5.dp else 0.5.dp,
                    if (isSelected) AppColors.CourtesyBorder else AppColors.Border
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        drink.name,
                        fontSize = 13.sp,
                        color = AppColors.TextDark,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatCurrency(drink.price),
                        fontSize = 12.sp,
                        color = AppColors.TextMuted
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AppColors.CourtesyGreen else androidx.compose.ui.graphics.Color.Transparent)
                            .border(
                                1.5.dp,
                                if (isSelected) AppColors.CourtesyGreen else AppColors.Border,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Text("✓", fontSize = 10.sp, color = AppColors.OnPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.Primary,
    unfocusedBorderColor = AppColors.Border,
    focusedLabelColor = AppColors.Primary,
    unfocusedLabelColor = AppColors.TextMuted
)

private fun formatCurrency(amount: Double): String = "$${amount.toLong()}"
