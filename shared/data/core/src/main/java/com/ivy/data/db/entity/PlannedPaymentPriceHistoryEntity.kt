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
@Entity(tableName = "planned_payment_price_history")
data class PlannedPaymentPriceHistoryEntity(
    @SerialName("ruleId")
    @Serializable(with = KSerializerUUID::class)
    val ruleId: UUID,
    @SerialName("amount")
    val amount: Double,
    @SerialName("effectiveFrom")
    @Serializable(with = KSerializerInstant::class)
    val effectiveFrom: Instant = Instant.now(),
    // created | edited | paid | hike
    @SerialName("source")
    val source: String = "created",

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)
