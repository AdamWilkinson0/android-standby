package com.adamwilkinson.standby.ui.pages

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.pages.clockfaces.AnalogFace
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFont
import com.adamwilkinson.standby.ui.pages.clockfaces.DigitalFace
import com.adamwilkinson.standby.ui.pages.clockfaces.FlipFace
import com.adamwilkinson.standby.ui.pages.clockfaces.StackedFace
import com.adamwilkinson.standby.ui.rememberCurrentTime
import com.adamwilkinson.standby.ui.theme.LocalAccent

@Composable
fun ClockPage(
    face: ClockFaceStyle,
    font: ClockFont,
    size: WidgetSize = WidgetSize.Full,
    modifier: Modifier = Modifier,
) {
    val time by rememberCurrentTime()
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val accent = LocalAccent.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val faceModifier = Modifier.fillMaxSize()
        when (face) {
            ClockFaceStyle.Digital ->
                DigitalFace(time, use24Hour, font, accent, size, faceModifier)
            ClockFaceStyle.Analog -> AnalogFace(time, accent)
            ClockFaceStyle.Flip -> FlipFace(time, use24Hour, accent, size, faceModifier)
            ClockFaceStyle.Minimal ->
                StackedFace(time, use24Hour, font, accent, size, faceModifier)
        }
    }
}
