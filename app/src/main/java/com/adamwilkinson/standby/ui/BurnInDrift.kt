package com.adamwilkinson.standby.ui

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * OLED burn-in mitigation: slowly drifts the content a few pixels in a random
 * direction every minute. The shift is animated over two seconds so it is
 * imperceptible at a glance.
 */
fun Modifier.burnInDrift(maxOffsetPx: Int = 8): Modifier = composed {
    var target by remember { mutableStateOf(IntOffset.Zero) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            target = IntOffset(
                Random.nextInt(-maxOffsetPx, maxOffsetPx + 1),
                Random.nextInt(-maxOffsetPx, maxOffsetPx + 1),
            )
        }
    }
    val animated by animateIntOffsetAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 2_000),
        label = "burnInDrift",
    )
    offset { animated }
}
