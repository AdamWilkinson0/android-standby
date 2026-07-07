package com.adamwilkinson.standby.ui.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.calendar.CalendarEvent
import com.adamwilkinson.standby.ui.components.PermissionCard
import com.adamwilkinson.standby.ui.rememberCurrentTime
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import com.adamwilkinson.standby.vm.CalendarUiState
import com.adamwilkinson.standby.vm.CalendarViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarPage(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.refresh() }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            CalendarUiState.Loading -> Unit

            CalendarUiState.NoPermission -> PermissionCard(
                title = "Your day at a glance",
                body = "Standby can show your upcoming events with calendar access.",
                buttonLabel = "Allow calendar",
                onGrant = {
                    permissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
                },
            )

            CalendarUiState.Empty -> BigDate(subtitle = "No events in the next two days")

            is CalendarUiState.Events -> EventsContent(current.events)
        }
    }
}

/** The empty state is a beautiful date display, never a useless page. */
@Composable
private fun BigDate(subtitle: String) {
    val time by rememberCurrentTime()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = time.format(DateTimeFormatter.ofPattern("d")),
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraLight,
                fontSize = 150.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = time.format(DateTimeFormatter.ofPattern("EEEE MMMM", Locale.getDefault())),
            style = MaterialTheme.typography.headlineMedium,
            color = StandbyDim,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = StandbyFaint,
        )
    }
}

@Composable
private fun EventsContent(events: List<CalendarEvent>) {
    val time by rememberCurrentTime()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp, vertical = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(56.dp),
    ) {
        Column {
            Text(
                text = time.format(DateTimeFormatter.ofPattern("d")),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraLight,
                    fontSize = 110.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = time.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())),
                style = MaterialTheme.typography.headlineMedium,
                color = StandbyDim,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        ) {
            events.forEach { event -> EventRow(event) }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (event.color != 0) Color(event.color) else StandbyDim),
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = eventTimeLabel(event),
                style = MaterialTheme.typography.bodyMedium,
                color = StandbyDim,
            )
        }
    }
}

private fun eventTimeLabel(event: CalendarEvent): String {
    val zone = ZoneId.systemDefault()
    val begin = Instant.ofEpochMilli(event.beginMillis).atZone(zone)
    val today = LocalDate.now(zone)
    val dayPrefix = when (begin.toLocalDate()) {
        today -> ""
        today.plusDays(1) -> "Tomorrow "
        else -> begin.format(DateTimeFormatter.ofPattern("EEE "))
    }
    if (event.allDay) return "${dayPrefix.ifBlank { "Today " }}· all day"

    val end = Instant.ofEpochMilli(event.endMillis).atZone(zone)
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    return "$dayPrefix${begin.format(timeFormat)} – ${end.format(timeFormat)}"
}
