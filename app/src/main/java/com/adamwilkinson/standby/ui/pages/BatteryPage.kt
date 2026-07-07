package com.adamwilkinson.standby.ui.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.battery.BatteryStatus
import com.adamwilkinson.standby.data.battery.PlugType
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.StandbyDim
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
        status?.let { BatteryContent(it) }
    }
}

@Composable
private fun BatteryContent(status: BatteryStatus) {
    val active = status.isCharging || status.isFull
    val ringColor = if (active) ChargeGreen else MaterialTheme.colorScheme.onBackground
    val sweep by animateFloatAsState(
        targetValue = status.percent / 100f * 360f,
        animationSpec = tween(900),
        label = "batterySweep",
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(260.dp)) {
            val stroke = Stroke(width = 14f, cap = StrokeCap.Round)
            val inset = stroke.width / 2f
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(
                color = Color(0xFF1C1C1C),
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
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraLight,
                    fontSize = 64.sp,
                    fontFeatureSettings = TABULAR_NUMS,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = when {
                    status.isFull -> "Fully charged"
                    status.isCharging && status.plug == PlugType.Wireless -> "Charging wirelessly"
                    status.isCharging -> "Charging"
                    else -> "On battery"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (active) ChargeGreen else StandbyDim,
            )
        }
    }
}
