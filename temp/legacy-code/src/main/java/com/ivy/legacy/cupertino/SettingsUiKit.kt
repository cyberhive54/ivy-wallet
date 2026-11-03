package com.ivy.legacy.cupertino

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.theme.components.IvySwitch

// -----------------------------------------------------------------------------------------
// Cupertino UI kit shared by all Settings child pages (dock-aware).
//
// Design language: large 34sp titles, 8pt grid, 20dp margins, inset grouped cards with
// 20dp radius, hairline separators, 44-52dp rows, no shadows. Rows that leave the app
// show an up-right external arrow instead of the chevron.
// -----------------------------------------------------------------------------------------

/** Bottom clearance for the floating dock rendered by MainScreen. */
val CupertinoDockClearance: Dp = 130.dp

fun cupertinoHairline(): Color = UI.colors.pureInverse.copy(alpha = 0.08f)

/**
 * Full-screen scaffold for a Settings child page: back row ("< Settings"), large title,
 * then the page content in the remaining space. The dock stays visible below.
 */
@Composable
fun BoxWithConstraintsScope.CupertinoChildPage(
    title: String,
    subtitle: String? = null,
    onClose: () -> Unit,
    backLabel: String = stringResource(R.string.settings),
    onSkip: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UI.colors.pure)
            .statusBarsPadding()
    ) {
        CupertinoBackRow(onClose = onClose, label = backLabel, onSkip = onSkip)

        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = title,
                style = ivyInter(34.sp, FontWeight.Bold, letterSpacing = (-0.5).sp),
                color = UI.colors.pureInverse,
                maxLines = 1
            )

            if (!subtitle.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = ivyInter(15.sp),
                    color = UI.colors.gray
                )
            }
        }

        Box(Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
fun CupertinoBackRow(
    onClose: () -> Unit,
    label: String = stringResource(R.string.settings),
    onSkip: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClose),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = UI.colors.pureInverse,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(180f)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = label,
                style = ivyInter(17.sp, FontWeight.Medium),
                color = UI.colors.pureInverse
            )
        }

        if (onSkip != null) {
            Spacer(Modifier.weight(1f))

            Text(
                modifier = Modifier
                    .clickable(onClick = onSkip)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                text = stringResource(R.string.skip),
                style = ivyInter(17.sp, FontWeight.Medium),
                color = UI.colors.gray
            )
        }
    }
}

@Composable
fun CupertinoSectionHeader(
    text: String,
    color: Color = UI.colors.gray,
) {
    Spacer(Modifier.height(24.dp))

    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = text.uppercase(),
        style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
        color = color
    )

    Spacer(Modifier.height(10.dp))
}

/** Inset grouped card with hairline separators between rows. Null rows are skipped. */
@Composable
fun CupertinoRowsCard(
    header: String? = null,
    headerColor: Color = UI.colors.gray,
    separatorStartIndent: Dp = 54.dp,
    rows: List<(@Composable () -> Unit)?>,
) {
    val visibleRows = rows.filterNotNull()
    if (visibleRows.isEmpty()) return

    header?.let { CupertinoSectionHeader(text = it, color = headerColor) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .border(1.dp, cupertinoHairline(), RoundedCornerShape(20.dp))
    ) {
        visibleRows.forEachIndexed { index, row ->
            if (index > 0) {
                CupertinoRowSeparator(startIndent = separatorStartIndent)
            }
            row()
        }
    }
}

@Composable
fun CupertinoRowSeparator(
    startIndent: Dp = 54.dp,
) {
    Box(
        modifier = Modifier
            .padding(start = startIndent)
            .fillMaxWidth()
            .height(1.dp)
            .background(UI.colors.pureInverse.copy(alpha = 0.06f))
    )
}

/**
 * Settings-style navigation row. Set [external] = true for rows that leave the app
 * (shows the up-right arrow). Pass [trailing] to fully replace the trailing icon
 * (e.g. an avatar or a value chip).
 */
@Composable
fun CupertinoNavRow(
    @DrawableRes icon: Int?,
    title: String,
    description: String? = null,
    value: String? = null,
    tint: Color = UI.colors.pureInverse,
    external: Boolean = false,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(14.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = ivyInter(17.sp, FontWeight.Medium),
                color = tint
            )

            if (!description.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))

                Text(
                    text = description,
                    style = ivyInter(13.sp),
                    color = UI.colors.gray
                )
            }
        }

        if (!value.isNullOrEmpty()) {
            Spacer(Modifier.width(8.dp))

            Text(
                text = value,
                style = ivyInter(16.sp),
                color = UI.colors.gray,
                textAlign = TextAlign.End
            )
        }

        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        } else {
            Spacer(Modifier.width(10.dp))

            Icon(
                painter = painterResource(
                    if (external) {
                        R.drawable.ic_arrow_up_right
                    } else {
                        R.drawable.ic_arrow_right
                    }
                ),
                contentDescription = if (external) {
                    stringResource(R.string.opens_externally)
                } else {
                    null
                },
                tint = UI.colors.gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CupertinoSwitchRow(
    @DrawableRes icon: Int?,
    title: String,
    description: String? = null,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = UI.colors.pureInverse,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(14.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = ivyInter(17.sp, FontWeight.Medium),
                color = UI.colors.pureInverse
            )

            if (!description.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))

                Text(
                    text = description,
                    style = ivyInter(13.sp),
                    color = UI.colors.gray
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        IvySwitch(enabled = checked) {
            onToggle(it)
        }
    }
}
