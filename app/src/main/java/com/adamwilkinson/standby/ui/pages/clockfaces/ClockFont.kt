package com.adamwilkinson.standby.ui.pages.clockfaces

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.Oswald
import com.adamwilkinson.standby.ui.theme.Rubik
import com.adamwilkinson.standby.ui.theme.SpaceGrotesk

/** Selectable typeface presets for the clock digits. */
enum class ClockFont(
    val id: String,
    val label: String,
    val family: FontFamily,
    val weight: FontWeight,
) {
    Classic("classic", "Classic", Inter, FontWeight.Bold),
    Rounded("rounded", "Rounded", Rubik, FontWeight.ExtraBold),
    Condensed("condensed", "Condensed", Oswald, FontWeight.SemiBold),
    Grotesk("grotesk", "Grotesk", SpaceGrotesk, FontWeight.Bold);

    companion object {
        fun fromId(id: String?): ClockFont = entries.firstOrNull { it.id == id } ?: Classic
    }
}
