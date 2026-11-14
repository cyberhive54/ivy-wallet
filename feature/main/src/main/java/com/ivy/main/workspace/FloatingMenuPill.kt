package com.ivy.main.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.White
import com.ivy.navigation.BudgetScreen
import com.ivy.navigation.CategoriesScreen
import com.ivy.navigation.LoansScreen
import com.ivy.navigation.PlannedPaymentsScreen
import com.ivy.navigation.ReportScreen
import com.ivy.navigation.SearchScreen
import com.ivy.navigation.SettingsScreen
import com.ivy.navigation.SubscriptionsScreen
import com.ivy.navigation.TagsScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter

/**
 * Inline dock member — the Flipkart-style "sparkle" circle, rendered as part of the
 * dock bar itself (not a floating overlay). Pinned to the end of the dock row,
 * visually separated by a 1dp seam before it.
 */
@Composable
fun RowScope.FloatingMenuDockTab(
    expanded: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
        label = "menu_press_scale"
    )

    // Seam between the last tab and the gradient circle
    Box(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .width(1.dp)
            .height(28.dp)
            .background(UI.colors.gray.copy(alpha = 0.30f), RoundedCornerShape(percent = 50))
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(46.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8E63F5),
                        Color(0xFF5E5CE6),
                        Color(0xFF7C3AED),
                    )
                ),
                RoundedCornerShape(percent = 50)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    Color.White.copy(alpha = 0.85f)
                } else {
                    Color.White.copy(alpha = 0.28f)
                },
                shape = RoundedCornerShape(percent = 50)
            )
            .clickable(onClick = onClick, role = Role.Button)
            .semantics {
                contentDescription = "Quick links"
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_menu_filled),
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Overlay for the inline [FloatingMenuDockTab]. Scrim + anchored panel +
 * About dialog. Call from inside a Box overlay scope.
 */
@Composable
fun BoxWithConstraintsScope.FloatingMenuOverlay(
    expanded: Boolean,
    onDismiss: () -> Unit,
    bottomInset: Dp,
) {
    val nav = navigation()
    val uriHandler = LocalUriHandler.current
    var aboutVisible by remember { mutableStateOf(false) }

    if (!expanded) {
        aboutVisible = false
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(180f)
    ) {
        // Scrim — tap anywhere behind the panel to dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
        )

        // Panel — anchored end above the dock
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 18.dp, bottom = bottomInset),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(spring()) + expandVertically(spring()),
                exit = fadeOut(spring()) + shrinkVertically(spring())
            ) {
                Panel(
                    onQuickLink = { target ->
                        onDismiss()
                        nav.navigateTo(target)
                    },
                    onLookAndFeel = {
                        onDismiss()
                        nav.navigateTo(SettingsScreen)
                    },
                    onWorkspaces = {
                        onDismiss()
                        nav.navigateTo(SettingsScreen)
                    },
                    onExportImport = {
                        onDismiss()
                        nav.navigateTo(SettingsScreen)
                    },
                    onReportBug = {
                        onDismiss()
                        uriHandler.openUri(REPORT_ISSUES_URL)
                    },
                    onAbout = {
                        aboutVisible = true
                    },
                )
            }
        }
    }

    if (aboutVisible) {
        AboutDialog(
            onDismiss = { aboutVisible = false },
            onOpenRepo = {
                uriHandler.openUri(REPO_URL)
                aboutVisible = false
            }
        )
    }
}

private val REPORT_ISSUES_URL = "https://github.com/cyberhive54/ivy-wallet/issues"
private val REPO_URL = "https://github.com/cyberhive54/ivy-wallet"

@Composable
private fun Panel(
    onQuickLink: (com.ivy.navigation.Screen) -> Unit,
    onLookAndFeel: () -> Unit,
    onWorkspaces: () -> Unit,
    onExportImport: () -> Unit,
    onReportBug: () -> Unit,
    onAbout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(UI.colors.pureInverse, RoundedCornerShape(22.dp))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp)
    ) {
        PanelSectionLabel(stringResource(R.string.pill_quick_links))
        PanelItem(R.drawable.ic_vue_money_card, stringResource(R.string.subscriptions_title)) {
            onQuickLink(SubscriptionsScreen)
        }
        PanelItem(R.drawable.ic_search, stringResource(R.string.search_transactions)) {
            onQuickLink(SearchScreen)
        }
        PanelItem(R.drawable.home_more_menu_categories, stringResource(R.string.categories)) {
            onQuickLink(CategoriesScreen)
        }
        PanelItem(R.drawable.ic_hashtag, stringResource(R.string.tags)) {
            onQuickLink(TagsScreen)
        }
        PanelItem(R.drawable.home_more_menu_budgets, stringResource(R.string.budgets)) {
            onQuickLink(BudgetScreen)
        }
        PanelItem(R.drawable.home_more_menu_reports, stringResource(R.string.reports)) {
            onQuickLink(ReportScreen)
        }
        PanelItem(R.drawable.home_more_menu_planned_payments, stringResource(R.string.planned_payments_short)) {
            onQuickLink(PlannedPaymentsScreen)
        }
        PanelItem(R.drawable.home_more_menu_loans, stringResource(R.string.loans)) {
            onQuickLink(LoansScreen)
        }

        PanelSectionLabel(stringResource(R.string.pill_utilities))
        PanelItem(R.drawable.ic_vue_money_receipt_items, stringResource(R.string.pill_look)) {
            onLookAndFeel()
        }
        PanelItem(R.drawable.ic_checklist_add, stringResource(R.string.pill_workspaces)) {
            onWorkspaces()
        }
        PanelItem(R.drawable.ic_export_csv, stringResource(R.string.pill_export_import)) {
            onExportImport()
        }
        PanelItem(R.drawable.ic_email, stringResource(R.string.pill_report_bug)) {
            onReportBug()
        }
        PanelItem(R.drawable.ic_menu, stringResource(R.string.pill_about)) {
            onAbout()
        }
    }
}

@Composable
private fun PanelSectionLabel(
    text: String,
) {
    Text(
        text = text,
        style = ivyInter(11.sp, FontWeight.Bold, letterSpacing = 1.2.sp),
        color = UI.colors.pure.copy(alpha = 0.55f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun PanelItem(
    icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = UI.colors.pure,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = ivyInter(15.sp, FontWeight.SemiBold),
            color = UI.colors.pure
        )
    }
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit,
    onOpenRepo: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(UI.colors.pureInverse, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home_filled),
                contentDescription = null,
                tint = UI.colors.pure,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.about_app_name),
                style = ivyInter(22.sp, FontWeight.ExtraBold),
                color = UI.colors.pure
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.about_version),
                style = ivyInter(13.sp),
                color = UI.colors.pure.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.about_license),
                style = ivyInter(13.sp),
                color = UI.colors.pure.copy(alpha = 0.8f)
            )
            Text(
                text = stringResource(R.string.about_repo),
                style = ivyInter(13.sp, FontWeight.SemiBold),
                color = UI.colors.pure.copy(alpha = 0.9f),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable(onClick = onOpenRepo)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.about_done),
                style = ivyInter(16.sp, FontWeight.Bold),
                color = Color(0xFF5E5CE6),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.pure.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 28.dp, vertical = 10.dp)
            )
        }
    }
}
