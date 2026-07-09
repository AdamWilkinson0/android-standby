package com.adamwilkinson.standby.ui.split.panes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.pages.BigDate
import com.adamwilkinson.standby.ui.pages.EventRow
import com.adamwilkinson.standby.ui.rememberCurrentTime
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.LocalAccent
import com.adamwilkinson.standby.vm.CalendarUiState
import com.adamwilkinson.standby.vm.CalendarViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarPane(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            CalendarUiState.Loading -> Unit

            // The pane stays a pretty date; granting happens on the full page.
            CalendarUiState.NoPermission,
            CalendarUiState.Empty,
            -> BigDate(subtitle = null, size = WidgetSize.Pane)

            is CalendarUiState.Events -> PaneEvents(current)
        }
    }
}

@Composable
private fun PaneEvents(state: CalendarUiState.Events) {
    val accent = LocalAccent.current
    val time by rememberCurrentTime()

    Column(
        modifier = Modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = time.format(DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())),
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 44.sp,
            ),
            color = accent.primary,
        )
        state.events.take(2).forEach { event -> EventRow(event) }
    }
}
