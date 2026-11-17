package com.ivy.main.workspace

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.ivy.design.l0_system.UI
import com.ivy.legacy.data.model.MainTab
import com.ivy.legacy.ivyWalletCtx
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter

/**
 * Splitly workspace — in-dock landing (dock slot "Home"). Headline balance
 * strip + recent groups; footer jumps to the Groups dock tab.
 */
@Composable
fun BoxWithConstraintsScope.SplitlyLanding() {
    val ivyContext = ivyWalletCtx()
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
            text = stringResource(R.string.splitly),
            style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(10.dp))

        // Single authoritative balance band (DESIGN.md)
        ExpandBalanceBand(
            receiveLabel = stringResource(R.string.splitly_they_owe),
            receiveAmount = state.youLentTotal,
            payLabel = stringResource(R.string.splitly_you_owe),
            payAmount = state.youOwedTotal,
            currency = "",
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        ExpandSectionTitle(
            text = stringResource(R.string.splitly_recent_groups),
            modifier = Modifier.padding(horizontal = 20.dp),
            action = stringResource(R.string.splitly_see_all),
            onAction = { ivyContext.selectMainTab(MainTab.SPLITLY_GROUPS) },
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
        ) {
            if (state.isEmpty) {
                item {
                    ExpandEmptyState(
                        glyph = "+",
                        title = stringResource(R.string.splitly_empty_title),
                        subtitle = stringResource(R.string.splitly_empty_subtitle),
                        actionLabel = stringResource(R.string.splitly_go_create),
                        onAction = { ivyContext.selectMainTab(MainTab.SPLITLY_GROUPS) },
                    )
                }
            } else {
                items(state.groups.take(5)) { group ->
                    SplitlyGroupRow(group = group)
                }
            }
        }
    }
}

/**
 * Splitly "Groups" dock tab — the full group list (create is always available
 * at the bottom, above the dock). Tap a row to open the group (P3 scope).
 */
@Composable
fun BoxWithConstraintsScope.SplitlyGroups() {
    val viewModel: SplitlyWorkspaceViewModel = screenScopedViewModel()
    val state by viewModel.state.collectAsState()
    var newGroupName by remember { mutableStateOf("") }

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
            text = stringResource(R.string.groups),
            style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp)
        ) {
            if (state.isEmpty) {
                item {
                    EmptySplitlyState(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        onCreate = {
                            if (newGroupName.isNotBlank()) {
                                viewModel.createGroup(newGroupName)
                                newGroupName = ""
                            }
                        },
                        buttonLabel = stringResource(R.string.splitly_create),
                    )
                }
            } else {
                items(state.groups) { group ->
                    SplitlyGroupRow(group = group)
                }
            }
        }

        // Create bar pinned above the dock
        if (!state.isEmpty) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 116.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    singleLine = true,
                    textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.CenterStart) {
                            if (newGroupName.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.splitly_group_name_hint),
                                    style = ivyInter(16.sp),
                                    color = UI.colors.gray
                                )
                            }
                            inner()
                        }
                    }
                )
                FilledTonalButton(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            viewModel.createGroup(newGroupName)
                            newGroupName = ""
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ExpandDesign.Accent.copy(alpha = 0.16f),
                        contentColor = ExpandDesign.Accent
                    )
                ) {
                    Text(
                        text = stringResource(R.string.splitly_create),
                        style = ivyInter(14.sp, FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitlyTotal(
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
private fun EmptySplitlyState(
    value: String,
    onValueChange: ((String) -> Unit)?,
    onCreate: () -> Unit,
    buttonLabel: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.splitly_empty_title),
            style = ivyInter(17.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.splitly_empty_subtitle),
            style = ivyInter(15.sp),
            color = UI.colors.gray
        )
        if (onValueChange == null) {
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = onCreate,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = ExpandDesign.Accent.copy(alpha = 0.16f),
                    contentColor = ExpandDesign.Accent
                )
            ) {
                Text(
                    text = buttonLabel,
                    style = ivyInter(15.sp, FontWeight.SemiBold)
                )
            }
            return@Column
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                decorationBox = { inner ->
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.splitly_group_name_hint),
                                style = ivyInter(16.sp),
                                color = UI.colors.gray
                            )
                        }
                        inner()
                    }
                }
            )
            FilledTonalButton(
                onClick = onCreate,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = ExpandDesign.Accent.copy(alpha = 0.16f),
                    contentColor = ExpandDesign.Accent
                )
            ) {
                Text(
                    text = buttonLabel,
                    style = ivyInter(15.sp, FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun SplitlyGroupRow(
    group: com.ivy.main.workspace.SplitlyGroup,
) {
    val nav = com.ivy.navigation.navigation()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                nav.navigateTo(com.ivy.navigation.SplitGroupScreen(groupId = group.id))
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar: first letter(s) of group name
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .width(44.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(ExpandDesign.Accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = group.name.trim().takeIf { it.isNotEmpty() }?.take(2)?.uppercase() ?: "??",
                style = ivyInter(15.sp, FontWeight.Bold),
                color = ExpandDesign.Accent
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = ivyInter(16.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${group.membersCount} ${stringResource(R.string.splitly_members)} · ${group.currency}",
                style = ivyInter(12.sp),
                color = UI.colors.gray
            )
        }
        Text(
            text = "${if (group.youBalance >= 0) "+" else ""}%,.2f".format(group.youBalance),
            style = ivyInter(15.sp, FontWeight.SemiBold),
            color = if (group.youBalance > 0) {
                ExpandDesign.Receive
            } else if (group.youBalance < 0) {
                ExpandDesign.Pay
            } else {
                UI.colors.gray
            }
        )
    }
}
