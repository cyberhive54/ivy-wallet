package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * One member's contribution to a group expense.
 *
 * A group expense can have several payers; the primary payer is also kept on
 * [SplitExpenseEntity.paidByMemberId] for compatibility and quick display.
 */
@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "split_expense_payers")
data class SplitExpensePayerEntity(
    @SerialName("expenseId")
    @Serializable(with = KSerializerUUID::class)
    val expenseId: UUID,
    @SerialName("memberId")
    @Serializable(with = KSerializerUUID::class)
    val memberId: UUID,
    @SerialName("amount")
    val amount: Double,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)
