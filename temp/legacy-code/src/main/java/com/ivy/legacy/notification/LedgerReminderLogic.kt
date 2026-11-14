package com.ivy.wallet.domain.deprecated.logic.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ivy.base.legacy.SharedPrefs
import com.ivy.legacy.utils.timeNowLocal
import com.ivy.legacy.utils.toEpochSeconds
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Daily ledger sweep at 09:00 local (quiet hours 22:00 → 07:00 are honoured by the worker).
 */
class LedgerReminderLogic @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val sharedPrefs: SharedPrefs,
) {
    companion object {
        private const val UNIQUE_WORK_NAME = "ledger_reminder_work"
        private const val START_HOUR = 9
        private const val START_MINUTE = 0
    }

    fun scheduleReminder() {
        if (!sharedPrefs.getBoolean(SharedPrefs.SHOW_NOTIFICATIONS, true)) {
            return
        }

        val now = timeNowLocal()
        val todayStart = now
            .withHour(START_HOUR)
            .withMinute(START_MINUTE)
            .withSecond(0)

        val initialDelaySeconds = if (todayStart.isAfter(now)) {
            todayStart.toEpochSeconds() - now.toEpochSeconds()
        } else {
            todayStart.plusDays(1).toEpochSeconds() - now.toEpochSeconds()
        }

        val workBuilder = PeriodicWorkRequestBuilder<LedgerReminderWorker>(24, TimeUnit.HOURS)
        if (initialDelaySeconds > 0) {
            workBuilder.setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
        }

        WorkManager
            .getInstance(appContext)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workBuilder.build()
            )
    }
}
