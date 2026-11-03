package com.ivy.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Transaction
import com.ivy.base.legacy.TransactionHistoryItem
import com.ivy.base.legacy.stringRes
import com.ivy.data.model.Category
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.api.LocalTimeProvider
import com.ivy.design.l0_system.BlueLight
import com.ivy.design.l0_system.UI
import com.ivy.legacy.data.AppBaseData
import com.ivy.legacy.data.LegacyDueSection
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.utils.capitalizeLocal
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.dateNowLocal
import com.ivy.legacy.utils.dateNowUTC
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.formatLocal
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.shortenAmount
import com.ivy.legacy.utils.shouldShortAmount
import com.ivy.legacy.utils.springBounce
import com.ivy.legacy.utils.timeNowUTC
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.TransactionsScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.time.TimeFormatter
import com.ivy.wallet.domain.data.IvyCurrency
import com.ivy.wallet.domain.data.TransactionHistoryDateDivider
import com.ivy.wallet.ui.theme.components.IvyDividerDot
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// -----------------------------------------------------------------------------------------
// iOS-style grouped transaction list. Mirrors the legacy `transactions()` section logic
// (upcoming / overdue / history / empty state) while rendering Cupertino rows: grouped
// cards with hairline separators, category chips and color-coded trailing amounts.
// -----------------------------------------------------------------------------------------

private val GroupRadius = 20.dp
private val GroupCardHPadding = 20.dp
private val GroupRowHPadding = 16.dp

fun LazyListScope.homeTransactions(
    baseData: AppBaseData,
    upcoming: LegacyDueSection?,
    overdue: LegacyDueSection?,
    history: List<TransactionHistoryItem>,
    emptyStateTitle: String,
    emptyStateText: String,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (Transaction) -> Unit,
    setUpcomingExpanded: (Boolean) -> Unit,
    setOverdueExpanded: (Boolean) -> Unit,
    onSkipTransaction: (Transaction) -> Unit = {},
    onSkipAllTransactions: (List<Transaction>) -> Unit = {},
) {
    upcomingSection(
        baseData = baseData,
        upcoming = upcoming,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        onPayOrGet = onPayOrGet,
        onSkipTransaction = onSkipTransaction,
        setExpanded = setUpcomingExpanded
    )

    overdueSection(
        baseData = baseData,
        overdue = overdue,
        onPayOrGet = onPayOrGet,
        onSkipTransaction = onSkipTransaction,
        onSkipAllTransactions = onSkipAllTransactions,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        setExpanded = setOverdueExpanded
    )

    historySection(
        baseData = baseData,
        history = history,
        shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
        onPayOrGet = onPayOrGet
    )

    if (
        (upcoming == null || upcoming.trns.isEmpty()) &&
        (overdue == null || overdue.trns.isEmpty()) &&
        history.isEmpty()
    ) {
        item {
            NoTransactionsEmptyState(
                emptyStateTitle = emptyStateTitle,
                emptyStateText = emptyStateText
            )
        }
    }

    scrollHackSpacer(
        history = history,
        upcoming = upcoming,
        overdue = overdue
    )
}

// -----------------------------------------------------------------------------------------
// Sections
// -----------------------------------------------------------------------------------------

private fun LazyListScope.upcomingSection(
    baseData: AppBaseData,
    upcoming: LegacyDueSection?,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (Transaction) -> Unit,
    onSkipTransaction: (Transaction) -> Unit,
    setExpanded: (Boolean) -> Unit
) {
    if (upcoming == null) return // guard

    if (upcoming.trns.isNotEmpty()) {
        item(key = "upcoming_section") {
            Spacer(Modifier.height(16.dp))

            HomeDueSectionHeader(
                expanded = upcoming.expanded,
                title = stringRes(R.string.upcoming),
                accent = UI.colors.orange,
                baseCurrency = baseData.baseCurrency,
                income = upcoming.stats.income.toDouble(),
                expenses = upcoming.stats.expense.abs().toDouble(),
                setExpanded = setExpanded
            )
        }

        if (upcoming.expanded) {
            dueItems(
                baseData = baseData,
                transactions = upcoming.trns,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                onPayOrGet = onPayOrGet,
                onSkipTransaction = onSkipTransaction
            )
        }
    }
}

private fun LazyListScope.overdueSection(
    baseData: AppBaseData,
    overdue: LegacyDueSection?,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (Transaction) -> Unit,
    onSkipTransaction: (Transaction) -> Unit,
    onSkipAllTransactions: (List<Transaction>) -> Unit,
    setExpanded: (Boolean) -> Unit
) {
    if (overdue == null) return

    if (overdue.trns.isNotEmpty()) {
        item(key = "overdue_section") {
            Spacer(Modifier.height(16.dp))

            HomeDueSectionHeader(
                expanded = overdue.expanded,
                title = stringRes(R.string.overdue),
                accent = UI.colors.red,
                baseCurrency = baseData.baseCurrency,
                income = overdue.stats.income.toDouble(),
                expenses = overdue.stats.expense.abs().toDouble(),
                setExpanded = setExpanded
            )
        }

        if (overdue.expanded) {
            item(key = "overdue_skip_all") {
                SkipAllButton(onClick = { onSkipAllTransactions(overdue.trns) })
            }

            dueItems(
                baseData = baseData,
                transactions = overdue.trns,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                onPayOrGet = onPayOrGet,
                onSkipTransaction = onSkipTransaction
            )
        }
    }
}

private fun LazyListScope.dueItems(
    baseData: AppBaseData,
    transactions: List<Transaction>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (Transaction) -> Unit,
    onSkipTransaction: (Transaction) -> Unit,
) {
    itemsIndexed(
        items = transactions,
        key = { _, it -> it.id }
    ) { index, transaction ->
        HomeTransactionRow(
            baseData = baseData,
            transaction = transaction,
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
            isFirstInGroup = index == 0,
            isLastInGroup = index == transactions.lastIndex,
            onPayOrGet = onPayOrGet,
            onSkipTransaction = onSkipTransaction
        )
    }
}

private fun LazyListScope.historySection(
    baseData: AppBaseData,
    history: List<TransactionHistoryItem>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (Transaction) -> Unit
) {
    if (history.isEmpty()) return

    itemsIndexed(
        items = history,
        key = { _, it ->
            when (it) {
                is Transaction -> it.id.toString()
                is TransactionHistoryDateDivider -> it.date.toString()
                else -> "unknown"
            }
        }
    ) { index, item ->
        when (item) {
            is Transaction -> {
                val next = history.getOrNull(index + 1)
                HomeTransactionRow(
                    baseData = baseData,
                    transaction = item,
                    shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
                    isFirstInGroup = index == 0 ||
                            history.getOrNull(index - 1) is TransactionHistoryDateDivider,
                    isLastInGroup = next == null || next is TransactionHistoryDateDivider,
                    onPayOrGet = onPayOrGet
                )
            }

            is TransactionHistoryDateDivider -> {
                HomeDateDivider(
                    date = item.date,
                    baseCurrency = baseData.baseCurrency,
                    income = item.income,
                    expenses = item.expenses
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Due section header (Upcoming / Overdue) + Skip all
// -----------------------------------------------------------------------------------------

@Composable
private fun LazyItemScope.HomeDueSectionHeader(
    expanded: Boolean,
    title: String,
    accent: Color,
    baseCurrency: String,
    income: Double,
    expenses: Double,
    setExpanded: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(rememberInteractionSource()) {
                setExpanded(!expanded)
            }
            .padding(horizontal = 36.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                modifier = Modifier.testTag("upcoming_title"),
                text = title.uppercase(),
                style = homeInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
                color = accent
            )

            Spacer(Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (expenses > 0) {
                    Text(
                        modifier = Modifier.testTag("upcoming_expense"),
                        text = "${expenses.format(baseCurrency)} $baseCurrency",
                        style = homeInter(12.sp, FontWeight.SemiBold),
                        color = UI.colors.pureInverse
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.expenses_lowercase),
                        style = homeInter(11.sp),
                        color = UI.colors.gray
                    )
                }

                if (income > 0 && expenses > 0) {
                    Spacer(Modifier.width(8.dp))
                    IvyDividerDot()
                    Spacer(Modifier.width(8.dp))
                }

                if (income > 0) {
                    Text(
                        modifier = Modifier.testTag("upcoming_income"),
                        text = "${income.format(baseCurrency)} $baseCurrency",
                        style = homeInter(12.sp, FontWeight.SemiBold),
                        color = UI.colors.green
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.income_lowercase),
                        style = homeInter(11.sp),
                        color = UI.colors.gray
                    )
                }
            }
        }

        val expandIconRotation by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (expanded) -180f else 0f,
            animationSpec = springBounce(),
            label = "due_section_expand"
        )

        IvyIcon(
            modifier = Modifier
                .rotate(expandIconRotation)
                .size(20.dp),
            icon = R.drawable.ic_expand_less,
            tint = UI.colors.gray
        )
    }
}

@Composable
private fun LazyItemScope.SkipAllButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(start = 36.dp, bottom = 10.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(UI.colors.red.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = stringResource(R.string.skip_all),
            style = homeInter(14.sp, FontWeight.SemiBold),
            color = UI.colors.red
        )
    }
}

// -----------------------------------------------------------------------------------------
// Date divider
// -----------------------------------------------------------------------------------------

@Composable
private fun LazyItemScope.HomeDateDivider(
    date: LocalDate,
    baseCurrency: String,
    income: Double,
    expenses: Double,
) {
    val today = dateNowLocal()
    val cashflow = income - expenses

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp)
            .padding(top = 28.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.formatLocal(
                    if (today.year == date.year) "MMMM dd." else "MMM dd. yyy"
                ),
                style = homeInter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "${cashflow.format(baseCurrency)} $baseCurrency",
                style = homeInter(13.sp, FontWeight.SemiBold),
                color = if (cashflow > 0) UI.colors.green else UI.colors.gray
            )
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = when (date) {
                today -> stringResource(R.string.today)
                today.minusDays(1) -> stringResource(R.string.yesterday)
                today.plusDays(1) -> stringResource(R.string.tomorrow)
                else -> date.formatLocal("EEEE")
            },
            style = homeInter(12.sp),
            color = UI.colors.gray
        )
    }
}

// -----------------------------------------------------------------------------------------
// Transaction row - one iOS grouped-list row
// -----------------------------------------------------------------------------------------

@Composable
private fun LazyItemScope.HomeTransactionRow(
    baseData: AppBaseData,
    transaction: Transaction,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    onPayOrGet: (Transaction) -> Unit,
    onSkipTransaction: (Transaction) -> Unit = {},
) {
    val nav = navigation()

    val account = accountOf(transaction.accountId, baseData.accounts)
    val toAccount = transaction.toAccountId?.let { accountOf(it, baseData.accounts) }
    val category = transaction.categoryId?.let { categoryOf(it, baseData.categories) }

    val transactionCurrency = account?.currency ?: baseData.baseCurrency
    val toAccountCurrency = toAccount?.currency ?: baseData.baseCurrency

    val topRadius = if (isFirstInGroup) GroupRadius else 0.dp
    val bottomRadius = if (isLastInGroup) GroupRadius else 0.dp
    val shape = RoundedCornerShape(
        topStart = topRadius,
        topEnd = topRadius,
        bottomStart = bottomRadius,
        bottomEnd = bottomRadius
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GroupCardHPadding)
            .clip(shape)
            .background(UI.colors.medium)
            .clickable {
                if (account != null) {
                    nav.navigateTo(
                        EditTransactionScreen(
                            initialTransactionId = transaction.id,
                            type = transaction.type
                        )
                    )
                }
            }
            .testTag("transaction_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .padding(horizontal = GroupRowHPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeadingChip(
                transaction = transaction,
                category = category,
                account = account,
                showAccountColor = shouldShowAccountSpecificColorInTransactions
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = rowTitle(transaction, category, account),
                    style = homeInter(16.sp, FontWeight.SemiBold),
                    color = UI.colors.pureInverse,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val caption = rowCaption(
                    transaction = transaction,
                    category = category,
                    account = account,
                    toAccount = toAccount,
                    showCategoryInCaption = transaction.title.isNullOrBlank() ||
                            transaction.title != category?.name?.value
                )
                if (caption != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = caption,
                        style = homeInter(13.sp),
                        color = UI.colors.gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val description = getTransactionDescription(transaction)
                if (!description.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = homeInter(13.sp),
                        color = UI.colors.gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (transaction.dueDate != null) {
                    Spacer(Modifier.height(3.dp))
                    DueDateCaption(transaction = transaction)
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatRowAmount(transaction.amount.toDouble(), transactionCurrency),
                    style = homeInter(16.sp, FontWeight.Bold, letterSpacing = (-0.2).sp),
                    color = typeAmountColor(
                        transaction = transaction,
                        dueDate = with(LocalTimeConverter.current) {
                            transaction.dueDate?.toLocalDateTime()
                        }
                    ),
                    maxLines = 1,
                )

                if (transaction.type == com.ivy.base.model.TransactionType.TRANSFER &&
                    toAccountCurrency != transactionCurrency
                ) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatRowAmount(
                            transaction.toAmount.toDouble(),
                            toAccountCurrency
                        ),
                        style = homeInter(12.sp),
                        color = UI.colors.gray,
                        maxLines = 1,
                    )
                }
            }
        }

        if (transaction.tags.isNotEmpty()) {
            TransactionTagsRow(transaction.tags)
        }

        if (transaction.dueDate != null && transaction.dateTime == null) {
            PlannedPaymentActions(
                transaction = transaction,
                onPayOrGet = onPayOrGet,
                onSkipTransaction = onSkipTransaction
            )
        }

        if (!isLastInGroup) {
            Box(
                modifier = Modifier
                    .padding(start = 76.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(homeRowSeparator())
            )
        }
    }
}

@Composable
private fun LazyItemScope.LeadingChip(
    transaction: Transaction,
    category: Category?,
    account: Account?,
    showAccountColor: Boolean,
) {
    val nav = navigation()

    when (transaction.type) {
        com.ivy.base.model.TransactionType.TRANSFER -> {
            Chip(
                backgroundColor = UI.colors.primary.copy(alpha = 0.14f),
                icon = null,
                fallbackIcon = R.drawable.ic_transfer,
                tint = UI.colors.primary,
                onClick = null
            )
        }

        else -> {
            if (category != null) {
                Chip(
                    backgroundColor = category.color.value.toComposeColor(),
                    icon = category.icon?.id,
                    fallbackIcon = R.drawable.ic_custom_category_s,
                    tint = findContrastTextColor(category.color.value.toComposeColor()),
                    onClick = {
                        nav.navigateTo(
                            TransactionsScreen(
                                accountId = null,
                                categoryId = category.id.value
                            )
                        )
                    }
                )
            } else {
                val backgroundColor = if (showAccountColor && account != null) {
                    account.color.toComposeColor()
                } else {
                    UI.colors.mediumInverse
                }
                Chip(
                    backgroundColor = backgroundColor,
                    icon = account?.icon,
                    fallbackIcon = R.drawable.ic_custom_account_s,
                    tint = findContrastTextColor(backgroundColor),
                    onClick = account?.let {
                        {
                            nav.navigateTo(
                                TransactionsScreen(
                                    accountId = it.id,
                                    categoryId = null
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.Chip(
    backgroundColor: Color,
    icon: String?,
    @DrawableRes fallbackIcon: Int,
    tint: Color,
    onClick: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        ItemIconSDefaultIcon(
            iconName = icon,
            defaultIcon = fallbackIcon,
            tint = tint
        )
    }
}

@Composable
private fun LazyItemScope.TransactionTagsRow(
    tags: ImmutableList<com.ivy.base.legacy.LegacyTag>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp, end = 16.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.tags),
            style = homeInter(11.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.width(6.dp))

        tags.forEach { tag ->
            Text(
                text = "#${tag.name}",
                style = homeInter(11.sp, FontWeight.Medium),
                color = BlueLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(6.dp))
        }
    }
}

@Composable
private fun LazyItemScope.PlannedPaymentActions(
    transaction: Transaction,
    onPayOrGet: (Transaction) -> Unit,
    onSkipTransaction: (Transaction) -> Unit,
) {
    val isExpense = transaction.type == com.ivy.base.model.TransactionType.EXPENSE
    val accent = if (isExpense) UI.colors.red else UI.colors.green

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(percent = 50))
                .background(UI.colors.pureInverse.copy(alpha = 0.06f))
                .clickable { onSkipTransaction(transaction) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.skip),
                style = homeInter(14.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(percent = 50))
                .background(accent.copy(alpha = 0.14f))
                .clickable { onPayOrGet(transaction) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isExpense) {
                    stringResource(R.string.pay)
                } else {
                    stringResource(R.string.get)
                },
                style = homeInter(14.sp, FontWeight.SemiBold),
                color = accent
            )
        }
    }
}

@Composable
private fun DueDateCaption(transaction: Transaction) {
    val timeFormatter = LocalTimeFormatter.current
    val timeProvider = LocalTimeProvider.current

    Text(
        text = stringResource(
            R.string.due_on,
            with(timeFormatter) {
                transaction.dueDate!!.formatLocal(
                    TimeFormatter.Style.DateOnly(
                        includeWeekDay = true
                    )
                )
            }
        ).uppercase(),
        style = homeInter(11.sp, FontWeight.SemiBold, letterSpacing = 0.4.sp),
        color = if (transaction.dueDate!!.isAfter(timeProvider.utcNow())) {
            UI.colors.orange
        } else {
            UI.colors.gray
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun LazyItemScope.NoTransactionsEmptyState(
    emptyStateTitle: String,
    emptyStateText: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(UI.colors.medium, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IvyIcon(
                icon = R.drawable.ic_notransactions,
                tint = UI.colors.gray,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = emptyStateTitle,
            style = homeInter(17.sp, FontWeight.SemiBold),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 40.dp),
            text = emptyStateText,
            style = homeInter(14.sp),
            color = UI.colors.gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(120.dp))
    }
}

// -----------------------------------------------------------------------------------------
// Helpers
// -----------------------------------------------------------------------------------------

private fun LazyListScope.scrollHackSpacer(
    history: List<TransactionHistoryItem>,
    upcoming: LegacyDueSection?,
    overdue: LegacyDueSection?,
) {
    item {
        // last spacer - scroll hack + clearance for the floating dock
        val trnCount = history.size.plus(
            if (upcoming != null && upcoming.expanded) upcoming.trns.size else 0
        ).plus(
            if (overdue != null && overdue.expanded) overdue.trns.size else 0
        )
        if (trnCount <= 5) {
            Spacer(Modifier.height(300.dp))
        } else {
            Spacer(Modifier.height(150.dp))
        }
    }
}

@Composable
private fun accountOf(accountId: UUID?, accounts: List<Account>): Account? {
    val targetId = accountId ?: return null
    return com.ivy.legacy.ivyWalletCtx().accountMap[targetId]
        ?: accounts.find { it.id == targetId }
}

@Composable
private fun categoryOf(categoryId: UUID, categories: List<Category>): Category? {
    return com.ivy.legacy.ivyWalletCtx().categoryMap[categoryId]
        ?: categories.find { it.id.value == categoryId }
}

private fun rowTitle(
    transaction: Transaction,
    category: Category?,
    account: Account?,
): String = when {
    !transaction.title.isNullOrBlank() -> transaction.title!!
    category != null -> category.name.value
    else -> account?.name ?: ""
}

private fun rowCaption(
    transaction: Transaction,
    category: Category?,
    account: Account?,
    toAccount: Account?,
    showCategoryInCaption: Boolean,
): String? = when (transaction.type) {
    com.ivy.base.model.TransactionType.TRANSFER -> {
        val from = account?.name ?: ""
        val to = toAccount?.name ?: ""
        if (to.isBlank()) from else "$from → $to"
    }

    else -> {
        val parts = mutableListOf<String>()
        if (showCategoryInCaption && category != null) {
            parts.add(category.name.value)
        }
        parts.add(account?.name ?: "")
        parts.filter { it.isNotBlank() }.joinToString(" · ").ifBlank { null }
    }
}

@Composable
private fun getTransactionDescription(transaction: Transaction): String? {
    val paidFor = with(LocalTimeConverter.current) {
        transaction.paidFor?.toLocalDateTime()
    }
    return when {
        !transaction.description.isNullOrBlank() -> transaction.description!!
        transaction.recurringRuleId != null &&
                transaction.dueDate == null &&
                paidFor != null -> {
            stringResource(
                R.string.bill_paid,
                paidFor.month.name.lowercase().capitalizeLocal(),
                paidFor.year.toString()
            )
        }

        else -> null
    }
}

@Composable
private fun typeAmountColor(
    transaction: Transaction,
    dueDate: LocalDateTime?,
): Color = when (transaction.type) {
    com.ivy.base.model.TransactionType.INCOME -> UI.colors.green

    com.ivy.base.model.TransactionType.EXPENSE -> when {
        dueDate != null && dueDate.isAfter(timeNowUTC()) -> UI.colors.orange

        dueDate != null && dueDate.isBefore(dateNowUTC().atStartOfDay()) -> UI.colors.red

        else -> UI.colors.pureInverse
    }

    com.ivy.base.model.TransactionType.TRANSFER -> UI.colors.primary
}

private fun formatRowAmount(amount: Double, currency: String): String =
    if (shouldShortAmount(amount)) {
        shortenAmount(amount)
    } else {
        amount.format(IvyCurrency.getDecimalPlaces(currency))
    }
