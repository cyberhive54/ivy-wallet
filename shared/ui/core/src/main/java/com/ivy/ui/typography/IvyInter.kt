package com.ivy.ui.typography

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.ivy.ui.R

/**
 * Apple-inspired typography shared across all settings-related screens.
 * Inter is SIL Open Font Licensed - an SF Pro analog, safe for redistribution.
 *
 * Used with the legacy `UI.colors` tokens by feature modules, so this file is
 * intentionally color-free and depends only on Compose + bundled fonts.
 */
val IvyInter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

fun ivyInter(
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
    fontFamily = IvyInter,
    fontSize = size,
    fontWeight = weight,
    letterSpacing = letterSpacing,
)
