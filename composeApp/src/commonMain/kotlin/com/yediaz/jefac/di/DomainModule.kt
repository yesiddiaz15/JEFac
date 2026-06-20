package com.yediaz.jefac.di

import com.yediaz.jefac.domain.usecase.auth.LoginUseCase
import com.yediaz.jefac.domain.usecase.appointment.CreateAppointmentUseCase
import com.yediaz.jefac.domain.usecase.appointment.GetAppointmentsByDateRangeUseCase
import com.yediaz.jefac.domain.usecase.appointment.UpdateAppointmentStatusUseCase
import com.yediaz.jefac.domain.usecase.employee.CreateEmployeeUseCase
import com.yediaz.jefac.domain.usecase.employee.GetActiveEmployeesUseCase
import com.yediaz.jefac.domain.usecase.service.CreateServiceUseCase
import com.yediaz.jefac.domain.usecase.service.GetActiveServicesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    // Auth
    factoryOf(::LoginUseCase)

    // Employee
    factoryOf(::GetActiveEmployeesUseCase)
    factoryOf(::CreateEmployeeUseCase)

    // Service
    factoryOf(::GetActiveServicesUseCase)
    factoryOf(::CreateServiceUseCase)

    // Appointment
    factoryOf(::GetAppointmentsByDateRangeUseCase)
    factoryOf(::CreateAppointmentUseCase)
    factoryOf(::UpdateAppointmentStatusUseCase)
}