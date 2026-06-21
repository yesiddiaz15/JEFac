package com.yediaz.jefac.presentation.components.appointment

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yediaz.jefac.domain.model.AppointmentStatus

@Composable
fun AppointmentStatusChip(
    status: AppointmentStatus,
    onStatusChanged: (AppointmentStatus) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    SuggestionChip(
        onClick = { isMenuExpanded = true },
        label = { Text(status.toDisplayLabel()) }
    )

    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { isMenuExpanded = false }
    ) {
        AppointmentStatus.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.toDisplayLabel()) },
                onClick = {
                    onStatusChanged(option)
                    isMenuExpanded = false
                }
            )
        }
    }
}

private fun AppointmentStatus.toDisplayLabel(): String = when (this) {
    AppointmentStatus.PENDING -> "Pendiente"
    AppointmentStatus.CONFIRMED -> "Confirmada"
    AppointmentStatus.COMPLETED -> "Completada"
    AppointmentStatus.CANCELLED -> "Cancelada"
    AppointmentStatus.NO_SHOW -> "No asistió"
}