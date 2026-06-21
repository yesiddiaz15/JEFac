package com.yediaz.jefac

import com.yediaz.jefac.presentation.theme.JefacTheme
import androidx.compose.runtime.Composable
import com.yediaz.jefac.di.dataModule
import com.yediaz.jefac.di.domainModule
import com.yediaz.jefac.di.presentationModule
import com.yediaz.jefac.presentation.nav.NavigationWrapper
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

@Composable
fun App() {
    JefacTheme {
        NavigationWrapper()
    }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(dataModule, domainModule, presentationModule)
    }
}