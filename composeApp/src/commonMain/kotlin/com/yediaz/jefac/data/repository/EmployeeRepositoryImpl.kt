package com.yediaz.jefac.data.repository

import com.yediaz.jefac.data.model.EmployeeDto
import com.yediaz.jefac.data.model.toDomain
import com.yediaz.jefac.data.model.toDto
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Employee
import com.yediaz.jefac.domain.repository.EmployeeRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class EmployeeRepositoryImpl(
    private val firestore: FirebaseFirestore
) : EmployeeRepository {

    private val collection = firestore.collection("employees")

    override fun getActiveEmployees() =
        collection
            .where { "isActive" equalTo true }
            .snapshots
            .map<_, DomainResult<List<Employee>>> { snapshot ->
                val employees = snapshot.documents.map { doc ->
                    doc.data<EmployeeDto>().copy(id = doc.id).toDomain()
                }
                DomainResult.Success(employees)
            }
            .catch { e ->
                emit(DomainResult.Error(e.message ?: "Error al obtener empleados"))
            }

    override suspend fun createEmployee(employee: Employee): DomainResult<String> {
        return try {
            val docRef = collection.document
            val dto = employee.copy(id = docRef.id).toDto()
            docRef.set(dto)
            DomainResult.Success(docRef.id)
        } catch (e: Exception) {
            DomainResult.Error(e.message ?: "Error al crear empleado")
        }
    }
}