package com.yediaz.jefac.domain.usecase.employee

import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Employee
import com.yediaz.jefac.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow

class GetActiveEmployeesUseCase(
    private val repository: EmployeeRepository
) {
    operator fun invoke(): Flow<DomainResult<List<Employee>>> =
        repository.getActiveEmployees()
}