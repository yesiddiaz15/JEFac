package com.yediaz.jefac.presentation.features.appointment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yediaz.jefac.domain.model.Appointment
import com.yediaz.jefac.domain.model.AppointmentStatus
import com.yediaz.jefac.presentation.components.appointment.AppointmentFormBottomSheet
import com.yediaz.jefac.presentation.components.appointment.AppointmentItemCard
import com.yediaz.jefac.presentation.components.appointment.EmptyAppointmentsPlaceholder
import com.yediaz.jefac.presentation.components.base.BaseScreen
import com.yediaz.jefac.presentation.components.base.BaseUi
import com.yediaz.jefac.presentation.model.TopBarConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppointmentScreen(
    viewModel: AppointmentViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                AppointmentEffect.AppointmentSaved -> Unit // Revisar, utilizar snackbar Global?
            }
        }
    ) { state, _, onIntent ->

        LaunchedEffect(Unit) {
            onIntent(AppointmentIntent.LoadAppointments)
        }

        AppointmentContent(
            state = state,
            onIntent = onIntent,
            onBackClick = onNavigateBack
        )
    }
}

@Composable
fun AppointmentContent(
    state: AppointmentState,
    onIntent: (AppointmentIntent) -> Unit,
    onBackClick: () -> Unit
) {
    BaseUi(
        state = state,
        onIntent = onIntent,
        topBarConfig = TopBarConfig(
            title = "Citas de hoy",
            showBackButton = true,
            onBackClick = onBackClick
        ),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(AppointmentIntent.OnCreateClicked) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agendar cita")
            }
        }
    ) { state, paddingValues, onIntent ->
        AppointmentListBody(
            state = state,
            paddingValues = paddingValues,
            onIntent = onIntent
        )
    }

    if (state.isFormVisible) {
        AppointmentFormBottomSheet(
            state = state,
            onIntent = onIntent
        )
    }
}

@Composable
private fun AppointmentListBody(
    state: AppointmentState,
    paddingValues: PaddingValues,
    onIntent: (AppointmentIntent) -> Unit
) {
    if (state.appointments.isEmpty() && !state.isLoading) {
        EmptyAppointmentsPlaceholder(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = state.appointments,
            key = { it.id }
        ) { appointment ->
            AppointmentItemCard(
                appointment = appointment,
                onStatusChanged = { newStatus ->
                    onIntent(
                        AppointmentIntent.OnStatusChanged(
                            appointmentId = appointment.id,
                            newStatus = newStatus
                        )
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun AppointmentContentPreview() {
    MaterialTheme {
        AppointmentContent(
            state = AppointmentState(
                appointments = listOf(
                    Appointment(
                        id = "1",
                        employeeName = "Laura Gómez",
                        serviceName = "Manicure Gel",
                        servicePrice = 35000.0,
                        clientName = "Camila Ruiz",
                        clientPhone = "3001234567",
                        durationMinutes = 45,
                        status = AppointmentStatus.PENDING
                    )
                )
            ),
            onIntent = {},
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun AppointmentContentEmptyPreview() {
    MaterialTheme {
        AppointmentContent(
            state = AppointmentState(),
            onIntent = {},
            onBackClick = {}
        )
    }
}