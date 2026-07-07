package com.adamwilkinson.standby.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.adamwilkinson.standby.ui.components.PageIndicator
import com.adamwilkinson.standby.ui.pages.BatteryPage
import com.adamwilkinson.standby.ui.pages.ClockPage
import com.adamwilkinson.standby.ui.pages.MediaPage
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import kotlin.math.absoluteValue

@Composable
fun StandbyPagerScreen(
    pages: List<StandbyPage>,
    clockFace: ClockFaceStyle,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { pages.size }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                    StandbyPage.Battery -> BatteryPage()
                    // Built in later milestones; filtered out of the list until then.
                    StandbyPage.Weather, StandbyPage.Calendar -> Unit
                }
            }
        }

        PageIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
        )
    }
}
