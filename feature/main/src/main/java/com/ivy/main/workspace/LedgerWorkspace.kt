package com.ivy.main.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.ivy.design.l0_system.UI
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.data.model.MainTab
import com.ivy.navigation.LoanDetailsScreen
import com.ivy.navigation.LoansScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter

/**
 * Ledger workspace — in-dock landing (dock slot "Home"). Shows outstanding totals,
 * then the biggest people you owe / are owed. Footer jumps to the Contacts tab.
 */
@Composable
fun BoxWithConstraintsScope.LedgerLanding() {
    val nav = navigation()
    val ivyContext = ivyWalletCtx()
    val viewModel: LedgerWorkspaceViewModel = screenScopedViewModel()
    val state by viewModel.state.collectAsState()
    var segment by remember { mutableStateOf(LedgerSegment.GET) }
    var filter by remember { mutableStateOf(LedgerFilter.ALL) }
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(UI.colors.pure)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.ledger),
            style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(10.dp))

        // Totals banner — What's due to you vs What you owe
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            TotalCell(
                label = stringResource(R.string.ledger_you_due),
                amount = state.totalLent,
                accent = Color(0xFF30D158),
            )
            TotalCell(
                label = stringResource(R.string.ledger_you_owe),
                amount = state.totalBorrowed,
                accent = Color(0xFFFF9F0A),
            )
        }

        Spacer(Modifier.height(14.dp))

        // Search by name
        LedgerSearchField(
            query = query,
            onQueryChange = { query = it }
        )
        Spacer(Modifier.height(12.dp))

        // Taken / Given split
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            LedgerSegmentButton(
                label = stringResource(R.string.ledger_get),
                amount = state.totalLent,
                selected = segment == LedgerSegment.GET,
                onClick = { segment = LedgerSegment.GET }
            )
            LedgerSegmentButton(
                label = stringResource(R.string.ledger_owe),
                amount = state.totalBorrowed,
                selected = segment == LedgerSegment.OWE,
                onClick = { segment = LedgerSegment.OWE }
            )
        }
        Spacer(Modifier.height(12.dp))

        // Filters: everyone / due soon / overdue
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            LedgerFilterChip(
                label = stringResource(R.string.ledger_filter_all),
                selected = filter == LedgerFilter.ALL,
                onClick = { filter = LedgerFilter.ALL }
            )
            LedgerFilterChip(
                label = stringResource(R.string.ledger_filter_due),
                selected = filter == LedgerFilter.DUE,
                onClick = { filter = LedgerFilter.DUE }
            )
            LedgerFilterChip(
                label = stringResource(R.string.ledger_filter_overdue_only, state.people.count { it.overdue }),
                selected = filter == LedgerFilter.OVERDUE,
                onClick = { filter = LedgerFilter.OVERDUE },
                accent = Color(0xFFFF6B5B)
            )
        }
        Spacer(Modifier.height(12.dp))

        val visiblePeople = state.people
            .filter { person ->
                when (segment) {
                    LedgerSegment.GET -> !person.youOwe
                    LedgerSegment.OWE -> person.youOwe
                }
            }
            .filter { person ->
                when (filter) {
                    LedgerFilter.ALL -> true
                    LedgerFilter.DUE -> person.overdue || person.dueSoonCount > 0
                    LedgerFilter.OVERDUE -> person.overdue
                }
            }
            .filter { person ->
                query.isBlank() || person.name.contains(query, ignoreCase = true)
            }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            if (state.isEmpty) {
                item { EmptyLedgerState(onCreateClick = { nav.navigateTo(LoansScreen) }) }
            } else if (visiblePeople.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ledger_no_overdue),
                        style = ivyInter(15.sp),
                        color = UI.colors.gray,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                    )
                }
            } else {
                items(visiblePeople.take(8)) { person ->
                    LedgerPersonRow(
                        person = person,
                        onClick = { nav.navigateTo(LoanDetailsScreen(loanId = person.id)) }
                    )
                }
                if (visiblePeople.size > 8) {
                    item {
                        Text(
                            text = stringResource(R.string.ledger_show_all, visiblePeople.size),
                            style = ivyInter(15.sp, FontWeight.SemiBold),
                            color = Color(0xFF5E5CE6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ivyContext.selectMainTab(MainTab.LEDGER_CONTACTS) }
                                .padding(horizontal = 24.dp, vertical = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ledger "Contacts" dock tab — every person ever recorded in the loan ledger,
 * settled or not, with client-side search. Dock-owned (no inner chrome).
 */
@Composable
fun BoxWithConstraintsScope.LedgerContacts() {
    val nav = navigation()
    val viewModel: LedgerWorkspaceViewModel = screenScopedViewModel()
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }

    val filtered = remember(state.contacts, query) {
        val q = query.trim()
        if (q.isBlank()) {
            state.contacts
        } else {
            state.contacts.filter { it.name.contains(q, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(UI.colors.pure)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.contacts),
            style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.ledger_search_hint),
                        style = ivyInter(16.sp),
                        color = UI.colors.gray
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ledger_no_match),
                        style = ivyInter(15.sp),
                        color = UI.colors.gray,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                    )
                }
            } else {
                items(filtered) { person ->
                    LedgerPersonRow(
                        person = person,
                        onClick = { nav.navigateTo(LoanDetailsScreen(loanId = person.id)) }
                    )
                }
            }
        }
    }
}

/** Which side of the book is shown: money they owe you, or money you owe them. */
enum class LedgerSegment { GET, OWE }

/** Book filters. */
enum class LedgerFilter { ALL, DUE, OVERDUE }

@Composable
private fun LedgerSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.ledger_search_hint),
                    style = ivyInter(16.sp),
                    color = UI.colors.gray
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RowScope.LedgerSegmentButton(
    label: String,
    amount: Double,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    UI.colors.pureInverse.copy(alpha = 0.08f)
                } else {
                    androidx.compose.ui.graphics.Color.Transparent
                },
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = ivyInter(13.sp, FontWeight.SemiBold),
            color = if (selected) UI.colors.pureInverse else UI.colors.gray
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "₹%,.0f".format(amount),
            style = ivyInter(22.sp, FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
            color = if (selected) UI.colors.pureInverse else UI.colors.gray
        )
    }
}

@Composable
private fun LedgerFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accent: Color = Color(0xFF5E5CE6),
) {
    Text(
        text = label,
        style = ivyInter(13.sp, FontWeight.SemiBold),
        color = if (selected) Color.White else UI.colors.pureInverse,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (selected) accent else UI.colors.pureInverse.copy(alpha = 0.06f),
                RoundedCornerShape(percent = 50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun TotalCell(
    label: String,
    amount: Double,
    accent: Color,
) {
    Column {
        Text(
            text = label,
            style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.2.sp),
            color = UI.colors.gray
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "₹" + "%,.2f".format(amount),
            style = ivyInter(26.sp, FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
            color = accent
        )
    }
}

@Composable
private fun EmptyLedgerState(
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.ledger_empty_title),
            style = ivyInter(17.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.ledger_empty_subtitle),
            style = ivyInter(15.sp),
            color = UI.colors.gray
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF5E5CE6).copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                .clickable(onClick = onCreateClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.ledger_add_customer),
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = Color(0xFF5E5CE6)
            )
        }
    }
}

@Composable
private fun LedgerPersonRow(
    person: LedgerPerson,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar: first letter(s) of the name
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .width(44.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF5E5CE6).copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = person.name.trim().takeIf { it.isNotEmpty() }?.take(2)?.uppercase() ?: "?",
                style = ivyInter(16.sp, FontWeight.Bold),
                color = Color(0xFF5E5CE6)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                style = ivyInter(16.sp, FontWeight.SemiBold),
                color = if (person.settled) UI.colors.gray else UI.colors.pureInverse,
                maxLines = 1
            )
            if (person.overdue) {
                Text(
                    text = stringResource(R.string.ledger_overdue_days, person.overdueDays),
                    style = ivyInter(12.sp, FontWeight.Bold),
                    color = Color(0xFFFF6B5B)
                )
            }
        }
        Text(
            text = if (person.settled) {
                stringResource(R.string.ledger_settled)
            } else {
                "₹" + "%,.2f".format(person.outstanding)
            },
            style = ivyInter(15.sp, FontWeight.ExtraBold, letterSpacing = (-0.2).sp),
            color = if (person.settled) UI.colors.gray
            else if (person.youOwe) Color(0xFFFF9F0A) else Color(0xFF30D158)
        )
    }
}
