package com.adamwilkinson.standby.ui.split

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.adamwilkinson.standby.ui.split.panes.MiniPaneContent
import com.adamwilkinson.standby.ui.split.panes.NowPlayingPane
import com.adamwilkinson.standby.ui.split.panes.WeatherPane
import kotlin.math.absoluteValue

/** Which of the (up to three) split slots a gesture targets. */
enum class PaneSlot { Left, RightTop, RightBottom }

/**
 * State for the independently swipeable slots of the split view. The right
 * side is either one full pager (rightPager) or, when split, a stack of
 * rightPager (top) over rightBottomPager (bottom). Hoisted so auto-split can
 * drive the top-right pane programmatically.
 */
@Stable
class SplitPaneState(
    val leftPager: PagerState,
    val rightPager: PagerState,
    val rightBottomPager: PagerState,
)

@Composable
fun rememberSplitPaneState(
    leftId: String?,
    rightId: String?,
    rightBottomId: String?,
): SplitPaneState {
    val left = rememberPagerState(
        initialPage = PaneWidget.All.indexOf(PaneWidget.fromId(leftId ?: PaneWidget.Clock.id)),
    ) { PaneWidget.All.size }
    val right = rememberPagerState(
        initialPage = PaneWidget.All.indexOf(PaneWidget.fromId(rightId ?: PaneWidget.Weather.id)),
    ) { PaneWidget.All.size }
    val rightBottom = rememberPagerState(
        initialPage = PaneWidget.All.indexOf(PaneWidget.fromId(rightBottomId ?: PaneWidget.Battery.id)),
    ) { PaneWidget.All.size }
    return remember(left, right, rightBottom) { SplitPaneState(left, right, rightBottom) }
}

/**
 * iPhone-StandBy style split screen. The left widget is given the larger share
 * of the width (and hugs less of the outer edge on the right) so the two halves
 * sit closer together, leaving the clock room to breathe. The right half can be
 * stacked into a top + bottom pair of compact widgets.
 */
@Composable
fun SplitView(
    state: SplitPaneState,
    clockFace: ClockFaceStyle,
    clockFont: ClockFont,
    rightSplit: Boolean,
    onPaneLongPress: (slot: PaneSlot, widget: PaneWidget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        PaneColumn(
            pagerState = state.leftPager,
            clockFace = clockFace,
            clockFont = clockFont,
            mini = false,
            indicatorAlignment = Alignment.CenterStart,
            onLongPress = { widget -> onPaneLongPress(PaneSlot.Left, widget) },
            modifier = Modifier
                .weight(1.35f)
                .fillMaxSize(),
        )
        if (rightSplit) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(end = 4.dp),
            ) {
                PaneColumn(
                    pagerState = state.rightPager,
                    clockFace = clockFace,
                    clockFont = clockFont,
                    mini = true,
                    indicatorAlignment = Alignment.CenterEnd,
                    onLongPress = { widget -> onPaneLongPress(PaneSlot.RightTop, widget) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                )
                PaneColumn(
                    pagerState = state.rightBottomPager,
                    clockFace = clockFace,
                    clockFont = clockFont,
                    mini = true,
                    indicatorAlignment = Alignment.CenterEnd,
                    onLongPress = { widget -> onPaneLongPress(PaneSlot.RightBottom, widget) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                )
            }
        } else {
            PaneColumn(
                pagerState = state.rightPager,
                clockFace = clockFace,
                clockFont = clockFont,
                mini = false,
                indicatorAlignment = Alignment.CenterEnd,
                onLongPress = { widget -> onPaneLongPress(PaneSlot.RightTop, widget) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(end = 4.dp),
            )
        }
    }
}

@Composable
private fun PaneColumn(
    pagerState: PagerState,
    clockFace: ClockFaceStyle,
    clockFont: ClockFont,
    mini: Boolean,
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
                val widget = PaneWidget.All[index]
                if (mini) {
                    MiniPaneContent(widget)
                } else {
                    when (widget) {
                        PaneWidget.Clock -> ClockPane(face = clockFace, font = clockFont)
                        PaneWidget.Weather -> WeatherPane()
                        PaneWidget.Calendar -> CalendarPane()
                        PaneWidget.Battery -> BatteryPane()
                        PaneWidget.NowPlaying -> NowPlayingPane()
                    }
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
