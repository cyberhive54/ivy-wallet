package com.ivy.legacy.cupertino

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.ivy.base.legacy.Look
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter

// -----------------------------------------------------------------------------------------
// Cupertino dialog primitives shared by Settings child pages.
//
// Dialog-based (own window layer) so they render ABOVE the floating dock.
// In the GLASS look, the dialog window blurs the app content behind it (API 31+)
// and the surface turns translucent - a frosted pane.
// -----------------------------------------------------------------------------------------

private const val GLASS_SURFACE_ALPHA = 0.80f
private const val GLASS_DIM_AMOUNT = 0.16f

/** Translucent pane surface when the GLASS look is active, solid otherwise. */
@Composable
internal fun dialogSurfaceColor(): Color {
    return if (UI.colors.look == Look.GLASS) {
        UI.colors.pure.copy(alpha = GLASS_SURFACE_ALPHA)
    } else {
        UI.colors.pure
    }
}

/**
 * Applies real window blur + reduced dim to the hosting dialog window (API 31+).
 * No-op on older APIs and outside dialogs - the translucent surface still reads glassy.
 */
@Composable
internal fun Modifier.cupertinoWindowGlass(enabled: Boolean): Modifier {
    if (!enabled) return this

    val view = LocalView.current
    val blurRadiusPx = with(LocalDensity.current) { 28.dp.toPx().toInt() }

    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.setBackgroundBlurRadius(blurRadiusPx)
            }
            window.setDimAmount(GLASS_DIM_AMOUNT)
        }
    }

    return this
}

/** Glass hairline: brighter top edge fading down, mimicking a lit pane border. */
@Composable
internal fun glassHairlineBrush(default: Color): Brush {
    return if (UI.colors.look == Look.GLASS) {
        Brush.verticalGradient(
            listOf(
                UI.colors.pureInverse.copy(alpha = 0.20f),
                UI.colors.pureInverse.copy(alpha = 0.04f)
            )
        )
    } else {
        SolidColor(default)
    }
}

/**
 * iOS-style bottom sheet. Rounded 20dp top corners, grabber, optional large title,
 * content slot. Height adapts to content, capped at 600dp.
 * Back press / outside tap dismiss.
 */
@Composable
fun CupertinoSheetModal(
    visible: Boolean,
    title: String?,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 600.dp)
                .cupertinoWindowGlass(enabled = UI.colors.look == Look.GLASS)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(dialogSurfaceColor())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 36.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(UI.colors.pureInverse.copy(alpha = 0.15f))
            )

            if (!title.isNullOrEmpty()) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    style = ivyInter(20.sp, FontWeight.Bold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

/**
 * iOS-style centered alert with a confirm/dismiss button pair.
 */
@Composable
fun CupertinoAlertModal(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .cupertinoWindowGlass(enabled = UI.colors.look == Look.GLASS)
                .clip(RoundedCornerShape(16.dp))
                .background(dialogSurfaceColor())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = ivyInter(17.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message,
                style = ivyInter(13.sp),
                color = UI.colors.gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CupertinoAlertButton(
                    label = dismissLabel,
                    weight = 1f,
                    filled = false,
                    onClick = onDismiss
                )

                CupertinoAlertButton(
                    label = confirmLabel,
                    weight = 1f,
                    filled = true,
                    destructive = destructive,
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
fun RowScope.CupertinoAlertButton(
    label: String,
    weight: Float,
    filled: Boolean,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val textColor = when {
        // Filled destructive (red) button: white label in both light and dark modes.
        // Never use `UI.colors.red` for the label here - red on red renders invisible.
        filled && destructive -> Color.White
        destructive -> UI.colors.red
        filled -> UI.colors.pure
        else -> UI.colors.pureInverse
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    filled && destructive -> UI.colors.red
                    filled -> UI.colors.pureInverse
                    else -> UI.colors.medium
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ivyInter(15.sp, FontWeight.SemiBold),
            color = textColor,
            maxLines = 1
        )
    }
}

/**
 * Compact non-dismissible progress card.
 */
@Composable
fun CupertinoProgressModal(
    visible: Boolean,
    title: String,
    message: String,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = { /* not dismissible while working */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 240.dp, max = 280.dp)
                .fillMaxWidth()
                .cupertinoWindowGlass(enabled = UI.colors.look == Look.GLASS)
                .clip(RoundedCornerShape(20.dp))
                .background(dialogSurfaceColor())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = UI.colors.pureInverse,
                strokeWidth = 3.dp,
                trackColor = UI.colors.medium
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = message,
                style = ivyInter(12.sp),
                color = UI.colors.gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** A 48dp rounded input container used inside sheets. */
@Composable
fun CupertinoInputContainer(
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(UI.colors.medium)
            .border(1.dp, cupertinoHairline(), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Filled 48dp primary action button used inside sheets and pages. */
@Composable
fun CupertinoFilledButton(
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) UI.colors.pureInverse else UI.colors.medium)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ivyInter(15.sp, FontWeight.SemiBold),
            color = if (enabled) UI.colors.pure else UI.colors.gray
        )
    }
}

/** Row used inside sheets. */
@Composable
fun CupertinoSheetRow(
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = ivyInter(16.sp, FontWeight.Medium),
                color = UI.colors.pureInverse
            )

            if (!description.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))

                Text(
                    text = description,
                    style = ivyInter(12.sp),
                    color = UI.colors.gray
                )
            }
        }

        Spacer(Modifier.widthIn(min = 8.dp))

        trailing()
    }
}

/** Thin full-width separator for sheet content. */
@Composable
fun CupertinoSheetSeparator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(cupertinoHairline())
    )
}
