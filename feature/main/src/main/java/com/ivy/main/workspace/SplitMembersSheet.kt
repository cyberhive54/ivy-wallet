package com.ivy.main.workspace

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.legacy.ui.theme.contact.ContactPickButton
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.time.ZoneId
import java.util.UUID
import java.time.format.DateTimeFormatter

private val MEMBER_DATE = DateTimeFormatter.ofPattern("d MMM yyyy")

/** Members management (P3.S2.M1): add, reorder, archive, leave. */
@Composable
fun SplitMembersSheet(
    state: SplitGroupState,
    onAdd: (String, String?) -> Unit,
    onReorder: (UUID, Int) -> Unit,
    onArchive: (UUID) -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf<String?>(null) }

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
                text = stringResource(R.string.split_members),
                style = ivyInter(20.sp, FontWeight.ExtraBold),
                color = UI.colors.pureInverse
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(state.members.filter { !it.archived }) { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name + if (member.isYou) " (You)" else "",
                                style = ivyInter(15.sp, FontWeight.SemiBold),
                                color = UI.colors.pureInverse
                            )
                            // Membership audit row (P3.S2.M4): joined date shown grey
                            member.joinedAt?.let { joined ->
                                Text(
                                    text = "Joined " + joined.atZone(ZoneId.systemDefault()).format(MEMBER_DATE),
                                    style = ivyInter(10.sp),
                                    color = UI.colors.gray
                                )
                            }
                        }
                        Text(
                            text = "↑",
                            style = ivyInter(18.sp, FontWeight.Bold),
                            color = UI.colors.gray,
                            modifier = Modifier
                                .width(28.dp)
                                .clickable { onReorder(member.id, -1) }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "↓",
                            style = ivyInter(18.sp, FontWeight.Bold),
                            color = UI.colors.gray,
                            modifier = Modifier
                                .width(28.dp)
                                .clickable { onReorder(member.id, 1) }
                        )
                        Spacer(Modifier.width(4.dp))
                        if (!member.isYou) {
                            Text(
                                text = "Remove",
                                style = ivyInter(11.sp),
                                color = Color(0xFFFF6B5B),
                                modifier = Modifier.clickable { onArchive(member.id) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            // Add member
            Row(verticalAlignment = Alignment.CenterVertically) {
                SplitTextField(
                    value = name,
                    onValueChange = { name = it },
                    hint = stringResource(R.string.split_member_name),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(UI.colors.pureInverse.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.split_add_member),
                    style = ivyInter(13.sp, FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF5E5CE6), RoundedCornerShape(10.dp))
                        .clickable(enabled = name.isNotBlank()) {
                            onAdd(name, phone)
                            name = ""
                            phone = null
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            ContactPickButton(onPicked = { picked -> phone = picked?.phone })
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.split_leave_group),
                    style = ivyInter(13.sp, FontWeight.SemiBold),
                    color = Color(0xFFFF6B5B),
                    modifier = Modifier.clickable(onClick = onLeave)
                )
                Text(
                    text = stringResource(R.string.cancel),
                    style = ivyInter(13.sp, FontWeight.SemiBold),
                    color = UI.colors.pureInverse,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
