package com.yediaz.jefac.feature.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.core.models.AppUser
import com.yediaz.jefac.core.ui.AppColors
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FinanceScreen(
    user: AppUser,
    refreshKey: Int = 0,
    onNavigateToNewAppointment: () -> Unit = {}
) {
    val viewModel: FinanceViewModel = viewModel(factory = FinanceViewModel.Factory(user.businessId))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) viewModel.handleIntent(FinanceIntent.LoadData)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is FinanceEffect.ShowError -> { /* could show snackbar */ }
            }
        }
    }

    Scaffold(
        containerColor = AppColors.BgMain,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.handleIntent(FinanceIntent.ShowExpenseForm) },
                containerColor = AppColors.Primary,
                contentColor = AppColors.OnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Registrar gasto")
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
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier.padding(
                            start = 20.dp, end = 20.dp, top = 52.dp, bottom = 12.dp
                        )
                    ) {
                        Text(
                            "Finanzas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextDark
                        )
                    }
                }

                // Period filter
                item { PeriodFilter(uiState.period, viewModel) }

                // Summary cards
                item { SummaryCards(uiState) }

                // Income breakdown
                item { SectionHeader("INGRESOS") }
                item { BreakdownCard(uiState) }

                // Professional commissions
                if (uiState.professionalCommissions.isNotEmpty()) {
                    item { SectionHeader("COMISIONES PROFESIONALES") }
                    items(uiState.professionalCommissions) { comm ->
                        CommissionCard(comm)
                    }
                }

                // Manual expenses
                item { SectionHeader("GASTOS REGISTRADOS") }
                if (uiState.expenseRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Sin gastos registrados en este período",
                                fontSize = 13.sp,
                                color = AppColors.TextMuted
                            )
                        }
                    }
                } else {
                    items(uiState.expenseRecords) { expense ->
                        ExpenseCard(expense)
                    }
                }
            }
        }
    }

    // Expense form dialog
    if (uiState.showExpenseForm) {
        ExpenseFormDialog(uiState = uiState, viewModel = viewModel)
    }
}

@Composable
private fun PeriodFilter(selected: FinancePeriod, viewModel: FinanceViewModel) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            FinancePeriod.TODAY to "Hoy",
            FinancePeriod.WEEK to "Semana",
            FinancePeriod.MONTH to "Mes"
        ).forEach { (period, label) ->
            val isSelected = selected == period
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AppColors.Primary else AppColors.BgCard)
                    .border(
                        0.5.dp,
                        if (isSelected) AppColors.Primary else AppColors.Border,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.handleIntent(FinanceIntent.SetPeriod(period)) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    color = if (isSelected) AppColors.OnPrimary else AppColors.TextMuted,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(uiState: FinanceUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            label = "Ingresos",
            amount = uiState.totalIncome,
            color = AppColors.CourtesyGreen,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Gastos",
            amount = uiState.totalExpenses,
            color = AppColors.Expense,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Ganancia",
            amount = uiState.netProfit,
            color = AppColors.Primary,
            modifier = Modifier.weight(1f),
            isBold = true
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier,
    isBold: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = AppColors.TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatCurrency(amount),
                fontSize = if (isBold) 15.sp else 14.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun BreakdownCard(uiState: FinanceUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Ingresos",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextDark
            )
            BreakdownRow("Citas", uiState.appointmentIncome, AppColors.CourtesyGreen)
            BreakdownRow("Cafeteria", uiState.cafeIncome, AppColors.CourtesyGreen)
            HorizontalDivider(color = AppColors.Border, thickness = 0.5.dp)
            Text(
                "Gastos",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextDark
            )
            BreakdownRow("Comisiones", uiState.commissionsExpense, AppColors.Expense)
            if (uiState.courtesyExpense > 0) {
                BreakdownRow("Cortecias", uiState.courtesyExpense, AppColors.Expense)
            }
            if (uiState.manualExpenses > 0) {
                BreakdownRow("Gastos manuales", uiState.manualExpenses, AppColors.Expense)
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AppColors.TextMuted)
        Text(
            formatCurrency(amount),
            fontSize = 13.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CommissionCard(comm: ProfessionalCommissionUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppColors.BgSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    comm.name.take(2).uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.PrimaryDark
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    comm.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                Text(
                    "${comm.appointmentCount} citas - Base: ${formatCurrency(comm.baseIncome)}",
                    fontSize = 11.sp,
                    color = AppColors.TextMuted
                )
            }
            Text(
                formatCurrency(comm.totalCommission),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Commission
            )
        }
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseRecordUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                Text(
                    "${expense.category} - ${expense.date}",
                    fontSize = 11.sp,
                    color = AppColors.TextMuted
                )
            }
            Text(
                formatCurrency(expense.amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Expense
            )
        }
    }
}

@Composable
private fun ExpenseFormDialog(uiState: FinanceUiState, viewModel: FinanceViewModel) {
    val categories = listOf(
        "insumos" to "Insumos",
        "servicios" to "Servicios",
        "nomina" to "Nomina",
        "arriendo" to "Arriendo",
        "otro" to "Otro"
    )

    AlertDialog(
        onDismissRequest = { viewModel.handleIntent(FinanceIntent.HideExpenseForm) },
        containerColor = AppColors.BgCard,
        title = {
            Text(
                "Registrar gasto",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.expenseAmount,
                    onValueChange = { viewModel.handleIntent(FinanceIntent.SetExpenseAmount(it)) },
                    label = { Text("Monto *") },
                    prefix = { Text("$") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Primary
                    )
                )
                OutlinedTextField(
                    value = uiState.expenseDescription,
                    onValueChange = { viewModel.handleIntent(FinanceIntent.SetExpenseDescription(it)) },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Border,
                        focusedLabelColor = AppColors.Primary
                    )
                )
                Text("Categoria", fontSize = 12.sp, color = AppColors.TextMuted)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { (key, label) ->
                        val isSelected = uiState.expenseCategory == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) AppColors.Primary else AppColors.BgSecondary
                                )
                                .border(
                                    0.5.dp,
                                    if (isSelected) AppColors.Primary else AppColors.Border,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    viewModel.handleIntent(FinanceIntent.SetExpenseCategory(key))
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                color = if (isSelected) AppColors.OnPrimary else AppColors.TextMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.handleIntent(FinanceIntent.SaveExpense) },
                enabled = uiState.expenseAmount.isNotBlank() && !uiState.isSavingExpense,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) {
                if (uiState.isSavingExpense) {
                    CircularProgressIndicator(
                        color = AppColors.OnPrimary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar", color = AppColors.OnPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.handleIntent(FinanceIntent.HideExpenseForm) }
            ) {
                Text("Cancelar", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.TextMuted,
        letterSpacing = 0.7.sp,
        modifier = Modifier.padding(
            start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp
        )
    )
}

private fun formatCurrency(amount: Double): String = "$${amount.toLong()}"
