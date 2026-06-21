package com.yediaz.jefac.presentation.components.appointment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.AppointmentStatus

@Composable
fun AppointmentItemCard(
    appointment: Appointment,
    onStatusChanged: (AppointmentStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.titleMedium
                )
                AppointmentStatusChip(
                    status = appointment.status,
                    onStatusChanged = onStatusChanged
                )
            }

            Spacer(modifier = Modifier.padding(top = 4.dp))

            Text(
                text = "Cliente: ${appointment.clientName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Profesional: ${appointment.employeeName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Duración: ${appointment.durationMinutes} min",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}