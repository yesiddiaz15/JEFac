package com.yediaz.jefac.domain.usecase.employee

import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Employee
import com.yediaz.jefac.domain.repository.EmployeeRepository

class CreateEmployeeUseCase(
    private val repository: EmployeeRepository
) {
    suspend operator fun invoke(employee: Employee): DomainResult<String> {
        if (employee.name.isBlank()) {
            return DomainResult.Error("El nombre del empleado es obligatorio")
        }
        if (employee.phone.isBlank()) {
            return DomainResult.Error("El teléfono del empleado es obligatorio")
        }
        return repository.createEmployee(employee)
    }
}