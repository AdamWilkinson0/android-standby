package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.adamwilkinson.standby.ui.theme.StandbyAccent
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Canvas-drawn analog face with a ticking second hand. */
@Composable
fun AnalogFace(time: LocalDateTime, modifier: Modifier = Modifier) {
    val handColor = MaterialTheme.colorScheme.onBackground

    Box(modifier = modifier.fillMaxHeight(0.85f).aspectRatio(1f)) {
        Canvas(modifier = Modifier.aspectRatio(1f).fillMaxHeight()) {
            val radius = min(size.width, size.height) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Hour markers: quarters slightly brighter and longer.
            repeat(12) { i ->
                val angle = Math.toRadians(i * 30.0 - 90.0)
                val isQuarter = i % 3 == 0
                val outer = radius * 0.98f
                val inner = radius * if (isQuarter) 0.88f else 0.93f
                drawLine(
                    color = if (isQuarter) StandbyDim else StandbyFaint,
                    start = center + Offset(
                        (cos(angle) * inner).toFloat(),
                        (sin(angle) * inner).toFloat(),
                    ),
                    end = center + Offset(
                        (cos(angle) * outer).toFloat(),
                        (sin(angle) * outer).toFloat(),
                    ),
                    strokeWidth = if (isQuarter) 6f else 3f,
                    cap = StrokeCap.Round,
                )
            }

            fun drawHand(fraction: Double, length: Float, width: Float, color: Color) {
                val angle = Math.toRadians(fraction * 360.0 - 90.0)
                drawLine(
                    color = color,
                    start = center,
                    end = center + Offset(
                        (cos(angle) * radius * length).toFloat(),
                        (sin(angle) * radius * length).toFloat(),
                    ),
                    strokeWidth = width,
                    cap = StrokeCap.Round,
                )
            }

            val second = time.second
            val minute = time.minute + second / 60.0
            val hour = time.hour % 12 + minute / 60.0

            drawHand(hour / 12.0, length = 0.5f, width = 14f, color = handColor)
            drawHand(minute / 60.0, length = 0.72f, width = 9f, color = handColor)
            drawHand(second / 60.0, length = 0.8f, width = 3f, color = StandbyAccent)

            drawCircle(color = handColor, radius = 10f, center = center)
        }
    }
}
