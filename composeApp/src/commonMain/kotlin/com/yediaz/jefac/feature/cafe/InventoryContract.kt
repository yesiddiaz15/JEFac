package com.yediaz.jefac.feature.cafe

import com.yediaz.jefac.core.models.Product

// ─────────────────────────────────────────────
// PANTALLA DE INVENTARIO
// ─────────────────────────────────────────────

data class InventoryUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null
)

sealed class InventoryIntent {
    object LoadProducts : InventoryIntent()
    data class AddStock(val productId: String, val quantity: Int) : InventoryIntent()
}
