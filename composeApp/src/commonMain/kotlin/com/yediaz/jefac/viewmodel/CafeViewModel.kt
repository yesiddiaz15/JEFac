package com.yediaz.jefac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.data.Result
import com.yediaz.jefac.repository.CafeRepository
import com.yediaz.jefac.ui.cafe.CafeEffect
import com.yediaz.jefac.ui.cafe.CafeIntent
import com.yediaz.jefac.ui.cafe.CafeUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class CafeViewModel(
    private val businessId: String,
    private val repository: CafeRepository = CafeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CafeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<CafeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { handleIntent(CafeIntent.LoadTables) }

    fun handleIntent(intent: CafeIntent) {
        when (intent) {
            is CafeIntent.LoadTables -> loadTables()
            is CafeIntent.SelectTable -> openTable(intent.table)
        }
    }

    private fun loadTables() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val tablesResult = repository.getTables(businessId)
            val ordersResult = repository.getActiveOrders(businessId)

            val tables = if (tablesResult is Result.Success) tablesResult.data else emptyList()
            val error = if (tablesResult is Result.Error) tablesResult.message else null
            val ordersMap = if (ordersResult is Result.Success) {
                ordersResult.data.filter { it.table_id != null }
                    .associateBy { it.table_id!! }
            } else emptyMap()

            _uiState.update { it.copy(isLoading = false, tables = tables, activeOrders = ordersMap, error = error) }
        }
    }

    private fun openTable(table: com.yediaz.jefac.data.CafeTable) {
        viewModelScope.launch {
            val order = _uiState.value.activeOrders[table.id]
            _effects.send(CafeEffect.NavigateToOrder(tableId = table.id, orderId = order?.id))
        }
    }

    class Factory(private val businessId: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return CafeViewModel(businessId) as T
        }
    }
}
