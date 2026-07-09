package com.adamwilkinson.standby.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun StandbyTheme(
    accent: AccentPreset = AccentPreset.Ice,
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = accent.primary,
        onPrimary = StandbyBlack,
        secondary = accent.secondary,
        onSecondary = StandbyBlack,
        primaryContainer = accent.deep,
        onPrimaryContainer = accent.primary,
        background = StandbyBlack,
        onBackground = StandbyWhite,
        surface = StandbyBlack,
        onSurface = StandbyWhite,
        surfaceVariant = StandbyFaint,
        onSurfaceVariant = StandbyDim,
    )
    CompositionLocalProvider(LocalAccent provides accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StandbyTypography,
            content = content,
        )
    }
}
