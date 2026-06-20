package com.yediaz.jefac.domain.usecase.appointment

import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow

class GetAppointmentsByDateRangeUseCase(
    private val repository: AppointmentRepository
) {
    operator fun invoke(
        startMillis: Long,
        endMillis: Long
    ): Flow<DomainResult<List<Appointment>>> =
        repository.getAppointmentsByDateRange(startMillis, endMillis)
}