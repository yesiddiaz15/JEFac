package com.yediaz.jefac.di

import com.yediaz.jefac.data.repository.AppointmentRepositoryImpl
import com.yediaz.jefac.data.repository.EmployeeRepositoryImpl
import com.yediaz.jefac.data.repository.FirebaseAuthRepository
import com.yediaz.jefac.data.repository.ServiceRepositoryImpl
import com.yediaz.jefac.domain.repository.AppointmentRepository
import com.yediaz.jefac.domain.repository.AuthRepository
import com.yediaz.jefac.domain.repository.EmployeeRepository
import com.yediaz.jefac.domain.repository.ServiceRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import org.koin.dsl.module

val dataModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    single<AuthRepository> { FirebaseAuthRepository(get()) }
    single<EmployeeRepository> { EmployeeRepositoryImpl(get()) }
    single<ServiceRepository> { ServiceRepositoryImpl(get()) }
    single<AppointmentRepository> { AppointmentRepositoryImpl(get()) }
}
