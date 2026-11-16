package com.ivy.main.workspace

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.ivy.design.l0_system.UI
import com.ivy.domain.split.ShareMode
import com.ivy.navigation.SplitExpenseDetailScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DAY_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")

/**
 * Group detail (P3.S2 + P3.S3): members, expenses, per-member balances,
 * simplify/settle plan, comments and group/member management.
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

    LaunchedEffect(screen.groupId) { viewModel.load(screen.groupId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(UI.colors.pure)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .clickable { nav.back() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = null,
                    tint = UI.colors.pureInverse
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.group?.name ?: stringResource(R.string.splitly),
                    style = ivyInter(20.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
                    color = UI.colors.pureInverse,
                    maxLines = 1
                )
                Text(
                    text = "${state.group?.currency ?: ""} · ${state.members.count { !it.archived }} ${stringResource(R.string.splitly_members)}",
                    style = ivyInter(11.sp),
                    color = UI.colors.gray
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.edit_group),
                tint = UI.colors.pureInverse,
                modifier = Modifier
                    .width(40.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .clickable { showGroupEdit = true }
                    .padding(8.dp)
            )
        }

        // Balance banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BalanceCell(
                label = stringResource(R.string.splitly_they_owe),
                amount = state.balances.values.filter { it > 0 }.sum(),
                accent = Color(0xFF30D158),
            )
            BalanceCell(
                label = stringResource(R.string.splitly_you_owe),
                amount = kotlin.math.abs(state.balances.values.filter { it < 0 }.sum()),
                accent = Color(0xFFFF9F0A),
            )
        }

        // Action buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionChip(label = stringResource(R.string.split_add_expense), accent = Color(0xFF5E5CE6)) {
                showExpenseSheet = true
            }
            ActionChip(label = stringResource(R.string.split_members), accent = Color(0xFF5E5CE6)) {
                showMembersSheet = true
            }
            ActionChip(label = stringResource(R.string.split_simplify), accent = Color(0xFF5E5CE6)) {
                viewModel.buildPlan()
                showPlanSheet = true
            }
        }

        // Per-person ledger (P3.S3.M1)
        if (state.balances.isNotEmpty()) {
            Text(
                text = stringResource(R.string.split_balances),
                style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
                color = UI.colors.gray,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
            ) {
                items(state.members.filter { !it.archived }) { member ->
                    val balance = state.balances[member.id] ?: 0.0
                    SplitBalanceRow(
                        name = member.name,
                        balance = balance,
                        isYou = member.isYou,
                        currency = state.group?.currency ?: "INR",
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
                items(state.expenses) { expense ->
                    SplitExpenseRow(
                        expense = expense,
                        currency = state.group?.currency ?: "INR",
                        onClick = {
                            nav.navigateTo(
                                SplitExpenseDetailScreen(
                                    expenseId = expense.id,
                                    groupId = screen.groupId,
                                )
                            )
                        }
                    )
                }
            }
        } else if (!state.loading) {
            Text(
                text = stringResource(R.string.split_no_expenses),
                style = ivyInter(15.sp),
                color = UI.colors.gray,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
            )
        }
    }

    if (showExpenseSheet) {
        SplitExpenseSheet(
            members = state.members.filter { !it.archived },
            currency = state.group?.currency ?: "INR",
            categoryIds = emptyList(),
            onSave = { note, amount, date, categoryId, attachmentUrl, payers, shares ->
                viewModel.saveExpense(
                    note = note,
                    amount = amount,
                    currency = state.group?.currency ?: "INR",
                    date = date,
                    categoryId = categoryId,
                    attachmentUrl = attachmentUrl,
                    payers = payers,
                    shares = shares,
                )
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
            onSave = { name, type, currency ->
                viewModel.updateGroup(name, type, currency)
                showGroupEdit = false
            },
            onArchive = {
                viewModel.archiveGroup()
                showGroupEdit = false
                nav.back()
            },
            onDismiss = { showGroupEdit = false },
        )
    }

    if (showPlanSheet) {
        SplitPlanSheet(
            state = state,
            onApply = {
                viewModel.applyPlan()
                showPlanSheet = false
            },
            onDismiss = {
                viewModel.dismissPlan()
                showPlanSheet = false
            },
        )
    }
}

@Composable
private fun BalanceCell(label: String, amount: Double, accent: Color) {
    Column {
        Text(text = label, style = ivyInter(12.sp, FontWeight.SemiBold), color = UI.colors.gray)
        Spacer(Modifier.height(3.dp))
        Text(
            text = "%,.2f".format(amount),
            style = ivyInter(20.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
            color = accent,
        )
    }
}

@Composable
private fun ActionChip(label: String, accent: Color, onClick: () -> Unit) {
    Text(
        text = label,
        style = ivyInter(12.sp, FontWeight.SemiBold),
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(accent, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun SplitBalanceRow(
    name: String,
    balance: Double,
    isYou: Boolean,
    currency: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name + if (isYou) " (You)" else "",
            style = ivyInter(15.sp, FontWeight.Medium),
            color = UI.colors.pureInverse,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${if (balance >= 0) "+" else ""}%,.2f".format(balance) + " $currency",
            style = ivyInter(14.sp, FontWeight.Bold),
            color = if (balance > 0) Color(0xFF30D158)
            else if (balance < 0) Color(0xFFFF9F0A) else UI.colors.gray
        )
    }
}

@Composable
private fun SplitExpenseRow(
    expense: SplitExpense,
    currency: String,
    onClick: () -> Unit,
) {
    val dateText = expense.date.atZone(ZoneId.systemDefault()).format(DAY_FORMAT)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.note?.takeIf { it.isNotBlank() } ?: stringResource(R.string.splitly_expense),
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse,
                maxLines = 1
            )
            Text(
                text = dateText + " · " + expense.payers.size + " payer(s)",
                style = ivyInter(11.sp),
                color = UI.colors.gray
            )
        }
        Text(
            text = "%,.2f".format(expense.amount) + " $currency",
            style = ivyInter(15.sp, FontWeight.ExtraBold),
            color = UI.colors.pureInverse
        )
    }
}
