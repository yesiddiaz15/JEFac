package com.yediaz.jefac.domain.usecase.service

import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Service
import com.yediaz.jefac.domain.repository.ServiceRepository

class CreateServiceUseCase(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(service: Service): DomainResult<String> {
        if (service.name.isBlank()) {
            return DomainResult.Error("El nombre del servicio es obligatorio")
        }
        if (service.price <= 0) {
            return DomainResult.Error("El precio debe ser mayor a cero")
        }
        if (service.durationMinutes <= 0) {
            return DomainResult.Error("La duración debe ser mayor a cero")
        }
        return repository.createService(service)
    }
}