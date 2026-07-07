package com.adamwilkinson.standby.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.adamwilkinson.standby.data.weather.WeatherKind
import kotlin.math.cos
import kotlin.math.sin

private val SunColor = Color(0xFFFFD262)
private val MoonColor = Color(0xFFD9DCE3)
private val CloudColor = Color(0xFFAab2BD)
private val RainColor = Color(0xFF7FB4FF)
private val SnowColor = Color(0xFFE8ECF4)
private val BoltColor = Color(0xFFFFD262)

/** Thin-line weather glyphs drawn to match the app's visual language. */
@Composable
fun WeatherGlyph(kind: WeatherKind, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        when (kind) {
            WeatherKind.ClearDay -> drawSun(center, size.minDimension * 0.28f)
            WeatherKind.ClearNight -> drawMoon()
            WeatherKind.PartlyCloudy -> {
                drawSun(
                    Offset(size.width * 0.36f, size.height * 0.34f),
                    size.minDimension * 0.18f,
                )
                drawCloud(yFraction = 0.62f)
            }
            WeatherKind.Cloudy -> drawCloud(yFraction = 0.5f)
            WeatherKind.Fog -> {
                drawCloud(yFraction = 0.4f)
                val stroke = size.minDimension * 0.045f
                listOf(0.72f, 0.84f).forEach { y ->
                    drawLine(
                        color = CloudColor.copy(alpha = 0.7f),
                        start = Offset(size.width * 0.2f, size.height * y),
                        end = Offset(size.width * 0.8f, size.height * y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
            }
            WeatherKind.Rain -> {
                drawCloud(yFraction = 0.42f)
                drawDrops()
            }
            WeatherKind.Snow -> {
                drawCloud(yFraction = 0.42f)
                listOf(0.32f, 0.52f, 0.72f).forEach { x ->
                    drawCircle(
                        color = SnowColor,
                        radius = size.minDimension * 0.035f,
                        center = Offset(size.width * x, size.height * 0.78f),
                    )
                }
            }
            WeatherKind.Thunder -> {
                drawCloud(yFraction = 0.4f)
                val bolt = Path().apply {
                    moveTo(size.width * 0.52f, size.height * 0.58f)
                    lineTo(size.width * 0.4f, size.height * 0.78f)
                    lineTo(size.width * 0.5f, size.height * 0.78f)
                    lineTo(size.width * 0.42f, size.height * 0.95f)
                    lineTo(size.width * 0.62f, size.height * 0.72f)
                    lineTo(size.width * 0.52f, size.height * 0.72f)
                    lineTo(size.width * 0.62f, size.height * 0.58f)
                    close()
                }
                drawPath(bolt, BoltColor)
            }
        }
    }
}

private fun DrawScope.drawSun(sunCenter: Offset, radius: Float) {
    val stroke = radius * 0.22f
    drawCircle(
        color = SunColor,
        radius = radius,
        center = sunCenter,
        style = Stroke(width = stroke),
    )
    repeat(8) { i ->
        val angle = Math.toRadians(i * 45.0)
        val from = radius * 1.45f
        val to = radius * 1.8f
        drawLine(
            color = SunColor,
            start = sunCenter + Offset(
                (cos(angle) * from).toFloat(),
                (sin(angle) * from).toFloat(),
            ),
            end = sunCenter + Offset(
                (cos(angle) * to).toFloat(),
                (sin(angle) * to).toFloat(),
            ),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawMoon() {
    // A crescent: bright disc with an offset background-colored disc on top.
    val radius = size.minDimension * 0.32f
    drawCircle(color = MoonColor, radius = radius, center = center)
    drawCircle(
        color = Color.Black,
        radius = radius * 0.92f,
        center = center + Offset(radius * 0.45f, -radius * 0.3f),
    )
}

private fun DrawScope.drawCloud(yFraction: Float) {
    val cy = size.height * yFraction
    val unit = size.minDimension
    drawCircle(CloudColor, radius = unit * 0.16f, center = Offset(size.width * 0.34f, cy))
    drawCircle(CloudColor, radius = unit * 0.22f, center = Offset(size.width * 0.52f, cy - unit * 0.07f))
    drawCircle(CloudColor, radius = unit * 0.16f, center = Offset(size.width * 0.68f, cy))
    drawRect(
        CloudColor,
        topLeft = Offset(size.width * 0.34f, cy),
        size = androidx.compose.ui.geometry.Size(size.width * 0.34f, unit * 0.16f),
    )
}

private fun DrawScope.drawDrops() {
    val stroke = size.minDimension * 0.05f
    listOf(0.36f, 0.52f, 0.68f).forEach { x ->
        drawLine(
            color = RainColor,
            start = Offset(size.width * x, size.height * 0.68f),
            end = Offset(size.width * (x - 0.05f), size.height * 0.86f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
