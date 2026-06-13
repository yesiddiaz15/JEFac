package com.yediaz.jefac.presentation.features.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.yediaz.jefac.presentation.components.base.BaseScreen
import com.yediaz.jefac.presentation.components.base.BaseUi
import com.yediaz.jefac.presentation.model.TopBarConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is LoginEffect.NavigateToFirstPeriod -> onNavigateBack()
            }
        }
    ) { state, snackbarHostState, onIntent ->
        LoginContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = onIntent
        )
    }
}

@Composable
fun LoginContent(
    state: LoginState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (LoginIntent) -> Unit
) {
    BaseUi(
        state = state,
        onIntent = onIntent,
        topBarConfig = TopBarConfig(
            title = "Login",
            showBackButton = false
        ),
        snackbarHostState = snackbarHostState
    ) { state, paddingValues, onIntent ->
        Column {

        }
    }
}


@Preview
@Composable
private fun LoginContentPreview() {
    MaterialTheme {
        LoginContent(
            state = LoginState(),
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun LoginContentLoadingPreview() {
    MaterialTheme {
        LoginContent(
            state = LoginState(isLoading = true),
            onIntent = {}
        )
    }
}
