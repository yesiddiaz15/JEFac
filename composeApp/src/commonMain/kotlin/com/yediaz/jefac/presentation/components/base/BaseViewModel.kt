package com.yediaz.jefac.presentation.components.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yediaz.jefac.presentation.model.UiEffect
import com.yediaz.jefac.presentation.model.UiIntent
import com.yediaz.jefac.presentation.model.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _intent = MutableSharedFlow<I>()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            _intent.collect { intent ->
                handleIntent(intent)
            }
        }
    }

    fun emitIntent(intent: I) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    protected abstract fun handleIntent(intent: I)

    protected fun updateState(reducer: S.() -> S) {
        _uiState.value = _uiState.value.reducer()
    }

    protected fun emitEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
