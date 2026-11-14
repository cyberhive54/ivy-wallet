package com.ivy.wallet.domain.deprecated.logic.notification

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ivy.base.legacy.SharedPrefs
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.db.dao.read.LoanRecordDao
import com.ivy.domain.AppStarter
import com.ivy.legacy.utils.ioThread
import com.ivy.wallet.android.notification.IvyNotificationChannel
import com.ivy.wallet.android.notification.NotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Daily ledger sweep: notifies about entries that are due today or already overdue.
 *
 * Quiet hours (22:00 → 07:00 local) are respected - nothing is posted in that window.
 */
@HiltWorker
class LedgerReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val loanDao: LoanDao,
    private val loanRecordDao: LoanRecordDao,
    private val notificationService: NotificationService,
    private val sharedPrefs: SharedPrefs,
    private val appStarter: AppStarter,
) : CoroutineWorker(appContext, params) {

    companion object {
        /** Quiet hours (local): 22:00 → 07:00. */
        const val QUIET_START_HOUR = 22
        const val QUIET_END_HOUR = 7

        /** Intent extra: open the Ledger workspace directly. */
        const val EXTRA_OPEN_LEDGER_WORKSPACE = "open_ledger_workspace_extra"
    }

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        if (!sharedPrefs.getBoolean(SharedPrefs.SHOW_NOTIFICATIONS, true)) {
            return@withContext Result.success()
        }

        val now = LocalDateTime.now()
        if (isQuietHours(now)) {
            return@withContext Result.success()
        }

        val today = now.toLocalDate()
        val overdue = ioThread {
            val loans = loanDao.findAll().associateBy { it.id }
            val records = loanRecordDao.findAll()
            records.mapNotNull { record ->
                val due = record.recordDueDate ?: return@mapNotNull null
                val days = ChronoUnit.DAYS.between(
                    due.atZone(ZoneId.systemDefault()).toLocalDate(),
                    today
                )
                if (days < 0) return@mapNotNull null
                val loan = loans[record.loanId] ?: return@mapNotNull null
                val person = loan.counterpartyName?.takeIf { it.isNotBlank() } ?: loan.name
                Triple(person, record.amount, days.toInt())
            }
        }

        if (overdue.isEmpty()) {
            return@withContext Result.success()
        }

        // One notification per person, listing the entries that are due/overdue
        overdue.groupBy { it.first }.forEach { (person, entries) ->
            val total = entries.sumOf { it.second }
            val oldestDays = entries.maxOf { it.third }
            val whenText = if (oldestDays > 0) {
                "overdue by $oldestDays day(s)"
            } else {
                "due today"
            }
            val notification = notificationService
                .defaultIvyNotification(
                    channel = IvyNotificationChannel.PAYMENTS,
                    priority = NotificationCompat.PRIORITY_HIGH
                )
                .setContentTitle(person)
                .setContentText("$whenText · ₹%,.0f in %d entries".format(total, entries.size))
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        person.hashCode(),
                        appStarter.getRootIntent().apply {
                            putExtra(EXTRA_OPEN_LEDGER_WORKSPACE, true)
                        },
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_UPDATE_CURRENT
                            or PendingIntent.FLAG_IMMUTABLE
                    )
                )

            notificationService.showNotification(notification, person.hashCode())
        }

        Result.success()
    }

    private fun isQuietHours(now: LocalDateTime): Boolean {
        val hour = now.hour
        return if (QUIET_START_HOUR > QUIET_END_HOUR) {
            hour >= QUIET_START_HOUR || hour < QUIET_END_HOUR
        } else {
            hour in QUIET_END_HOUR until QUIET_START_HOUR
        }
    }
}
