package com.ivy.main.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.model.Workspace
import com.ivy.design.l0_system.UI
import com.ivy.legacy.ivyWalletCtx
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter

/**
 * Top workspace switcher rail (Swiggy/Flipkart pattern per fork design).
 *
 * Shows one card per enabled workspace. Tap = workspace switch via
 * [com.ivy.legacy.IvyWalletCtx]; the dock content swaps in place (the dock is
 * reconfigured per workspace) — nothing is pushed onto the backstack.
 * Haptic feedback and a spring indicator are deferred to P4 polish; tapping
 * the already-active workspace is a no-op. Rendered only on workspace home tabs.
 */
@Composable
fun BoxWithConstraintsScope.WorkspaceRail(
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = SharedPrefs(context)
    val ivyContext = ivyWalletCtx()

    // Compose state reads — ledger/splitly toggles are observed here.
    val ledgerEnabled = ivyContext.ledgerWorkspaceEnabled
    val splitlyEnabled = ivyContext.splitlyWorkspaceEnabled

    val visible = buildList {
        add(Workspace.FINANCE)
        if (ledgerEnabled) add(Workspace.LEDGER)
        if (splitlyEnabled) add(Workspace.SPLITLY)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visible.forEach { workspace ->
            WorkspaceRailCard(
                modifier = Modifier.weight(1f),
                workspace = workspace,
                selected = ivyContext.activeWorkspace == workspace,
                onTap = {
                    // No-op when already on the active workspace — dock content
                    // swaps in place; nothing is pushed onto the backstack.
                    if (ivyContext.activeWorkspace != workspace) {
                        ivyContext.switchWorkspace(workspace, sharedPrefs)
                    }
                },
            )
        }
    }
}

@Composable
private fun WorkspaceRailCard(
    modifier: Modifier = Modifier,
    workspace: Workspace,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val background = if (selected) {
        Color(0xFF5E5CE6).copy(alpha = 0.14f)
    } else {
        Color.Transparent
    }
    val border = if (selected) Color(0xFF5E5CE6) else Color.Transparent

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background, RoundedCornerShape(16.dp))
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onTap)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconColor = if (selected) Color(0xFF5E5CE6) else UI.colors.pureInverse.copy(alpha = 0.45f)
        Icon(
            painter = painterResource(iconFor(workspace)),
            contentDescription = stringResource(labelFor(workspace)),
            tint = iconColor,
            modifier = Modifier.width(26.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(labelFor(workspace)),
            style = ivyInter(14.sp, FontWeight.SemiBold),
            color = if (selected) Color(0xFF5E5CE6) else UI.colors.pureInverse.copy(alpha = 0.75f)
        )
    }
}

private fun iconFor(workspace: Workspace): Int = when (workspace) {
    Workspace.FINANCE -> R.drawable.ic_home_filled
    Workspace.LEDGER -> R.drawable.home_more_menu_loans
    Workspace.SPLITLY -> R.drawable.ic_checklist_add
}

private fun labelFor(workspace: Workspace): Int = when (workspace) {
    Workspace.FINANCE -> R.string.finance
    Workspace.LEDGER -> R.string.ledger
    Workspace.SPLITLY -> R.string.splitly
}
