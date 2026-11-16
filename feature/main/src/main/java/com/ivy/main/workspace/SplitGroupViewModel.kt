package com.ivy.main.workspace

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.db.dao.read.GroupDao
import com.ivy.data.db.dao.read.GroupMemberDao
import com.ivy.data.db.dao.read.SplitCommentDao
import com.ivy.data.db.dao.read.SplitExpenseDao
import com.ivy.data.db.dao.read.SplitExpensePayerDao
import com.ivy.data.db.dao.read.SplitExpenseParticipantDao
import com.ivy.data.db.dao.read.SplitSettlementDao
import com.ivy.data.db.dao.write.WriteGroupDao
import com.ivy.data.db.dao.write.WriteGroupMemberDao
import com.ivy.data.db.dao.write.WriteSplitCommentDao
import com.ivy.data.db.dao.write.WriteSplitExpenseDao
import com.ivy.data.db.dao.write.WriteSplitExpensePayerDao
import com.ivy.data.db.dao.write.WriteSplitExpenseParticipantDao
import com.ivy.data.db.entity.GroupEntity
import com.ivy.data.db.entity.GroupMemberEntity
import com.ivy.data.db.entity.SplitCommentEntity
import com.ivy.data.db.entity.SplitExpenseEntity
import com.ivy.data.db.entity.SplitExpensePayerEntity
import com.ivy.data.db.entity.SplitExpenseParticipantEntity
import com.ivy.domain.split.GroupLedger
import com.ivy.domain.split.LedgerExpense
import com.ivy.domain.split.LedgerSettlement
import com.ivy.domain.split.Payer
import com.ivy.domain.split.ShareMode
import com.ivy.domain.split.SplitEngine
import com.ivy.domain.split.SplitParticipant
import com.ivy.domain.split.SplitSettlementUseCase
import com.ivy.domain.split.Transfer
import com.ivy.legacy.utils.ioThread
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

/** A member as shown in the group UI. */
data class SplitMember(
    val id: UUID,
    val name: String,
    val phone: String?,
    val isYou: Boolean,
    val orderNum: Double,
    val archived: Boolean,
    val joinedAt: Instant?,
)

/** A participant's owed line for an expense. */
data class SplitShare(
    val memberId: UUID,
    val mode: ShareMode,
    val value: Double?,
    val owed: Double,
)

/** A payer's contribution for an expense. */
data class SplitPayer(
    val memberId: UUID,
    val amount: Double,
)

/** One group expense with its participants and payers. */
data class SplitExpense(
    val id: UUID,
    val note: String?,
    val amount: Double,
    val currency: String,
    val date: Instant,
    val categoryId: UUID?,
    val attachmentUrl: String?,
    val settled: Boolean,
    val payers: List<SplitPayer>,
    val shares: List<SplitShare>,
)

/** A comment on a group expense. */
data class SplitComment(
    val id: UUID,
    val expenseId: UUID,
    val authorMemberId: UUID,
    val authorName: String,
    val text: String,
    val createdAt: Instant,
    val visibility: String,
)

/** The full state of the group detail screen. */
data class SplitGroupState(
    val loading: Boolean = true,
    val group: GroupEntity? = null,
    val members: List<SplitMember> = emptyList(),
    val expenses: List<SplitExpense> = emptyList(),
    val comments: List<SplitComment> = emptyList(),
    val balances: Map<UUID, Double> = emptyMap(),
    val youMemberId: UUID? = null,
    /** The simplified transfer plan, computed on demand. */
    val plan: List<Transfer> = emptyList(),
    val accountId: UUID? = null,
    val accountCurrency: String = "INR",
) {
    val isEmpty: Boolean get() = !loading && expenses.isEmpty()
    val youBalance: Double get() = youMemberId?.let { balances[it] ?: 0.0 } ?: 0.0
}

/**
 * Group detail: members, expenses, per-member balances, simplify / settle-up,
 * comments and group/member CRUD.
 */
@HiltViewModel
class SplitGroupViewModel @Inject constructor(
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val splitExpenseDao: SplitExpenseDao,
    private val splitExpenseParticipantDao: SplitExpenseParticipantDao,
    private val splitExpensePayerDao: SplitExpensePayerDao,
    private val splitSettlementDao: SplitSettlementDao,
    private val splitCommentDao: SplitCommentDao,
    private val groupWriter: WriteGroupDao,
    private val groupMemberWriter: WriteGroupMemberDao,
    private val expenseWriter: WriteSplitExpenseDao,
    private val participantWriter: WriteSplitExpenseParticipantDao,
    private val payerWriter: WriteSplitExpensePayerDao,
    private val commentWriter: WriteSplitCommentDao,
    private val settlementUseCase: SplitSettlementUseCase,
    private val accountsAct: com.ivy.wallet.domain.action.account.AccountsAct,
    @ApplicationContext
    private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SplitGroupState())
    val state: StateFlow<SplitGroupState> = _state.asStateFlow()

    private var currentGroupId: UUID? = null

    fun load(groupId: UUID) {
        currentGroupId = groupId
        refresh()
    }

    fun refresh() {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            val accounts = accountsAct(Unit)
            val fallbackAccount = accounts.firstOrNull { it.includeInBalance }
            _state.value = _state.value.copy(
                accountId = fallbackAccount?.id,
                accountCurrency = fallbackAccount?.currency ?: "INR",
            )
            _state.value = withContext(Dispatchers.IO) {
                val group = groupDao.findById(groupId) ?: return@withContext SplitGroupState()

                val allMembers = groupMemberDao.findByGroupId(groupId)
                val activeMembers = allMembers.filter { !it.archived }
                val youMemberId = activeMembers.firstOrNull { it.memberIsYou }?.id
                    ?: allMembers.firstOrNull { it.memberIsYou }?.id

                val expenses = splitExpenseDao.findByGroupId(groupId)
                val payers = splitExpensePayerDao.findAll()
                val shares = splitExpenseParticipantDao.findAll()
                val settlements = splitSettlementDao.findByGroupId(groupId)

                val expenseModels = expenses.map { expense ->
                    SplitExpense(
                        id = expense.id,
                        note = expense.note,
                        amount = expense.amount,
                        currency = expense.currency,
                        date = expense.date,
                        categoryId = expense.categoryId,
                        attachmentUrl = expense.attachmentUrl,
                        settled = expense.settled,
                        payers = payers.filter { it.expenseId == expense.id }.map {
                            SplitPayer(it.memberId, it.amount)
                        }.ifEmpty {
                            // Legacy rows only had one payer
                            listOf(SplitPayer(expense.paidByMemberId, expense.amount))
                        },
                        shares = shares.filter { it.expenseId == expense.id }.map {
                            SplitShare(
                                memberId = it.memberId,
                                mode = runCatching { ShareMode.valueOf(it.shareMode) }
                                    .getOrDefault(ShareMode.EQUAL),
                                value = it.shareValue,
                                owed = it.owedAmount,
                            )
                        },
                    )
                }

                val ledgerExpenses = expenseModels.map { expense ->
                    LedgerExpense(
                        id = expense.id,
                        currency = expense.currency,
                        amount = BigDecimal.valueOf(expense.amount),
                        paidBy = expense.payers.associate {
                            it.memberId to BigDecimal.valueOf(it.amount)
                        },
                        owed = expense.shares.associate {
                            it.memberId to BigDecimal.valueOf(it.owed)
                        },
                    )
                }
                val ledgerSettlements = settlements.map {
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
                    expenses = ledgerExpenses,
                    settlements = ledgerSettlements,
                ).mapValues { (_, value) -> value.toDouble() }

                val memberNames: Map<UUID, String> = allMembers.associate { member -> member.id to member.memberName }
                val comments = splitCommentDao.findAll()
                    .filter { comment -> expenses.any { it.id == comment.expenseId } }
                    .map {
                        SplitComment(
                            id = it.id,
                            expenseId = it.expenseId,
                            authorMemberId = it.authorMemberId,
                            authorName = memberNames[it.authorMemberId] ?: "Member",
                            text = it.text,
                            createdAt = it.createdAt,
                            visibility = it.visibility,
                        )
                    }
                    .sortedByDescending { it.createdAt }

                SplitGroupState(
                    loading = false,
                    group = group,
                    members = allMembers.sortedBy { it.orderNum }.map {
                        SplitMember(
                            id = it.id,
                            name = it.memberName,
                            phone = it.memberPhone,
                            isYou = it.memberIsYou,
                            orderNum = it.orderNum,
                            archived = it.archived,
                            joinedAt = it.createdAt,
                        )
                    },
                    expenses = expenseModels.sortedByDescending { it.date },
                    comments = comments,
                    balances = balances,
                    youMemberId = youMemberId,
                    accountId = _state.value.accountId,
                    accountCurrency = _state.value.accountCurrency,
                )
            }
        }
    }

    /** Computes the minimal transfer plan for the current balances. */
    fun buildPlan() {
        viewModelScope.launch {
            val s = _state.value
            val net = s.balances
                .filterValues { kotlin.math.abs(it) > 0.009 }
                .mapValues { (_, v) -> BigDecimal.valueOf(v) }
            val plan = SplitEngine.simplify(net)
            _state.value = s.copy(plan = plan)
        }
    }

    /** Clears the displayed plan. */
    fun dismissPlan() {
        _state.value = _state.value.copy(plan = emptyList())
    }

    /**
     * Applies the current plan: writes ledger settlements + real transactions,
     * then refreshes the balances (which should now be zero).
     */
    fun applyPlan() {
        val s = _state.value
        val group = s.group ?: return
        val accountId = s.accountId ?: return
        val plan = s.plan
        if (plan.isEmpty()) return
        viewModelScope.launch {
            val memberNames = s.members.associate { it.id to it.name }
            withContext(Dispatchers.IO) {
                settlementUseCase.apply(
                    groupId = group.id,
                    groupName = group.name,
                    currency = group.currency,
                    transfers = plan,
                    memberNames = memberNames,
                    localMemberId = s.youMemberId ?: UUID(0, 0),
                    accountId = accountId,
                    assetCode = s.accountCurrency,
                )
            }
            _state.value = s.copy(plan = emptyList())
            refresh()
        }
    }

    /** Saves a new group expense with its payers and share rows. */
    fun saveExpense(
        note: String?,
        amount: Double,
        currency: String,
        date: Instant,
        categoryId: UUID?,
        attachmentUrl: String?,
        payers: List<Pair<UUID, Double>>,
        shares: List<Triple<UUID, ShareMode, Double?>>,
    ) {
        val groupId = currentGroupId ?: return
        if (amount <= 0.0 || payers.isEmpty() || shares.isEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val participants = shares.map { (memberId, mode, value) ->
                    SplitParticipant(
                        memberId = memberId,
                        mode = mode,
                        exactAmount = if (mode == ShareMode.EXACT) {
                            BigDecimal.valueOf(value ?: 0.0)
                        } else null,
                        percent = if (mode == ShareMode.PERCENT) value else null,
                        shares = if (mode == ShareMode.SHARES) (value?.toInt() ?: 1) else null,
                        adjustment = if (mode == ShareMode.ADJUSTMENT) {
                            BigDecimal.valueOf(value ?: 0.0)
                        } else null,
                    )
                }
                val owed = SplitEngine.split(BigDecimal.valueOf(amount), participants)
                val expense = SplitExpenseEntity(
                    groupId = groupId,
                    paidByMemberId = payers.first().first,
                    amount = amount,
                    currency = currency,
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                    date = date,
                    categoryId = categoryId,
                    attachmentUrl = attachmentUrl,
                    settled = false,
                )
                expenseWriter.save(expense)
                payerWriter.saveMany(
                    payers.map { (memberId, paid) ->
                        SplitExpensePayerEntity(
                            expenseId = expense.id,
                            memberId = memberId,
                            amount = paid,
                        )
                    }
                )
                participantWriter.saveMany(
                    owed.map { (memberId, value) ->
                        val p = participants.first { it.memberId == memberId }
                        SplitExpenseParticipantEntity(
                            expenseId = expense.id,
                            memberId = memberId,
                            shareMode = p.mode.name,
                            shareValue = when (p.mode) {
                                ShareMode.EXACT -> p.exactAmount?.toDouble()
                                ShareMode.PERCENT -> p.percent
                                ShareMode.SHARES -> p.shares?.toDouble()
                                ShareMode.ADJUSTMENT -> p.adjustment?.toDouble()
                                ShareMode.EQUAL -> null
                            },
                            owedAmount = value.toDouble(),
                        )
                    }
                )
            }
            refresh()
        }
    }

    /** Deletes an expense and its related rows + receipt. */
    fun deleteExpense(expense: SplitExpense) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                com.ivy.legacy.ui.theme.receipt.ReceiptStorage.delete(context, expense.attachmentUrl)
                participantWriter.deleteByExpenseId(expense.id)
                splitCommentDao.findByExpenseId(expense.id).forEach {
                    commentWriter.deleteById(it.id)
                }
                payerWriter.deleteByExpenseId(expense.id)
                expenseWriter.deleteById(expense.id)
            }
            refresh()
        }
    }

    /** Adds a comment to an expense. */
    fun addComment(expenseId: UUID, text: String, visibility: String) {
        val s = _state.value
        val authorId = s.youMemberId ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                commentWriter.save(
                    SplitCommentEntity(
                        expenseId = expenseId,
                        authorMemberId = authorId,
                        text = text.trim(),
                        createdAt = Instant.now(),
                        visibility = visibility,
                    )
                )
            }
            refresh()
        }
    }

    /** Adds a member to the group. */
    fun addMember(name: String, phone: String?) {
        val groupId = currentGroupId ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val members = groupMemberDao.findByGroupId(groupId)
                val nextOrder = (members.maxOfOrNull { it.orderNum } ?: -1.0) + 1.0
                groupMemberWriter.save(
                    GroupMemberEntity(
                        groupId = groupId,
                        memberName = name.trim(),
                        memberPhone = phone?.trim()?.takeIf { it.isNotBlank() },
                        memberIsYou = false,
                        orderNum = nextOrder,
                        createdAt = Instant.now(),
                    )
                )
            }
            refresh()
        }
    }

    /** Moves a member up/down in the ordering. */
    fun reorderMember(memberId: UUID, delta: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val members = groupMemberDao.findByGroupId(currentGroupId ?: return@withContext)
                    .sortedBy { it.orderNum }
                val index = members.indexOfFirst { it.id == memberId }
                val target = index + delta
                if (index < 0 || target < 0 || target >= members.size) return@withContext
                val swapped = members.toMutableList().apply {
                    val tmp = this[index]
                    this[index] = this[target]
                    this[target] = tmp
                }
                swapped.forEachIndexed { i, member ->
                    groupMemberWriter.save(member.copy(orderNum = i.toDouble()))
                }
            }
            refresh()
        }
    }

    /** "Leave group" = archive the member row (history is preserved). */
    fun leaveGroup() {
        val s = _state.value
        val memberId = s.youMemberId ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val member = groupMemberDao.findById(memberId) ?: return@withContext
                groupMemberWriter.save(member.copy(archived = true))
            }
            refresh()
        }
    }

    /** Archives (or restores) a member row. */
    fun setMemberArchived(memberId: UUID, archived: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val member = groupMemberDao.findById(memberId) ?: return@withContext
                groupMemberWriter.save(member.copy(archived = archived))
            }
            refresh()
        }
    }

    /** Edits group name / type / currency. */
    fun updateGroup(name: String, type: String, currency: String) {
        val groupId = currentGroupId ?: return
        if (name.isBlank() || currency.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val group = groupDao.findById(groupId) ?: return@withContext
                groupWriter.save(
                    group.copy(
                        name = name.trim(),
                        type = type,
                        currency = currency.trim().uppercase(),
                    )
                )
            }
            refresh()
        }
    }

    /** Archives the whole group (hidden from the list, data preserved). */
    fun archiveGroup() {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val group = groupDao.findById(groupId) ?: return@withContext
                groupWriter.save(group.copy(archived = true))
            }
            refresh()
        }
    }
}
