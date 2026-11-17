package com.ivy.main.workspace

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.runtime.collectAsState
import com.ivy.base.model.LoanRecordType
import com.ivy.design.l0_system.UI
import com.ivy.domain.statement.Statement
import com.ivy.domain.statement.StatementLine
import com.ivy.domain.statement.StatementRenderer
import com.ivy.legacy.ivyWalletCtx
import com.ivy.navigation.LedgerStatementScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val STATEMENT_DATE = DateTimeFormatter.ofPattern("d MMM yyyy")

/**
 * Statement of account: period picker, in-app preview and a shareable PDF.
 */
@Composable
fun BoxWithConstraintsScope.LedgerStatement(screen: LedgerStatementScreen) {
    val nav = navigation()
    val context = LocalContext.current
    val ivyContext = ivyWalletCtx()
    val viewModel: LedgerCustomerDetailViewModel = screenScopedViewModel()
    val customer by viewModel.customer.collectAsState()

    var range by remember { mutableStateOf(StatementRange.THREE_MONTHS) }
    var customFrom by remember { mutableStateOf<LocalDate?>(null) }
    var customTo by remember { mutableStateOf<LocalDate?>(null) }
    var sharing by remember { mutableStateOf(false) }

    LaunchedEffect(screen.loanId) {
        viewModel.load(screen.loanId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(UI.colors.pure)
    ) {
        // Top bar
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
                text = stringResource(R.string.ledger_statement_title),
                style = ivyInter(22.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
                color = UI.colors.pureInverse
            )
        }

        // Period chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatementRange.entries.forEach { option ->
                StatementRangeChip(
                    label = stringResource(
                        when (option) {
                            StatementRange.MONTH -> R.string.ledger_range_month
                            StatementRange.THREE_MONTHS -> R.string.ledger_range_three_months
                            StatementRange.CUSTOM -> R.string.ledger_range_custom
                        }
                    ),
                    selected = range == option,
                    onClick = { range = option }
                )
            }
        }

        // Custom range pickers
        if (range == StatementRange.CUSTOM) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = customFrom?.format(STATEMENT_DATE)
                        ?: stringResource(R.string.ledger_range_from),
                    style = ivyInter(13.sp, FontWeight.SemiBold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(percent = 50))
                        .clickable {
                            ivyContext.datePicker(
                                initialDate = customFrom ?: LocalDate.now(),
                                maxDate = customTo
                            ) { picked -> customFrom = picked }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
                Text(
                    text = customTo?.format(STATEMENT_DATE)
                        ?: stringResource(R.string.ledger_range_to),
                    style = ivyInter(13.sp, FontWeight.SemiBold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(percent = 50))
                        .clickable {
                            ivyContext.datePicker(
                                initialDate = customTo ?: LocalDate.now(),
                                minDate = customFrom
                            ) { picked -> customTo = picked }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        val c = customer
        if (c == null) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.ledger_customer_not_found),
                style = ivyInter(15.sp),
                color = UI.colors.gray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            return@Column
        }

        val (from, to) = range.bounds(customFrom, customTo)
        val lines = c.entries
            .filter { entry ->
                val date = entry.date?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: return@filter false
                (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to))
            }
            .sortedBy { it.date }

        val statement = Statement(
            title = c.name,
            subtitle = if (c.youOwe) {
                stringResource(R.string.ledger_you_owe_else)
            } else {
                stringResource(R.string.ledger_they_owe_you)
            },
            period = periodLabel(from, to),
            lines = lines.map { entry ->
                StatementLine(
                    date = entry.date?.atZone(ZoneId.systemDefault())
                        ?.format(STATEMENT_DATE).orEmpty(),
                    description = entry.note?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.ledger_entry),
                    amount = "Rs. %,.2f".format(entry.amount)
                )
            },
            netLabel = stringResource(R.string.ledger_statement_net),
            netAmount = "Rs. %,.2f".format(c.outstanding)
        )

        // Preview + share
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ledger_statement_preview),
                        style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
                        color = UI.colors.gray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = statement.period,
                        style = ivyInter(12.sp),
                        color = UI.colors.gray
                    )
                }
            }

            if (lines.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ledger_no_entries),
                        style = ivyInter(15.sp),
                        color = UI.colors.gray,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(lines) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = entry.date?.atZone(ZoneId.systemDefault())
                                ?.format(STATEMENT_DATE).orEmpty(),
                            style = ivyInter(13.sp),
                            color = UI.colors.gray,
                            modifier = Modifier.width(88.dp)
                        )
                        Text(
                            text = entry.note?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.ledger_entry),
                            style = ivyInter(14.sp, FontWeight.Medium),
                            color = UI.colors.pureInverse,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Rs. %,.2f".format(entry.amount),
                            style = ivyInter(14.sp, FontWeight.Bold),
                            color = if (entry.type == LoanRecordType.INCREASE) {
                                Color(0xFFFF9F0A)
                            } else {
                                Color(0xFF30D158)
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "${statement.netLabel}: ${statement.netAmount}",
                    style = ivyInter(15.sp, FontWeight.Bold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ledger_share_pdf),
                        style = ivyInter(15.sp, FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF5E5CE6), RoundedCornerShape(14.dp))
                            .clickable {
                                sharing = true
                                shareStatement(context, statement, c.name)
                            }
                            .padding(horizontal = 18.dp, vertical = 11.dp)
                    )
                    if (sharing) {
                        Text(
                            text = stringResource(R.string.ledger_statement_preparing),
                            style = ivyInter(13.sp),
                            color = UI.colors.gray,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

private enum class StatementRange { MONTH, THREE_MONTHS, CUSTOM }

/** Resolves the picker range into concrete dates. */
private fun StatementRange.bounds(
    customFrom: LocalDate?,
    customTo: LocalDate?,
): Pair<LocalDate?, LocalDate?> = when (this) {
    StatementRange.MONTH -> {
        val today = LocalDate.now()
        today.withDayOfMonth(1) to today
    }
    StatementRange.THREE_MONTHS -> {
        val today = LocalDate.now()
        today.minusMonths(3) to today
    }
    StatementRange.CUSTOM -> customFrom to customTo
}

@Composable
private fun StatementRangeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = ivyInter(13.sp, FontWeight.SemiBold),
        color = if (selected) Color.White else UI.colors.pureInverse,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (selected) Color(0xFF5E5CE6) else UI.colors.pureInverse.copy(alpha = 0.06f),
                RoundedCornerShape(percent = 50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun periodLabel(from: LocalDate?, to: LocalDate?): String {
    val fmt = DateTimeFormatter.ofPattern("d MMM yyyy")
    return when {
        from != null && to != null -> "${from.format(fmt)} - ${to.format(fmt)}"
        else -> stringResource(R.string.ledger_statement_all_time)
    }
}

/** Renders the PDF and opens the system share sheet. */
private fun shareStatement(
    context: android.content.Context,
    statement: Statement,
    personName: String,
) {
    try {
        val renderer = StatementRenderer()
        val file = renderer.render(
            context = context,
            statement = statement,
            fileName = "statement-" + personName.replace(Regex("[^A-Za-z0-9]"), "_")
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share statement"))
    } catch (e: Exception) {
        // Sharing is best-effort
    }
}
