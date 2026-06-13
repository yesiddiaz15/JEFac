package com.yediaz.jefac.presentation.components.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.yediaz.jefac.presentation.model.TopBarConfig
import com.yediaz.jefac.presentation.model.UiIntent
import com.yediaz.jefac.presentation.model.UiState

@Composable
fun <S : UiState, I : UiIntent> BaseUi(
    state: S,
    onIntent: (I) -> Unit,
    topBarConfig: TopBarConfig? = null,
    topBar: @Composable () -> Unit = {
        topBarConfig?.let { BaseTopAppBar(it) }
    },
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (
        state: S,
        paddingValues: PaddingValues,
        onIntent: (I) -> Unit
    ) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            content(state, paddingValues, onIntent)
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {}, // Block clicks
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
