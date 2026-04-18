package com.yediaz.jefac.feature.appointments

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.yediaz.jefac.core.models.AppUser
import com.yediaz.jefac.core.models.Client
import com.yediaz.jefac.core.models.Product
import com.yediaz.jefac.core.models.Professional
import com.yediaz.jefac.core.models.Service
import com.yediaz.jefac.core.ui.AppColors
import com.yediaz.jefac.feature.finance.PricingResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
fun NewAppointmentScreen(
    user: AppUser,
    onNavigateBack: () -> Unit = {},
    onAppointmentCreated: () -> Unit = {}
) {
    val viewModel: NewAppointmentViewModel = viewModel(
        factory = NewAppointmentViewModel.Factory(user.businessId)
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
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
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
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
                    showCreateClient = uiState.showCreateClient,
                    newClientName = uiState.newClientName,
                    newClientPhone = uiState.newClientPhone,
                    newClientError = uiState.newClientError,
                    isCreatingClient = uiState.isCreatingClient,
                    onSelect = { viewModel.handleIntent(NewAppointmentIntent.SelectClient(it)) },
                    onShowCreate = { viewModel.handleIntent(NewAppointmentIntent.ShowCreateClient) },
                    onHideCreate = { viewModel.handleIntent(NewAppointmentIntent.HideCreateClient) },
                    onNameChanged = {
                        viewModel.handleIntent(
                            NewAppointmentIntent.NewClientNameChanged(it)
                        )
                    },
                    onPhoneChanged = {
                        viewModel.handleIntent(
                            NewAppointmentIntent.NewClientPhoneChanged(it)
                        )
                    },
                    onConfirmCreate = { viewModel.handleIntent(NewAppointmentIntent.ConfirmCreateClient) }
                )
            }
            item {
                FieldLabel("Servicio")
                ServiceGrid(
                    services = uiState.availableServices,
                    selectedServices = uiState.selectedServices,
                    error = uiState.serviceError,
                    onToggle = { viewModel.handleIntent(NewAppointmentIntent.ToggleService(it)) }
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
                        DatePickerField(
                            value = uiState.scheduledDate,
                            isError = uiState.dateError != null,
                            onSelect = { viewModel.handleIntent(NewAppointmentIntent.SetDate(it)) }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Hora")
                        TimePickerField(
                            value = uiState.scheduledTime,
                            isError = uiState.timeError != null,
                            onSelect = { viewModel.handleIntent(NewAppointmentIntent.SetTime(it)) }
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
                            NewAppointmentIntent.SetDiscountValue(it)
                        )
                    }
                )
            }
            uiState.pricing?.let { pricing ->
                item {
                    FieldLabel("Desglose de pago")
                    PricingBreakdown(
                        pricing = pricing,
                        depositAmount = uiState.depositAmount,
                        onDepositChange = { viewModel.handleIntent(NewAppointmentIntent.SetDeposit(it)) }
                    )
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
    showCreateClient: Boolean,
    newClientName: String,
    newClientPhone: String,
    newClientError: String?,
    isCreatingClient: Boolean,
    onSelect: (Client) -> Unit,
    onShowCreate: () -> Unit,
    onHideCreate: () -> Unit,
    onNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onConfirmCreate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredClients = remember(searchQuery, clients) {
        if (searchQuery.isBlank()) clients
        else clients.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // Campo de búsqueda principal
    OutlinedTextField(
        value = selected?.name ?: searchQuery,
        onValueChange = {
            searchQuery = it
            expanded = true
        },
        placeholder = {
            Text(
                "Buscar o crear cliente...",
                color = AppColors.TextLight,
                fontSize = 13.sp
            )
        },
        isError = error != null,
        supportingText = { error?.let { Text(it, color = AppColors.Expense) } },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Primary,
            unfocusedBorderColor = AppColors.Border,
            focusedLabelColor = AppColors.Primary
        ),
        singleLine = true
    )

    // Dropdown con resultados
    if (expanded && !showCreateClient) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
            border = BorderStroke(0.5.dp, AppColors.Border)
        ) {
            Column {
                if (filteredClients.isEmpty()) {
                    // No hay resultados — mostrar opción de crear
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                onShowCreate()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AppColors.BgSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 16.sp, color = AppColors.Primary)
                        }
                        Column {
                            Text(
                                text = "Crear \"$searchQuery\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Primary
                            )
                            Text(
                                text = "Nuevo cliente",
                                fontSize = 11.sp,
                                color = AppColors.TextMuted
                            )
                        }
                    }
                } else {
                    filteredClients.take(5).forEach { client ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(client)
                                    searchQuery = ""
                                    expanded = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.BgSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = client.name.take(1).uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.PrimaryDark
                                )
                            }
                            Column {
                                Text(
                                    text = client.name,
                                    fontSize = 13.sp,
                                    color = AppColors.TextDark
                                )
                                client.phone?.let {
                                    Text(text = it, fontSize = 11.sp, color = AppColors.TextMuted)
                                }
                            }
                        }
                        if (client != filteredClients.take(5).last()) {
                            HorizontalDivider(color = AppColors.Border, thickness = 0.5.dp)
                        }
                    }

                    // Opción de crear al final si hay resultados pero quieren crear uno nuevo
                    HorizontalDivider(color = AppColors.Border, thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                onShowCreate()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AppColors.BgSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 14.sp, color = AppColors.Primary)
                        }
                        Text(
                            text = "Crear nuevo cliente",
                            fontSize = 13.sp,
                            color = AppColors.Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Mini formulario de creación inline
    if (showCreateClient) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
            border = BorderStroke(1.5.dp, AppColors.Primary)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo cliente",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextDark
                    )
                    TextButton(onClick = onHideCreate) {
                        Text("Cancelar", color = AppColors.TextMuted, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nombre
                OutlinedTextField(
                    value = newClientName,
                    onValueChange = onNameChanged,
                    label = { Text("Nombre *") },
                    isError = newClientError != null,
                    supportingText = {
                        newClientError?.let { Text(it, color = AppColors.Expense) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Teléfono (opcional)
                OutlinedTextField(
                    value = newClientPhone,
                    onValueChange = onPhoneChanged,
                    label = { Text("Teléfono (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onConfirmCreate,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = AppColors.OnPrimary
                    ),
                    enabled = !isCreatingClient && newClientName.isNotBlank()
                ) {
                    if (isCreatingClient) {
                        CircularProgressIndicator(
                            color = AppColors.OnPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar cliente", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceGrid(
    services: List<Service>,
    selectedServices: List<Service>,
    error: String?,
    onToggle: (Service) -> Unit
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
                val isSelected = selectedServices.any { it.id == service.id }
                Card(
                    modifier = Modifier.weight(1f).clickable { onToggle(service) },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = when (service.category) {
                                    "nail_spa" -> "💅"
                                    "access_bars" -> "✨"
                                    else -> "·"
                                },
                                fontSize = 18.sp
                            )
                            // Checkmark cuando está seleccionado
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✓", fontSize = 10.sp, color = AppColors.OnPrimary)
                                }
                            }
                        }
                        Text(
                            text = service.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = formatCurrency(service.basePrice),
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
                            text = "${professional.defaultCommission.toInt()}%",
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
private fun PricingBreakdown(
    pricing: PricingResult,
    depositAmount: Double,
    onDepositChange: (Double) -> Unit
) {
    val balance = pricing.finalPrice - depositAmount

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

            // ── Abono ──────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = AppColors.Border,
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Abono recibido", fontSize = 12.sp, color = AppColors.TextMuted)
                OutlinedTextField(
                    value = if (depositAmount == 0.0) "" else depositAmount.toLong().toString(),
                    onValueChange = { onDepositChange(it.toDoubleOrNull() ?: 0.0) },
                    placeholder = { Text("0", fontSize = 13.sp, color = AppColors.TextLight) },
                    prefix = { Text("$", fontSize = 13.sp, color = AppColors.TextMuted) },
                    modifier = Modifier.width(130.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextDark
                    )
                )
            }
            if (depositAmount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                PricingRow(
                    label = "Saldo pendiente",
                    value = formatCurrency(balance.coerceAtLeast(0.0)),
                    valueColor = if (balance <= 0) AppColors.CourtesyGreen else AppColors.PrimaryDark,
                    isBold = true
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
                    else "Sin costo para el cliente",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    value: String,
    isError: Boolean,
    onSelect: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text("Seleccionar", color = AppColors.TextLight, fontSize = 13.sp) },
            enabled = false,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (isError) AppColors.Expense else AppColors.Border,
                disabledTextColor = AppColors.TextDark,
                disabledPlaceholderColor = AppColors.TextLight,
                disabledContainerColor = AppColors.BgCard
            ),
            singleLine = true
        )
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Usando kotlinx-datetime en lugar de java.time
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val date = instant.toLocalDateTime(kotlinx.datetime.TimeZone.UTC).date
                            onSelect(date.toString())
                        }
                        showPicker = false
                    }
                ) {
                    Text("Confirmar", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar", color = AppColors.TextMuted)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = AppColors.BgCard,
                titleContentColor = AppColors.TextDark,
                headlineContentColor = AppColors.TextDark,
                weekdayContentColor = AppColors.TextMuted,
                dayContentColor = AppColors.TextDark,
                selectedDayContainerColor = AppColors.Primary,
                selectedDayContentColor = AppColors.OnPrimary,
                todayDateBorderColor = AppColors.Primary,
                todayContentColor = AppColors.Primary
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = AppColors.BgCard,
                    selectedDayContainerColor = AppColors.Primary,
                    selectedDayContentColor = AppColors.OnPrimary,
                    todayDateBorderColor = AppColors.Primary,
                    todayContentColor = AppColors.Primary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    value: String,
    isError: Boolean,
    onSelect: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text("Seleccionar", color = AppColors.TextLight, fontSize = 13.sp) },
            enabled = false,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (isError) AppColors.Expense else AppColors.Border,
                disabledTextColor = AppColors.TextDark,
                disabledPlaceholderColor = AppColors.TextLight,
                disabledContainerColor = AppColors.BgCard
            ),
            singleLine = true
        )
    }

    if (showPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 9,
            initialMinute = 0,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = AppColors.BgCard,
            title = {
                Text(
                    text = "Seleccionar hora",
                    color = AppColors.TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = AppColors.BgSecondary,
                        clockDialSelectedContentColor = AppColors.OnPrimary,
                        clockDialUnselectedContentColor = AppColors.TextDark,
                        selectorColor = AppColors.Primary,
                        containerColor = AppColors.BgCard,
                        periodSelectorBorderColor = AppColors.Border,
                        timeSelectorSelectedContainerColor = AppColors.Primary,
                        timeSelectorUnselectedContainerColor = AppColors.BgSecondary,
                        timeSelectorSelectedContentColor = AppColors.OnPrimary,
                        timeSelectorUnselectedContentColor = AppColors.TextDark
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Formatear como HH:mm
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        onSelect("$hour:$minute")
                        showPicker = false
                    }
                ) {
                    Text("Confirmar", color = AppColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar", color = AppColors.TextMuted)
                }
            }
        )
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
