package com.yediaz.jefac.di

import com.yediaz.jefac.data.repository.FirebaseAuthRepository
import com.yediaz.jefac.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    single<AuthRepository> { FirebaseAuthRepository(get()) }
}
