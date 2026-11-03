package com.ivy.settings

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.ivy.ui.typography.IvyInter
import com.ivy.ui.typography.ivyInter

// -----------------------------------------------------------------------------------------
// Settings typography - delegates to the shared Inter family (shared/ui/core).
// -----------------------------------------------------------------------------------------

internal val Inter: FontFamily = IvyInter

internal fun inter(
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = ivyInter(size, weight, letterSpacing)
