package com.yediaz.jefac.di

import com.yediaz.jefac.domain.usecase.LoginUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::LoginUseCase)
}
