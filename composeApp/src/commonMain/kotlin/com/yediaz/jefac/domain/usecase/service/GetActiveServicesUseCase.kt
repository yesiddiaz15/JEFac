package com.yediaz.jefac.domain.usecase.service

import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Service
import com.yediaz.jefac.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow

class GetActiveServicesUseCase(
    private val repository: ServiceRepository
) {
    operator fun invoke(): Flow<DomainResult<List<Service>>> =
        repository.getActiveServices()
}