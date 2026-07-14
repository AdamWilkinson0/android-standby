package com.adamwilkinson.standby.ui.split.panes

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.media.NowPlaying
import com.adamwilkinson.standby.data.weather.Weather
import com.adamwilkinson.standby.data.weather.WeatherUiState
import com.adamwilkinson.standby.data.weather.weatherDescription
import com.adamwilkinson.standby.data.weather.weatherKind
import com.adamwilkinson.standby.ui.components.MusicNoteIcon
import com.adamwilkinson.standby.ui.components.PlayPauseIcon
import com.adamwilkinson.standby.ui.components.WeatherGlyph
import com.adamwilkinson.standby.ui.pages.AlbumArt
import com.adamwilkinson.standby.ui.pages.BatteryRing
import com.adamwilkinson.standby.ui.rememberCurrentTime
import com.adamwilkinson.standby.ui.split.PaneWidget
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.LocalAccent
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import com.adamwilkinson.standby.vm.BatteryViewModel
import com.adamwilkinson.standby.vm.CalendarUiState
import com.adamwilkinson.standby.vm.CalendarViewModel
import com.adamwilkinson.standby.vm.MediaViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels
import com.adamwilkinson.standby.vm.WeatherViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Compact widgets for a stacked (split) right pane — each slot is roughly a
 * quarter of the screen, so these are horizontal, low-height layouts rather
 * than the tall single-pane designs. Every one follows the active accent.
 */
@Composable
fun MiniPaneContent(widget: PaneWidget, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        when (widget) {
            PaneWidget.Clock -> ClockMini()
            PaneWidget.Weather -> WeatherMini()
            PaneWidget.Calendar -> CalendarMini()
            PaneWidget.Battery -> BatteryMini()
            PaneWidget.NowPlaying -> NowPlayingMini()
        }
    }
}

@Composable
private fun ClockMini() {
    val accent = LocalAccent.current
    val time by rememberCurrentTime()
    val pattern = if (DateFormat.is24HourFormat(LocalContext.current)) "HH:mm" else "h:mm"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = time.format(DateTimeFormatter.ofPattern(pattern)),
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp,
                fontFeatureSettings = TABULAR_NUMS,
            ),
            color = accent.primary,
        )
        Text(
            text = time.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())),
            style = MaterialTheme.typography.bodyMedium,
            color = accent.secondary,
        )
    }
}

@Composable
private fun WeatherMini(viewModel: WeatherViewModel = viewModel(factory = StandbyViewModels.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val weather = when (val s = state) {
        is WeatherUiState.Data -> s.weather
        is WeatherUiState.Error -> s.lastKnown
        else -> null
    }
    if (weather == null) {
        MiniPlaceholder("Weather")
        return
    }
    WeatherMiniContent(weather)
}

@Composable
private fun WeatherMiniContent(weather: Weather) {
    val accent = LocalAccent.current
    val unit = if (weather.fahrenheit) "°F" else "°C"
    Row(verticalAlignment = Alignment.CenterVertically) {
        WeatherGlyph(
            kind = weatherKind(weather.weatherCode, weather.isDay),
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "${weather.temperature.roundToInt()}°",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp,
                    fontFeatureSettings = TABULAR_NUMS,
                ),
                color = accent.primary,
            )
            Text(
                text = weatherDescription(weather.weatherCode),
                style = MaterialTheme.typography.bodyMedium,
                color = accent.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "H ${weather.todayHigh.roundToInt()}$unit  L ${weather.todayLow.roundToInt()}$unit",
                style = MaterialTheme.typography.bodySmall,
                color = StandbyDim,
            )
        }
    }
}

@Composable
private fun BatteryMini(viewModel: BatteryViewModel = viewModel(factory = StandbyViewModels.Factory)) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    status?.let {
        BatteryRing(
            status = it,
            diameter = 120.dp,
            strokeWidth = 11.dp,
            percentStyle = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                fontFeatureSettings = TABULAR_NUMS,
            ),
            showStatusText = false,
        )
    }
}

@Composable
private fun CalendarMini(viewModel: CalendarViewModel = viewModel(factory = StandbyViewModels.Factory)) {
    val accent = LocalAccent.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val time by rememberCurrentTime()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = time.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
                    .uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = accent.secondary,
            )
            Text(
                text = time.format(DateTimeFormatter.ofPattern("d")),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp,
                ),
                color = accent.primary,
            )
        }
        val events = (state as? CalendarUiState.Events)?.events.orEmpty()
        if (events.isNotEmpty()) {
            Spacer(Modifier.width(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                events.take(2).forEach { event ->
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingMini(viewModel: MediaViewModel = viewModel(factory = StandbyViewModels.Factory)) {
    val hasAccess by viewModel.hasNotificationAccess.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()
    val media = nowPlaying

    when {
        !hasAccess || media == null -> MiniPlaceholder(
            title = if (!hasAccess) "See what's playing" else "Nothing playing",
            icon = true,
        )

        else -> NowPlayingMiniContent(media, viewModel::playPause)
    }
}

@Composable
private fun NowPlayingMiniContent(media: NowPlaying, onPlayPause: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AlbumArt(
            art = media.art,
            artUri = media.artUri,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp)),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.width(120.dp)) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = media.artist,
                style = MaterialTheme.typography.bodySmall,
                color = StandbyDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LocalAccent.current.primary)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            PlayPauseIcon(
                isPlaying = media.isPlaying,
                color = Color.Black,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun MiniPlaceholder(title: String, icon: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon) MusicNoteIcon(color = StandbyFaint, modifier = Modifier.size(30.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = StandbyDim,
        )
    }
}
