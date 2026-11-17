package com.ivy.main.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.ivy.design.l0_system.UI
import com.ivy.domain.split.Transfer
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter

private val GROUP_TYPES = listOf("TRIP", "HOME", "COUPLE", "OTHER")

/** Group create/edit sheet (P3.S2.M1): name, type chips, currency picker. */
@Composable
fun SplitGroupEditSheet(
    group: com.ivy.data.db.entity.GroupEntity?,
    onSave: (String, String, String) -> Unit,
    onArchive: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var type by remember { mutableStateOf(group?.type ?: "TRIP") }
    var currency by remember { mutableStateOf(group?.currency ?: "INR") }

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
                text = stringResource(R.string.edit_group),
                style = ivyInter(20.sp, FontWeight.ExtraBold),
                color = UI.colors.pureInverse
            )
            Spacer(Modifier.height(14.dp))

            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.pureInverse.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.split_group_type),
                style = ivyInter(12.sp, FontWeight.SemiBold),
                color = UI.colors.gray
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                GROUP_TYPES.forEach { option ->
                    val selected = type == option
                    Text(
                        text = option,
                        style = ivyInter(12.sp, FontWeight.SemiBold),
                        color = if (selected) Color.White else UI.colors.pureInverse,
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (selected) ExpandDesign.Accent else UI.colors.pureInverse.copy(alpha = 0.06f),
                                RoundedCornerShape(percent = 50)
                            )
                            .clickable { type = option }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.split_group_currency),
                style = ivyInter(12.sp, FontWeight.SemiBold),
                color = UI.colors.gray
            )
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = currency,
                onValueChange = { currency = it.uppercase() },
                singleLine = true,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.pureInverse.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = ivyInter(14.sp, FontWeight.SemiBold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
                if (group != null) {
                    Text(
                        text = stringResource(R.string.split_archive_group),
                        style = ivyInter(14.sp, FontWeight.SemiBold),
                        color = ExpandDesign.Danger,
                        modifier = Modifier.clickable(onClick = onArchive)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.save),
                    style = ivyInter(14.sp, FontWeight.Bold),
                    color = if (name.isNotBlank() && currency.isNotBlank()) Color.White else UI.colors.gray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (name.isNotBlank() && currency.isNotBlank()) ExpandDesign.Accent
                            else UI.colors.pureInverse.copy(alpha = 0.06f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = name.isNotBlank() && currency.isNotBlank()) {
                            onSave(name, type, currency)
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Simplify plan sheet (P3.S3.M2): shows minimal transfers + apply. */
@Composable
fun SplitPlanSheet(
    state: SplitGroupState,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val memberNames = state.members.associate { it.id to it.name }
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
                text = stringResource(R.string.split_plan_title),
                style = ivyInter(20.sp, FontWeight.ExtraBold),
                color = UI.colors.pureInverse
            )
            Spacer(Modifier.height(12.dp))

            if (state.plan.isEmpty()) {
                Text(
                    text = stringResource(R.string.split_plan_empty),
                    style = ivyInter(15.sp),
                    color = UI.colors.gray
                )
            } else {
                state.plan.forEach { transfer ->
                    SplitPlanRow(
                        transfer = transfer,
                        fromName = memberNames[transfer.fromMemberId] ?: "Member",
                        toName = memberNames[transfer.toMemberId] ?: "Member",
                        currency = state.group?.currency ?: "INR",
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = ivyInter(14.sp, FontWeight.SemiBold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.split_plan_apply),
                    style = ivyInter(14.sp, FontWeight.Bold),
                    color = if (state.plan.isNotEmpty()) Color.White else UI.colors.gray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (state.plan.isNotEmpty()) ExpandDesign.Accent else UI.colors.pureInverse.copy(alpha = 0.06f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = state.plan.isNotEmpty(), onClick = onApply)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SplitPlanRow(
    transfer: Transfer,
    fromName: String,
    toName: String,
    currency: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.split_plan_line, fromName, toName),
            style = ivyInter(14.sp, FontWeight.Medium),
            color = UI.colors.pureInverse,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%,.2f".format(transfer.amount.toDouble()) + " $currency",
            style = ivyInter(14.sp, FontWeight.Bold),
            color = ExpandDesign.Accent
        )
    }
}
