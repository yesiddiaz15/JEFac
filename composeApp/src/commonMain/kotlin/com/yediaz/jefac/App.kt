package com.yediaz.jefac

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.yediaz.jefac.presentation.nav.NavigationWrapper

@Composable
fun App() {
    MaterialTheme {
        NavigationWrapper()
    }
}
