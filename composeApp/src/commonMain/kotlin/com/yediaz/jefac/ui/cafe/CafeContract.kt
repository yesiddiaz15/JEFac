package com.yediaz.jefac.ui.cafe

import com.yediaz.jefac.data.CafeTable
import com.yediaz.jefac.data.Order
import com.yediaz.jefac.data.Product

// ─────────────────────────────────────────────
// PANTALLA DE MESAS
// ─────────────────────────────────────────────

data class CafeUiState(
    val isLoading: Boolean = false,
    val tables: List<CafeTable> = emptyList(),
    val activeOrders: Map<String, Order> = emptyMap(),  // tableId -> Order
    val error: String? = null
)

sealed class CafeIntent {
    object LoadTables : CafeIntent()
    data class SelectTable(val table: CafeTable) : CafeIntent()
}

sealed class CafeEffect {
    data class NavigateToOrder(val tableId: String, val orderId: String?) : CafeEffect()
}

// ─────────────────────────────────────────────
// PANTALLA DE ORDEN
// ─────────────────────────────────────────────

data class OrderUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val tableNumber: Int = 0,
    val tableId: String = "",
    val orderId: String? = null,
    val items: List<OrderItemUi> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val activeAppointments: List<ActiveAppointmentUi> = emptyList(),
    val showProductSelector: Boolean = false,
    val pendingCourtesyItemIndex: Int? = null,  // índice del item esperando asignación de cita
    val total: Double = 0.0,
    val error: String? = null
)

data class OrderItemUi(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val isCourtesy: Boolean = false,
    val linkedAppointmentId: String? = null,
    val linkedClientName: String? = null
)

data class ActiveAppointmentUi(
    val id: String,
    val clientName: String,
    val serviceName: String
)

sealed class OrderIntent {
    object LoadOrder : OrderIntent()
    object ShowProductSelector : OrderIntent()
    object HideProductSelector : OrderIntent()
    data class AddProduct(val product: Product, val isCourtesy: Boolean = false) : OrderIntent()
    data class RemoveItem(val itemIndex: Int) : OrderIntent()
    data class LinkCourtesyToAppointment(val itemIndex: Int, val appointment: ActiveAppointmentUi) : OrderIntent()
    data class ShowAppointmentSelector(val itemIndex: Int) : OrderIntent()
    object HideAppointmentSelector : OrderIntent()
    object CloseOrder : OrderIntent()
    object NavigateBack : OrderIntent()
}

sealed class OrderEffect {
    object NavigateBack : OrderEffect()
    object OrderClosed : OrderEffect()
}

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
