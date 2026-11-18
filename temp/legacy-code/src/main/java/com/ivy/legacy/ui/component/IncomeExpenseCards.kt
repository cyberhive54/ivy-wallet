package com.ivy.legacy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.base.legacy.Transaction
import com.ivy.base.legacy.TransactionHistoryItem
import com.ivy.base.legacy.stringRes
import com.ivy.base.model.TransactionType
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.domain.data.IvyCurrency

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Suppress("ParameterNaming")
@Composable
fun IncomeExpensesCards(
    history: List<TransactionHistoryItem>,
    currency: String,
    income: Double,
    expenses: Double,

    hasAddButtons: Boolean,
    itemColor: Color,

    incomeHeaderCardClicked: () -> Unit = {},
    expenseHeaderCardClicked: () -> Unit = {},
    onAddTransaction: (TransactionType) -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        HeaderCard(
            title = stringRes(R.string.income_uppercase),
            currencyCode = currency,
            amount = income,
            transactionCount = history
                .filterIsInstance(Transaction::class.java)
                .count { it.type == TransactionType.INCOME },
            addButtonText = if (hasAddButtons) stringResource(R.string.add_income) else null,
            isIncome = true,

            onHeaderCardClicked = { incomeHeaderCardClicked() }
        ) {
            onAddTransaction(TransactionType.INCOME)
        }

        Spacer(Modifier.width(12.dp))

        HeaderCard(
            title = stringRes(R.string.expenses_uppercase),
            currencyCode = currency,
            amount = expenses,
            transactionCount = history
                .filterIsInstance(Transaction::class.java)
                .count { it.type == TransactionType.EXPENSE },
            addButtonText = if (hasAddButtons) stringResource(R.string.add_expense) else null,
            isIncome = false,

            onHeaderCardClicked = { expenseHeaderCardClicked() }
        ) {
            onAddTransaction(TransactionType.EXPENSE)
        }

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
@Suppress("ParameterNaming")
private fun RowScope.HeaderCard(
    title: String,
    currencyCode: String,
    amount: Double,
    transactionCount: Int,

    isIncome: Boolean,
    addButtonText: String?,

    onHeaderCardClicked: () -> Unit = {},
    onAddClick: () -> Unit
) {
    val semantic = if (isIncome) Color(0xFF30D158) else Color(0xFFFF9F0A)
    val surface = UI.colors.pureInverse.copy(alpha = 0.04f)
    val hairline = UI.colors.pureInverse.copy(alpha = 0.10f)
    val primary = UI.colors.pureInverse
    val secondary = UI.colors.pureInverse.copy(alpha = 0.72f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(UI.shapes.r2)
            .background(surface, UI.shapes.r2)
            .border(1.dp, hairline, UI.shapes.r2)
            .clickable { onHeaderCardClicked() },
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = title,
            style = UI.typo.b2.style(color = secondary, fontWeight = FontWeight.Medium)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = amount.format(currencyCode),
            style = UI.typo.nB1.style(color = semantic, fontWeight = FontWeight.SemiBold)
        )
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = IvyCurrency.fromCode(currencyCode)?.name ?: "",
            style = UI.typo.b2.style(color = secondary, fontWeight = FontWeight.Normal)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = transactionCount.toString(),
            style = UI.typo.nB1.style(color = primary, fontWeight = FontWeight.Medium)
        )
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringRes(R.string.transactions),
            style = UI.typo.b2.style(color = secondary, fontWeight = FontWeight.Normal)
        )

        Spacer(Modifier.height(20.dp))

        if (addButtonText != null) {
            Text(
                text = addButtonText,
                style = UI.typo.b2.style(color = primary, fontWeight = FontWeight.Medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(UI.shapes.r2)
                    .background(surface, UI.shapes.r2)
                    .border(1.dp, hairline, UI.shapes.r2)
                    .clickable { onAddClick() }
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}
