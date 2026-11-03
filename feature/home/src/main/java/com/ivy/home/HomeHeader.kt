package com.ivy.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.model.TransactionType
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.api.LocalTimeProvider
import com.ivy.design.l0_system.UI
import com.ivy.legacy.data.model.TimePeriod
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.horizontalSwipeListener
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.rememberSwipeListenerState
import com.ivy.legacy.utils.shortenAmount
import com.ivy.legacy.utils.shouldShortAmount
import com.ivy.legacy.utils.springBounce
import com.ivy.navigation.PieChartStatisticScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon
import kotlin.math.absoluteValue

@ExperimentalAnimationApi
@Composable
internal fun HomeHeader(
    expanded: Boolean,
    name: String,
    period: TimePeriod,
    currency: String,
    balance: Double,
    onShowMonthModal: () -> Unit,
    onBalanceClick: () -> Unit,
    onSelectNextMonth: () -> Unit,
    hideBalance: Boolean,
    onHiddenBalanceClick: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
) {
    Column {
        val percentExpanded by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = springBounce(
                stiffness = Spring.StiffnessLow
            ),
            label = "Home Header Expand Collapse"
        )

        Spacer(Modifier.height(16.dp))

        HeaderStickyRow(
            percentExpanded = percentExpanded,
            name = name,
            period = period,
            currency = currency,
            balance = balance,
            hideBalance = hideBalance,

            onShowMonthModal = onShowMonthModal,
            onBalanceClick = onBalanceClick,
            onHiddenBalanceClick = onHiddenBalanceClick,
            onSelectNextMonth = onSelectNextMonth,
            onSelectPreviousMonth = onSelectPreviousMonth,
        )

        if (percentExpanded < 0.5f) {
            Spacer(Modifier.height(16.dp))

            HairlineDivider(
                modifier = Modifier.alpha(1f - percentExpanded)
            )
        }
    }
}

@Composable
private fun HeaderStickyRow(
    percentExpanded: Float,
    name: String,
    period: TimePeriod,
    currency: String,
    balance: Double,
    onShowMonthModal: () -> Unit,
    onBalanceClick: () -> Unit,
    onSelectNextMonth: () -> Unit,
    hideBalance: Boolean,
    onHiddenBalanceClick: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                modifier = Modifier
                    .alpha(percentExpanded)
                    .testTag("home_greeting_text"),
                text = if (name.isNotNullOrBlank()) {
                    stringResource(
                        R.string.hi_name,
                        name,
                    )
                } else {
                    stringResource(R.string.hi)
                },
                style = homeInter(24.sp, FontWeight.Bold, letterSpacing = (-0.3).sp),
                color = UI.colors.pureInverse,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            // Mini balance - fades in as the list scrolls away from the top
            if (percentExpanded < 1f) {
                Row(
                    modifier = Modifier
                        .alpha(alpha = 1f - percentExpanded)
                        .clickableNoIndication(rememberInteractionSource()) {
                            if (hideBalance) {
                                onHiddenBalanceClick()
                            } else {
                                onBalanceClick()
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (hideBalance) {
                            HiddenAmount
                        } else {
                            displayAmount(balance, currency)
                        },
                        style = homeInter(17.sp, FontWeight.Bold, letterSpacing = (-0.2).sp),
                        color = UI.colors.pureInverse,
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = currency,
                        style = homeInter(13.sp, FontWeight.SemiBold),
                        color = UI.colors.gray,
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        MonthCapsule(
            period = period,
            onShowMonthModal = onShowMonthModal,
            onSelectNextMonth = onSelectNextMonth,
            onSelectPreviousMonth = onSelectPreviousMonth,
        )
    }
}

// -----------------------------------------------------------------------------------------
// Month capsule - tappable pill that opens the period picker; chevrons step months and a
// horizontal swipe on the capsule pages through months.
// -----------------------------------------------------------------------------------------

@Composable
private fun MonthCapsule(
    period: TimePeriod,
    onShowMonthModal: () -> Unit,
    onSelectNextMonth: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(UI.colors.medium)
            .border(1.dp, homeHairline(), RoundedCornerShape(percent = 50))
            .height(44.dp)
            .horizontalSwipeListener(
                sensitivity = 75,
                state = rememberSwipeListenerState(),
                onSwipeLeft = { onSelectNextMonth() },
                onSwipeRight = { onSelectPreviousMonth() },
            )
            .clickableNoIndication(rememberInteractionSource()) {
                onShowMonthModal()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CapsuleChevron(
            icon = R.drawable.ic_back,
            contentDescription = stringResource(R.string.previous_month),
            onClick = onSelectPreviousMonth,
        )

        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = period.toDisplayShort(
                startDateOfMonth = ivyWalletCtx().startDayOfMonth,
                timeConverter = LocalTimeConverter.current,
                timeProvider = LocalTimeProvider.current,
                timeFormatter = LocalTimeFormatter.current,
            ),
            style = homeInter(15.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse,
            maxLines = 1,
        )

        CapsuleChevron(
            icon = R.drawable.ic_arrow_right,
            contentDescription = stringResource(R.string.next_month),
            onClick = onSelectNextMonth,
        )
    }
}

@Composable
private fun CapsuleChevron(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IvyIcon(
            icon = icon,
            contentDescription = contentDescription,
            tint = UI.colors.pureInverse,
            modifier = Modifier.size(18.dp)
        )
    }
}

// -----------------------------------------------------------------------------------------
// Cash flow hero - the large balance plus a grouped Income | Expenses card.
// -----------------------------------------------------------------------------------------

@ExperimentalAnimationApi
@Composable
fun CashFlowInfo(
    currency: String,
    balance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    hideBalance: Boolean,
    hideIncome: Boolean,
    onHiddenIncomeClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onHiddenBalanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HeroBalance(
            currency = currency,
            balance = balance,
            hideBalance = hideBalance,
            onBalanceClick = onBalanceClick,
            onHiddenBalanceClick = onHiddenBalanceClick,
        )

        Spacer(Modifier.height(20.dp))

        CashFlowCard(
            currency = currency,
            monthlyIncome = monthlyIncome,
            monthlyExpenses = monthlyExpenses,
            hideIncome = hideIncome,
            onHiddenIncomeClick = onHiddenIncomeClick
        )

        val cashflow = monthlyIncome - monthlyExpenses
        if (cashflow != 0.0 && !hideBalance) {
            Spacer(Modifier.height(12.dp))

            Text(
                modifier = Modifier.padding(
                    start = 24.dp,
                ),
                text = stringResource(
                    R.string.cashflow,
                    (if (cashflow > 0) "+" else ""),
                    cashflow.format(currency),
                    currency,
                ),
                style = homeInter(13.sp, FontWeight.SemiBold),
                color = if (cashflow < 0) UI.colors.gray else UI.colors.green,
            )
        }
    }
}

@Composable
private fun HeroBalance(
    currency: String,
    balance: Double,
    hideBalance: Boolean,
    onBalanceClick: () -> Unit,
    onHiddenBalanceClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickableNoIndication(rememberInteractionSource()) {
                if (hideBalance) {
                    onHiddenBalanceClick()
                } else {
                    onBalanceClick()
                }
            }
            .testTag("home_balance"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (hideBalance) HiddenAmount else displayAmount(balance, currency),
            style = homeInter(40.sp, FontWeight.Bold, letterSpacing = (-0.8).sp),
            color = UI.colors.pureInverse,
            maxLines = 1,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = currency,
            style = homeInter(17.sp, FontWeight.SemiBold),
            color = UI.colors.gray,
        )
    }
}

@Composable
private fun CashFlowCard(
    currency: String,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    hideIncome: Boolean,
    onHiddenIncomeClick: () -> Unit,
) {
    val nav = navigation()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .border(1.dp, homeHairline(), RoundedCornerShape(20.dp)),
    ) {
        CashFlowHalf(
            modifier = Modifier.weight(1f),
            testTag = "home_card_income",
            icon = R.drawable.ic_income,
            tint = UI.colors.green,
            label = stringResource(R.string.income),
            currency = currency,
            amount = monthlyIncome,
            hidden = hideIncome,
            onClick = {
                if (hideIncome) {
                    onHiddenIncomeClick()
                } else {
                    nav.navigateTo(
                        PieChartStatisticScreen(
                            type = TransactionType.INCOME,
                        ),
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(homeRowSeparator())
        )

        CashFlowHalf(
            modifier = Modifier.weight(1f),
            testTag = "home_card_expense",
            icon = R.drawable.ic_expense,
            tint = UI.colors.red,
            label = stringResource(R.string.expenses),
            currency = currency,
            amount = monthlyExpenses.absoluteValue,
            hidden = false,
            onClick = {
                nav.navigateTo(
                    PieChartStatisticScreen(
                        type = TransactionType.EXPENSE,
                    ),
                )
            }
        )
    }
}

@Composable
private fun CashFlowHalf(
    testTag: String,
    icon: Int,
    tint: Color,
    label: String,
    currency: String,
    amount: Double,
    hidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(tint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                IvyIcon(
                    icon = icon,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = label.uppercase(),
                style = homeInter(12.sp, FontWeight.SemiBold, letterSpacing = 0.5.sp),
                color = UI.colors.gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = if (hidden) HiddenAmount else displayAmount(amount, currency),
            style = homeInter(19.sp, FontWeight.Bold, letterSpacing = (-0.3).sp),
            color = UI.colors.pureInverse,
            maxLines = 1,
        )
    }
}

// -----------------------------------------------------------------------------------------
// Helpers
// -----------------------------------------------------------------------------------------

private const val HiddenAmount = "••••"

private fun displayAmount(value: Double, currency: String): String =
    if (shouldShortAmount(value)) {
        shortenAmount(value)
    } else {
        value.format(currency)
    }
