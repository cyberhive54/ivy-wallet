package com.ivy.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter
import java.util.Locale

// -----------------------------------------------------------------------------------------
// Home typography - delegates to the shared Inter family (shared/ui/core), mirroring the
// Settings module idiom so both surfaces read as one system.
// -----------------------------------------------------------------------------------------

internal fun homeInter(
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = ivyInter(size, weight, letterSpacing)

/** iOS hairline - 8% of the ink color, mode & material aware. */
@Composable
internal fun homeHairline(): Color = UI.colors.pureInverse.copy(alpha = 0.08f)

/** Row separator used inside grouped cards - slightly softer than the card border. */
@Composable
internal fun homeRowSeparator(): Color = UI.colors.pureInverse.copy(alpha = 0.06f)

/** Uppercase 13sp section header, aligned with grouped card content. */
@Composable
internal fun HomeSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = UI.colors.gray,
) {
    androidx.compose.foundation.layout.Spacer(Modifier.height(28.dp))

    Text(
        modifier = modifier
            .padding(start = 36.dp),
        text = text.uppercase(Locale.getDefault()),
        style = homeInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
        color = color
    )

    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
}

/** Full-width 1dp hairline. */
@Composable
internal fun HairlineDivider(
    modifier: Modifier = Modifier,
    color: Color = homeRowSeparator(),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}
