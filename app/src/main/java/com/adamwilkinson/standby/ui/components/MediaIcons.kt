package com.adamwilkinson.standby.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/*
 * Hand-drawn transport icons instead of material-icons-extended: keeps the
 * dependency footprint tiny and the geometry matches the app's thin, rounded
 * visual language.
 */

@Composable
fun PlayPauseIcon(isPlaying: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (isPlaying) {
            val barWidth = size.width * 0.26f
            val gap = size.width * 0.22f
            val left = (size.width - 2 * barWidth - gap) / 2f
            listOf(left, left + barWidth + gap).forEach { x ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2.5f),
                )
            }
        } else {
            val path = Path().apply {
                moveTo(size.width * 0.16f, size.height * 0.06f)
                lineTo(size.width * 0.88f, size.height / 2f)
                lineTo(size.width * 0.16f, size.height * 0.94f)
                close()
            }
            drawRoundedTriangle(path, color, size.minDimension * 0.16f)
        }
    }
}

/**
 * Fills [path] and strokes it with a round join so the corners read as soft
 * curves instead of sharp points. The round-join stroke widens the shape by
 * [radius] on every side, so triangle geometry is inset to compensate.
 */
private fun DrawScope.drawRoundedTriangle(path: Path, color: Color, radius: Float) {
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = radius * 2f, join = StrokeJoin.Round, cap = StrokeCap.Round),
    )
    drawPath(path, color)
}

@Composable
fun SkipIcon(forward: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barWidth = w * 0.12f
        val radius = w * 0.12f

        fun triangle(fromX: Float, toX: Float) = Path().apply {
            moveTo(fromX, h * 0.08f)
            lineTo(toX, h / 2f)
            lineTo(fromX, h * 0.92f)
            close()
        }

        if (forward) {
            drawRoundedTriangle(triangle(w * 0.06f, w * 0.56f), color, radius)
            drawRoundRect(
                color = color,
                topLeft = Offset(w - barWidth, 0f),
                size = Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2.5f),
            )
        } else {
            drawRoundedTriangle(triangle(w * 0.94f, w * 0.44f), color, radius)
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, 0f),
                size = Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2.5f),
            )
        }
    }
}

/** Simple eighth-note glyph for the "nothing playing" state. */
@Composable
fun MusicNoteIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stemX = w * 0.62f
        drawLine(
            color = color,
            start = Offset(stemX, h * 0.12f),
            end = Offset(stemX, h * 0.72f),
            strokeWidth = w * 0.07f,
            cap = StrokeCap.Round,
        )
        drawOval(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.62f),
            size = Size(w * 0.44f, h * 0.3f),
        )
        drawLine(
            color = color,
            start = Offset(stemX, h * 0.12f),
            end = Offset(w * 0.92f, h * 0.28f),
            strokeWidth = w * 0.07f,
            cap = StrokeCap.Round,
        )
    }
}
