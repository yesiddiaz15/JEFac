package com.yediaz.jefac.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.data.AppUser
import com.yediaz.jefac.ui.AppColors
import com.yediaz.jefac.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onNavigateByRole: (AppUser) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateByRole -> onNavigateByRole(effect.user)
                is AuthEffect.ShowError -> {}
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.BgMain
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo / nombre
            Text(
                text = "Vibra Bonito",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextDark
            )
            Text(
                text = "Nails & Coffee",
                fontSize = 14.sp,
                color = AppColors.Primary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "Inicia sesión para continuar",
                fontSize = 13.sp,
                color = AppColors.TextMuted,
                modifier = Modifier.padding(top = 6.dp, bottom = 40.dp)
            )

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.handleIntent(AuthIntent.EmailChanged(it)) },
                label = { Text("Correo electrónico") },
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let {
                        Text(it, color = AppColors.Expense)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Border,
                    focusedLabelColor = AppColors.Primary,
                    unfocusedLabelColor = AppColors.TextMuted
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.handleIntent(AuthIntent.PasswordChanged(it)) },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let {
                        Text(it, color = AppColors.Expense)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Border,
                    focusedLabelColor = AppColors.Primary,
                    unfocusedLabelColor = AppColors.TextMuted
                )
            )

            // Error general
            uiState.generalError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = AppColors.Expense, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón
            Button(
                onClick = { viewModel.handleIntent(AuthIntent.SignIn) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = AppColors.OnPrimary
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.OnPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Ingresar", fontSize = 16.sp)
                }
            }
        }
    }
}
