@file:OptIn(ExperimentalTextApi::class)

package com.adamwilkinson.standby.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.adamwilkinson.standby.R

private fun interFont(weight: FontWeight) = Font(
    R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Inter = FontFamily(
    interFont(FontWeight.Thin),
    interFont(FontWeight.ExtraLight),
    interFont(FontWeight.Light),
    interFont(FontWeight.Normal),
    interFont(FontWeight.Medium),
    interFont(FontWeight.SemiBold),
    interFont(FontWeight.Bold),
)

private fun oswaldFont(weight: FontWeight) = Font(
    R.font.oswald_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Oswald = FontFamily(
    oswaldFont(FontWeight.Light),
    oswaldFont(FontWeight.Normal),
    oswaldFont(FontWeight.Medium),
    oswaldFont(FontWeight.SemiBold),
)

private fun rubikFont(weight: FontWeight) = Font(
    R.font.rubik_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Rubik = FontFamily(
    rubikFont(FontWeight.Normal),
    rubikFont(FontWeight.Medium),
    rubikFont(FontWeight.SemiBold),
    rubikFont(FontWeight.Bold),
    rubikFont(FontWeight.ExtraBold),
    rubikFont(FontWeight.Black),
)

private fun spaceGroteskFont(weight: FontWeight) = Font(
    R.font.space_grotesk_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val SpaceGrotesk = FontFamily(
    spaceGroteskFont(FontWeight.Normal),
    spaceGroteskFont(FontWeight.Medium),
    spaceGroteskFont(FontWeight.SemiBold),
    spaceGroteskFont(FontWeight.Bold),
)

/** Tabular figures so clock digits never jiggle as they change. */
const val TABULAR_NUMS = "tnum"

// Bold-first scale: confident weights everywhere, thin weights are gone.
val StandbyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 120.sp,
        fontFeatureSettings = TABULAR_NUMS,
    ),
    displayMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 56.sp,
        fontFeatureSettings = TABULAR_NUMS,
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 1.5.sp,
    ),
)
