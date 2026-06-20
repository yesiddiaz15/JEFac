package com.yediaz.jefac.data.repository

import com.yediaz.jefac.data.model.AppointmentDto
import com.yediaz.jefac.data.model.toDomain
import com.yediaz.jefac.data.model.toDto
import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.AppointmentStatus
import com.yediaz.jefac.domain.model.DomainResult
import com.yediaz.jefac.domain.repository.AppointmentRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AppointmentRepositoryImpl(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    private val collection = firestore.collection("appointments")

    override fun getAppointmentsByDateRange(
        startMillis: Long,
        endMillis: Long
    ) = collection
        .where {
            ("date" greaterThanOrEqualTo startMillis) and
                ("date" lessThanOrEqualTo endMillis)
        }
        .snapshots
        .map<_, DomainResult<List<Appointment>>> { snapshot ->
            val appointments = snapshot.documents.map { doc ->
                doc.data<AppointmentDto>().copy(id = doc.id).toDomain()
            }
            DomainResult.Success(appointments)
        }
        .catch { e ->
            emit(DomainResult.Error(e.message ?: "Error al obtener citas"))
        }

    override suspend fun createAppointment(appointment: Appointment): DomainResult<String> {
        return try {
            val docRef = collection.document
            val dto = appointment.copy(id = docRef.id).toDto()
            docRef.set(dto)
            DomainResult.Success(docRef.id)
        } catch (e: Exception) {
            DomainResult.Error(e.message ?: "Error al agendar la cita")
        }
    }

    override suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): DomainResult<Unit> {
        return try {
            collection.document(appointmentId)
                .update("status" to status.name)
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            DomainResult.Error(e.message ?: "Error al actualizar la cita")
        }
    }
}