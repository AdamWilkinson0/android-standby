package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.theme.AccentPreset
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

/** iPhone-StandBy style stack: hours over minutes, as large as they fit. */
@Composable
fun StackedFace(
    time: LocalDateTime,
    use24Hour: Boolean,
    font: ClockFont,
    accent: AccentPreset,
    size: WidgetSize = WidgetSize.Full,
    modifier: Modifier = Modifier,
) {
    val hours = time.format(DateTimeFormatter.ofPattern(if (use24Hour) "HH" else "hh"))
    val minutes = time.format(DateTimeFormatter.ofPattern("mm"))

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        // Two stacked rows of two digits; height is the binding constraint.
        val digitSize = min(maxHeight.value / 2.1f, maxWidth.value / 1.6f).sp
        val digitStyle = MaterialTheme.typography.displayLarge.copy(
            fontFamily = font.family,
            fontWeight = font.weight,
            fontSize = digitSize,
            lineHeight = digitSize * 0.82f,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
            fontFeatureSettings = TABULAR_NUMS,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row {
                hours.forEach {
                    AnimatedDigit(digit = it, style = digitStyle.copy(color = accent.primary))
                }
            }
            Row {
                minutes.forEach {
                    AnimatedDigit(digit = it, style = digitStyle.copy(color = accent.secondary))
                }
            }
            if (size == WidgetSize.Full) {
                Text(
                    text = time
                        .format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))
                        .uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent.secondary,
                )
            }
        }
    }
}
