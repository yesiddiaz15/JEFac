package com.yediaz.jefac.repository

import com.yediaz.jefac.data.Appointment
import com.yediaz.jefac.data.Client
import com.yediaz.jefac.data.Product
import com.yediaz.jefac.data.Professional
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.data.Service
import com.yediaz.jefac.data.supabase
import com.yediaz.jefac.ui.appointments.AppointmentItemUi
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock

class AppointmentRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // ─────────────────────────────────────────
    // Obtener citas del día
    // ─────────────────────────────────────────
    suspend fun getTodayAppointments(
        businessId: String
    ): Result<List<AppointmentItemUi>> {
        return try {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val start = "${today}T00:00:00.000Z"
            val end = "${today}T23:59:59.999Z"

            val response = supabase.postgrest["appointments"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        gte("scheduled_at", start)
                        lte("scheduled_at", end)
                    }
                    order("scheduled_at", Order.ASCENDING)
                }.data

            val appointments = json.decodeFromString<List<AppointmentRow>>(response)
            val items = appointments.map { it.toUi(businessId) }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar citas")
        }
    }

    // ─────────────────────────────────────────
    // Obtener citas de la semana
    // ─────────────────────────────────────────
    suspend fun getWeekAppointments(
        businessId: String
    ): Result<List<AppointmentItemUi>> {
        return try {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val weekStart = today.minus(6, DateTimeUnit.DAY)
            val start = "${weekStart}T00:00:00.000Z"
            val end = "${today}T23:59:59.999Z"

            val response = supabase.postgrest["appointments"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        gte("scheduled_at", start)
                        lte("scheduled_at", end)
                    }
                    order("scheduled_at", Order.ASCENDING)
                }.data

            val appointments = json.decodeFromString<List<AppointmentRow>>(response)
            val items = appointments.map { it.toUi(businessId) }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar citas de la semana")
        }
    }

    // ─────────────────────────────────────────
    // Obtener una cita por ID
    // ─────────────────────────────────────────
    suspend fun getAppointmentById(id: String): Result<Appointment> {
        return try {
            val response = supabase.postgrest["appointments"]
                .select { filter { eq("id", id) } }
                .data

            val appointments = json.decodeFromString<List<Appointment>>(response)
            val appointment = appointments.firstOrNull()
                ?: return Result.Error("Cita no encontrada")

            Result.Success(appointment)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar la cita")
        }
    }

    // ─────────────────────────────────────────
    // Crear nueva cita
    // ─────────────────────────────────────────
    suspend fun createAppointment(appointment: Appointment): Result<Appointment> {
        return try {
            val response = supabase.postgrest["appointments"]
                .insert(appointment)
                .data

            val created = json.decodeFromString<List<Appointment>>(response)
            val result = created.firstOrNull()
                ?: return Result.Error("Error al crear la cita")

            // Registrar la transacción de ingreso
            registerAppointmentTransaction(result)

            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear la cita")
        }
    }

    // ─────────────────────────────────────────
    // Actualizar estado de una cita
    // ─────────────────────────────────────────
    suspend fun updateAppointmentStatus(
        id: String,
        status: String
    ): Result<Unit> {
        return try {
            supabase.postgrest["appointments"]
                .update({
                    set("status", status)
                }) {
                    filter { eq("id", id) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar la cita")
        }
    }

    // ─────────────────────────────────────────
    // Agregar bebida cortesía a una cita
    // ─────────────────────────────────────────
    suspend fun setCourtesyDrink(
        appointmentId: String,
        drink: Product?
    ): Result<Unit> {
        return try {
            supabase.postgrest["appointments"]
                .update({
                    set("has_courtesy_drink", drink != null)
                    set("courtesy_drink_id", drink?.id)
                    set("courtesy_cost", drink?.price ?: 0.0)
                }) {
                    filter { eq("id", appointmentId) }
                }

            // Si se agrega una bebida, registrar el gasto de cortesía
            if (drink != null) {
                registerCourtesyExpense(appointmentId, drink)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al actualizar la bebida cortesía")
        }
    }

    // ─────────────────────────────────────────
    // Cargar clientes del negocio
    // ─────────────────────────────────────────
    suspend fun getClients(businessId: String): Result<List<Client>> {
        return try {
            val response = supabase.postgrest["clients"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("name", Order.ASCENDING)
                }.data

            Result.Success(json.decodeFromString<List<Client>>(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar clientes")
        }
    }

    // ─────────────────────────────────────────
    // Crear nuevo cliente
    // ─────────────────────────────────────────
    suspend fun createClient(client: Client): Result<Client> {
        return try {
            val response = supabase.postgrest["clients"]
                .insert(client)
                .data

            val created = json.decodeFromString<List<Client>>(response)
            Result.Success(
                created.firstOrNull() ?: return Result.Error("Error al crear cliente")
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al crear cliente")
        }
    }

    // ─────────────────────────────────────────
    // Cargar servicios activos
    // ─────────────────────────────────────────
    suspend fun getServices(businessId: String): Result<List<Service>> {
        return try {
            val response = supabase.postgrest["services"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        eq("is_active", true)
                    }
                }.data

            Result.Success(json.decodeFromString<List<Service>>(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar servicios")
        }
    }

    // ─────────────────────────────────────────
    // Cargar profesionales activos
    // ─────────────────────────────────────────
    suspend fun getProfessionals(businessId: String): Result<List<Professional>> {
        return try {
            val response = supabase.postgrest["professionals"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        eq("is_active", true)
                    }
                    order("name", Order.ASCENDING)
                }.data

            Result.Success(json.decodeFromString<List<Professional>>(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar profesionales")
        }
    }

    // ─────────────────────────────────────────
    // Cargar productos disponibles (para bebida cortesía)
    // ─────────────────────────────────────────
    suspend fun getAvailableProducts(businessId: String): Result<List<Product>> {
        return try {
            val response = supabase.postgrest["products"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        eq("is_available", true)
                    }
                    order("name", Order.ASCENDING)
                }.data

            Result.Success(json.decodeFromString<List<Product>>(response))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar productos")
        }
    }

    // ─────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────
    private suspend fun registerAppointmentTransaction(appointment: Appointment) {
        try {
            val category = when {
                appointment.service_id.isNotEmpty() -> "nail_spa" // se actualiza con el servicio real
                else -> "other"
            }
            supabase.postgrest["transactions"].insert(
                buildJsonObject {
                    put("business_id", appointment.business_id)
                    put("appointment_id", appointment.id)
                    put("type", "income")
                    put("category", category)
                    put("amount", appointment.final_price)
                    put("description", "Cita completada")
                }
            )

            // Registrar comisión si hay profesional
            if (appointment.professional_earn > 0) {
                supabase.postgrest["transactions"].insert(
                    buildJsonObject {
                        put("business_id", appointment.business_id)
                        put("appointment_id", appointment.id)
                        put("type", "expense")
                        put("category", "commission")
                        put("amount", appointment.professional_earn)
                        put("description", "Comisión profesional")
                    }
                )
            }
        } catch (e: Exception) {
            // No romper el flujo si falla el registro de transacción
        }
    }

    private suspend fun registerCourtesyExpense(
        appointmentId: String,
        drink: Product
    ) {
        try {
            val appointment = getAppointmentById(appointmentId)
            if (appointment is Result.Success) {
                supabase.postgrest["transactions"].insert(
                    buildJsonObject {
                        put("business_id", appointment.data.business_id)
                        put("appointment_id", appointmentId)
                        put("type", "expense")
                        put("category", "courtesy")
                        put("amount", drink.price)
                        put("description", "Bebida cortesía: ${drink.name}")
                    }
                )
            }
        } catch (e: Exception) {
            // No romper el flujo
        }
    }

    // ─────────────────────────────────────────
    // Modelos internos
    // ─────────────────────────────────────────
    @Serializable
    private data class AppointmentRow(
        val id: String,
        val business_id: String,
        val client_id: String,
        val service_id: String,
        val professional_id: String? = null,
        val scheduled_at: String,
        val status: String,
        val base_price: Double,
        val discount_type: String? = null,
        val discount_value: Double = 0.0,
        val final_price: Double,
        val commission_pct: Double = 0.0,
        val professional_earn: Double = 0.0,
        val business_earn: Double = 0.0,
        val has_courtesy_drink: Boolean = false
    ) {
        suspend fun toUi(businessId: String): AppointmentItemUi {
            val repo = AppointmentRepository()
            val clientName = try {
                val clients = repo.getClients(businessId)
                if (clients is Result.Success) {
                    clients.data.find { it.id == client_id }?.name ?: "Cliente"
                } else "Cliente"
            } catch (e: Exception) {
                "Cliente"
            }

            return AppointmentItemUi(
                id = id,
                clientName = clientName,
                serviceName = service_id,  // se mejora con join en el próximo paso
                professionalName = professional_id ?: "Negocio",
                scheduledAt = scheduled_at,
                status = status,
                finalPrice = final_price,
                hasCourtesyDrink = has_courtesy_drink
            )
        }
    }
}
