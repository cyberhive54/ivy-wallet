package com.ivy.main.workspace

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.collectAsState
import com.ivy.base.model.LoanRecordType
import com.ivy.design.l0_system.UI
import com.ivy.legacy.ui.theme.receipt.ReceiptStorage
import com.ivy.navigation.LedgerCustomerDetailScreen
import com.ivy.navigation.LedgerStatementScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ENTRY_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")

/**
 * Customer detail (ledger): name + totals, the last entries (statement preview),
 * the receipts and the action row (WhatsApp / Call / Statement / Delete).
 */
@Composable
fun BoxWithConstraintsScope.LedgerCustomerDetail(screen: LedgerCustomerDetailScreen) {
    val nav = navigation()
    val context = LocalContext.current
    val viewModel: LedgerCustomerDetailViewModel = screenScopedViewModel()
    val customer by viewModel.customer.collectAsState()
    val fallbackAccountId by viewModel.fallbackAccountId.collectAsState()
    val current = customer
    var confirmDelete by remember { mutableStateOf(false) }
    var showSettle by remember { mutableStateOf(false) }

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
                text = customer?.name ?: stringResource(R.string.ledger),
                style = ivyInter(22.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
                color = UI.colors.pureInverse
            )
        }

        if (current == null) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.ledger_customer_not_found),
                style = ivyInter(15.sp),
                color = UI.colors.gray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            // Avatar + totals
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ExpandDesign.Accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = current.name.trim().takeIf { it.isNotEmpty() }
                                ?.take(2)?.uppercase() ?: "?",
                            style = ivyInter(20.sp, FontWeight.Bold),
                            color = ExpandDesign.Accent
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(
                                if (current.youOwe) R.string.ledger_you_owe_else
                                else R.string.ledger_they_owe_you
                            ),
                            style = ivyInter(13.sp, FontWeight.Medium),
                            color = UI.colors.gray
                        )
                        Text(
                            text = "â‚¹%,.0f".format(current.outstanding),
                            style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
                            color = if (current.youOwe) ExpandDesign.Pay else ExpandDesign.Receive
                        )
                    }
                }
            }

            // Totals strip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailStat(
                        label = stringResource(R.string.ledger_stat_given),
                        amount = current.lent
                    )
                    DetailStat(
                        label = stringResource(R.string.ledger_stat_received),
                        amount = current.received
                    )
                    DetailStat(
                        label = stringResource(R.string.ledger_stat_entries),
                        amount = current.entries.size.toDouble()
                    )
                }
            }

            // Action row: WhatsApp / Call / Statement / Delete
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionChip(
                        icon = R.drawable.ic_vue_money_send,
                        label = stringResource(R.string.ledger_action_whatsapp),
                        onClick = {
                            val phone = current.phone?.replace(Regex("[^0-9+]"), "") ?: ""
                            val text = Uri.encode(
                                "Hi ${current.name}, regarding our ledger entry of â‚¹%,.0f."
                                    .format(current.outstanding)
                            )
                            val url = if (phone.isNotBlank()) {
                                "https://wa.me/$phone?text=$text"
                            } else {
                                "https://wa.me/?text=$text"
                            }
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                )
                            }
                        }
                    )
                    ActionChip(
                        icon = R.drawable.ic_vue_pc_phone,
                        label = stringResource(R.string.ledger_action_call),
                        onClick = {
                            val phone = current.phone?.replace(Regex("[^0-9+]"), "") ?: ""
                            if (phone.isNotBlank()) {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    )
                                }
                            }
                        }
                    )
                    ActionChip(
                        icon = R.drawable.ic_vue_edu_book,
                        label = stringResource(R.string.ledger_action_statement),
                        onClick = {
                            nav.navigateTo(LedgerStatementScreen(loanId = current.id))
                        }
                    )
                    ActionChip(
                        icon = R.drawable.ic_check,
                        label = stringResource(R.string.ledger_action_settle),
                        onClick = { showSettle = true }
                    )
                    ActionChip(
                        icon = R.drawable.ic_vue_main_trash,
                        label = stringResource(R.string.ledger_action_delete),
                        danger = true,
                        onClick = { confirmDelete = true }
                    )
                }
            }

            // Statement preview (last entries)
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    text = stringResource(R.string.ledger_statement_preview),
                    style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
                    color = UI.colors.gray
                )
            }

            if (current.entries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ledger_no_entries),
                        style = ivyInter(15.sp),
                        color = UI.colors.gray,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(current.entries.take(20)) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.note?.takeIf { it.isNotBlank() }
                                    ?: stringResource(R.string.ledger_entry),
                                style = ivyInter(15.sp, FontWeight.SemiBold),
                                color = UI.colors.pureInverse,
                                maxLines = 1
                            )
                            Text(
                                text = entry.date?.atZone(ZoneId.systemDefault())
                                    ?.format(ENTRY_DATE_FORMAT).orEmpty(),
                                style = ivyInter(12.sp),
                                color = UI.colors.gray
                            )
                        }
                        if (entry.receiptPath != null) {
                            val receiptFile = ReceiptStorage.fileFor(context, entry.receiptPath)
                            if (receiptFile != null && receiptFile.exists()) {
                                coil.compose.AsyncImage(
                                    model = receiptFile,
                                    contentDescription = stringResource(R.string.ledger_receipt_attached),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_attachment),
                                    contentDescription = stringResource(R.string.ledger_receipt_attached),
                                    tint = ExpandDesign.Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = "â‚¹%,.0f".format(entry.amount),
                            style = ivyInter(15.sp, FontWeight.ExtraBold),
                            color = if (entry.type == LoanRecordType.INCREASE) {
                                ExpandDesign.Pay
                            } else {
                                ExpandDesign.Receive
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSettle && current != null) {
        SettleDialog(
            name = current.name,
            amount = current.outstanding,
            youOwe = current.youOwe,
            onConfirm = {
                showSettle = false
                viewModel.settleAll(fallbackAccountId = fallbackAccountId)
                nav.back()
            },
            onDismiss = { showSettle = false }
        )
    }

    if (confirmDelete && current != null) {
        DeleteConfirmDialog(
            title = stringResource(R.string.ledger_delete_customer_title, current.name),
            onConfirm = {
                confirmDelete = false
                viewModel.deleteCustomer(current.id)
                nav.back()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun DetailStat(
    label: String,
    amount: Double,
) {
    Column {
        Text(
            text = label,
            style = ivyInter(11.sp, FontWeight.SemiBold, letterSpacing = 0.4.sp),
            color = UI.colors.gray
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (amount % 1.0 == 0.0) "%,.0f".format(amount) else "%,.2f".format(amount),
            style = ivyInter(17.sp, FontWeight.ExtraBold),
            color = UI.colors.pureInverse
        )
    }
}

@Composable
private fun ActionChip(
    icon: Int,
    label: String,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (danger) {
                    ExpandDesign.Danger.copy(alpha = 0.12f)
                } else {
                    UI.colors.pureInverse.copy(alpha = 0.06f)
                },
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (danger) ExpandDesign.Danger else UI.colors.pureInverse,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = ivyInter(10.sp, FontWeight.SemiBold),
            color = if (danger) ExpandDesign.Danger else UI.colors.pureInverse,
            maxLines = 1
        )
    }
}

@Composable
private fun SettleDialog(
    name: String,
    amount: Double,
    youOwe: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
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
                .padding(horizontal = 40.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(UI.colors.pureInverse, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.ledger_settle_title, name),
                style = ivyInter(17.sp, FontWeight.ExtraBold),
                color = UI.colors.pure
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (youOwe) R.string.ledger_settle_you_pay else R.string.ledger_settle_you_receive,
                    "â‚¹%,.0f".format(amount)
                ),
                style = ivyInter(14.sp),
                color = UI.colors.pure.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = ivyInter(14.sp, FontWeight.SemiBold),
                    color = UI.colors.pure,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(UI.colors.pure.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Text(
                    text = stringResource(R.string.ledger_settle_confirm),
                    style = ivyInter(14.sp, FontWeight.Bold),
                    color = ExpandDesign.Accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExpandDesign.Accent.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
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
                .padding(horizontal = 40.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(UI.colors.pureInverse, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                text = title,
                style = ivyInter(17.sp, FontWeight.ExtraBold),
                color = ExpandDesign.Danger
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = ivyInter(14.sp, FontWeight.SemiBold),
                    color = UI.colors.pure,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(UI.colors.pure.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Text(
                    text = stringResource(R.string.delete),
                    style = ivyInter(14.sp, FontWeight.Bold),
                    color = ExpandDesign.Danger,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExpandDesign.Danger.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
