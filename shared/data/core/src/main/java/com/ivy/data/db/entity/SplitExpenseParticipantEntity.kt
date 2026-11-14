package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "split_expense_participants")
data class SplitExpenseParticipantEntity(
    @SerialName("expenseId")
    @Serializable(with = KSerializerUUID::class)
    val expenseId: UUID,
    @SerialName("memberId")
    @Serializable(with = KSerializerUUID::class)
    val memberId: UUID,
    @SerialName("shareMode")
    val shareMode: String,
    @SerialName("shareValue")
    val shareValue: Double? = null,
    @SerialName("owedAmount")
    val owedAmount: Double,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)
