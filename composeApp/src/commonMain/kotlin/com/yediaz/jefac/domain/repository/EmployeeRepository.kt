package com.yediaz.jefac.domain.repository

import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Employee
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    fun getActiveEmployees(): Flow<DomainResult<List<Employee>>>
    suspend fun createEmployee(employee: Employee): DomainResult<String>
}