package com.adamwilkinson.standby.ui.split

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.adamwilkinson.standby.ui.components.PageIndicator
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFont
import com.adamwilkinson.standby.ui.split.panes.BatteryPane
import com.adamwilkinson.standby.ui.split.panes.CalendarPane
import com.adamwilkinson.standby.ui.split.panes.ClockPane
import com.adamwilkinson.standby.ui.split.panes.NowPlayingPane
import com.adamwilkinson.standby.ui.split.panes.WeatherPane
import kotlin.math.absoluteValue

/**
 * State for the two independently swipeable halves of the split view.
 * Hoisted so auto-split can drive the right pane programmatically.
 */
@Stable
class SplitPaneState(
    val leftPager: PagerState,
    val rightPager: PagerState,
)

@Composable
fun rememberSplitPaneState(leftId: String?, rightId: String?): SplitPaneState {
    val left = rememberPagerState(
        initialPage = PaneWidget.All.indexOf(PaneWidget.fromId(leftId ?: PaneWidget.Clock.id)),
    ) { PaneWidget.All.size }
    val right = rememberPagerState(
        initialPage = PaneWidget.All.indexOf(PaneWidget.fromId(rightId ?: PaneWidget.Weather.id)),
    ) { PaneWidget.All.size }
    return remember(left, right) { SplitPaneState(left, right) }
}

/**
 * iPhone-StandBy style split screen: two half-width vertical pagers, each
 * flipping independently through the compact widgets.
 */
@Composable
fun SplitView(
    state: SplitPaneState,
    clockFace: ClockFaceStyle,
    clockFont: ClockFont,
    onPaneLongPress: (isLeft: Boolean, widget: PaneWidget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        PaneColumn(
            pagerState = state.leftPager,
            clockFace = clockFace,
            clockFont = clockFont,
            indicatorAlignment = Alignment.CenterStart,
            onLongPress = { widget -> onPaneLongPress(true, widget) },
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        )
        Spacer(Modifier.width(12.dp))
        PaneColumn(
            pagerState = state.rightPager,
            clockFace = clockFace,
            clockFont = clockFont,
            indicatorAlignment = Alignment.CenterEnd,
            onLongPress = { widget -> onPaneLongPress(false, widget) },
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun PaneColumn(
    pagerState: PagerState,
    clockFace: ClockFaceStyle,
    clockFont: ClockFont,
    indicatorAlignment: Alignment,
    onLongPress: (PaneWidget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        VerticalPager(
            state = pagerState,
            beyondViewportPageCount = 0,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(index) {
                        // Long-press only; taps stay free for the chrome toggle.
                        detectTapGestures(onLongPress = {
                            onLongPress(PaneWidget.All[index])
                        })
                    }
                    .graphicsLayer {
                        // Same fade + shrink the horizontal pager uses.
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
                when (PaneWidget.All[index]) {
                    PaneWidget.Clock -> ClockPane(face = clockFace, font = clockFont)
                    PaneWidget.Weather -> WeatherPane()
                    PaneWidget.Calendar -> CalendarPane()
                    PaneWidget.Battery -> BatteryPane()
                    PaneWidget.NowPlaying -> NowPlayingPane()
                }
            }
        }

        PageIndicator(
            pagerState = pagerState,
            orientation = Orientation.Vertical,
            modifier = Modifier
                .align(indicatorAlignment)
                .padding(horizontal = 10.dp),
        )
    }
}
