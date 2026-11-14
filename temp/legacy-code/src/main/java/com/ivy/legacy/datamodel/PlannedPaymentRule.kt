package com.ivy.legacy.datamodel

import androidx.compose.runtime.Immutable
import com.ivy.base.model.TransactionType
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.model.IntervalType
import java.time.Instant
import java.util.UUID

@Deprecated("Legacy data model. Will be deleted")
@Immutable
data class PlannedPaymentRule(
    val startDate: Instant?,
    val intervalN: Int?,
    val intervalType: IntervalType?,
    val oneTime: Boolean,

    val type: TransactionType,
    val accountId: UUID,
    val amount: Double = 0.0,
    val categoryId: UUID? = null,
    val title: String? = null,
    val description: String? = null,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Expand-suite (v132) — subscriptions. Must round-trip entity <-> legacy losslessly.
    val remainingOccurrences: Int? = null,
    val icon: String? = null,
    val paymentMethodNote: String? = null,
    val cardOrMethod: String? = null,
    val isTrial: Boolean = false,
    val trialEndsAt: Instant? = null,
    val reminderLeadDays: Int = 7,
    val paused: Boolean = false,
    val archived: Boolean = false,

    val id: UUID = UUID.randomUUID()
) {
    fun toEntity(): PlannedPaymentRuleEntity = PlannedPaymentRuleEntity(
        startDate = startDate,
        intervalN = intervalN,
        intervalType = intervalType,
        oneTime = oneTime,
        type = type,
        accountId = accountId,
        amount = amount,
        categoryId = categoryId,
        title = title,
        description = description,
        isSynced = isSynced,
        isDeleted = isDeleted,
        remainingOccurrences = remainingOccurrences,
        icon = icon,
        paymentMethodNote = paymentMethodNote,
        cardOrMethod = cardOrMethod,
        isTrial = isTrial,
        trialEndsAt = trialEndsAt,
        reminderLeadDays = reminderLeadDays,
        paused = paused,
        archived = archived,
        id = id
    )
}
