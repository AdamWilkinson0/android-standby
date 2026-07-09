package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.theme.AccentPreset
import com.adamwilkinson.standby.ui.theme.Oswald
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

private val CardColor = Color(0xFF161616)

/** Split-flap style: each digit on its own dark card with a center seam. */
@Composable
fun FlipFace(
    time: LocalDateTime,
    use24Hour: Boolean,
    accent: AccentPreset,
    size: WidgetSize = WidgetSize.Full,
    modifier: Modifier = Modifier,
) {
    val pattern = if (use24Hour) "HHmm" else "hhmm"
    val digits = time.format(DateTimeFormatter.ofPattern(pattern))

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        // Four cards + gaps: size the digits so the row spans ~90% of the
        // width without ever outgrowing the height.
        val digitSize = min(maxWidth.value / 4.4f, maxHeight.value / 1.5f).sp
        val cardPadding = (digitSize.value / 6f).dp
        val digitStyle = MaterialTheme.typography.displayLarge.copy(
            fontFamily = Oswald,
            fontWeight = FontWeight.SemiBold,
            fontSize = digitSize,
            fontFeatureSettings = TABULAR_NUMS,
            color = accent.primary,
        )
        val gap = if (size == WidgetSize.Full) 10.dp else 6.dp

        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            digits.forEachIndexed { index, digit ->
                if (index == 2) {
                    // Gap between hours and minutes stands in for the colon.
                    Box(Modifier.size(gap))
                }
                Box(
                    modifier = Modifier
                        .background(CardColor, RoundedCornerShape(18.dp))
                        .padding(horizontal = cardPadding, vertical = cardPadding / 2.5f),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedDigit(digit = digit, style = digitStyle)
                    // Center seam of the split-flap card. matchParentSize keeps the
                    // seam from inflating the card's wrap-content width.
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Black.copy(alpha = 0.6f)),
                        )
                    }
                }
            }
        }
    }
}
