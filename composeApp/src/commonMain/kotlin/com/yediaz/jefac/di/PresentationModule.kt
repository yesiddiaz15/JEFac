package com.yediaz.jefac.di

import com.yediaz.jefac.presentation.features.appointment.AppointmentViewModel
import com.yediaz.jefac.presentation.features.dashboard.DashboardViewModel
import com.yediaz.jefac.presentation.features.login.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::AppointmentViewModel)
}
