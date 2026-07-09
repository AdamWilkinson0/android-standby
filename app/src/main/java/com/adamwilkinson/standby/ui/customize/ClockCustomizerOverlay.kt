package com.adamwilkinson.standby.ui.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.pages.ClockPage
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFont
import com.adamwilkinson.standby.ui.theme.AccentPreset
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.drop

/**
 * iOS-style edit mode: long-press the clock and a carousel of live faces
 * springs in, with font chips and accent dots underneath. Every selection
 * applies immediately, so the previews are the real thing.
 */
@Composable
fun ClockCustomizerOverlay(
    visible: Boolean,
    face: ClockFaceStyle,
    font: ClockFont,
    accent: AccentPreset,
    onSelectFace: (ClockFaceStyle) -> Unit,
    onSelectFont: (ClockFont) -> Unit,
    onSelectAccent: (AccentPreset) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.85f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        ) + fadeIn(),
        exit = scaleOut(targetScale = 0.9f) + fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                // Any tap that misses a control dismisses, like iOS edit mode.
                .pointerInput(Unit) { detectTapGestures(onTap = { onDone() }) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FaceCarousel(
                    face = face,
                    font = font,
                    onSelectFace = onSelectFace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Spacer(Modifier.height(14.dp))
                FontChips(selected = font, onSelect = onSelectFont)
                Spacer(Modifier.height(14.dp))
                AccentDots(selected = accent, onSelect = onSelectAccent)
                Spacer(Modifier.height(16.dp))
                DonePill(onDone = onDone)
            }
        }
    }
}

@Composable
private fun FaceCarousel(
    face: ClockFaceStyle,
    font: ClockFont,
    onSelectFace: (ClockFaceStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val faces = ClockFaceStyle.entries
    val pagerState = rememberPagerState(initialPage = faces.indexOf(face)) { faces.size }

    // Settling on a page selects it — live preview through real settings.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.drop(1).collect {
            onSelectFace(faces[it])
        }
    }

    BoxWithConstraints(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = maxWidth * 0.27f),
            pageSpacing = 20.dp,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            val style = faces[index]
            val selected = index == pagerState.currentPage
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distance = pagerState
                            .getOffsetDistanceInPages(index)
                            .absoluteValue
                            .coerceIn(0f, 1f)
                        val scale = 1f - distance * 0.12f
                        scaleX = scale
                        scaleY = scale
                        alpha = 1f - distance * 0.35f
                    }
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF0E0E0E))
                    .border(
                        width = 2.dp,
                        color = if (selected) StandbyDim else Color(0xFF242424),
                        shape = RoundedCornerShape(26.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ClockPage(
                        face = style,
                        font = font,
                        size = WidgetSize.Pane,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(14.dp),
                    )
                    Text(
                        text = style.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.onBackground else StandbyDim,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FontChips(selected: ClockFont, onSelect: (ClockFont) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ClockFont.entries.forEach { font ->
            val active = font == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (active) Color(0xFF242424) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (active) StandbyDim else Color(0xFF2A2A2A),
                        shape = RoundedCornerShape(50),
                    )
                    .clickable { onSelect(font) }
                    .padding(horizontal = 18.dp, vertical = 9.dp),
            ) {
                Text(
                    text = font.label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = font.family,
                        fontWeight = font.weight,
                    ),
                    color = if (active) MaterialTheme.colorScheme.onBackground else StandbyDim,
                )
            }
        }
    }
}

@Composable
private fun AccentDots(selected: AccentPreset, onSelect: (AccentPreset) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccentPreset.entries.forEach { preset ->
            val active = preset == selected
            val ring by animateDpAsState(
                targetValue = if (active) 3.dp else 0.dp,
                label = "accentRing",
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) Color.White else StandbyFaint,
                        shape = CircleShape,
                    )
                    .padding(ring)
                    .clip(CircleShape)
                    .background(preset.primary)
                    .clickable { onSelect(preset) },
            )
        }
    }
}

@Composable
private fun DonePill(onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .clickable(onClick = onDone)
            .padding(horizontal = 34.dp, vertical = 11.dp),
    ) {
        Text(
            text = "Done",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
        )
    }
}
