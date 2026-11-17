package com.ivy.main.workspace

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DAY_FORMAT = DateTimeFormatter.ofPattern("d MMM")

/**
 * Splitly "Activity" dock tab — merged chronological feed of every split
 * expense and settlement across all groups. Read-only in P1.
 */
@Composable
fun BoxWithConstraintsScope.SplitlyActivity() {
    val viewModel: SplitlyWorkspaceViewModel = screenScopedViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(UI.colors.pure)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.activity),
            style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            if (!state.hasActivity) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(ExpandDesign.Accent.copy(alpha = 0.14f))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_activity),
                                contentDescription = null,
                                tint = ExpandDesign.Accent
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.splitly_no_activity_title),
                            style = ivyInter(17.sp, FontWeight.SemiBold),
                            color = UI.colors.pureInverse
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.splitly_no_activity_subtitle),
                            style = ivyInter(15.sp),
                            color = UI.colors.gray
                        )
                    }
                }
            } else {
                items(state.activity) { item ->
                    ActivityRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    item: SplitlyActivityItem,
) {
    val isExpense = item.kind == SplitlyActivityKind.EXPENSE
    val icon = if (isExpense) R.drawable.ic_vue_money_receipt_items else R.drawable.ic_checklist
    val amountColor = if (isExpense) ExpandDesign.Pay else ExpandDesign.Receive
    val dateText = DAY_FORMAT.format(item.time.atZone(ZoneId.systemDefault()))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .width(40.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(ExpandDesign.Accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = ExpandDesign.Accent,
                modifier = Modifier
                    .padding(10.dp)
                    .width(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.groupName,
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse,
                maxLines = 1
            )
            Text(
                text = item.label,
                style = ivyInter(12.sp),
                color = UI.colors.gray,
                maxLines = 1
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹" + "%,.2f".format(item.amount),
                style = ivyInter(15.sp, FontWeight.ExtraBold, letterSpacing = (-0.2).sp),
                color = amountColor
            )
            Text(
                text = dateText,
                style = ivyInter(11.sp),
                color = UI.colors.gray
            )
        }
    }
}
