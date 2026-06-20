package com.yediaz.jefac.presentation.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yediaz.jefac.presentation.components.base.BaseScreen
import com.yediaz.jefac.presentation.components.base.BaseUi
import com.yediaz.jefac.presentation.model.TopBarConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit
) {
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is DashboardEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    ) { state, _, onIntent ->
        DashboardContent(
            state = state,
            onIntent = onIntent
        )
    }
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onIntent: (DashboardIntent) -> Unit
) {
    BaseUi(
        state = state,
        onIntent = onIntent,
        topBarConfig = TopBarConfig(
            title = "Dashboard",
            showBackButton = false
        )
    ) { state, paddingValues, onIntent ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, ${state.userName}!",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { onIntent(DashboardIntent.OnLogoutClicked) }) {
                Text("Logout")
            }
        }
    }
}

@Preview
@Composable
private fun DashboardContentPreview() {
    MaterialTheme {
        DashboardContent(
            state = DashboardState(userName = "Juan Felipe"),
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun DashboardContentLoadingPreview() {
    MaterialTheme {
        DashboardContent(
            state = DashboardState(isLoading = true),
            onIntent = {}
        )
    }
}
