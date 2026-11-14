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
 * Daily subscriptions reminder job. Fires the first check at the next local 09:30
 * (outside the quiet window), then every 24h. Existing periodic work is kept.
 */
class SubscriptionsReminderLogic @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val sharedPrefs: SharedPrefs,
) {
    companion object {
        private const val UNIQUE_WORK_NAME = "subscriptions_reminder_work"
        private const val START_HOUR = 9
        private const val START_MINUTE = 30
    }

    fun scheduleReminder() {
        if (!fetchShowNotifications()) {
            return
        }

        val timeNowLocal = timeNowLocal()
        val todayStart = timeNowLocal()
            .withHour(START_HOUR)
            .withMinute(START_MINUTE)
            .withSecond(0)
            .withNano(0)

        val initialDelaySeconds = if (todayStart.isAfter(timeNowLocal)) {
            todayStart.toEpochSeconds() - timeNowLocal.toEpochSeconds()
        } else {
            todayStart.plusDays(1).toEpochSeconds() - timeNowLocal.toEpochSeconds()
        }

        val workBuilder = PeriodicWorkRequestBuilder<SubscriptionsReminderWorker>(24, TimeUnit.HOURS)
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

    private fun fetchShowNotifications(): Boolean =
        sharedPrefs.getBoolean(SharedPrefs.SHOW_NOTIFICATIONS, true)
}
