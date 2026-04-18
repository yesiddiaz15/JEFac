package com.yediaz.jefac.feature.appointments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yediaz.jefac.core.models.AppUser
import com.yediaz.jefac.core.ui.AppColors
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun AppointmentsScreen(
    user: AppUser,
    refreshKey: Int = 0,
    onNavigateToNewAppointment: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val viewModel: AppointmentListViewModel = viewModel(
        factory = AppointmentListViewModel.Factory(user.businessId)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) viewModel.handleIntent(AppointmentListIntent.LoadAppointments)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AppointmentListEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                is AppointmentListEffect.NavigateToNewAppointment -> onNavigateToNewAppointment()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.handleIntent(AppointmentListIntent.NavigateToNewAppointment) },
                containerColor = AppColors.Primary,
                contentColor = AppColors.OnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva cita")
            }
        },
        containerColor = AppColors.BgMain
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item { AppointmentsHeader(uiState) }
            item { MiniCalendar(uiState, viewModel) }
            item { FilterRow(uiState.selectedFilter, viewModel) }
            item {
                Text(
                    text = "${uiState.appointments.size} citas",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextMuted,
                    letterSpacing = 0.7.sp,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    )
                )
            }
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = AppColors.Primary) }
                }
            }
            if (uiState.appointments.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay citas para este período",
                            fontSize = 14.sp,
                            color = AppColors.TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(uiState.appointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = {
                            viewModel.handleIntent(
                                AppointmentListIntent.OpenAppointment(appointment.id)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentsHeader(uiState: AppointmentListUiState) {
    val monthName = monthName(uiState.calendarMonth)
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = "Agenda",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextDark
        )
        Text(
            text = "$monthName ${uiState.calendarYear}",
            fontSize = 12.sp,
            color = AppColors.TextMuted,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun MiniCalendar(
    uiState: AppointmentListUiState,
    viewModel: AppointmentListViewModel
) {
    val year = uiState.calendarYear.takeIf { it > 0 } ?: return
    val month = uiState.calendarMonth.takeIf { it > 0 } ?: return

    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val firstOfMonth = LocalDate(year, month, 1)
    val daysInMonth = LocalDate(year, month, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day
    // ordinal: Mon=0..Sun=6 → convert to Sun=0..Sat=6: (ordinal + 1) % 7
    val startOffset = (firstOfMonth.dayOfWeek.ordinal + 1) % 7

    val dayNames = listOf("D", "L", "M", "X", "J", "V", "S")

    // Derive days with appointments from current loaded list
    val daysWithAppt = uiState.appointments
        .mapNotNull { appt ->
            try {
                val apptDate = appt.scheduledAt.substring(0, 10)
                val apptLocalDate = LocalDate.parse(apptDate)
                if (apptLocalDate.year == year && apptLocalDate.month.ordinal + 1 == month) {
                    apptLocalDate.day
                } else null
            } catch (e: Exception) { null }
        }.toSet()

    val selectedDay: Int? = uiState.selectedDate?.let {
        try {
            val d = LocalDate.parse(it)
            if (d.year == year && d.month.ordinal + 1 == month) d.day else null
        } catch (e: Exception) { null }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.handleIntent(AppointmentListIntent.PrevMonth) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = AppColors.TextMuted)
                }
                Text(
                    text = "${monthName(month)} $year",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                IconButton(onClick = { viewModel.handleIntent(AppointmentListIntent.NextMonth) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = AppColors.TextMuted)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                dayNames.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        color = AppColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val rows = (startOffset + daysInMonth + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1
                        val isValidDay = dayNumber in 1..daysInMonth
                        val isToday = isValidDay && today.year == year && today.month.ordinal + 1 == month && dayNumber == today.day
                        val isSelected = isValidDay && selectedDay == dayNumber
                        val hasAppt = dayNumber in daysWithAppt

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> AppColors.Primary
                                        isToday -> AppColors.BgSecondary
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = isValidDay) {
                                    val date = "${year}-${month.toString().padStart(2, '0')}-${dayNumber.toString().padStart(2, '0')}"
                                    viewModel.handleIntent(AppointmentListIntent.SelectCalendarDay(date))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 12.sp,
                                        color = when {
                                            isSelected -> AppColors.OnPrimary
                                            isToday -> AppColors.Primary
                                            else -> AppColors.TextDark
                                        },
                                        textAlign = TextAlign.Center
                                    )
                                    if (hasAppt) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) AppColors.OnPrimary.copy(alpha = 0.7f)
                                                    else AppColors.PrimaryLight
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedFilter: AppointmentFilter,
    viewModel: AppointmentListViewModel
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AppointmentFilter.entries) { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AppColors.Primary else AppColors.BgCard)
                    .border(
                        0.5.dp,
                        if (isSelected) AppColors.Primary else AppColors.Border,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.handleIntent(AppointmentListIntent.FilterChanged(filter)) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = when (filter) {
                        AppointmentFilter.TODAY -> "Hoy"
                        AppointmentFilter.WEEK -> "Semana"
                        AppointmentFilter.ALL -> "Todas"
                    },
                    fontSize = 13.sp,
                    color = if (isSelected) AppColors.OnPrimary else AppColors.TextMuted,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun AppointmentCard(appointment: AppointmentItemUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgCard),
        border = BorderStroke(0.5.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(appointment.scheduledAt),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextMuted,
                modifier = Modifier.width(40.dp)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor(appointment.status))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.clientName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextDark
                )
                Text(
                    text = "${appointment.serviceName} · ${appointment.professionalName}",
                    fontSize = 11.sp,
                    color = AppColors.TextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(appointment.finalPrice),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Primary
                )
                StatusBadge(appointment.status)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, textColor, label) = when (status) {
        "confirmed" -> Triple(AppColors.CourtesyBg, AppColors.CourtesyGreen, "Confirmada")
        "in_progress" -> Triple(AppColors.BgSecondary, AppColors.Primary, "En curso")
        "pending" -> Triple(Color(0xFFF5EDD6), AppColors.PrimaryDark, "Pendiente")
        "completed" -> Triple(Color(0xFFE8F0F8), AppColors.BusinessEarn, "Completada")
        else -> Triple(AppColors.Border, AppColors.TextMuted, status)
    }
    Box(
        modifier = Modifier
            .padding(top = 3.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

private fun formatTime(scheduledAt: String): String =
    try {
        scheduledAt.substring(11, 16)
    } catch (e: Exception) {
        "--:--"
    }

private fun formatCurrency(amount: Double): String = "$${amount.toLong()}"

private fun statusColor(status: String): Color = when (status) {
    "confirmed" -> AppColors.StatusConfirmed
    "in_progress" -> AppColors.StatusInProgress
    "pending" -> AppColors.StatusPending
    "completed" -> AppColors.StatusCompleted
    else -> AppColors.Border
}

private fun monthName(month: Int): String = when (month) {
    1 -> "Enero"; 2 -> "Febrero"; 3 -> "Marzo"; 4 -> "Abril"
    5 -> "Mayo"; 6 -> "Junio"; 7 -> "Julio"; 8 -> "Agosto"
    9 -> "Septiembre"; 10 -> "Octubre"; 11 -> "Noviembre"; 12 -> "Diciembre"
    else -> ""
}
