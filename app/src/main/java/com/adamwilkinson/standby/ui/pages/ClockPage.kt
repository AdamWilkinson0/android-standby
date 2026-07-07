package com.adamwilkinson.standby.ui.pages

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adamwilkinson.standby.ui.pages.clockfaces.AnalogFace
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.pages.clockfaces.DigitalFace
import com.adamwilkinson.standby.ui.pages.clockfaces.FlipFace
import com.adamwilkinson.standby.ui.pages.clockfaces.MinimalFace
import com.adamwilkinson.standby.ui.rememberCurrentTime

@Composable
fun ClockPage(face: ClockFaceStyle, modifier: Modifier = Modifier) {
    val time by rememberCurrentTime()
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (face) {
            ClockFaceStyle.Digital -> DigitalFace(time, use24Hour)
            ClockFaceStyle.Analog -> AnalogFace(time)
            ClockFaceStyle.Flip -> FlipFace(time, use24Hour)
            ClockFaceStyle.Minimal -> MinimalFace(time, use24Hour)
        }
    }
}
