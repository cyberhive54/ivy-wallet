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
@Entity(tableName = "split_settlements")
data class SplitSettlementEntity(
    @SerialName("groupId")
    @Serializable(with = KSerializerUUID::class)
    val groupId: UUID,
    // Sender of the money. Null means the local user paid themselves ("just split the settled balance" style logs)
    @SerialName("fromMemberId")
    @Serializable(with = KSerializerUUID::class)
    val fromMemberId: UUID? = null,
    @SerialName("toMemberId")
    @Serializable(with = KSerializerUUID::class)
    val toMemberId: UUID,
    @SerialName("amount")
    val amount: Double,
    @SerialName("currency")
    val currency: String = "INR",
    @SerialName("createdAt")
    @Serializable(with = KSerializerInstant::class)
    val createdAt: Instant = Instant.now(),
    @SerialName("linkedTransactionId")
    @Serializable(with = KSerializerUUID::class)
    val linkedTransactionId: UUID? = null,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)
