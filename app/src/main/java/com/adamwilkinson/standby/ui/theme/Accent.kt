package com.adamwilkinson.standby.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Global accent theme. One preset colors every widget — clock digits,
 * battery ring, weather glyphs, media progress — so the whole app reads
 * as one family.
 */
enum class AccentPreset(
    val id: String,
    val label: String,
    /** Hero color: digits, rings, glyphs, active controls. */
    val primary: Color,
    /** Dimmed companion: dates, labels, secondary text. */
    val secondary: Color,
    /** Darker tone for gradients and containers. */
    val deep: Color,
) {
    Ice("ice", "Ice", Color(0xFFF2F2F2), Color(0xFF9A9A9A), Color(0xFF3A3A3A)),
    Sky("sky", "Sky", Color(0xFF7FB4FF), Color(0xFF6E88AC), Color(0xFF1B3050)),
    Sunset("sunset", "Sunset", Color(0xFFFF9F5A), Color(0xFFB07E5C), Color(0xFF4A2410)),
    Mint("mint", "Mint", Color(0xFF6FDF97), Color(0xFF6FA383), Color(0xFF123A22)),
    Rose("rose", "Rose", Color(0xFFFF7A9E), Color(0xFFB06C80), Color(0xFF4A1522)),
    Violet("violet", "Violet", Color(0xFFB18CFF), Color(0xFF8A78B0), Color(0xFF2E1E50));

    companion object {
        fun fromId(id: String?): AccentPreset = entries.firstOrNull { it.id == id } ?: Ice
    }
}

val LocalAccent = staticCompositionLocalOf { AccentPreset.Ice }
