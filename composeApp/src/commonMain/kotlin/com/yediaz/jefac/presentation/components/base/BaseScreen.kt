package com.yediaz.jefac.presentation.components.base

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yediaz.jefac.presentation.model.UiEffect
import com.yediaz.jefac.presentation.model.UiIntent
import com.yediaz.jefac.presentation.model.UiState


@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> BaseScreen(
    viewModel: BaseViewModel<S, I, E>,
    onEffect: (E) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (state: S, snackbarHostState: SnackbarHostState, onIntent: (I) -> Unit) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    actionLabel = effect.actionLabel
                )

                else -> onEffect(effect)
            }
        }
    }

    content(state, snackbarHostState, viewModel::emitIntent)
}