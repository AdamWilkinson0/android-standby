package com.adamwilkinson.standby.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamwilkinson.standby.ui.theme.AccentPreset
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint

private val CardColor = Color(0xFF141414)
private val DividerColor = Color(0xFF262626)

/** iOS-style large screen title. */
@Composable
fun LargeTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

/** Grouped rounded card of settings rows with inset dividers, iOS style. */
@Composable
fun SettingsGroup(
    header: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (header != null) {
            Text(
                text = header.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = StandbyFaint,
                modifier = Modifier.padding(start = 18.dp, bottom = 8.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(CardColor),
        ) {
            content()
        }
    }
}

/** Inset divider between rows of a [SettingsGroup]. */
@Composable
fun GroupDivider() {
    HorizontalDivider(
        color = DividerColor,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 18.dp),
    )
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StandbyDim,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                checkedThumbColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

/** Row of selectable text chips, used for the face and font pickers. */
@Composable
fun <T> ChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (active) Color(0xFF262626) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color(0xFF2A2A2A)
                        },
                        shape = RoundedCornerShape(50),
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 18.dp, vertical = 9.dp),
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) MaterialTheme.colorScheme.primary else StandbyDim,
                )
            }
        }
    }
}

/** Six accent dots; the chosen one wears a white ring. */
@Composable
fun AccentSwatchRow(
    selected: AccentPreset,
    onSelect: (AccentPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccentPreset.entries.forEach { preset ->
            val active = preset == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) Color.White else StandbyFaint,
                        shape = CircleShape,
                    )
                    .padding(if (active) 4.dp else 1.dp)
                    .clip(CircleShape)
                    .background(preset.primary)
                    .clickable { onSelect(preset) },
            )
        }
    }
}
