package com.yediaz.jefac.repository

import com.yediaz.jefac.data.Result
import com.yediaz.jefac.data.supabase
import com.yediaz.jefac.ui.home.AppointmentSummary
import com.yediaz.jefac.ui.home.CafeTableSummary
import com.yediaz.jefac.ui.home.DailySummary
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class HomeRepository {
    private val json = Json { ignoreUnknownKeys = true }
    suspend fun getTodayAppointments(
        businessId: String
    ): Result<List<AppointmentSummary>> {
        return try {
            val response = supabase.postgrest["appointments"]
                .select {
                    filter {
                        eq("business_id", businessId)
                    }
                    order("scheduled_at", Order.ASCENDING)
                }
                .data

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val todayStr = today.toString()
            val appointments = json.decodeFromString<List<AppointmentRow>>(response)
                .filter { it.scheduled_at.substring(0, 10) == todayStr }
            val summaries = appointments.map { appt ->
                val clientName = getClientName(appt.client_id)
                val serviceName = getServiceName(appt.service_id)
                val proName = appt.professional_id?.let {
                    getProfessionalName(it)
                } ?: "Negocio"

                AppointmentSummary(
                    id = appt.id,
                    clientName = clientName,
                    serviceName = serviceName,
                    professionalName = proName,
                    scheduledAt = appt.scheduled_at,
                    status = appt.status,
                    finalPrice = appt.final_price
                )
            }

            Result.Success(summaries)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar citas")
        }
    }

    suspend fun getTodayIncome(businessId: String): Result<Double> {
        return try {
            val response = supabase.postgrest["transactions"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        eq("type", "income")
                    }
                }
                .data

            val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val transactions = json.decodeFromString<List<TransactionRow>>(response)
                .filter { it.date.substring(0, 10) == todayStr }
            val total = transactions.sumOf { it.amount }

            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar ingresos")
        }
    }

    suspend fun getTodayExpenses(businessId: String): Result<Double> {
        return try {
            val response = supabase.postgrest["transactions"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        eq("type", "expense")
                    }
                }
                .data

            val todayStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val transactions = json.decodeFromString<List<TransactionRow>>(response)
                .filter { it.date.substring(0, 10) == todayStr }
            val total = transactions.sumOf { it.amount }

            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar gastos")
        }
    }

    suspend fun getCafeTables(businessId: String): Result<List<CafeTableSummary>> {
        return try {
            val response = supabase.postgrest["cafe_tables"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("table_number", Order.ASCENDING)
                }
                .data

            val tables = json.decodeFromString<List<CafeTableRow>>(response)

            val summaries = tables.map { table ->
                val total = getTableCurrentTotal(table.id)

                CafeTableSummary(
                    id = table.id,
                    tableNumber = table.table_number,
                    status = table.status,
                    currentTotal = total
                )
            }

            Result.Success(summaries)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar mesas")
        }
    }

    suspend fun getWeeklySummary(businessId: String): Result<List<DailySummary>> {
        return try {
            val response = supabase.postgrest["transactions"]
                .select {
                    filter {
                        eq("business_id", businessId)
                    }
                }
                .data

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val weekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            val transactions = json.decodeFromString<List<TransactionRow>>(response)
                .filter {
                    val d = it.date.substring(0, 10)
                    d >= weekStart.toString() && d <= weekEnd.toString()
                }

            val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Hoy")
            val summaries = days.mapIndexed { index, label ->
                val dayTransactions = transactions.filter {
                    getDayIndex(it.date) == index
                }
                DailySummary(
                    dayLabel = label,
                    income = dayTransactions.filter { it.type == "income" }.sumOf { it.amount },
                    expenses = dayTransactions.filter { it.type == "expense" }.sumOf { it.amount }
                )
            }

            Result.Success(summaries)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar resumen semanal")
        }
    }

    private suspend fun getClientName(clientId: String): String {
        return try {
            val response = supabase.postgrest["clients"]
                .select { filter { eq("id", clientId) } }
                .data
            val clients = json.decodeFromString<List<NameRow>>(response)
            clients.firstOrNull()?.name ?: "Cliente"
        } catch (e: Exception) {
            "Cliente"
        }
    }

    private suspend fun getServiceName(serviceId: String): String {
        return try {
            val response = supabase.postgrest["services"]
                .select { filter { eq("id", serviceId) } }
                .data
            val services = json.decodeFromString<List<NameRow>>(response)
            services.firstOrNull()?.name ?: "Servicio"
        } catch (e: Exception) {
            "Servicio"
        }
    }

    private suspend fun getProfessionalName(professionalId: String): String {
        return try {
            val response = supabase.postgrest["professionals"]
                .select { filter { eq("id", professionalId) } }
                .data
            val professionals = json.decodeFromString<List<NameRow>>(response)
            professionals.firstOrNull()?.name ?: "Profesional"
        } catch (e: Exception) {
            "Profesional"
        }
    }

    private suspend fun getTableCurrentTotal(tableId: String): Double {
        return try {
            val response = supabase.postgrest["orders"]
                .select {
                    filter {
                        eq("table_id", tableId)
                        eq("status", "open")
                    }
                }
                .data
            val orders = json.decodeFromString<List<OrderRow>>(response)
            orders.sumOf { it.total }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun getTodayStart(): String {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${today}T00:00:00.000Z"
    }

    private fun getTodayEnd(): String {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${today}T23:59:59.999Z"
    }

    private fun getWeekStart(): String {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val weekStart = today.minus(6, DateTimeUnit.DAY)
        return "${weekStart}T00:00:00.000Z"
    }

    private fun getDayIndex(dateString: String): Int {
        return try {
            val date = Instant.parse(dateString)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val diff = today.toEpochDays() - date.toEpochDays()
            (6 - diff).toInt().coerceIn(0, 6)
        } catch (e: Exception) {
            0
        }
    }
}

@Serializable
private data class AppointmentRow(
    val id: String,
    val client_id: String,
    val service_id: String,
    val professional_id: String? = null,
    val scheduled_at: String,
    val status: String,
    val final_price: Double
)

@Serializable
private data class TransactionRow(
    val id: String,
    val type: String,
    val amount: Double,
    val date: String
)

@Serializable
private data class CafeTableRow(
    val id: String,
    val table_number: Int,
    val status: String
)

@Serializable
private data class OrderRow(
    val id: String,
    val total: Double
)

@Serializable
private data class NameRow(
    val name: String
)