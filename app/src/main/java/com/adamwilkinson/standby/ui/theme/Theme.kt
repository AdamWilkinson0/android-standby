package com.adamwilkinson.standby.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StandbyColorScheme = darkColorScheme(
    primary = StandbyAccent,
    onPrimary = StandbyBlack,
    background = StandbyBlack,
    onBackground = StandbyWhite,
    surface = StandbyBlack,
    onSurface = StandbyWhite,
    surfaceVariant = StandbyFaint,
    onSurfaceVariant = StandbyDim,
)

@Composable
fun StandbyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StandbyColorScheme,
        typography = StandbyTypography,
        content = content,
    )
}
