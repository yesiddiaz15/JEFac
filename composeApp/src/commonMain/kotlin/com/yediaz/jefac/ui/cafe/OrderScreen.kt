package com.yediaz.jefac.ui.cafe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.data.Product
import com.yediaz.jefac.ui.AppColors
import com.yediaz.jefac.viewmodel.OrderViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OrderScreen(
    user: AppUser,
    tableId: String,
    tableNumber: Int,
    existingOrderId: String?,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: OrderViewModel = viewModel(
        key = tableId,
        factory = OrderViewModel.Factory(user.business_id, tableId, tableNumber, existingOrderId)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is OrderEffect.NavigateBack, is OrderEffect.OrderClosed -> onNavigateBack()
            }
        }
    }

    Scaffold(
        containerColor = AppColors.BgMain,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.handleIntent(OrderIntent.NavigateBack) }) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(11.dp))
                            .background(AppColors.BgCard).border(0.5.dp, AppColors.Border, RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Volver",
                            tint = AppColors.TextDark, modifier = Modifier.size(20.dp))
                    }
                }
                Text("Mesa ${uiState.tableNumber}", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(AppColors.BgSecondary)
                        .border(0.5.dp, AppColors.Border, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text("${uiState.items.size} ítems", fontSize = 12.sp, color = AppColors.PrimaryDark)
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().background(AppColors.BgCard)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", fontSize = 14.sp, color = AppColors.TextMuted)
                    Text(
                        text = "$${uiState.total.toLong()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.handleIntent(OrderIntent.ShowProductSelector) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AppColors.Primary)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", color = AppColors.Primary, fontSize = 14.sp)
                    }
                    Button(
                        onClick = { viewModel.handleIntent(OrderIntent.CloseOrder) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        enabled = uiState.items.isNotEmpty() && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = AppColors.OnPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Cerrar orden", fontSize = 14.sp, color = AppColors.OnPrimary)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("☕", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Orden vacía", fontSize = 16.sp, color = AppColors.TextMuted)
                    Text("Agregá productos con el botón de abajo", fontSize = 13.sp, color = AppColors.TextLight)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.items) { index, item ->
                    OrderItemCard(
                        item = item,
                        onRemove = { viewModel.handleIntent(OrderIntent.RemoveItem(index)) },
                        onLinkAppointment = { viewModel.handleIntent(OrderIntent.ShowAppointmentSelector(index)) }
                    )
                }
            }
        }
    }

    // Product Selector Bottom Sheet
    if (uiState.showProductSelector) {
        ProductSelectorSheet(
            products = uiState.availableProducts,
            onDismiss = { viewModel.handleIntent(OrderIntent.HideProductSelector) },
            onSelect = { product, isCourtesy ->
                viewModel.handleIntent(OrderIntent.AddProduct(product, isCourtesy))
            }
        )
    }

    // Appointment Selector Dialog
    uiState.pendingCourtesyItemIndex?.let { index ->
        AppointmentSelectorDialog(
            appointments = uiState.activeAppointments,
            onDismiss = { viewModel.handleIntent(OrderIntent.HideAppointmentSelector) },
            onSelect = { appt ->
                viewModel.handleIntent(OrderIntent.LinkCourtesyToAppointment(index, appt))
            }
        )
    }
}

@Composable
private fun OrderItemCard(
    item: OrderItemUi,
    onRemove: () -> Unit,
    onLinkAppointment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCourtesy) AppColors.CourtesyBg else AppColors.BgCard
        ),
        border = BorderStroke(
            0.5.dp,
            if (item.isCourtesy) AppColors.CourtesyBorder else AppColors.Border
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono cortesía / normal
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (item.isCourtesy) AppColors.CourtesyBorder else AppColors.BgSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(if (item.isCourtesy) "🎁" else "☕", fontSize = 16.sp)
            }

            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
                if (item.isCourtesy && item.linkedClientName != null) {
                    Text(
                        "Cortesía → ${item.linkedClientName}",
                        fontSize = 11.sp,
                        color = AppColors.CourtesyGreen
                    )
                } else if (item.isCourtesy) {
                    Text(
                        "Cortesía · Toca para asignar a cita",
                        fontSize = 11.sp,
                        color = AppColors.CourtesyGreen,
                        modifier = Modifier.clickable { onLinkAppointment() }
                    )
                } else {
                    Text("$${item.unitPrice.toLong()}", fontSize = 11.sp, color = AppColors.TextMuted)
                }
            }

            // Precio o "sin cobro"
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                if (item.isCourtesy) {
                    Text("Sin cobro", fontSize = 11.sp, color = AppColors.CourtesyGreen, fontWeight = FontWeight.Medium)
                } else {
                    Text("$${item.unitPrice.toLong()}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.Primary)
                }
            }

            // Botón eliminar
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(AppColors.Border.copy(alpha = 0.5f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Text("×", fontSize = 16.sp, color = AppColors.TextMuted)
            }
        }
    }
}

@Composable
private fun ProductSelectorSheet(
    products: List<Product>,
    onDismiss: () -> Unit,
    onSelect: (Product, Boolean) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgCard,
        title = {
            Text("Agregar producto", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                products.forEach { product ->
                    val isSelected = product.id == selectedProduct?.id
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AppColors.BgSecondary else AppColors.BgMain)
                            .border(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) AppColors.Primary else AppColors.Border,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedProduct = product }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
                            Text("Stock: ${product.stock}", fontSize = 11.sp, color = AppColors.TextMuted)
                        }
                        Text("$${product.price.toLong()}", fontSize = 13.sp, color = AppColors.Primary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { selectedProduct?.let { onSelect(it, true) } },
                    enabled = selectedProduct != null
                ) {
                    Text("Cortesía 🎁", color = AppColors.CourtesyGreen)
                }
                Button(
                    onClick = { selectedProduct?.let { onSelect(it, false) } },
                    enabled = selectedProduct != null,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                ) {
                    Text("Agregar", color = AppColors.OnPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.TextMuted) }
        }
    )
}

@Composable
private fun AppointmentSelectorDialog(
    appointments: List<ActiveAppointmentUi>,
    onDismiss: () -> Unit,
    onSelect: (ActiveAppointmentUi) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgCard,
        title = {
            Text("Asignar a cita", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
        },
        text = {
            if (appointments.isEmpty()) {
                Text("No hay citas activas en este momento", fontSize = 13.sp, color = AppColors.TextMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    appointments.forEach { appt ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.BgSecondary)
                                .clickable { onSelect(appt) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(34.dp).clip(CircleShape).background(AppColors.BgCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(appt.clientName.take(2).uppercase(), fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium, color = AppColors.PrimaryDark)
                            }
                            Column {
                                Text(appt.clientName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
                                Text(appt.serviceName, fontSize = 11.sp, color = AppColors.TextMuted)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.TextMuted) }
        }
    )
}
