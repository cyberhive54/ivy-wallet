package com.ivy.wallet.domain.deprecated.logic.notification

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.time.TimeProvider
import com.ivy.base.model.TransactionType
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.model.IntervalType
import com.ivy.domain.AppStarter
import com.ivy.legacy.forDisplay
import com.ivy.wallet.android.notification.IvyNotificationChannel
import com.ivy.wallet.android.notification.NotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Daily check for subscription renewals coming due.
 *
 * Fires one notification per rule on its configured reminder day (or on the due date when the lead is 0),
 * skips paused/exhausted and non-recurring rules, and respects the global show-notifications setting.
 * Tap opens the subscription editor for the rule.
 */
@HiltWorker
class SubscriptionsReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val notificationService: NotificationService,
    private val sharedPrefs: SharedPrefs,
    private val appStarter: AppStarter,
    private val timeProvider: TimeProvider,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val EXTRA_OPEN_SUBSCRIPTION_RULE = "open_subscription_rule_extra"
    }

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        // Double-check: the user can disable notifications after the work has been scheduled
        if (!sharedPrefs.getBoolean(SharedPrefs.SHOW_NOTIFICATIONS, true)) {
            return@withContext Result.success()
        }

        val today: LocalDate = timeProvider.localDateNow()
        val nowEpoch = System.currentTimeMillis()

        val rules = plannedPaymentRuleDao.findAll()

        for (rule in rules) {
            val remaining = rule.remainingOccurrences
            if (!isSubscriptionRule(rule)) continue
            if (remaining != null && remaining <= 0) {
                // Exhausted — silent; the rule is "off"
                continue
            }
            if (rule.paused || rule.archived) {
                // Paused / archived — silent; user manage those in the board
                continue
            }

            val isTrialRule = rule.isTrial && rule.trialEndsAt != null

            val nextDue = if (isTrialRule) {
                rule.trialEndsAt?.atZone(ZoneOffset.UTC)?.toLocalDate()
            } else {
                nextOccurrence(rule.startDate, rule.intervalN, rule.intervalType, nowEpoch)
                    ?.let { LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
            } ?: continue

            val daysUntil = ChronoUnit.DAYS.between(today, nextDue)
            val lead = rule.reminderLeadDays.coerceIn(0, 14)

            val shouldNotify = when {
                lead == 0 -> daysUntil == 0L
                else -> daysUntil == lead.toLong() || daysUntil == 0L
            }
            if (!shouldNotify) continue

            val title = rule.title?.takeIf { it.isNotBlank() } ?: "Subscription"
            val cycle = cycleLabel(rule)
            val amount = rule.amount

            val whenText = if (isTrialRule) {
                when {
                    daysUntil == 0L -> "Trial ends today — keep or cancel?"
                    daysUntil == 1L -> "Trial ends tomorrow — decide before auto-pay"
                    daysUntil > 1L -> "Trial ends in $daysUntil days"
                    else -> "Trial has ended"
                }
            } else {
                when {
                    daysUntil == 0L -> "Due today"
                    daysUntil == 1L -> "Renews tomorrow"
                    daysUntil > 1L -> "Renews in $daysUntil days"
                    else -> "Past due"
                }
            }

            val notification = notificationService
                .defaultIvyNotification(
                    channel = IvyNotificationChannel.SUBSCRIPTIONS,
                    priority = NotificationCompat.PRIORITY_HIGH
                )
                .setContentTitle(title)
                .setContentText("$whenText · ${amountFormat(amount, cycle)}")
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        rule.id.hashCode(),
                        appStarter.getRootIntent().apply {
                            putExtra(EXTRA_OPEN_SUBSCRIPTION_RULE, rule.id.toString())
                        },
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_UPDATE_CURRENT
                            or PendingIntent.FLAG_IMMUTABLE
                    )
                )

            notificationService.showNotification(
                notification,
                rule.id.hashCode(),
            )
        }

        return@withContext Result.success()
    }

    private fun isSubscriptionRule(rule: PlannedPaymentRuleEntity): Boolean {
        val intervalN = rule.intervalN
        val intervalType = rule.intervalType
        return rule.type == TransactionType.EXPENSE &&
            !rule.oneTime &&
            intervalN != null && intervalN > 0 &&
            intervalType != null
    }

    private fun nextOccurrence(
        start: Instant?,
        intervalN: Int?,
        intervalType: IntervalType?,
        nowEpoch: Long,
    ): Long? {
        if (start == null || intervalN == null || intervalType == null || intervalN <= 0) {
            return null
        }
        var t = start.toEpochMilli()
        var guard = 0
        while (t < nowEpoch && guard < 512) {
            t = increment(t, intervalN, intervalType)
            guard++
        }
        return t
    }

    private fun increment(
        epochMillis: Long,
        n: Int,
        type: IntervalType,
    ): Long {
        val instant = Instant.ofEpochMilli(epochMillis)
        return when (type) {
            IntervalType.DAY -> instant.plus(n.toLong(), ChronoUnit.DAYS).toEpochMilli()
            IntervalType.WEEK -> instant.plus(n * 7L, ChronoUnit.DAYS).toEpochMilli()
            IntervalType.MONTH ->
                instant.atZone(ZoneOffset.UTC).plusMonths(n.toLong()).toInstant().toEpochMilli()

            IntervalType.YEAR ->
                instant.atZone(ZoneOffset.UTC).plusYears(n.toLong()).toInstant().toEpochMilli()
        }
    }

    private fun cycleLabel(
        rule: PlannedPaymentRuleEntity,
    ): String {
        val n = rule.intervalN ?: return "recurring"
        if (n == 1) {
            return when (rule.intervalType) {
                IntervalType.DAY -> "daily"
                IntervalType.WEEK -> "weekly"
                IntervalType.MONTH -> "monthly"
                IntervalType.YEAR -> "yearly"
                else -> "recurring"
            }
        }
        return "every ${n} ${rule.intervalType?.forDisplay(n) ?: "cycles"}"
    }

    private fun amountFormat(
        amount: Double,
        cycle: String,
    ): String =
        "₹%,.0f · %s".format(amount, cycle)
}
