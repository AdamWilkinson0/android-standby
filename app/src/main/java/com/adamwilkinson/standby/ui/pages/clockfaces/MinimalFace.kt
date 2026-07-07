package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Small, quiet time in a sea of black — maximal OLED rest. */
@Composable
fun MinimalFace(time: LocalDateTime, use24Hour: Boolean, modifier: Modifier = Modifier) {
    val pattern = if (use24Hour) "HH:mm" else "hh:mm"
    val digitStyle = MaterialTheme.typography.displayMedium.copy(
        fontFamily = Inter,
        fontWeight = FontWeight.Thin,
        fontSize = 72.sp,
        fontFeatureSettings = TABULAR_NUMS,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            time.format(DateTimeFormatter.ofPattern(pattern)).forEach { char ->
                if (char.isDigit()) {
                    AnimatedDigit(digit = char, style = digitStyle)
                } else {
                    Text(text = char.toString(), style = digitStyle, color = StandbyFaint)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = time.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))
                .uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = StandbyFaint,
        )
    }
}
