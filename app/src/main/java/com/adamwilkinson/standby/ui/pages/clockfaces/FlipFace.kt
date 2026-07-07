package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.adamwilkinson.standby.ui.theme.Oswald
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val CardColor = Color(0xFF161616)

/** Split-flap style: each digit on its own dark card with a center seam. */
@Composable
fun FlipFace(time: LocalDateTime, use24Hour: Boolean, modifier: Modifier = Modifier) {
    val pattern = if (use24Hour) "HHmm" else "hhmm"
    val digits = time.format(DateTimeFormatter.ofPattern(pattern))
    val digitStyle = MaterialTheme.typography.displayLarge.copy(
        fontFamily = Oswald,
        fontWeight = FontWeight.Medium,
        fontSize = 130.sp,
        fontFeatureSettings = TABULAR_NUMS,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        digits.forEachIndexed { index, digit ->
            if (index == 2) {
                // Gap between hours and minutes stands in for the colon.
                Box(Modifier.size(10.dp))
            }
            Box(
                modifier = Modifier
                    .background(CardColor, RoundedCornerShape(18.dp))
                    .padding(horizontal = 22.dp, vertical = 8.dp),
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
