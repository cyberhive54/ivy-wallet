package com.ivy.main.workspace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ivy.design.l0_system.UI
import com.ivy.domain.split.ShareMode
import com.ivy.domain.split.SplitEngine
import com.ivy.domain.split.SplitParticipant
import com.ivy.legacy.ui.theme.contact.ContactPickButton
import com.ivy.legacy.ui.theme.receipt.ReceiptStorage
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Split expense sheet (P3.S2.M2): payer picker (multi), five share-mode chips,
 * live-computed amounts, category shortlist and receipt attach.
 *
 * The split is recomputed on every input change through [SplitEngine], so the
 * owed lines always sum to the entered amount - validation at save is a no-op.
 */
@Composable
fun SplitExpenseSheet(
    members: List<SplitMember>,
    currency: String,
    categories: List<SplitCategory> = emptyList(),
    onCreateCategory: (String) -> Unit = {},
    onSave: (
        note: String?,
        amount: Double,
        date: Instant,
        categoryId: UUID?,
        attachmentUrl: String?,
        payers: List<Pair<UUID, Double>>,
        shares: List<Triple<UUID, ShareMode, Double?>>,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var note by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var pickedPhone by remember { mutableStateOf<String?>(null) }
    var attachmentUrl by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<UUID?>(null) }
    var newCategoryName by remember { mutableStateOf("") }

    // Payer amounts — default to the first ("You") member covering the whole bill
    val payerAmounts = remember {
        mutableStateMapOf<UUID, Double>().apply {
            members.firstOrNull { it.isYou }?.let { put(it.id, 0.0) }
        }
    }
    // Participants included in the split (default: all active members)
    val included = remember { mutableStateMapOf<UUID, Boolean>().apply { members.forEach { put(it.id, true) } } }
    val modes = remember { mutableStateMapOf<UUID, ShareMode>().apply { members.forEach { put(it.id, ShareMode.EQUAL) } } }
    val shareValues = remember { mutableStateMapOf<UUID, Double>().apply { members.forEach { put(it.id, 0.0) } } }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val participants = members.filter { included[it.id] == true }
    val splitParticipants = participants.map { member ->
        val mode = modes[member.id] ?: ShareMode.EQUAL
        val value = shareValues[member.id] ?: 0.0
        SplitParticipant(
            memberId = member.id,
            mode = mode,
            exactAmount = if (mode == ShareMode.EXACT) BigDecimal.valueOf(value) else null,
            percent = if (mode == ShareMode.PERCENT) value else null,
            shares = if (mode == ShareMode.SHARES) value.toInt().coerceAtLeast(1) else null,
            adjustment = if (mode == ShareMode.ADJUSTMENT) BigDecimal.valueOf(value) else null,
        )
    }
    val owed = if (amount > 0.0 && splitParticipants.isNotEmpty()) {
        SplitEngine.split(BigDecimal.valueOf(amount), splitParticipants)
    } else {
        emptyMap()
    }
    // Keep a single selected payer covering the whole bill as the amount changes.
    // With multiple payers we leave their split amounts untouched.
    LaunchedEffect(amount) {
        if (payerAmounts.size == 1) {
            val only = payerAmounts.keys.first()
            payerAmounts[only] = amount
        }
    }

    val totalPaid = payerAmounts.values.sum()
    val valid = amount > 0.0 && participants.isNotEmpty() && totalPaid > 0.0

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) attachmentUrl = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch { attachmentUrl = ReceiptStorage.saveFrom(context, uri) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(UI.colors.pure)
                .clickable(enabled = false) {}
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.split_add_expense),
                style = ivyInter(20.sp, FontWeight.ExtraBold),
                color = UI.colors.pureInverse
            )
            Spacer(Modifier.height(12.dp))

            // Amount + note
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = currency, style = ivyInter(18.sp, FontWeight.Bold), color = UI.colors.gray)
                Spacer(Modifier.width(8.dp))
                SplitTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    hint = stringResource(R.string.split_amount),
                    modifier = Modifier.weight(1f),
                    textStyle = ivyInter(24.sp, FontWeight.ExtraBold).copy(color = UI.colors.pureInverse),
                    autoFocus = true,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                )
            }
            Spacer(Modifier.height(8.dp))
            SplitTextField(
                value = note,
                onValueChange = { note = it },
                hint = stringResource(R.string.split_expense_note),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.pureInverse.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            )
            Spacer(Modifier.height(14.dp))

            // Contact picker (prefill note/phone — manual entry still works)
            ContactPickButton(onPicked = { picked -> pickedPhone = picked?.phone })
            Spacer(Modifier.height(12.dp))

            // Payers
            SectionLabel(stringResource(R.string.split_paid_by))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(members) { member ->
                    PayerChip(
                        name = member.name,
                        selected = payerAmounts.containsKey(member.id),
                        amount = payerAmounts[member.id] ?: 0.0,
                        currency = currency,
                        onToggle = { on ->
                            if (on) {
                                payerAmounts[member.id] = if (amount > 0.0) amount else 0.0
                            } else {
                                payerAmounts.remove(member.id)
                            }
                        },
                        onAmount = { value -> payerAmounts[member.id] = value },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // Participants + mode chips + live owed amounts
            SectionLabel(stringResource(R.string.split_split_between))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(UI.colors.pureInverse.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                participants.forEach { member ->
                    val mode = modes[member.id] ?: ShareMode.EQUAL
                    val value = shareValues[member.id] ?: 0.0
                    val owedAmount = owed[member.id]?.toDouble() ?: 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name + if (member.isYou) " (You)" else "",
                            style = ivyInter(14.sp, FontWeight.SemiBold),
                            color = UI.colors.pureInverse,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "%,.2f".format(owedAmount),
                            style = ivyInter(14.sp, FontWeight.Bold),
                            color = ExpandDesign.Accent
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ShareMode.entries.forEach { option ->
                            val selected = mode == option
                            Text(
                                text = stringResource(
                                    when (option) {
                                        ShareMode.EQUAL -> R.string.split_mode_equal
                                        ShareMode.EXACT -> R.string.split_mode_exact
                                        ShareMode.PERCENT -> R.string.split_mode_percent
                                        ShareMode.SHARES -> R.string.split_mode_shares
                                        ShareMode.ADJUSTMENT -> R.string.split_mode_adjustment
                                    }
                                ),
                                style = ivyInter(10.sp, FontWeight.SemiBold),
                                color = if (selected) Color.White else UI.colors.gray,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(
                                        if (selected) ExpandDesign.Accent else UI.colors.pureInverse.copy(alpha = 0.06f),
                                        RoundedCornerShape(percent = 50)
                                    )
                                    .clickable { modes[member.id] = option }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (mode != ShareMode.EQUAL && mode != ShareMode.SHARES && mode != ShareMode.PERCENT) {
                        Spacer(Modifier.height(6.dp))
                        BasicTextField(
                            value = if (value == 0.0) "" else value.toString(),
                            onValueChange = { text ->
                                shareValues[member.id] = text.toDoubleOrNull() ?: 0.0
                            },
                            singleLine = true,
                            textStyle = ivyInter(13.sp).copy(color = UI.colors.pureInverse),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(UI.colors.pureInverse.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    } else if (mode == ShareMode.SHARES || mode == ShareMode.PERCENT) {
                        Spacer(Modifier.height(6.dp))
                        BasicTextField(
                            value = if (value == 0.0) "" else value.toString(),
                            onValueChange = { text ->
                                shareValues[member.id] = text.toDoubleOrNull() ?: 0.0
                            },
                            singleLine = true,
                            textStyle = ivyInter(13.sp).copy(color = UI.colors.pureInverse),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(UI.colors.pureInverse.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            Spacer(Modifier.height(12.dp))

            // Category shortlist + "+ New" (global categories are shared)
            SectionLabel(stringResource(R.string.split_category))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    Text(
                        text = category.name,
                        style = ivyInter(12.sp, FontWeight.SemiBold),
                        color = if (selectedCategory == category.id) Color.White else UI.colors.pureInverse,
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (selectedCategory == category.id) ExpandDesign.Accent else UI.colors.pureInverse.copy(alpha = 0.06f),
                                RoundedCornerShape(percent = 50)
                            )
                            .clickable { selectedCategory = if (selectedCategory == category.id) null else category.id }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(ExpandDesign.Accent.copy(alpha = 0.10f), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            singleLine = true,
                            textStyle = ivyInter(12.sp, FontWeight.SemiBold)
                                .copy(color = if (newCategoryName.isEmpty()) UI.colors.gray else ExpandDesign.Accent),
                            modifier = Modifier.width(72.dp),
                            decorationBox = { inner ->
                                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.CenterStart) {
                                    if (newCategoryName.isEmpty()) {
                                        Text("new", style = ivyInter(12.sp), color = UI.colors.gray)
                                    }
                                    inner()
                                }
                            }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "+",
                            style = ivyInter(14.sp, FontWeight.Bold),
                            color = ExpandDesign.Accent,
                            modifier = Modifier.clickable {
                                if (newCategoryName.isNotBlank()) {
                                    onCreateCategory(newCategoryName.trim())
                                    newCategoryName = ""
                                }
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Receipt
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_attachment),
                    contentDescription = stringResource(R.string.split_receipt),
                    tint = UI.colors.pureInverse,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (attachmentUrl == null) stringResource(R.string.split_receipt) else "✓",
                    style = ivyInter(13.sp),
                    color = UI.colors.gray,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Camera",
                    style = ivyInter(12.sp, FontWeight.SemiBold),
                    color = ExpandDesign.Accent,
                    modifier = Modifier.clickable {
                        ReceiptStorage.newCameraUri(context)?.let { (uri, _) -> cameraLauncher.launch(uri) }
                    }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Gallery",
                    style = ivyInter(12.sp, FontWeight.SemiBold),
                    color = ExpandDesign.Accent,
                    modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = ivyInter(15.sp, FontWeight.SemiBold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
                Text(
                    text = stringResource(R.string.split_save_expense),
                    style = ivyInter(15.sp, FontWeight.Bold),
                    color = if (valid) Color.White else UI.colors.gray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (valid) ExpandDesign.Accent else UI.colors.pureInverse.copy(alpha = 0.06f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = valid) {
                            onSave(
                                note.ifBlank { null },
                                amount,
                                Instant.now(),
                                selectedCategory,
                                attachmentUrl,
                                payerAmounts.filter { it.value > 0.0 }.map { it.key to it.value },
                                splitParticipants.map {
                                    Triple(
                                        it.memberId,
                                        it.mode,
                                        when (it.mode) {
                                            ShareMode.EXACT -> it.exactAmount?.toDouble()
                                            ShareMode.PERCENT -> it.percent
                                            ShareMode.SHARES -> it.shares?.toDouble()
                                            ShareMode.ADJUSTMENT -> it.adjustment?.toDouble()
                                            ShareMode.EQUAL -> null
                                        }
                                    )
                                },
                            )
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = ivyInter(12.sp, FontWeight.SemiBold, letterSpacing = 0.5.sp),
        color = UI.colors.gray
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun PayerChip(
    name: String,
    selected: Boolean,
    amount: Double,
    currency: String,
    onToggle: (Boolean) -> Unit,
    onAmount: (Double) -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) ExpandDesign.Accent.copy(alpha = 0.15f) else UI.colors.pureInverse.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (selected) ExpandDesign.Accent else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.clickable { onToggle(!selected) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = ivyInter(13.sp, FontWeight.SemiBold),
                color = if (selected) ExpandDesign.Accent else UI.colors.pureInverse
            )
        }
        if (selected) {
            Spacer(Modifier.height(4.dp))
            SplitTextField(
                value = if (amount == 0.0) "" else amount.toString(),
                onValueChange = { text -> onAmount(text.toDoubleOrNull() ?: 0.0) },
                hint = "0.00 $currency",
                modifier = Modifier.width(70.dp),
                textStyle = ivyInter(12.sp).copy(color = UI.colors.pureInverse),
            )
        }
    }
}
