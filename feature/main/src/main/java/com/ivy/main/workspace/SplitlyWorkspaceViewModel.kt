package com.ivy.main.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.db.dao.read.GroupDao
import com.ivy.data.db.dao.read.GroupMemberDao
import com.ivy.data.db.dao.read.SplitExpenseDao
import com.ivy.data.db.dao.read.SplitSettlementDao
import com.ivy.data.db.dao.write.WriteGroupDao
import com.ivy.data.db.dao.write.WriteGroupMemberDao
import com.ivy.data.db.entity.GroupEntity
import com.ivy.data.db.entity.GroupMemberEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class SplitlyGroup(
    val id: UUID,
    val name: String,
    val type: String,
    val currency: String,
    val membersCount: Int,
    val youOwedTotal: Double,
)

enum class SplitlyActivityKind { EXPENSE, SETTLEMENT }

data class SplitlyActivityItem(
    val id: UUID,
    val groupName: String,
    val kind: SplitlyActivityKind,
    val label: String,
    val amount: Double,
    val currency: String,
    val time: Instant,
)

data class SplitlyState(
    val loading: Boolean = true,
    val groups: List<SplitlyGroup> = emptyList(),
    val youOwedTotal: Double = 0.0,
    val youLentTotal: Double = 0.0,
    val activity: List<SplitlyActivityItem> = emptyList(),
) {
    val isEmpty: Boolean get() = !loading && groups.isEmpty()
    val hasActivity: Boolean get() = activity.isNotEmpty()
}

@HiltViewModel
class SplitlyWorkspaceViewModel @Inject constructor(
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val splitExpenseDao: SplitExpenseDao,
    private val splitSettlementDao: SplitSettlementDao,
    private val groupWriter: WriteGroupDao,
    private val groupMemberWriter: WriteGroupMemberDao,
) : ViewModel() {

    private val _state = MutableStateFlow(SplitlyState())
    val state: StateFlow<SplitlyState> = _state.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = SplitlyState(loading = true)

            _state.value = withContext(Dispatchers.Default) {
                val groups = groupDao.findActive()
                val members = groupMemberDao.findAll()
                val groupNames = groupDao.findAll().associate { it.id to it.name }
                val splitlyGroups = groups.map { g ->
                    SplitlyGroup(
                        id = g.id,
                        name = g.name,
                        type = g.type,
                        currency = g.currency,
                        membersCount = members.count { it.groupId == g.id },
                        youOwedTotal = 0.0,
                    )
                }

                val expenses = splitExpenseDao.findAll()
                val settlements = splitSettlementDao.findAll()
                val activity = buildList {
                    expenses.forEach { e ->
                        add(
                            SplitlyActivityItem(
                                id = e.id,
                                groupName = groupNames[e.groupId] ?: "—",
                                kind = SplitlyActivityKind.EXPENSE,
                                label = e.note?.takeIf { it.isNotBlank() } ?: "Expense",
                                amount = e.amount,
                                currency = e.currency,
                                time = e.date,
                            )
                        )
                    }
                    settlements.forEach { s ->
                        add(
                            SplitlyActivityItem(
                                id = s.id,
                                groupName = groupNames[s.groupId] ?: "—",
                                kind = SplitlyActivityKind.SETTLEMENT,
                                label = "Settlement",
                                amount = s.amount,
                                currency = s.currency,
                                time = s.createdAt,
                            )
                        )
                    }
                }.sortedByDescending { it.time }.take(40)

                SplitlyState(
                    loading = false,
                    groups = splitlyGroups,
                    youOwedTotal = 0.0,
                    youLentTotal = 0.0,
                    activity = activity,
                )
            }
        }
    }

    fun createGroup(name: String, currency: String = "INR") {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val group = GroupEntity(name = trimmed, currency = currency)
                groupWriter.save(group)
                // Always create "You" as the first member so settle-up can attribute money
                groupMemberWriter.save(
                    GroupMemberEntity(
                        groupId = group.id,
                        memberName = "You",
                        memberIsYou = true,
                    )
                )
            }
            refresh()
        }
    }
}
