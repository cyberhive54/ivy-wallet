package com.ivy.legacy.datamodel

import androidx.compose.runtime.Immutable
import com.ivy.base.model.LoanRecordType
import com.ivy.data.db.entity.LoanRecordEntity
import java.time.Instant
import java.util.UUID

@Deprecated("Legacy data model. Will be deleted")
@Immutable
data class LoanRecord(
    val loanId: UUID,
    val amount: Double,
    val note: String? = null,
    val dateTime: Instant,
    val interest: Boolean = false,
    val accountId: UUID? = null,
    // This is used store the converted amount for currencies which are different from the loan account currency
    val convertedAmount: Double? = null,
    val loanRecordType: LoanRecordType,
    // Phone of the person this record belongs to (ledger, phase 2)
    val recordPhone: String? = null,
    // Optional due date of this individual entry (ledger, phase 2)
    val recordDueDate: Instant? = null,
    // Optional receipt/proof photo (relative path in the app-private receipts folder)
    val attachmentUrl: String? = null,
    // Optional global category tag for this ledger entry (v134)
    val categoryId: java.util.UUID? = null,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
) {
    fun toEntity(): LoanRecordEntity = LoanRecordEntity(
        loanId = loanId,
        amount = amount,
        note = note,
        dateTime = dateTime,
        interest = interest,
        accountId = accountId,
        convertedAmount = convertedAmount,
        loanRecordType = loanRecordType,
        recordPhone = recordPhone,
        recordDueDate = recordDueDate,
        attachmentUrl = attachmentUrl,
        categoryId = categoryId,
        isSynced = isSynced,
        isDeleted = isDeleted,
        id = id
    )
}
