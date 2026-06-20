package com.yediaz.jefac.data.repository

import com.yediaz.jefac.data.model.ServiceDto
import com.yediaz.jefac.data.model.toDomain
import com.yediaz.jefac.data.model.toDto
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.model.Service
import com.yediaz.jefac.domain.repository.ServiceRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ServiceRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ServiceRepository {

    private val collection = firestore.collection("services")

    override fun getActiveServices() =
        collection
            .where { "isActive" equalTo true }
            .snapshots
            .map<_, DomainResult<List<Service>>> { snapshot ->
                val services = snapshot.documents.map { doc ->
                    doc.data<ServiceDto>().copy(id = doc.id).toDomain()
                }
                DomainResult.Success(services)
            }
            .catch { e ->
                emit(DomainResult.Error(e.message ?: "Error al obtener servicios"))
            }

    override suspend fun createService(service: Service): DomainResult<String> {
        return try {
            val docRef = collection.document
            val dto = service.copy(id = docRef.id).toDto()
            docRef.set(dto)
            DomainResult.Success(docRef.id)
        } catch (e: Exception) {
            DomainResult.Error(e.message ?: "Error al crear servicio")
        }
    }
}