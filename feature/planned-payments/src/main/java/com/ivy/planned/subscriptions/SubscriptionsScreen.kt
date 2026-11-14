package com.ivy.planned.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
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
import com.ivy.navigation.SubscriptionEditScreen
import com.ivy.navigation.SubscriptionsScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.util.Locale

private val MONTH_FORMAT = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

/** Keys stored in PlannedPaymentRuleEntity.icon → glyph. */
private fun currencySymbol(code: String): String = when (code.uppercase()) {
    "INR" -> "₹"
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    else -> "$code "
}

@Composable
fun BoxWithConstraintsScope.SubscriptionsScreen(screen: SubscriptionsScreen) {
    val nav = navigation()
    val viewModel: SubscriptionsViewModel = screenScopedViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    val symbol = currencySymbol(state.currency)
    var hikeDismissed by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UI.colors.pure)
            .statusBarsPadding()
    ) {
        // Top bar — back + title + add
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { nav.back() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = null,
                    tint = UI.colors.pureInverse
                )
            }
            Text(
                text = stringResource(R.string.subscriptions_title),
                style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
                color = UI.colors.pureInverse,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF5E5CE6).copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                    .clickable {
                        nav.navigateTo(
                            SubscriptionEditScreen(
                                plannedPaymentRuleId = null
                            )
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+ " + stringResource(R.string.subscriptions_add),
                    style = ivyInter(14.sp, FontWeight.SemiBold),
                    color = Color(0xFF5E5CE6)
                )
            }
        }

        // Segmented control — current board vs archived rules
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.subscriptions_segment_active),
                selected = !showArchived,
                onClick = { showArchived = false }
            )
            SegmentButton(
                label = stringResource(R.string.subscriptions_segment_archived, state.archivedItems.size),
                selected = showArchived,
                onClick = { showArchived = true }
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (showArchived) {
                if (state.archivedItems.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.subscriptions_none_archived),
                            style = ivyInter(15.sp),
                            color = UI.colors.gray,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                        )
                    }
                } else {
                    items(state.archivedItems, key = { "arch-${it.ruleId}" }) { item ->
                        ArchivedRow(
                            item = item,
                            symbol = symbol,
                            onOpen = {
                                nav.navigateTo(
                                    SubscriptionEditScreen(plannedPaymentRuleId = item.ruleId)
                                )
                            },
                            onRestore = { viewModel.restore(item.ruleId) }
                        )
                    }
                }
                return@LazyColumn
            }

            if (state.isEmpty) {
                item { SubscriptionsEmptyState() }
                return@LazyColumn
            }

            // Price-hike banner — shown only when history shows real increases
            val hiked = state.items.filter { it.hikePercent != null }
            if (hiked.isNotEmpty() && !hikeDismissed) {
                item {
                    HikeBanner(
                        hiked = hiked,
                        symbol = symbol,
                        onDismiss = { hikeDismissed = true }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Headline — monthly normalized total + projection + count
            item {
                HeadlineCard(
                    symbol = symbol,
                    monthly = state.monthlyTotal,
                    yearly = state.yearlyTotal,
                    count = state.totalCount,
                )
                Spacer(Modifier.height(14.dp))
            }

            // Filter chips
            item {
                LazyRow(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(state.chips) { chip ->
                        val selected = chip.id == state.selectedChipId
                        CategoryChipView(
                            label = chip.label,
                            count = chip.count,
                            selected = selected,
                            onClick = { viewModel.selectChip(chip.id) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // List — this month's renewals, biggest reality first
            item {
                SectionLabel(stringResource(R.string.subscriptions_this_month))
            }
            items(state.items, key = { it.ruleId }) { item ->
                SubscriptionRow(
                    item = item,
                    symbol = symbol,
                    onClick = {
                        nav.navigateTo(
                            SubscriptionEditScreen(
                                plannedPaymentRuleId = item.ruleId
                            )
                        )
                    }
                )
            }

            // 30-day timeline
            item {
                Spacer(Modifier.height(8.dp))
                SectionLabel(stringResource(R.string.subscriptions_timeline))
            }
            if (state.timeline.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.subscriptions_nothing_due),
                        style = ivyInter(14.sp),
                        color = UI.colors.gray,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(state.timeline, key = { "tl-${it.id}-${it.date}" }) { entry ->
                    TimelineRow(entry = entry, symbol = symbol)
                }
            }

            item { Spacer(Modifier.height(140.dp)) }
        }
    }

    // Expired trial — keep/cancel prompt (M6). Non-dismissible: the user must pick one.
    val expiredTrial = state.items.firstOrNull { item ->
        val ends = item.trialEnds
        item.isTrial && ends != null &&
            !ends.isAfter(java.time.LocalDate.now(ZoneOffset.UTC))
    }
    if (expiredTrial != null) {
        TrialEndDialog(
            item = expiredTrial,
            symbol = symbol,
            onKeep = { viewModel.convertTrialToPaid(expiredTrial.ruleId) },
            onCancel = { viewModel.endSubscription(expiredTrial.ruleId) }
        )
    }
}

@Composable
private fun HeadlineCard(
    symbol: String,
    monthly: Double,
    yearly: Double,
    count: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF5E5CE6), RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.subscriptions_count, count),
            style = ivyInter(12.sp, FontWeight.SemiBold, letterSpacing = 0.8.sp),
            color = Color.White.copy(alpha = 0.82f)
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = symbol + "%,.0f".format(monthly),
                style = ivyInter(38.sp, FontWeight.ExtraBold, letterSpacing = (-1).sp),
                color = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.subscriptions_month_label),
                style = ivyInter(14.sp, FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "≈ $symbol%,.0f %s".format(yearly, stringResource(R.string.subscriptions_year_label)),
            style = ivyInter(14.sp, FontWeight.Medium),
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = ivyInter(12.sp, FontWeight.Bold, letterSpacing = 1.1.sp),
        color = UI.colors.gray,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun CategoryChipView(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        Color(0xFF5E5CE6).copy(alpha = 0.18f)
    } else {
        UI.colors.pureInverse.copy(alpha = 0.06f)
    }
    val content = if (selected) Color(0xFF5E5CE6) else UI.colors.pureInverse

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(background, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = ivyInter(13.sp, FontWeight.SemiBold),
            color = content
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$count",
            style = ivyInter(12.sp, FontWeight.Bold),
            color = content.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun SubscriptionRow(
    item: SubscriptionItem,
    symbol: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val contentAlpha = if (item.isPaused) 0.55f else 1f

        // Icon bubble
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(item.categoryColor ?: 0xFF5E5CE6.toInt())),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(SUBSCRIPTION_ICONS[item.iconKey] ?: R.drawable.ic_planned_payments),
                contentDescription = null,
                tint = Color.White.copy(alpha = contentAlpha),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = ivyInter(16.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse.copy(alpha = contentAlpha),
                maxLines = 1
            )
            Text(
                text = buildString {
                    append(item.intervalLabel)
                    if (!item.cardOrMethod.isNullOrBlank()) {
                        append(" · ")
                        append(item.cardOrMethod)
                    } else if (!item.methodNote.isNullOrBlank()) {
                        append(" · ")
                        append(item.methodNote)
                    }
                },
                style = ivyInter(12.sp),
                color = UI.colors.gray,
                maxLines = 1
            )
            if (item.isPaused) {
                Text(
                    text = stringResource(R.string.subscriptions_paused),
                    style = ivyInter(12.sp, FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = Color(0xFF8E8E93)
                )
            } else if (item.remaining == 0) {
                Text(
                    text = stringResource(R.string.subscriptions_ended),
                    style = ivyInter(12.sp, FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = Color(0xFF8E8E93)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.nextDate?.let { date ->
                        Text(
                            text = stringResource(R.string.subscriptions_next, date.format(MONTH_FORMAT)),
                            style = ivyInter(11.sp, FontWeight.Medium),
                            color = UI.colors.gray
                        )
                    }
                item.remaining?.let { remaining ->
                    Text(
                        text = "  ·  " + stringResource(R.string.subscriptions_remaining, remaining),
                        style = ivyInter(11.sp, FontWeight.SemiBold),
                        color = Color(0xFF8E8E93)
                    )
                }
                item.hikePercent?.let { hike ->
                    Text(
                        text = "  ·  " + stringResource(R.string.subscriptions_hike, hike),
                        style = ivyInter(11.sp, FontWeight.Bold),
                        color = Color(0xFFFF9F0A)
                    )
                }
                item.trialEnds?.let { trialEnd ->
                    Text(
                        text = "  ·  " + stringResource(R.string.subscriptions_trial_ends, trialEnd.format(MONTH_FORMAT)),
                        style = ivyInter(11.sp, FontWeight.Bold),
                        color = Color(0xFF5E5CE6)
                    )
                }
                }
            }
        }

        Text(
            text = symbol + "%,.0f".format(item.amount),
            style = ivyInter(17.sp, FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
                color = UI.colors.pureInverse.copy(alpha = contentAlpha)
        )
    }
}

@Composable
private fun TimelineRow(
    entry: SubscriptionTimelineEntry,
    symbol: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = entry.date.dayOfMonth.toString(),
                style = ivyInter(18.sp, FontWeight.ExtraBold),
                color = UI.colors.pureInverse
            )
            Text(
                text = entry.date.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())).uppercase(),
                style = ivyInter(10.sp, FontWeight.Bold, letterSpacing = 0.6.sp),
                color = UI.colors.gray
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = entry.title,
            style = ivyInter(15.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = symbol + "%,.0f".format(entry.amount),
            style = ivyInter(15.sp, FontWeight.Bold),
            color = UI.colors.pureInverse
        )
    }
}

@Composable
private fun SubscriptionsEmptyState() {
    val nav = navigation()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_planned_payments),
            contentDescription = null,
            tint = UI.colors.gray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.subscriptions_empty_title),
            style = ivyInter(18.sp, FontWeight.Bold),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.subscriptions_empty_subtitle),
            style = ivyInter(15.sp),
            color = UI.colors.gray
        )
        Spacer(Modifier.height(20.dp))
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF5E5CE6).copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                .clickable {
                    nav.navigateTo(
                        SubscriptionEditScreen(
                            plannedPaymentRuleId = null
                        )
                    )
                }
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.subscriptions_empty_cta),
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = Color(0xFF5E5CE6)
            )
        }
    }
}

/**
 * Amber "price increases" banner — lists hiked subscriptions with their old amounts.
 * Dismissible for the session.
 */
@Composable
private fun HikeBanner(
    hiked: List<SubscriptionItem>,
    symbol: String,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFF9F0A).copy(alpha = 0.14f), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFFF9F0A).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.subscriptions_hike_banner),
                style = ivyInter(11.sp, FontWeight.Bold, letterSpacing = 1.1.sp),
                color = Color(0xFFFF9F0A),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.subscriptions_hike_dismiss),
                style = ivyInter(12.sp, FontWeight.SemiBold),
                color = Color(0xFFFF9F0A),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        hiked.take(3).forEach { item ->
            Text(
                text = stringResource(
                    R.string.subscriptions_hike_row,
                    item.title,
                    symbol,
                    item.hikePreviousAmount ?: item.amount,
                ),
                style = ivyInter(13.sp, FontWeight.Medium),
                color = UI.colors.pureInverse,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
        if (hiked.size > 3) {
            Text(
                text = "+ ${hiked.size - 3} more",
                style = ivyInter(12.sp),
                color = UI.colors.gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Expired-trial keep/cancel prompt. Fork-style scrim card; not dismissible by scrim tap —
 * the user must choose. Keep converts to a paid recurring rule; Cancel deletes it.
 */
@Composable
private fun TrialEndDialog(
    item: SubscriptionItem,
    symbol: String,
    onKeep: () -> Unit,
    onCancel: () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(UI.colors.pureInverse, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = null,
                tint = Color(0xFFFF9F0A),
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.subscriptions_trial_ended_title),
                style = ivyInter(20.sp, FontWeight.ExtraBold),
                color = UI.colors.pure
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.subscriptions_trial_ended_body,
                    item.title,
                    item.trialEnds?.format(DateTimeFormatter.ofPattern("d MMM yyyy")) ?: "",
                ),
                style = ivyInter(14.sp),
                color = UI.colors.pure.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = symbol + "%,.0f · %s".format(item.amount, item.intervalLabel),
                style = ivyInter(13.sp, FontWeight.SemiBold),
                color = UI.colors.pure.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.subscriptions_cancel_subscription),
                    style = ivyInter(14.sp, FontWeight.SemiBold),
                    color = Color(0xFFFF6B5B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF6B5B).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
                Text(
                    text = stringResource(R.string.subscriptions_keep_subscription),
                    style = ivyInter(14.sp, FontWeight.Bold),
                    color = Color(0xFF5E5CE6),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(UI.colors.pure.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onKeep)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SegmentButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = ivyInter(12.sp, FontWeight.Bold, letterSpacing = 0.9.sp),
        color = if (selected) Color.White else UI.colors.gray,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (selected) Color(0xFF5E5CE6) else UI.colors.pureInverse.copy(alpha = 0.06f),
                RoundedCornerShape(percent = 50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    )
}

@Composable
private fun ArchivedRow(
    item: SubscriptionItem,
    symbol: String,
    onOpen: () -> Unit,
    onRestore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon bubble — dimmed
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(item.categoryColor ?: 0xFF8E8E93.toInt())),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(SUBSCRIPTION_ICONS[item.iconKey] ?: R.drawable.ic_planned_payments),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = ivyInter(16.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse.copy(alpha = 0.6f),
                maxLines = 1
            )
            Text(
                text = item.intervalLabel,
                style = ivyInter(12.sp),
                color = UI.colors.gray,
                maxLines = 1
            )
        }
        Text(
            text = stringResource(R.string.subscriptions_restore),
            style = ivyInter(13.sp, FontWeight.SemiBold),
            color = Color(0xFF5E5CE6),
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF5E5CE6).copy(alpha = 0.14f), RoundedCornerShape(percent = 50))
                .clickable(onClick = onRestore)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
