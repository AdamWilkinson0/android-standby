package com.adamwilkinson.standby.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.adamwilkinson.standby.data.weather.WeatherKind
import com.adamwilkinson.standby.ui.theme.LocalAccent
import com.adamwilkinson.standby.ui.theme.StandbyBlack
import kotlin.math.cos
import kotlin.math.sin

/**
 * Thin-line weather glyphs drawn to match the app's visual language.
 * Every glyph follows the active accent: the hero element (sun, moon, bolt,
 * precipitation) uses the primary tone and clouds use the dimmer secondary,
 * so the icon reads as one family with the rest of the page.
 */
@Composable
fun WeatherGlyph(kind: WeatherKind, modifier: Modifier = Modifier) {
    val accent = LocalAccent.current
    val hero = accent.primary
    val cloud = accent.secondary
    Canvas(modifier = modifier) {
        when (kind) {
            WeatherKind.ClearDay -> drawSun(center, size.minDimension * 0.28f, hero)
            WeatherKind.ClearNight -> drawMoon(hero)
            WeatherKind.PartlyCloudy -> {
                drawSun(
                    Offset(size.width * 0.36f, size.height * 0.34f),
                    size.minDimension * 0.18f,
                    hero,
                )
                drawCloud(yFraction = 0.62f, color = cloud)
            }
            WeatherKind.Cloudy -> drawCloud(yFraction = 0.5f, color = cloud)
            WeatherKind.Fog -> {
                drawCloud(yFraction = 0.4f, color = cloud)
                val stroke = size.minDimension * 0.045f
                listOf(0.72f, 0.84f).forEach { y ->
                    drawLine(
                        color = cloud.copy(alpha = 0.7f),
                        start = Offset(size.width * 0.2f, size.height * y),
                        end = Offset(size.width * 0.8f, size.height * y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
            }
            WeatherKind.Rain -> {
                drawCloud(yFraction = 0.42f, color = cloud)
                drawDrops(hero)
            }
            WeatherKind.Snow -> {
                drawCloud(yFraction = 0.42f, color = cloud)
                listOf(0.32f, 0.52f, 0.72f).forEach { x ->
                    drawCircle(
                        color = hero,
                        radius = size.minDimension * 0.035f,
                        center = Offset(size.width * x, size.height * 0.78f),
                    )
                }
            }
            WeatherKind.Thunder -> {
                drawCloud(yFraction = 0.4f, color = cloud)
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
                drawPath(bolt, hero)
            }
        }
    }
}

private fun DrawScope.drawSun(sunCenter: Offset, radius: Float, color: Color) {
    val stroke = radius * 0.22f
    drawCircle(
        color = color,
        radius = radius,
        center = sunCenter,
        style = Stroke(width = stroke),
    )
    repeat(8) { i ->
        val angle = Math.toRadians(i * 45.0)
        val from = radius * 1.45f
        val to = radius * 1.8f
        drawLine(
            color = color,
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

private fun DrawScope.drawMoon(color: Color) {
    // A crescent: bright disc with an offset background-colored disc on top.
    val radius = size.minDimension * 0.32f
    drawCircle(color = color, radius = radius, center = center)
    drawCircle(
        color = StandbyBlack,
        radius = radius * 0.92f,
        center = center + Offset(radius * 0.45f, -radius * 0.3f),
    )
}

private fun DrawScope.drawCloud(yFraction: Float, color: Color) {
    val cy = size.height * yFraction
    val unit = size.minDimension
    drawCircle(color, radius = unit * 0.16f, center = Offset(size.width * 0.34f, cy))
    drawCircle(color, radius = unit * 0.22f, center = Offset(size.width * 0.52f, cy - unit * 0.07f))
    drawCircle(color, radius = unit * 0.16f, center = Offset(size.width * 0.68f, cy))
    drawRect(
        color,
        topLeft = Offset(size.width * 0.34f, cy),
        size = androidx.compose.ui.geometry.Size(size.width * 0.34f, unit * 0.16f),
    )
}

private fun DrawScope.drawDrops(color: Color) {
    val stroke = size.minDimension * 0.05f
    listOf(0.36f, 0.52f, 0.68f).forEach { x ->
        drawLine(
            color = color,
            start = Offset(size.width * x, size.height * 0.68f),
            end = Offset(size.width * (x - 0.05f), size.height * 0.86f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
