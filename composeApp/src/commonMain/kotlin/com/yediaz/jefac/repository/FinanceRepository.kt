package com.yediaz.jefac.repository

import com.yediaz.jefac.data.Result
import com.yediaz.jefac.data.supabase
import com.yediaz.jefac.ui.finance.ExpenseRecordUi
import com.yediaz.jefac.ui.finance.FinancePeriod
import com.yediaz.jefac.ui.finance.ProfessionalCommissionUi
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FinanceRepository {
    private val json = Json { ignoreUnknownKeys = true }

    // ── Transacciones del período ───────────────
    suspend fun getTransactions(businessId: String, period: FinancePeriod): Result<List<TransactionRow>> {
        return try {
            val response = supabase.postgrest["transactions"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("date", Order.DESCENDING)
                }.data
            val all = json.decodeFromString<List<TransactionRow>>(response)
            val filtered = all.filter { inPeriod(it.date, period) }
            Result.Success(filtered)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar transacciones")
        }
    }

    // ── Comisiones por profesional ──────────────
    suspend fun getProfessionalCommissions(businessId: String, period: FinancePeriod): Result<List<ProfessionalCommissionUi>> {
        return try {
            // Fetch all completed appointments in period
            val apptResponse = supabase.postgrest["appointments"]
                .select { filter { eq("business_id", businessId) } }.data

            val appointments = json.decodeFromString<List<AppointmentRow>>(apptResponse)
                .filter { it.status == "completed" && inPeriod(it.scheduled_at, period) && it.professional_id != null }

            // Fetch professional names
            val profResponse = supabase.postgrest["professionals"]
                .select { filter { eq("business_id", businessId) } }.data
            val professionals = json.decodeFromString<List<ProfessionalRow>>(profResponse)

            val grouped = appointments.groupBy { it.professional_id!! }
            val result = grouped.map { (profId, appts) ->
                val name = professionals.find { it.id == profId }?.name ?: "Profesional"
                ProfessionalCommissionUi(
                    name = name,
                    appointmentCount = appts.size,
                    totalCommission = appts.sumOf { it.professional_earn },
                    baseIncome = appts.sumOf { it.base_price }
                )
            }.sortedByDescending { it.totalCommission }

            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al cargar comisiones")
        }
    }

    // ── Registrar gasto manual ──────────────────
    suspend fun saveExpense(
        businessId: String,
        amount: Double,
        category: String,
        description: String
    ): Result<Unit> {
        return try {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            supabase.postgrest["transactions"].insert(buildJsonObject {
                put("business_id", businessId)
                put("type", "expense")
                put("category", category)
                put("amount", amount)
                put("description", description)
                put("date", today)
            })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al guardar gasto")
        }
    }

    // ── Helpers ─────────────────────────────────
    private fun inPeriod(dateStr: String, period: FinancePeriod): Boolean {
        return try {
            val date = dateStr.substring(0, 10)
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            when (period) {
                FinancePeriod.TODAY -> date == today.toString()
                FinancePeriod.WEEK -> {
                    val weekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                    val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
                    date >= weekStart.toString() && date <= weekEnd.toString()
                }
                FinancePeriod.MONTH -> {
                    date.startsWith("${today.year}-${today.monthNumber.toString().padStart(2, '0')}")
                }
            }
        } catch (e: Exception) { false }
    }

    @Serializable
    data class TransactionRow(
        val id: String,
        val type: String,
        val category: String = "",
        val amount: Double,
        val description: String? = null,
        val date: String
    )

    @Serializable
    private data class AppointmentRow(
        val id: String,
        val professional_id: String? = null,
        val scheduled_at: String,
        val status: String,
        val base_price: Double = 0.0,
        val professional_earn: Double = 0.0
    )

    @Serializable
    private data class ProfessionalRow(val id: String, val name: String)
}
