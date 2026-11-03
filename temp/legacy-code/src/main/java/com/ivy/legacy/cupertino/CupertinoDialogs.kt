package com.ivy.legacy.cupertino

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter

// -----------------------------------------------------------------------------------------
// Cupertino dialog primitives shared by Settings child pages.
//
// Dialog-based (own window layer) so they render ABOVE the floating dock.
// -----------------------------------------------------------------------------------------

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
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(UI.colors.pure)
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
                .clip(RoundedCornerShape(16.dp))
                .background(UI.colors.pure)
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
                .clip(RoundedCornerShape(20.dp))
                .background(UI.colors.pure)
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
