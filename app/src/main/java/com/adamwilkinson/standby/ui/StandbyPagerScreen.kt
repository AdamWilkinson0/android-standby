package com.adamwilkinson.standby.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.adamwilkinson.standby.ui.theme.StandbyAccent
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import kotlinx.coroutines.delay
import com.adamwilkinson.standby.ui.components.PageIndicator
import com.adamwilkinson.standby.ui.pages.BatteryPage
import com.adamwilkinson.standby.ui.pages.CalendarPage
import com.adamwilkinson.standby.ui.pages.ClockPage
import com.adamwilkinson.standby.ui.pages.MediaPage
import com.adamwilkinson.standby.ui.pages.WeatherPage
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import kotlin.math.absoluteValue

@Composable
fun StandbyPagerScreen(
    pages: List<StandbyPage>,
    clockFace: ClockFaceStyle,
    nightDimEnabled: Boolean,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onBrightnessCommit: (Float) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { pages.size }
    // Chrome (gear + brightness) only appears on tap and fades away.
    var chromeVisible by remember { mutableStateOf(false) }
    var chromeInteraction by remember { mutableIntStateOf(0) }
    LaunchedEffect(chromeVisible, chromeInteraction) {
        if (chromeVisible) {
            delay(4_000)
            chromeVisible = false
        }
    }

    val time by rememberCurrentTime()
    val isNight = nightDimEnabled && (time.hour >= 22 || time.hour < 7)
    val nightAlpha by animateFloatAsState(
        targetValue = if (isNight) 0.45f else 0f,
        animationSpec = tween(1_500),
        label = "nightDim",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures { chromeVisible = !chromeVisible }
            },
    ) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .burnInDrift(),
        ) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Subtle fade + shrink as a page moves off-center.
                        val distance = pagerState
                            .getOffsetDistanceInPages(index)
                            .absoluteValue
                            .coerceIn(0f, 1f)
                        alpha = 1f - distance * 0.5f
                        val scale = 1f - distance * 0.06f
                        scaleX = scale
                        scaleY = scale
                    },
            ) {
                when (pages[index]) {
                    StandbyPage.Clock -> ClockPage(face = clockFace)
                    StandbyPage.NowPlaying -> MediaPage()
                    StandbyPage.Weather -> WeatherPage()
                    StandbyPage.Calendar -> CalendarPage()
                    StandbyPage.Battery -> BatteryPage()
                }
            }
        }

        // Night dim: warm-black wash between 22:00 and 07:00. Draw-only, so
        // it never intercepts touches.
        if (nightAlpha > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D0300).copy(alpha = nightAlpha)),
            )
        }

        PageIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
        )

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = StandbyDim,
                )
            }
        }

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
        ) {
            var sliderValue by remember(brightness) { mutableFloatStateOf(brightness) }
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    chromeInteraction++
                    onBrightnessChange(it)
                },
                onValueChangeFinished = { onBrightnessCommit(sliderValue) },
                valueRange = 0.05f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = StandbyDim,
                    activeTrackColor = StandbyAccent.copy(alpha = 0.7f),
                    inactiveTrackColor = StandbyFaint.copy(alpha = 0.4f),
                ),
                modifier = Modifier.width(320.dp),
            )
        }
    }
}
