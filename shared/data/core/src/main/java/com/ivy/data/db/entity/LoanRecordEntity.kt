package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import androidx.room.ColumnInfo
import com.ivy.base.model.LoanRecordType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "loan_records")
data class LoanRecordEntity(
    @SerialName("loanId")
    @Serializable(with = KSerializerUUID::class)
    val loanId: UUID,
    @SerialName("amount")
    val amount: Double,
    @SerialName("note")
    val note: String? = null,
    @SerialName("dateTime")
    @Serializable(with = KSerializerInstant::class)
    val dateTime: Instant,
    @SerialName("interest")
    val interest: Boolean = false,
    @SerialName("accountId")
    @Serializable(with = KSerializerUUID::class)
    val accountId: UUID? = null,
    // This is used store the converted amount for currencies which are different from the loan account currency
    @SerialName("convertedAmount")
    val convertedAmount: Double? = null,
    // Optional due date for this individual ledger entry
    @SerialName("recordDueDate")
    @Serializable(with = KSerializerInstant::class)
    val recordDueDate: Instant? = null,
    // Optional phone number belonging to the person behind this record
    @SerialName("recordPhone")
    val recordPhone: String? = null,
    // Optional receipt/proof attached to this record (private storage path)
    @SerialName("attachmentUrl")
    val attachmentUrl: String? = null,
    // Optional global category tag for this ledger entry (v134).
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID? = null,

    // In order to keep backups valid, loanRecordType is by default DECREASE.
    // This is because before issue 2740 all records were of this type implicitly.
    @SerialName("loanRecordType")
    val loanRecordType: LoanRecordType = LoanRecordType.DECREASE,

    @Deprecated("Obsolete field used for cloud sync. Can't be deleted because of backwards compatibility")
    @SerialName("isSynced")
    val isSynced: Boolean = false,
    @Deprecated("Obsolete field used for cloud sync. Can't be deleted because of backwards compatibility")
    @SerialName("isDeleted")
    val isDeleted: Boolean = false,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID()
)
