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
import kotlinx.datetime.LocalDate
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
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getAppointmentsForDate(today.toString(), businessId)
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

            val allResult = getAllAppointments(businessId)
            if (allResult is Result.Error) return allResult

            val filtered = (allResult as Result.Success).data
                .filter { appt ->
                    val d = appt.scheduledAt.substring(0, 10)
                    d >= weekStart.toString() && d <= today.toString()
                }
                .sortedBy { it.scheduledAt }

            Result.Success(filtered)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar citas de la semana")
        }
    }

    // ─────────────────────────────────────────
    // Obtener citas de una fecha específica
    // ─────────────────────────────────────────
    suspend fun getAppointmentsForDate(
        date: String,   // "2026-04-17"
        businessId: String
    ): Result<List<AppointmentItemUi>> {
        return try {
            val allResult = getAllAppointments(businessId)
            if (allResult is Result.Error) return allResult

            val filtered = (allResult as Result.Success).data
                .filter { appt -> appt.scheduledAt.substring(0, 10) == date }
                .sortedBy { it.scheduledAt }

            Result.Success(filtered)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar citas")
        }
    }

    private suspend fun resolveAppointmentNames(
        appointments: List<AppointmentRow>,
        businessId: String
    ): List<AppointmentItemUi> {
        // Cargar listas una sola vez en lugar de una consulta por cita
        val clients = getClients(businessId).let {
            if (it is Result.Success) it.data else emptyList()
        }
        val services = getServices(businessId).let {
            if (it is Result.Success) it.data else emptyList()
        }
        val professionals = getProfessionals(businessId).let {
            if (it is Result.Success) it.data else emptyList()
        }

        return appointments.map { appt ->
            AppointmentItemUi(
                id = appt.id,
                clientName = clients.find { it.id == appt.client_id }?.name ?: "Cliente",
                serviceName = services.find { it.id == appt.service_id }?.name ?: "Servicio",
                professionalName = appt.professional_id?.let { proId ->
                    professionals.find { it.id == proId }?.name
                } ?: "Negocio",
                scheduledAt = appt.scheduled_at,
                status = appt.status,
                finalPrice = appt.final_price,
                hasCourtesyDrink = appt.has_courtesy_drink
            )
        }
    }

    suspend fun getAllAppointments(
        businessId: String
    ): Result<List<AppointmentItemUi>> {
        return try {
            val response = supabase.postgrest["appointments"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("scheduled_at", Order.DESCENDING)
                }.data

            val appointments = json.decodeFromString<List<AppointmentRow>>(response)
            val items = resolveAppointmentNames(appointments, businessId)
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar citas")
        }
    }

    suspend fun getAppointmentServices(
        appointmentId: String,
        businessId: String
    ): Result<List<Service>> {
        return try {
            val response = supabase.postgrest["appointment_services"]
                .select {
                    filter { eq("appointment_id", appointmentId) }
                }.data

            val apptServices = json.decodeFromString<List<AppointmentServiceRow>>(response)
            val allServices = getServices(businessId).let {
                if (it is Result.Success) it.data else emptyList()
            }

            val services = apptServices.mapNotNull { apptSvc ->
                allServices.find { it.id == apptSvc.service_id }
            }

            Result.Success(services)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar servicios de la cita")
        }
    }

    @Serializable
    private data class AppointmentServiceRow(
        val id: String,
        val appointment_id: String,
        val service_id: String,
        val base_price: Double
    )

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
    suspend fun createAppointment(
        appointment: Appointment,
        services: List<Service>
    ): Result<Appointment> {
        return try {
            // Crear la cita principal
            val response = supabase.postgrest["appointments"]
                .insert(
                    buildJsonObject {
                        put("business_id", appointment.business_id)
                        put("client_id", appointment.client_id)
                        put("service_id", appointment.service_id)
                        if (appointment.professional_id != null)
                            put("professional_id", appointment.professional_id)
                        put("scheduled_at", appointment.scheduled_at)
                        put("status", appointment.status)
                        put("base_price", appointment.base_price)
                        if (appointment.discount_type != null)
                            put("discount_type", appointment.discount_type)
                        put("discount_value", appointment.discount_value)
                        put("final_price", appointment.final_price)
                        put("commission_pct", appointment.commission_pct)
                        put("professional_earn", appointment.professional_earn)
                        put("business_earn", appointment.business_earn)
                        put("has_courtesy_drink", appointment.has_courtesy_drink)
                        if (appointment.courtesy_drink_id != null)
                            put("courtesy_drink_id", appointment.courtesy_drink_id)
                        put("courtesy_cost", appointment.courtesy_cost)
                        if (appointment.notes != null)
                            put("notes", appointment.notes)
                    }
                ) {
                    select()
                }.data

            val created = json.decodeFromString<List<Appointment>>(response)
            val result = created.firstOrNull()
                ?: return Result.Error("Error al crear la cita")

            // Guardar los servicios en la tabla intermedia
            services.forEach { service ->
                supabase.postgrest["appointment_services"].insert(
                    buildJsonObject {
                        put("appointment_id", result.id)
                        put("service_id", service.id)
                        put("base_price", service.base_price)
                    }
                )
            }

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
                .insert(
                    buildJsonObject {
                        put("business_id", client.business_id)
                        put("name", client.name)
                        if (client.phone != null) put("phone", client.phone)
                    }
                ) {
                    select() // ← esto le dice a Supabase que devuelva el registro creado
                }
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
