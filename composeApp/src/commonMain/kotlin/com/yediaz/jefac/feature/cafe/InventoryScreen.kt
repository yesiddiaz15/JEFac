package com.yediaz.jefac.feature.cafe

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.core.Result
import com.yediaz.jefac.core.models.AppUser
import com.yediaz.jefac.core.ui.AppColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

// ─── ViewModel ───────────────────────────────
class InventoryViewModel(
    private val businessId: String,
    private val repository: CafeRepository = CafeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getProducts(businessId)
            _uiState.update {
                when (result) {
                    is Result.Success -> it.copy(isLoading = false, products = result.data)
                    is Result.Error -> it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun addStock(productId: String, currentStock: Int, quantity: Int) {
        viewModelScope.launch {
            repository.addStock(productId, currentStock, quantity)
            load()
        }
    }

    class Factory(private val businessId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(businessId) as T
        }
    }
}

// ─── Screen ──────────────────────────────────
@Composable
fun InventoryScreen(user: AppUser) {
    val viewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModel.Factory(user.businessId)
    )
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgMain)
    ) {
        Column(
            modifier = Modifier.padding(
                start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp
            )
        ) {
            Text(
                "Inventario",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextDark
            )
            Text(
                "Stock de productos",
                fontSize = 12.sp,
                color = AppColors.TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Alertas de stock bajo
        val lowStock = uiState.products.filter { it.stock <= it.minStock }
        if (lowStock.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CourtesyBg),
                border = BorderStroke(0.5.dp, AppColors.Expense.copy(alpha = 0.3f))
            ) {
                Text(
                    "${lowStock.size} producto(s) con stock bajo",
                    fontSize = 13.sp,
                    color = AppColors.Expense,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.products) { product ->
                    var showAddDialog by remember { mutableStateOf(false) }
                    var addQty by remember { mutableStateOf("") }

                    val stockLevel = when {
                        product.stock <= 0 -> StockLevel.EMPTY
                        product.stock <= product.minStock -> StockLevel.LOW
                        else -> StockLevel.OK
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
                        border = BorderStroke(
                            0.5.dp,
                            when (stockLevel) {
                                StockLevel.EMPTY -> AppColors.Expense.copy(alpha = 0.5f)
                                StockLevel.LOW -> AppColors.PrimaryLight.copy(alpha = 0.7f)
                                StockLevel.OK -> AppColors.Border
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    product.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextDark
                                )
                                Text(
                                    product.category,
                                    fontSize = 11.sp,
                                    color = AppColors.TextMuted
                                )
                            }
                            // Stock badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (stockLevel) {
                                            StockLevel.EMPTY -> AppColors.Expense.copy(alpha = 0.15f)
                                            StockLevel.LOW -> AppColors.PrimaryLight.copy(alpha = 0.2f)
                                            StockLevel.OK -> AppColors.CourtesyBg
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "${product.stock} uds",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when (stockLevel) {
                                        StockLevel.EMPTY -> AppColors.Expense
                                        StockLevel.LOW -> AppColors.PrimaryDark
                                        StockLevel.OK -> AppColors.CourtesyGreen
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("+ Stock", color = AppColors.Primary, fontSize = 12.sp)
                            }
                        }
                    }

                    if (showAddDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showAddDialog = false
                                addQty = ""
                            },
                            containerColor = AppColors.BgCard,
                            title = {
                                Text(
                                    "Agregar stock",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextDark
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "${product.name} - Stock actual: ${product.stock}",
                                        fontSize = 13.sp,
                                        color = AppColors.TextMuted
                                    )
                                    OutlinedTextField(
                                        value = addQty,
                                        onValueChange = { addQty = it },
                                        label = { Text("Cantidad a agregar") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.Primary,
                                            unfocusedBorderColor = AppColors.Border,
                                            focusedLabelColor = AppColors.Primary
                                        )
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val qty = addQty.toIntOrNull() ?: 0
                                        if (qty > 0) {
                                            viewModel.addStock(product.id, product.stock, qty)
                                            showAddDialog = false
                                            addQty = ""
                                        }
                                    },
                                    enabled = addQty.toIntOrNull()?.let { it > 0 } ?: false,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Primary
                                    )
                                ) {
                                    Text("Agregar", color = AppColors.OnPrimary)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showAddDialog = false
                                        addQty = ""
                                    }
                                ) {
                                    Text("Cancelar", color = AppColors.TextMuted)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class StockLevel { OK, LOW, EMPTY }
