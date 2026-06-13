package com.yediaz.jefac.presentation.components.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.yediaz.jefac.presentation.model.TopBarConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTopAppBar(config: TopBarConfig) {
    TopAppBar(
        title = { Text(text = config.title) },
        navigationIcon = {
            if (config.showBackButton) {
                IconButton(onClick = config.onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        }
    )
}