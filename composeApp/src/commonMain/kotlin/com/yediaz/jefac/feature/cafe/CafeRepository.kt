package com.yediaz.jefac.feature.cafe

import com.yediaz.jefac.core.Result
import com.yediaz.jefac.core.models.CafeTable
import com.yediaz.jefac.core.models.Order
import com.yediaz.jefac.core.models.Product
import com.yediaz.jefac.core.supabase
import com.yediaz.jefac.feature.appointments.AppointmentRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order as SortOrder
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CafeRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // ─────────────────────────────────────────
    // Mesas
    // ─────────────────────────────────────────
    suspend fun getTables(businessId: String): Result<List<CafeTable>> {
        return try {
            val response = supabase.postgrest["cafe_tables"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("table_number", SortOrder.ASCENDING)
                }.data
            Result.Success(json.decodeFromString<List<CafeTable>>(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar mesas")
        }
    }

    suspend fun updateTableStatus(tableId: String, status: String): Result<Unit> {
        return try {
            supabase.postgrest["cafe_tables"]
                .update({ set("status", status) }) {
                    filter { eq("id", tableId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar mesa")
        }
    }

    // ─────────────────────────────────────────
    // Órdenes activas
    // ─────────────────────────────────────────
    suspend fun getActiveOrders(businessId: String): Result<List<Order>> {
        return try {
            val response = supabase.postgrest["orders"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        eq("status", "open")
                    }
                }.data
            Result.Success(json.decodeFromString<List<Order>>(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar órdenes")
        }
    }

    suspend fun getOrCreateOrder(
        businessId: String,
        tableId: String?,
        appointmentId: String?
    ): Result<Order> {
        return try {
            // Crear nueva orden
            val created = supabase.postgrest["orders"]
                .insert(buildJsonObject {
                    put("business_id", businessId)
                    if (tableId != null) put("table_id", tableId)
                    if (appointmentId != null) put("appointment_id", appointmentId)
                    put("status", "open")
                    put("total", 0.0)
                }) { select() }.data

            val order = json.decodeFromString<List<Order>>(created).firstOrNull()
                ?: return Result.Error("Error al crear orden")

            // Marcar mesa como ocupada si aplica
            if (tableId != null) updateTableStatus(tableId, "occupied")

            Result.Success(order)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener orden")
        }
    }

    // ─────────────────────────────────────────
    // Ítems de orden
    // ─────────────────────────────────────────
    suspend fun getOrderItems(orderId: String, products: List<Product>): Result<List<OrderItemUi>> {
        return try {
            val response = supabase.postgrest["order_items"]
                .select {
                    filter { eq("order_id", orderId) }
                    order("created_at", SortOrder.ASCENDING)
                }.data

            val items = json.decodeFromString<List<OrderItemRow>>(response)
            val uiItems = items.map { item ->
                val product = products.find { it.id == item.product_id }
                OrderItemUi(
                    id            = item.id,
                    productId     = item.product_id,
                    productName   = product?.name ?: "Producto",
                    quantity      = item.quantity,
                    unitPrice     = item.unit_price,
                    isCourtesy    = item.is_courtesy,
                    linkedAppointmentId = item.appointment_id
                )
            }
            Result.Success(uiItems)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar ítems")
        }
    }

    suspend fun addItemToOrder(
        orderId: String,
        product: Product,
        isCourtesy: Boolean,
        appointmentId: String?
    ): Result<OrderItemUi> {
        return try {
            val response = supabase.postgrest["order_items"]
                .insert(buildJsonObject {
                    put("order_id", orderId)
                    put("product_id", product.id)
                    put("quantity", 1)
                    put("unit_price", product.price)
                    put("is_courtesy", isCourtesy)
                    if (appointmentId != null) put("appointment_id", appointmentId)
                }) { select() }.data

            val item = json.decodeFromString<List<OrderItemRow>>(response).firstOrNull()
                ?: return Result.Error("Error al agregar ítem")

            // Descontar stock
            supabase.postgrest["products"]
                .update({ set("stock", product.stock - 1) }) {
                    filter { eq("id", product.id) }
                }

            Result.Success(
                OrderItemUi(
                    id          = item.id,
                    productId   = item.product_id,
                    productName = product.name,
                    quantity    = item.quantity,
                    unitPrice   = item.unit_price,
                    isCourtesy  = item.is_courtesy,
                    linkedAppointmentId = item.appointment_id
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al agregar ítem")
        }
    }

    suspend fun removeOrderItem(itemId: String): Result<Unit> {
        return try {
            supabase.postgrest["order_items"]
                .delete { filter { eq("id", itemId) } }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al eliminar ítem")
        }
    }

    suspend fun linkItemToAppointment(itemId: String, appointmentId: String): Result<Unit> {
        return try {
            supabase.postgrest["order_items"]
                .update({
                    set("appointment_id", appointmentId)
                    set("is_courtesy", true)
                }) { filter { eq("id", itemId) } }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al vincular ítem")
        }
    }

    suspend fun updateOrderTotal(orderId: String, total: Double): Result<Unit> {
        return try {
            supabase.postgrest["orders"]
                .update({ set("total", total) }) {
                    filter { eq("id", orderId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar total")
        }
    }

    suspend fun closeOrder(orderId: String, tableId: String?, total: Double, businessId: String): Result<Unit> {
        return try {
            // 1. Marcar orden como pagada (crítico — falla si no sale)
            supabase.postgrest["orders"]
                .update({ set("status", "paid") }) {
                    filter { eq("id", orderId) }
                }

            // 2. Liberar mesa (no crítico — no falla la operación si hay error de RLS)
            try {
                if (tableId != null) updateTableStatus(tableId, "free")
            } catch (_: Exception) {}

            // 3. Registrar transacción (no crítico)
            try {
                if (total > 0) {
                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    supabase.postgrest["transactions"].insert(buildJsonObject {
                        put("business_id", businessId)
                        put("order_id", orderId)
                        put("type", "income")
                        put("category", "cafe")
                        put("amount", total)
                        put("description", "Orden cafetería cerrada")
                        put("date", today)
                    })
                }
            } catch (_: Exception) {}

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cerrar orden")
        }
    }

    // ─────────────────────────────────────────
    // Citas activas para vincular cortesías
    // ─────────────────────────────────────────
    suspend fun getActiveAppointments(businessId: String): Result<List<ActiveAppointmentUi>> {
        return try {
            val apptRepo = AppointmentRepository()
            val all = apptRepo.getAllAppointments(businessId)
            if (all is Result.Error) return Result.Error(all.message)

            val active = (all as Result.Success).data
                .filter { it.status == "in_progress" || it.status == "confirmed" }
                .map { ActiveAppointmentUi(id = it.id, clientName = it.clientName, serviceName = it.serviceName) }

            Result.Success(active)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar citas activas")
        }
    }

    // ─────────────────────────────────────────
    // Inventario
    // ─────────────────────────────────────────
    suspend fun getProducts(businessId: String): Result<List<Product>> {
        return try {
            val response = supabase.postgrest["products"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("name", SortOrder.ASCENDING)
                }.data
            Result.Success(json.decodeFromString<List<Product>>(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar productos")
        }
    }

    suspend fun addStock(productId: String, currentStock: Int, quantity: Int): Result<Unit> {
        return try {
            supabase.postgrest["products"]
                .update({ set("stock", currentStock + quantity) }) {
                    filter { eq("id", productId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar stock")
        }
    }

    // ─────────────────────────────────────────
    // Modelos internos
    // ─────────────────────────────────────────
    @Serializable
    private data class OrderItemRow(
        val id: String,
        val order_id: String,
        val product_id: String,
        val quantity: Int,
        val unit_price: Double,
        val is_courtesy: Boolean = false,
        val appointment_id: String? = null,
        val created_at: String? = null
    )
}
