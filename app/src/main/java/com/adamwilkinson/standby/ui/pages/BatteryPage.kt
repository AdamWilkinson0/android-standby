package com.adamwilkinson.standby.ui.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.battery.BatteryStatus
import com.adamwilkinson.standby.data.battery.PlugType
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.LocalAccent
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import com.adamwilkinson.standby.vm.BatteryViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels

private val ChargeGreen = Color(0xFF6FCF97)

@Composable
fun BatteryPage(
    modifier: Modifier = Modifier,
    viewModel: BatteryViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val status by viewModel.status.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        status?.let {
            BatteryRing(
                status = it,
                diameter = 280.dp,
                strokeWidth = 22.dp,
                percentStyle = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 88.sp,
                    fontFeatureSettings = TABULAR_NUMS,
                ),
                showStatusText = true,
            )
        }
    }
}

/** Thick progress ring with a bold percentage — shared by page and pane. */
@Composable
fun BatteryRing(
    status: BatteryStatus,
    diameter: Dp,
    strokeWidth: Dp,
    percentStyle: TextStyle,
    showStatusText: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccent.current
    val active = status.isCharging || status.isFull
    val ringColor = if (active) ChargeGreen else accent.primary
    val sweep by animateFloatAsState(
        targetValue = status.percent / 100f * 360f,
        animationSpec = tween(900),
        label = "batterySweep",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2f
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(
                color = Color(0xFF232323),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${status.percent}%",
                style = percentStyle,
                color = if (active) ChargeGreen else accent.primary,
            )
            if (showStatusText) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = batteryStatusLabel(status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) ChargeGreen else accent.secondary,
                )
            }
        }
    }
}

/**
 * Bold, non-circular battery readout — a big percentage over a chunky capsule
 * fill with a battery-terminal nub. Fills the width of a split tile far better
 * than a ring, so it's the layout used on the main screen's panes.
 */
@Composable
fun BatteryBar(
    status: BatteryStatus,
    percentStyle: TextStyle,
    barHeight: Dp,
    showStatusText: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccent.current
    val active = status.isCharging || status.isFull
    val color = if (active) ChargeGreen else accent.primary

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(barHeight * 0.65f),
    ) {
        Column {
            Text(
                text = "${status.percent}%",
                style = percentStyle,
                color = color,
            )
            if (showStatusText) {
                Text(
                    text = batteryStatusLabel(status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) ChargeGreen else accent.secondary,
                )
            }
        }
        BatteryCapsule(
            fraction = status.percent / 100f,
            color = color,
            height = barHeight,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Rounded track + animated fill + a small terminal nub, so it reads as a battery. */
@Composable
fun BatteryCapsule(
    fraction: Float,
    color: Color,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "batteryFill",
    )
    val shape = RoundedCornerShape(percent = 50)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(height)
                .clip(shape)
                .background(Color(0xFF232323)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated.coerceAtLeast(0.04f))
                    .fillMaxHeight()
                    .clip(shape)
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(alpha = 0.82f), color)),
                    ),
            )
        }
        Spacer(Modifier.width(height * 0.22f))
        Box(
            modifier = Modifier
                .width(height * 0.3f)
                .height(height * 0.44f)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF303030)),
        )
    }
}

private fun batteryStatusLabel(status: BatteryStatus): String = when {
    status.isFull -> "Fully charged"
    status.isCharging && status.plug == PlugType.Wireless -> "Charging wirelessly"
    status.isCharging -> "Charging"
    else -> "On battery"
}
