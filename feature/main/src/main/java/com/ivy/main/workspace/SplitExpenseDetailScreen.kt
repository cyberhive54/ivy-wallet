package com.ivy.main.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.ivy.navigation.SplitExpenseDetailScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DETAIL_DATE = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

/** Expense detail (P3.S2.M3): payer/date/note/participants/owed lines + comments. */
@Composable
fun BoxWithConstraintsScope.SplitExpenseDetailScreen(
    screen: SplitExpenseDetailScreen,
) {
    val nav = navigation()
    val viewModel: SplitGroupViewModel = screenScopedViewModel()
    val state by viewModel.state.collectAsState()

    var commentText by remember { mutableStateOf("") }
    var commentVisibility by remember { mutableStateOf("PUBLIC") }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(screen.groupId) { viewModel.load(screen.groupId) }

    val expense = state.expenses.firstOrNull { it.id == screen.expenseId }
    val currentExpense = expense
    val memberNames = state.members.associate { it.id to it.name }
    val currency = state.group?.currency ?: "INR"
    val comments = state.comments.filter { it.expenseId == screen.expenseId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(UI.colors.pure)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(percent = 50))
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
                text = stringResource(R.string.splitly_expense),
                style = ivyInter(20.sp, FontWeight.ExtraBold),
                color = UI.colors.pureInverse,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.split_delete_expense),
                style = ivyInter(12.sp, FontWeight.SemiBold),
                color = ExpandDesign.Danger,
                modifier = Modifier.clickable { confirmDelete = true }
            )
        }

        if (expense == null) {
            Text(
                text = stringResource(R.string.splitly_no_activity_title),
                style = ivyInter(15.sp),
                color = UI.colors.gray,
                modifier = Modifier.padding(24.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(
                        text = expense.note?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.splitly_expense),
                        style = ivyInter(22.sp, FontWeight.ExtraBold),
                        color = UI.colors.pureInverse
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "%,.2f".format(expense.amount) + " $currency",
                        style = ivyInter(26.sp, FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
                        color = ExpandDesign.Accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = expense.date.atZone(ZoneId.systemDefault()).format(DETAIL_DATE),
                        style = ivyInter(13.sp),
                        color = UI.colors.gray
                    )
                }
            }

            item {
                DetailSectionTitle(stringResource(R.string.split_payers))
            }
            items(expense.payers) { payer ->
                DetailRow(
                    label = memberNames[payer.memberId] ?: "Member",
                    value = "%,.2f".format(payer.amount) + " $currency",
                )
            }

            item {
                Spacer(Modifier.height(12.dp))
                DetailSectionTitle(stringResource(R.string.split_owed))
            }
            items(expense.shares) { share ->
                DetailRow(
                    label = (memberNames[share.memberId] ?: "Member") + " · " + share.mode.name,
                    value = "%,.2f".format(share.owed) + " $currency",
                    valueColor = if (share.owed > 0) ExpandDesign.Pay else ExpandDesign.Receive,
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                DetailSectionTitle(stringResource(R.string.split_comments))
            }
            items(comments) { comment ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Row {
                        Text(
                            text = comment.authorName,
                            style = ivyInter(13.sp, FontWeight.SemiBold),
                            color = UI.colors.pureInverse
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (comment.visibility == "PRIVATE") "🔒" else "",
                            style = ivyInter(11.sp),
                            color = UI.colors.gray
                        )
                    }
                    Text(
                        text = comment.text,
                        style = ivyInter(14.sp),
                        color = UI.colors.pureInverse.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Comment composer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SplitTextField(
                value = commentText,
                onValueChange = { commentText = it },
                hint = stringResource(R.string.split_add_comment),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (commentVisibility == "PRIVATE") R.string.split_comment_private
                    else R.string.split_comment_public
                ),
                style = ivyInter(11.sp, FontWeight.SemiBold),
                color = if (commentVisibility == "PRIVATE") ExpandDesign.Pay else ExpandDesign.Accent,
                modifier = Modifier.clickable {
                    commentVisibility = if (commentVisibility == "PRIVATE") "PUBLIC" else "PRIVATE"
                }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "➤",
                style = ivyInter(18.sp, FontWeight.Bold),
                color = ExpandDesign.Accent,
                modifier = Modifier.clickable(enabled = commentText.isNotBlank()) {
                    viewModel.addComment(expense.id, commentText, commentVisibility)
                    commentText = ""
                }
            )
        }
    }

    if (confirmDelete) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = { confirmDelete = false }),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(UI.colors.pureInverse, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.split_delete_expense),
                    style = ivyInter(17.sp, FontWeight.ExtraBold),
                    color = UI.colors.pure
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = ivyInter(14.sp, FontWeight.SemiBold),
                        color = UI.colors.pure,
                        modifier = Modifier.clickable { confirmDelete = false }
                    )
                    Text(
                        text = stringResource(R.string.split_delete_expense),
                        style = ivyInter(14.sp, FontWeight.Bold),
                        color = ExpandDesign.Danger,
                        modifier = Modifier.clickable {
                            currentExpense?.let { viewModel.deleteExpense(it) }
                            confirmDelete = false
                            nav.back()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSectionTitle(text: String) {
    Text(
        text = text,
        style = ivyInter(12.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
        color = UI.colors.gray,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = UI.colors.pureInverse,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = ivyInter(14.sp),
            color = UI.colors.pureInverse.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        Text(text = value, style = ivyInter(14.sp, FontWeight.Bold), color = valueColor)
    }
}
