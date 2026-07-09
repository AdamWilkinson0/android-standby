package com.adamwilkinson.standby.ui.split.panes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.pages.ClockPage
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFont

/** The clock at half width — same faces, pane-scaled by their constraints. */
@Composable
fun ClockPane(face: ClockFaceStyle, font: ClockFont, modifier: Modifier = Modifier) {
    ClockPage(
        face = face,
        font = font,
        size = WidgetSize.Pane,
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
    )
}
