package com.ivy.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.ivy.legacy.cupertino.CupertinoAlertModal
import com.ivy.legacy.cupertino.CupertinoProgressModal
import com.ivy.legacy.cupertino.CupertinoSheetModal
import com.ivy.legacy.cupertino.CupertinoSheetRow
import com.ivy.legacy.cupertino.CupertinoSheetSeparator
import com.ivy.legacy.cupertino.cupertinoHairline

// -----------------------------------------------------------------------------------------
// Cupertino modal primitives used by Settings.
//
// Thin delegations to the shared kit (temp/legacy-code :: com.ivy.legacy.cupertino).
// All primitives are Dialog-based (own window layer) so they render ABOVE the dock,
// unlike the legacy Box-overlay modals. Back press and outside tap dismiss.
// -----------------------------------------------------------------------------------------

internal fun hairline() = cupertinoHairline()

/**
 * iOS-style bottom sheet. Rounded 20dp top corners, grabber, optional large title,
 * scrollable content slot. Max height 88% of the screen.
 */
@Composable
internal fun CupertinoSheet(
    visible: Boolean,
    title: String?,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    CupertinoSheetModal(
        visible = visible,
        title = title,
        onDismiss = onDismiss,
        content = content
    )
}

/**
 * iOS-style centered alert. Rounded 16dp card, centered title & message,
 * horizontal action buttons. Destructive action is tinted red.
 */
@Composable
internal fun CupertinoAlert(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    CupertinoAlertModal(
        visible = visible,
        title = title,
        message = message,
        confirmLabel = confirmLabel,
        dismissLabel = dismissLabel,
        destructive = destructive,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Compact centered progress card ("Exporting data…").
 */
@Composable
internal fun CupertinoProgressCard(
    visible: Boolean,
    title: String,
    message: String,
) {
    CupertinoProgressModal(
        visible = visible,
        title = title,
        message = message
    )
}

/**
 * A row used inside sheets: title (+ optional description) + trailing slot.
 */
@Composable
internal fun SheetRow(
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {},
) {
    CupertinoSheetRow(
        title = title,
        description = description,
        onClick = onClick,
        trailing = trailing
    )
}

@Composable
internal fun SheetSeparator() {
    CupertinoSheetSeparator()
}
