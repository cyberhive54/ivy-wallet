package com.ivy.main.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.db.dao.read.GroupDao
import com.ivy.data.db.dao.read.GroupMemberDao
import com.ivy.data.db.dao.read.SplitExpenseDao
import com.ivy.data.db.dao.read.SplitExpensePayerDao
import com.ivy.data.db.dao.read.SplitExpenseParticipantDao
import com.ivy.data.db.dao.read.SplitSettlementDao
import com.ivy.data.db.dao.write.WriteGroupDao
import com.ivy.data.db.dao.write.WriteGroupMemberDao
import com.ivy.data.db.entity.GroupEntity
import com.ivy.data.db.entity.GroupMemberEntity
import com.ivy.domain.split.GroupLedger
import com.ivy.domain.split.LedgerExpense
import com.ivy.domain.split.LedgerSettlement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
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
    /** The local user's net balance in this group: positive = they are owed. */
    val youBalance: Double = 0.0,
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
    private val splitExpensePayerDao: SplitExpensePayerDao,
    private val splitExpenseParticipantDao: SplitExpenseParticipantDao,
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

            _state.value = withContext(Dispatchers.IO) {
                val groups = groupDao.findActive()
                val allMembers = groupMemberDao.findAll()
                val allExpenses = splitExpenseDao.findAll()
                val allPayers = splitExpensePayerDao.findAll()
                val allShares = splitExpenseParticipantDao.findAll()
                val allSettlements = splitSettlementDao.findAll()
                val groupNames = groupDao.findAll().associate { it.id to it.name }

                var totalOwedToYou = 0.0
                var totalYouOwe = 0.0

                val splitlyGroups = groups.map { group ->
                    val members = allMembers.filter { it.groupId == group.id }
                    val youMemberId = members.firstOrNull { it.memberIsYou }?.id
                    val expenses = allExpenses.filter { it.groupId == group.id }
                    val expenseModels = expenses.map { expense ->
                        LedgerExpense(
                            id = expense.id,
                            currency = expense.currency,
                            amount = BigDecimal.valueOf(expense.amount),
                            paidBy = allPayers.filter { it.expenseId == expense.id }
                                .ifEmpty {
                                    // Legacy rows only had one payer
                                    listOf(
                                        com.ivy.data.db.entity.SplitExpensePayerEntity(
                                            expenseId = expense.id,
                                            memberId = expense.paidByMemberId,
                                            amount = expense.amount,
                                        )
                                    )
                                }
                                .associate { it.memberId to BigDecimal.valueOf(it.amount) },
                            owed = allShares.filter { it.expenseId == expense.id }
                                .associate { it.memberId to BigDecimal.valueOf(it.owedAmount) },
                        )
                    }
                    val settlementModels = allSettlements.filter { it.groupId == group.id }.map {
                        LedgerSettlement(
                            id = it.id,
                            currency = it.currency,
                            amount = BigDecimal.valueOf(it.amount),
                            fromMemberId = it.fromMemberId ?: it.toMemberId,
                            toMemberId = it.toMemberId,
                        )
                    }
                    val balances = GroupLedger.balances(
                        groupCurrency = group.currency,
                        expenses = expenseModels,
                        settlements = settlementModels,
                    )
                    val youBalance = youMemberId?.let { balances[it]?.toDouble() ?: 0.0 } ?: 0.0
                    if (youBalance > 0) totalOwedToYou += youBalance else totalYouOwe += kotlin.math.abs(youBalance)

                    SplitlyGroup(
                        id = group.id,
                        name = group.name,
                        type = group.type,
                        currency = group.currency,
                        membersCount = members.count { !it.archived },
                        youOwedTotal = kotlin.math.abs(youBalance),
                        youBalance = youBalance,
                    )
                }

                val activity = buildList {
                    allExpenses.forEach { e ->
                        add(
                            SplitlyActivityItem(
                                id = e.id,
                                groupName = groupNames[e.groupId] ?: "Group",
                                kind = SplitlyActivityKind.EXPENSE,
                                label = e.note?.takeIf { it.isNotBlank() } ?: "Expense",
                                amount = e.amount,
                                currency = e.currency,
                                time = e.date,
                            )
                        )
                    }
                    allSettlements.forEach { s ->
                        add(
                            SplitlyActivityItem(
                                id = s.id,
                                groupName = groupNames[s.groupId] ?: "Group",
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
                    youOwedTotal = totalYouOwe,
                    youLentTotal = totalOwedToYou,
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
                        createdAt = Instant.now(),
                    )
                )
            }
            refresh()
        }
    }
}
