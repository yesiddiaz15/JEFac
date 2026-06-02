package com.yediaz.jefac.presentation.features.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OnboardingScreen(
    onNavigateToFirstPeriod: () -> Unit
) {
    OnboardingContent()
}


@Composable
fun OnboardingContent() {

}


@Preview
@Composable
fun OnboardingPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingContent()
        }
    }
}
