package com.yediaz.jefac.feature.cafe

import com.yediaz.jefac.core.models.CafeTable
import com.yediaz.jefac.core.models.Order
import com.yediaz.jefac.core.models.Product

// ─────────────────────────────────────────────
// PANTALLA DE MESAS
// ─────────────────────────────────────────────

data class CafeUiState(
    val isLoading: Boolean = false,
    val tables: List<CafeTable> = emptyList(),
    val activeOrders: Map<String, Order> = emptyMap(),  // tableId -> Order
    val activeAppointments: List<ActiveAppointmentUi> = emptyList(),
    val error: String? = null
)

sealed class CafeIntent {
    object LoadTables : CafeIntent()
    data class SelectTable(val table: CafeTable) : CafeIntent()
    data class SelectAppointment(val appointment: ActiveAppointmentUi) : CafeIntent()
}

sealed class CafeEffect {
    data class NavigateToOrder(
        val tableId: String?,
        val tableNumber: Int,
        val orderId: String?,
        val appointmentId: String?,
        val clientName: String
    ) : CafeEffect()
}

// ─────────────────────────────────────────────
// Shared UI model for active appointments
// Used by both CafeScreen and AppointmentDetailScreen
// ─────────────────────────────────────────────

data class ActiveAppointmentUi(
    val id: String,
    val clientName: String,
    val serviceName: String
)
