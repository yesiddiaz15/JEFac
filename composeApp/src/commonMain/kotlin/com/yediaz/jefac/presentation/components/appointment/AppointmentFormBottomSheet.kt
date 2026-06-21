package com.yediaz.jefac.presentation.components.appointment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yediaz.jefac.domain.model.Employee
import com.yediaz.jefac.domain.model.Service
import com.yediaz.jefac.presentation.features.appointment.AppointmentIntent
import com.yediaz.jefac.presentation.features.appointment.AppointmentState
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormBottomSheet(
    state: AppointmentState,
    onIntent: (AppointmentIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onIntent(AppointmentIntent.OnDismissForm) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Agendar nueva cita",
                style = MaterialTheme.typography.titleLarge
            )

            EmployeeDropdown(
                employees = state.employees,
                selectedId = state.selectedEmployeeId,
                onSelected = { onIntent(AppointmentIntent.OnEmployeeSelected(it)) }
            )

            ServiceDropdown(
                services = state.services,
                selectedId = state.selectedServiceId,
                onSelected = { onIntent(AppointmentIntent.OnServiceSelected(it)) }
            )

            OutlinedTextField(
                value = state.clientName,
                onValueChange = { onIntent(AppointmentIntent.OnClientNameChanged(it)) },
                label = { Text("Nombre del cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.clientPhone,
                onValueChange = { onIntent(AppointmentIntent.OnClientPhoneChanged(it)) },
                label = { Text("Teléfono del cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            DateTimeSelector(
                selectedMillis = state.selectedDateMillis,
                onDateTimeSelected = { onIntent(AppointmentIntent.OnDateSelected(it)) }
            )

            OutlinedTextField(
                value = state.durationMinutes.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let {
                        onIntent(AppointmentIntent.OnDurationChanged(it))
                    }
                },
                label = { Text("Duración (minutos)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.formError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { onIntent(AppointmentIntent.OnSaveAppointment) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                Text(if (state.isSaving) "Guardando..." else "Agendar cita")
            }
        }
    }
}

// ─────────────────────────────────────────────
// Dropdown de Empleados
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeDropdown(
    employees: List<Employee>,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = employees.firstOrNull { it.id == selectedId }?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Profesional") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            employees.forEach { employee ->
                DropdownMenuItem(
                    text = { Text(employee.name) },
                    onClick = {
                        onSelected(employee.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Dropdown de Servicios
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceDropdown(
    services: List<Service>,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = services.firstOrNull { it.id == selectedId }?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Servicio") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            services.forEach { service ->
                DropdownMenuItem(
                    text = { Text("${service.name} - ${service.durationMinutes} min") },
                    onClick = {
                        onSelected(service.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Selector de Fecha y Hora
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeSelector(
    selectedMillis: Long,
    onDateTimeSelected: (Long) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var dateOnlyMillis by remember { mutableStateOf<Long?>(null) }

    val displayText = if (selectedMillis > 0L) {
        val localDateTime = Instant.fromEpochMilliseconds(selectedMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    } else {
        ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Fecha y hora de la cita") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateOnlyMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("Siguiente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        BasicAlertDialog(
            onDismissRequest = { showTimePicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)

                Button(
                    onClick = {
                        val baseDate = dateOnlyMillis ?: 0L
                        val localDate = Instant.fromEpochMilliseconds(baseDate)
                            .toLocalDateTime(TimeZone.UTC).date

                        val combined = LocalDateTime(
                            date = localDate,
                            time = LocalTime(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute
                            )
                        ).toInstant(TimeZone.currentSystemDefault())
                            .toEpochMilliseconds()

                        onDateTimeSelected(combined)
                        showTimePicker = false
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Confirmar")
                }
            }
        }
    }
}