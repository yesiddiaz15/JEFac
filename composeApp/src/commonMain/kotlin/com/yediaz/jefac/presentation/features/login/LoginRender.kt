package com.yediaz.jefac.presentation.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yediaz.jefac.presentation.components.base.BaseScreen
import com.yediaz.jefac.presentation.components.base.BaseUi
import com.yediaz.jefac.presentation.model.TopBarConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToDashboard: () -> Unit,
) {
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is LoginEffect.NavigateToDashboard -> onNavigateToDashboard()
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
    var passwordVisible by remember { mutableStateOf(false) }

    BaseUi(
        state = state,
        onIntent = onIntent,
        topBarConfig = TopBarConfig(
            title = "JEFac Login",
            showBackButton = false
        ),
        snackbarHostState = snackbarHostState
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
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(LoginIntent.OnEmailChanged(it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(LoginIntent.OnPasswordChanged(it)) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                singleLine = true
            )

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(LoginIntent.OnLoginClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Login")
            }
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
