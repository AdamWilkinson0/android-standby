package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Text

enum class ClockFaceStyle(val id: String, val label: String) {
    Digital("digital", "Digital"),
    Analog("analog", "Analog"),
    Flip("flip", "Flip"),
    Minimal("minimal", "Minimal");

    companion object {
        fun fromId(id: String?): ClockFaceStyle =
            entries.firstOrNull { it.id == id } ?: Digital
    }
}

/**
 * A single clock digit that rolls vertically when its value changes.
 * Each digit animates independently, so a minute rollover only moves the
 * digits that actually changed.
 */
@Composable
fun AnimatedDigit(digit: Char, style: TextStyle, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            (slideInVertically { height -> -height / 3 } + fadeIn()) togetherWith
                (slideOutVertically { height -> height / 3 } + fadeOut())
        },
        label = "digit",
        modifier = modifier,
    ) { value ->
        Text(text = value.toString(), style = style)
    }
}
