package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import com.ivy.base.model.TransactionType
import com.ivy.data.model.IntervalType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "planned_payment_rules")
data class PlannedPaymentRuleEntity(
    @SerialName("startDate")
    @Serializable(with = KSerializerInstant::class)
    val startDate: Instant?,
    @SerialName("intervalN")
    val intervalN: Int?,
    @SerialName("intervalType")
    val intervalType: IntervalType?,
    @SerialName("oneTime")
    val oneTime: Boolean,
    @SerialName("type")
    val type: TransactionType,
    @SerialName("accountId")
    @Serializable(with = KSerializerUUID::class)
    val accountId: UUID,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,
    // End after N cycles (subscriptions). Null = continues indefinitely.
    @SerialName("remainingOccurrences")
    val remainingOccurrences: Int? = null,
    // User-picked icon for subscription rules
    @SerialName("icon")
    val icon: String? = null,
    // Free-text card/method note (never PAN), e.g. "HDFC 4932"
    @SerialName("paymentMethodNote")
    val paymentMethodNote: String? = null,
    // Short label of the card/account the subscription charges
    @SerialName("cardOrMethod")
    val cardOrMethod: String? = null,
    // Trial bookkeeping: isTrial marks a rule currently in trial;
    // trialEndsAt says when it converts to a paid subscription.
    @SerialName("isTrial")
    @ColumnInfo(defaultValue = "0")
    val isTrial: Boolean = false,
    @SerialName("trialEndsAt")
    @Serializable(with = KSerializerInstant::class)
    val trialEndsAt: Instant? = null,
    // Per-subscription reminder lead in days (0/2/3/7/14); drives WorkManager reminders
    @SerialName("reminderLeadDays")
    @ColumnInfo(defaultValue = "7")
    val reminderLeadDays: Int = 7,
    // Paused — still on the board (dimmed), excluded from totals, timeline and reminders
    @SerialName("paused")
    @ColumnInfo(defaultValue = "0")
    val paused: Boolean = false,
    // Archived — hidden from the board, restorable; excluded from everything
    @SerialName("archived")
    @ColumnInfo(defaultValue = "0")
    val archived: Boolean = false,

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
