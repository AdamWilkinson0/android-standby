package com.adamwilkinson.standby.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Palette pulled from album art; every media surface shares one of these. */
@Immutable
data class ArtColors(
    val accent: Color,
    val accentDark: Color,
    val muted: Color,
)

/**
 * Extracts vibrant/dark/muted swatches from the artwork, smoothly animating
 * between tracks. Falls back to the global accent when there is no art.
 */
@Composable
fun rememberArtColors(art: Bitmap?): ArtColors {
    val preset = LocalAccent.current
    val fallback = ArtColors(preset.primary, preset.deep, preset.secondary)
    var colors by remember { mutableStateOf(fallback) }

    LaunchedEffect(art, fallback) {
        colors = if (art == null) {
            fallback
        } else {
            withContext(Dispatchers.Default) {
                val palette = Palette.from(art).generate()
                val accent = palette.vibrantSwatch?.rgb
                    ?: palette.lightVibrantSwatch?.rgb
                    ?: palette.dominantSwatch?.rgb
                val dark = palette.darkVibrantSwatch?.rgb ?: palette.darkMutedSwatch?.rgb
                val muted = palette.mutedSwatch?.rgb
                ArtColors(
                    accent = accent?.let { Color(it) } ?: fallback.accent,
                    accentDark = dark?.let { Color(it) } ?: fallback.accentDark,
                    muted = muted?.let { Color(it) } ?: fallback.muted,
                )
            }
        }
    }

    val accent by animateColorAsState(colors.accent, tween(600), label = "artAccent")
    val accentDark by animateColorAsState(colors.accentDark, tween(600), label = "artAccentDark")
    val muted by animateColorAsState(colors.muted, tween(600), label = "artMuted")
    return ArtColors(accent, accentDark, muted)
}
