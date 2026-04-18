package com.yediaz.jefac.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.core.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class FinanceViewModel(
    private val businessId: String,
    private val repository: FinanceRepository = FinanceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<FinanceEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { handleIntent(FinanceIntent.LoadData) }

    fun handleIntent(intent: FinanceIntent) {
        when (intent) {
            is FinanceIntent.LoadData -> loadData()
            is FinanceIntent.SetPeriod -> {
                _uiState.update { it.copy(period = intent.period) }
                loadData()
            }
            is FinanceIntent.ShowExpenseForm -> _uiState.update { it.copy(showExpenseForm = true) }
            is FinanceIntent.HideExpenseForm -> _uiState.update {
                it.copy(showExpenseForm = false, expenseAmount = "", expenseDescription = "", expenseCategory = "insumos")
            }
            is FinanceIntent.SetExpenseAmount -> _uiState.update { it.copy(expenseAmount = intent.value) }
            is FinanceIntent.SetExpenseCategory -> _uiState.update { it.copy(expenseCategory = intent.category) }
            is FinanceIntent.SetExpenseDescription -> _uiState.update { it.copy(expenseDescription = intent.value) }
            is FinanceIntent.SaveExpense -> saveExpense()
        }
    }

    private fun loadData() {
        val period = _uiState.value.period
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val txResult = repository.getTransactions(businessId, period)
            val commResult = repository.getProfessionalCommissions(businessId, period)

            if (txResult is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = txResult.message) }
                return@launch
            }

            val transactions = (txResult as Result.Success).data
            val commissions = if (commResult is Result.Success) commResult.data else emptyList()

            val income = transactions.filter { it.type == "income" }
            val expenses = transactions.filter { it.type == "expense" }

            val appointmentIncome = income.filter {
                it.category == "nail_spa" || it.category == "access_bars" || it.category == "appointment"
            }.sumOf { it.amount }
            val cafeIncome = income.filter { it.category == "cafe" }.sumOf { it.amount }
            val otherIncome = income.filter {
                it.category !in listOf("nail_spa", "access_bars", "appointment", "cafe")
            }.sumOf { it.amount }

            val commissionsExp = expenses.filter { it.category == "commission" }.sumOf { it.amount }
            val courtesyExp = expenses.filter { it.category == "courtesy" }.sumOf { it.amount }
            val manualExp = expenses.filter { it.category !in listOf("commission", "courtesy") }.sumOf { it.amount }

            val totalIncome = income.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.amount }

            val expenseRecords = expenses
                .filter { it.category !in listOf("commission", "courtesy") }
                .map {
                    ExpenseRecordUi(
                        id = it.id,
                        description = it.description ?: it.category,
                        category = it.category,
                        amount = it.amount,
                        date = it.date.substring(0, 10)
                    )
                }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netProfit = totalIncome - totalExpenses,
                    appointmentIncome = appointmentIncome + otherIncome,
                    cafeIncome = cafeIncome,
                    commissionsExpense = commissionsExp,
                    courtesyExpense = courtesyExp,
                    manualExpenses = manualExp,
                    professionalCommissions = commissions,
                    expenseRecords = expenseRecords
                )
            }
        }
    }

    private fun saveExpense() {
        val state = _uiState.value
        val amount = state.expenseAmount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingExpense = true) }
            val result = repository.saveExpense(businessId, amount, state.expenseCategory, state.expenseDescription)
            if (result is Result.Success) {
                _uiState.update {
                    it.copy(isSavingExpense = false, showExpenseForm = false, expenseAmount = "", expenseDescription = "")
                }
                loadData()
            } else {
                _uiState.update { it.copy(isSavingExpense = false) }
                _effects.send(FinanceEffect.ShowError((result as Result.Error).message))
            }
        }
    }

    class Factory(private val businessId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(businessId) as T
        }
    }
}
