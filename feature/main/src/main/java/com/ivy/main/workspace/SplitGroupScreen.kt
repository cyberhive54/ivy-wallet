package com.ivy.main.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.ivy.design.l0_system.UI
import com.ivy.navigation.SplitExpenseDetailScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DAY_FORMAT = DateTimeFormatter.ofPattern("d MMM")

/** History window (P3.S2.M4). */
private enum class HistoryRange { ALL, MONTH, THREE_MONTHS }

/**
 * Group detail — redesigned (DESIGN.md). One calm surface, one authoritative
 * balance band, then the record. Hairline surfaces, critically damped motion,
 * semantic color, and every action one tap away.
 */
@Composable
fun BoxWithConstraintsScope.SplitGroupScreen(screen: com.ivy.navigation.SplitGroupScreen) {
    val nav = navigation()
    val viewModel: SplitGroupViewModel = screenScopedViewModel()
    val state by viewModel.state.collectAsState()

    var showExpenseSheet by remember { mutableStateOf(false) }
    var showMembersSheet by remember { mutableStateOf(false) }
    var showGroupEdit by remember { mutableStateOf(false) }
    var showPlanSheet by remember { mutableStateOf(false) }
    var range by remember { mutableStateOf(HistoryRange.ALL) }
    var showSettled by remember { mutableStateOf(false) }

    LaunchedEffect(screen.groupId) { viewModel.load(screen.groupId) }

    val currency = state.group?.currency ?: "INR"
    val filteredExpenses = state.expenses.filter { expense ->
        (showSettled || !expense.settled) && when (range) {
            HistoryRange.ALL -> true
            HistoryRange.MONTH -> {
                val d = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
                d >= LocalDate.now().withDayOfMonth(1)
            }
            HistoryRange.THREE_MONTHS -> {
                val d = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
                d >= LocalDate.now().minusMonths(3)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(UI.colors.pure)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable { nav.back() },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_back), null, tint = UI.colors.pureInverse)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    state.group?.name ?: stringResource(R.string.splitly),
                    style = ivyInter(24.sp, FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
                    color = UI.colors.pureInverse,
                    maxLines = 1,
                )
                Text(
                    "$currency · ${state.members.count { !it.archived }} ${stringResource(R.string.splitly_members)}",
                    style = ivyInter(12.sp),
                    color = UI.colors.gray,
                )
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable { showGroupEdit = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_settings), null, tint = UI.colors.pureInverse)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Single authoritative balance band
        ExpandBalanceBand(
            receiveLabel = stringResource(R.string.splitly_they_owe),
            receiveAmount = state.balances.values.filter { it > 0 }.sum(),
            payLabel = stringResource(R.string.splitly_you_owe),
            payAmount = kotlin.math.abs(state.balances.values.filter { it < 0 }.sum()),
            currency = currency,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(14.dp))

        // Actions
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpandPillAction(stringResource(R.string.split_add_expense), Modifier.weight(1f)) {
                showExpenseSheet = true
            }
            ExpandPillAction(stringResource(R.string.split_members), Modifier.weight(1f), filled = false) {
                showMembersSheet = true
            }
            ExpandPillAction(stringResource(R.string.split_simplify), Modifier.weight(1f), filled = false) {
                viewModel.buildPlan()
                showPlanSheet = true
            }
        }

        // Undo affordance (P3.S3.M2) — appears after applying a settle-up plan
        AnimatedVisibility(
            visible = state.lastSettlement != null,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        ) {
            state.lastSettlement?.let { undo ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ExpandDesign.Accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.split_settled_undo_hint),
                        style = ivyInter(13.sp, FontWeight.Medium),
                        color = UI.colors.pureInverse,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(R.string.split_undo),
                        style = ivyInter(13.sp, FontWeight.Bold),
                        color = ExpandDesign.AccentSoft,
                        modifier = Modifier.clickable { viewModel.undoSettlement() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "✕",
                        style = ivyInter(13.sp, FontWeight.Bold),
                        color = UI.colors.gray,
                        modifier = Modifier.clickable { viewModel.dismissUndo() },
                    )
                }
            }
        }

        // Balances
        val activeBalances = state.members.filter { !it.archived && (state.balances[it.id] ?: 0.0) != 0.0 }
        if (activeBalances.isNotEmpty()) {
            ExpandSectionTitle(stringResource(R.string.split_balances))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(activeBalances) { member ->
                    SplitBalanceRow(
                        name = member.name,
                        balance = state.balances[member.id] ?: 0.0,
                        isYou = member.isYou,
                        currency = currency,
                    )
                }
            }
        } else if (filteredExpenses.isEmpty() && !state.loading) {
            ExpandEmptyState(
                glyph = "+",
                title = stringResource(R.string.split_no_expenses),
                subtitle = stringResource(R.string.split_empty_subtitle),
                actionLabel = stringResource(R.string.split_add_expense),
                onAction = { showExpenseSheet = true },
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        // History filter row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HistoryRange.entries.forEach { option ->
                val label = when (option) {
                    HistoryRange.ALL -> stringResource(R.string.ledger_filter_all)
                    HistoryRange.MONTH -> stringResource(R.string.history_month)
                    HistoryRange.THREE_MONTHS -> stringResource(R.string.ledger_range_three_months)
                }
                Text(
                    label,
                    style = ivyInter(12.sp, FontWeight.SemiBold),
                    color = if (range == option) Color.White else UI.colors.gray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (range == option) ExpandDesign.Accent else UI.colors.pureInverse.copy(alpha = 0.05f),
                            RoundedCornerShape(50)
                        )
                        .clickable { range = option }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(if (showSettled) R.string.history_hide_settled else R.string.history_show_settled),
                style = ivyInter(11.sp, FontWeight.Medium),
                color = ExpandDesign.Accent,
                modifier = Modifier.clickable { showSettled = !showSettled },
            )
        }

        // History list (last section, bounded height)
        LazyColumn(
            modifier = Modifier.height(220.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            items(filteredExpenses) { expense ->
                SplitExpenseRow(
                    expense = expense,
                    currency = currency,
                    onClick = {
                        nav.navigateTo(
                            SplitExpenseDetailScreen(expenseId = expense.id, groupId = screen.groupId)
                        )
                    },
                )
            }
        }
    }

    if (showExpenseSheet) {
        SplitExpenseSheet(
            members = state.members.filter { !it.archived },
            currency = currency,
            categories = state.categories,
            onCreateCategory = { viewModel.createCategory(it) },
            onSave = { note, amount, date, categoryId, attachmentUrl, payers, shares ->
                viewModel.saveExpense(note, amount, currency, date, categoryId, attachmentUrl, payers, shares)
                showExpenseSheet = false
            },
            onDismiss = { showExpenseSheet = false },
        )
    }

    if (showMembersSheet) {
        SplitMembersSheet(
            state = state,
            onAdd = { name, phone -> viewModel.addMember(name, phone) },
            onReorder = { id, delta -> viewModel.reorderMember(id, delta) },
            onArchive = { id -> viewModel.setMemberArchived(id, true) },
            onLeave = { viewModel.leaveGroup() },
            onDismiss = { showMembersSheet = false },
        )
    }

    if (showGroupEdit) {
        SplitGroupEditSheet(
            group = state.group,
            onSave = { name, type, cur -> viewModel.updateGroup(name, type, cur); showGroupEdit = false },
            onArchive = { viewModel.archiveGroup(); showGroupEdit = false; nav.back() },
            onDismiss = { showGroupEdit = false },
        )
    }

    if (showPlanSheet) {
        SplitPlanSheet(
            state = state,
            onApply = { viewModel.applyPlan(); showPlanSheet = false },
            onDismiss = { viewModel.dismissPlan(); showPlanSheet = false },
        )
    }
}

@Composable
private fun SplitBalanceRow(name: String, balance: Double, isYou: Boolean, currency: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape)
                .background(ExpandDesign.Accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1).uppercase(), style = ivyInter(14.sp, FontWeight.Bold), color = ExpandDesign.Accent)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            name + if (isYou) " (You)" else "",
            style = ivyInter(15.sp, FontWeight.Medium),
            color = UI.colors.pureInverse,
            modifier = Modifier.weight(1f),
        )
        Text(
            "${if (balance >= 0) "+" else ""}${"%,.2f".format(balance)} $currency",
            style = ivyInter(14.sp, FontWeight.Bold),
            color = if (balance > 0) ExpandDesign.Receive else ExpandDesign.Pay,
        )
    }
}

@Composable
private fun SplitExpenseRow(expense: SplitExpense, currency: String, onClick: () -> Unit) {
    val dateText = expense.date.atZone(ZoneId.systemDefault()).format(DAY_FORMAT)
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                expense.note?.takeIf { it.isNotBlank() } ?: stringResource(R.string.splitly_expense),
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse,
                maxLines = 1,
            )
            Text(
                "$dateText · ${expense.payers.size} payer(s)" + if (expense.settled) " · settled" else "",
                style = ivyInter(11.sp),
                color = UI.colors.gray,
            )
        }
        Text(
            "$currency ${"%,.2f".format(expense.amount)}",
            style = ivyInter(15.sp, FontWeight.ExtraBold),
            color = if (expense.settled) UI.colors.gray else UI.colors.pureInverse,
        )
    }
}
