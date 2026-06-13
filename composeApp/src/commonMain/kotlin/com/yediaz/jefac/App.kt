package com.yediaz.jefac

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.yediaz.jefac.di.dataModule
import com.yediaz.jefac.di.domainModule
import com.yediaz.jefac.di.presentationModule
import com.yediaz.jefac.presentation.nav.NavigationWrapper
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

@Composable
fun App() {
    MaterialTheme {
        NavigationWrapper()
    }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(dataModule, domainModule, presentationModule)
    }
}