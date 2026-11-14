package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    @SerialName("groupId")
    @Serializable(with = KSerializerUUID::class)
    val groupId: UUID,
    @SerialName("paidByMemberId")
    @Serializable(with = KSerializerUUID::class)
    val paidByMemberId: UUID,
    @SerialName("amount")
    val amount: Double,
    @SerialName("currency")
    val currency: String = "INR",
    @SerialName("note")
    val note: String? = null,
    @SerialName("date")
    @Serializable(with = KSerializerInstant::class)
    val date: Instant = Instant.now(),
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID? = null,
    @SerialName("attachmentUrl")
    val attachmentUrl: String? = null,
    @SerialName("recurringGroupRuleId")
    @Serializable(with = KSerializerUUID::class)
    val groupRecurringRuleId: UUID? = null,
    @SerialName("settled")
    val settled: Boolean = false,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)
