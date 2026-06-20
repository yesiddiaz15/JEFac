package com.yediaz.jefac.domain.repository

import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Service
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    fun getActiveServices(): Flow<DomainResult<List<Service>>>
    suspend fun createService(service: Service): DomainResult<String>
}