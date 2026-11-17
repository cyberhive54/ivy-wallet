package com.ivy.main.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.LoanType
import com.ivy.base.model.LoanRecordType
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.db.dao.read.LoanRecordDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

data class LedgerPerson(
    val id: UUID,
    val name: String,
    val youOwe: Boolean,
    val outstanding: Double,
    /** Entries with a due date in the past. */
    val overdueCount: Int = 0,
    /** Days since the oldest overdue entry. */
    val overdueDays: Int = 0,
    /** Entries due within the next week. */
    val dueSoonCount: Int = 0,
) {
    val settled: Boolean get() = outstanding <= 0.0
    val overdue: Boolean get() = overdueCount > 0
}

data class LedgerSummary(
    val loading: Boolean = true,
    val totalLent: Double = 0.0,       // they owe you
    val totalBorrowed: Double = 0.0,   // you owe them
    /** Only people with an outstanding balance, biggest first (landing preview). */
    val people: List<LedgerPerson> = listOf(),
    /** Everyone ever recorded, outstanding first, settled last (contacts tab). */
    val contacts: List<LedgerPerson> = listOf(),
) {
    val isEmpty: Boolean get() = !loading && people.isEmpty()
}

/**
 * Drives the Ledger workspace landing + contacts tab. Computes net outstanding per loan
 * by summing loan records: INCREASE = more was owed/lent than previously,
 * DECREASE = repaid/forgiven amount. The sign of the net reflects this.
 */
@HiltViewModel
class LedgerWorkspaceViewModel @Inject constructor(
    private val loanDao: LoanDao,
    private val loanRecordDao: LoanRecordDao,
) : ViewModel() {

    companion object {
        /** Entries due within this many days count as "due soon". */
        const val DUE_SOON_DAYS = 7L
    }

    private val _state = MutableStateFlow(LedgerSummary())
    val state: StateFlow<LedgerSummary> = _state.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LedgerSummary(loading = true)
            _state.value = withContext(Dispatchers.Default) {
                val loans = loanDao.findAll()
                if (loans.isEmpty()) {
                    return@withContext LedgerSummary(loading = false)
                }
                val allRecords = loanRecordDao.findAll()
                val now = Instant.now()
                var lent = 0.0
                var borrowed = 0.0

                val everyone = loans.map { loan ->
                    val records = allRecords.filter { it.loanId == loan.id }
                    val increased = records.filter { it.loanRecordType == LoanRecordType.INCREASE }
                        .sumOf { it.amount }
                    val decreased = records.filter { it.loanRecordType == LoanRecordType.DECREASE }
                        .sumOf { it.amount }
                    // Outstanding includes the loan principal (legacy loan.amount);
                    // records are adjustments on top of it (see LoanViewModel).
                    val net = (loan.amount + increased - decreased).coerceAtLeast(0.0)

                    if (net > 0.0) {
                        if (loan.type == LoanType.LEND) lent += net else borrowed += net
                    }

                    // Overdue = entries with a due date in the past
                    val overdueRecords = records.filter { record ->
                        val due = record.recordDueDate ?: return@filter false
                        due.isBefore(now)
                    }
                    val oldestOverdueDays = overdueRecords.mapNotNull { record ->
                        record.recordDueDate?.let { due ->
                            ChronoUnit.DAYS.between(due, now).toInt().coerceAtLeast(0)
                        }
                    }.maxOrNull() ?: 0

                    // Due soon = due within the next 7 days (not yet overdue)
                    val dueSoonCount = records.count { record ->
                        val due = record.recordDueDate ?: return@count false
                        !due.isBefore(now) &&
                            ChronoUnit.DAYS.between(now, due) <= DUE_SOON_DAYS
                    }

                    LedgerPerson(
                        id = loan.id,
                        name = loan.counterpartyName?.takeIf { it.isNotBlank() }
                            ?: loan.name,
                        youOwe = loan.type != LoanType.LEND,
                        outstanding = net,
                        overdueCount = overdueRecords.size,
                        overdueDays = oldestOverdueDays,
                        dueSoonCount = dueSoonCount,
                    )
                }
                val sorted = everyone.sortedWith(
                    compareByDescending<LedgerPerson> { it.outstanding }.thenBy { it.name.lowercase() }
                ).toImmutableList()
                LedgerSummary(
                    loading = false,
                    totalLent = lent,
                    totalBorrowed = borrowed,
                    people = sorted.filter { !it.settled }.toImmutableList(),
                    contacts = sorted,
                )
            }
        }
    }
}
