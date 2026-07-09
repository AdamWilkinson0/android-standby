package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.theme.AccentPreset
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

/** Huge bold digits filling the screen — the hero face. */
@Composable
fun DigitalFace(
    time: LocalDateTime,
    use24Hour: Boolean,
    font: ClockFont,
    accent: AccentPreset,
    size: WidgetSize = WidgetSize.Full,
    modifier: Modifier = Modifier,
) {
    val pattern = if (use24Hour) "HH:mm" else "hh:mm"
    val text = time.format(DateTimeFormatter.ofPattern(pattern))

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        // Digits scale with the container: ~90% of the width for "HH:mm",
        // capped so the column (digits + date) always fits the height.
        val digitSize = min(maxWidth.value / 3.5f, maxHeight.value / 1.7f).sp
        val digitStyle = MaterialTheme.typography.displayLarge.copy(
            fontFamily = font.family,
            fontWeight = font.weight,
            fontSize = digitSize,
            fontFeatureSettings = TABULAR_NUMS,
            color = accent.primary,
        )
        val datePattern = if (size == WidgetSize.Full) "EEEE d MMMM" else "EEE d MMM"

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                text.forEach { char ->
                    if (char.isDigit()) {
                        AnimatedDigit(digit = char, style = digitStyle)
                    } else {
                        Text(text = char.toString(), style = digitStyle, color = accent.secondary)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = time.format(DateTimeFormatter.ofPattern(datePattern, Locale.getDefault())),
                style = if (size == WidgetSize.Full) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = accent.secondary,
            )
        }
    }
}
