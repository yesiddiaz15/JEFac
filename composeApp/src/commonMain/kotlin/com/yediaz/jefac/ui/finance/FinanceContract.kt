package com.yediaz.jefac.ui.finance

enum class FinancePeriod { TODAY, WEEK, MONTH }

data class FinanceUiState(
    val isLoading: Boolean = false,
    val period: FinancePeriod = FinancePeriod.TODAY,

    // Resumen
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,

    // Desglose por categoría
    val appointmentIncome: Double = 0.0,
    val cafeIncome: Double = 0.0,
    val commissionsExpense: Double = 0.0,
    val courtesyExpense: Double = 0.0,
    val manualExpenses: Double = 0.0,

    // Comisiones por profesional
    val professionalCommissions: List<ProfessionalCommissionUi> = emptyList(),

    // Gastos manuales registrados
    val expenseRecords: List<ExpenseRecordUi> = emptyList(),

    // Formulario de nuevo gasto
    val showExpenseForm: Boolean = false,
    val expenseAmount: String = "",
    val expenseCategory: String = "insumos",
    val expenseDescription: String = "",
    val isSavingExpense: Boolean = false,

    val error: String? = null
)

data class ProfessionalCommissionUi(
    val name: String,
    val appointmentCount: Int,
    val totalCommission: Double,
    val baseIncome: Double
)

data class ExpenseRecordUi(
    val id: String,
    val description: String,
    val category: String,
    val amount: Double,
    val date: String
)

sealed class FinanceIntent {
    object LoadData : FinanceIntent()
    data class SetPeriod(val period: FinancePeriod) : FinanceIntent()
    object ShowExpenseForm : FinanceIntent()
    object HideExpenseForm : FinanceIntent()
    data class SetExpenseAmount(val value: String) : FinanceIntent()
    data class SetExpenseCategory(val category: String) : FinanceIntent()
    data class SetExpenseDescription(val value: String) : FinanceIntent()
    object SaveExpense : FinanceIntent()
}

sealed class FinanceEffect {
    data class ShowError(val message: String) : FinanceEffect()
}
