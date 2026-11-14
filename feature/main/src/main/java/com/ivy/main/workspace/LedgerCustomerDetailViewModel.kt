package com.ivy.main.workspace

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.base.model.LoanRecordType
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.db.dao.read.LoanRecordDao
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.legacy.datamodel.temp.toDomain
import com.ivy.legacy.utils.ioThread
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class LedgerEntry(
    val id: UUID,
    val amount: Double,
    val note: String?,
    val date: Instant?,
    val type: LoanRecordType,
    val receiptPath: String?,
    val phone: String?,
)

data class LedgerCustomer(
    val id: UUID,
    val name: String,
    val phone: String?,
    val youOwe: Boolean,
    val accountId: UUID? = null,
    val lent: Double = 0.0,     // money that went out (you lent / borrowed)
    val received: Double = 0.0, // money that came back
    val entries: List<LedgerEntry> = emptyList(),
) {
    val outstanding: Double get() = (lent - received).coerceAtLeast(0.0)
    val lastEntryAt: Instant? get() = entries.maxByOrNull { it.date ?: Instant.EPOCH }?.date
}

/**
 * Customer detail for the Ledger workspace: totals, the last entries (statement
 * preview) and the per-entry receipts.
 */
@HiltViewModel
class LedgerCustomerDetailViewModel @Inject constructor(
    private val loanDao: LoanDao,
    private val loanRecordDao: LoanRecordDao,
    private val writeLoanDao: WriteLoanDao,
    private val writeLoanRecordDao: WriteLoanRecordDao,
    private val transactionRepository: com.ivy.data.repository.TransactionRepository,
    private val transactionMapper: com.ivy.data.repository.mapper.TransactionMapper,
    private val accountsAct: com.ivy.wallet.domain.action.account.AccountsAct,
    @ApplicationContext
    private val context: Context,
) : ViewModel() {

    private val _customer = MutableStateFlow<LedgerCustomer?>(null)
    val customer: StateFlow<LedgerCustomer?> = _customer.asStateFlow()

    /** First account — used as the settle account when the loan has none. */
    private val _fallbackAccountId = MutableStateFlow<UUID?>(null)
    val fallbackAccountId: StateFlow<UUID?> = _fallbackAccountId.asStateFlow()

    fun load(loanId: UUID) {
        viewModelScope.launch {
            _fallbackAccountId.value = accountsAct(Unit).firstOrNull()?.id
            _customer.value = withContext(Dispatchers.IO) {
                val loan = loanDao.findById(loanId) ?: return@withContext null
                val records = loanRecordDao.findAll()
                    .filter { it.loanId == loanId }
                    .map { record ->
                        LedgerEntry(
                            id = record.id,
                            amount = record.amount,
                            note = record.note,
                            date = record.dateTime,
                            type = record.loanRecordType,
                            receiptPath = record.attachmentUrl,
                            phone = record.recordPhone,
                        )
                    }
                    .sortedByDescending { it.date ?: Instant.EPOCH }

                val lent = records
                    .filter { it.type == LoanRecordType.INCREASE }
                    .sumOf { it.amount }
                val received = records
                    .filter { it.type == LoanRecordType.DECREASE }
                    .sumOf { it.amount }

                LedgerCustomer(
                    id = loan.id,
                    name = loan.counterpartyName?.takeIf { it.isNotBlank() } ?: loan.name,
                    phone = loan.customerPhone?.takeIf { it.isNotBlank() }
                        ?: records.firstOrNull { !it.phone.isNullOrBlank() }?.phone,
                    youOwe = loan.type != com.ivy.data.model.LoanType.LEND,
                    accountId = loan.accountId,
                    lent = lent,
                    received = received,
                    entries = records,
                )
            }
        }
    }

    /**
     * Settles the whole ledger with this person: writes a real transaction
     * (INCOME when they owe you, EXPENSE when you owe them) tagged "Settle"
     * and clears the entries.
     */
    fun settleAll(fallbackAccountId: UUID?) {
        viewModelScope.launch {
            val c = _customer.value ?: return@launch
            viewModelScope.launch {
                ioThread {
                    val accountId = c.accountId ?: fallbackAccountId
                    if (accountId != null && c.outstanding > 0.0) {
                        val type = if (c.youOwe) {
                            com.ivy.base.model.TransactionType.EXPENSE
                        } else {
                            com.ivy.base.model.TransactionType.INCOME
                        }
                        com.ivy.base.legacy.Transaction(
                            accountId = accountId,
                            type = type,
                            amount = c.outstanding.toBigDecimal(),
                            title = "Settle - ${c.name}",
                            description = "Ledger settlement",
                            dateTime = Instant.now(),
                            loanId = c.id,
                        ).toDomain(transactionMapper)?.let { trn ->
                            transactionRepository.save(trn)
                        }
                    }
                    // The ledger is settled - the entries go with it
                    val records = loanRecordDao.findAll().filter { it.loanId == c.id }
                    records.forEach { record ->
                        com.ivy.legacy.ui.theme.receipt.ReceiptStorage.delete(
                            context = context,
                            relativePath = record.attachmentUrl
                        )
                        writeLoanRecordDao.deleteById(record.id)
                    }
                    writeLoanDao.deleteById(c.id)
                }
                _customer.value = null
            }
        }
    }

    /** Deletes the person with all their entries and receipts. */
    fun deleteCustomer(loanId: UUID) {
        viewModelScope.launch {
            ioThread {
                val records = loanRecordDao.findAll().filter { it.loanId == loanId }
                records.forEach { record ->
                    // Lazy cleanup: the receipt dies with the entry
                    com.ivy.legacy.ui.theme.receipt.ReceiptStorage.delete(
                        context = context,
                        relativePath = record.attachmentUrl
                    )
                    writeLoanRecordDao.deleteById(record.id)
                }
                writeLoanDao.deleteById(loanId)
            }
            _customer.value = null
        }
    }
}
