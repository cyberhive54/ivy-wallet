package com.ivy.widget.subscriptions

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.ivy.base.model.TransactionType
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.model.IntervalType
import com.ivy.domain.AppStarter
import com.ivy.legacy.utils.ioThread
import com.ivy.wallet.domain.action.settings.SettingsAct
import com.ivy.wallet.domain.deprecated.logic.notification.SubscriptionsReminderWorker
import com.ivy.widgets.WidgetBase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject

object SubscriptionsPrefsKey {
    const val TOTAL = "subs_total"
    const val CURRENCY = "subs_currency"
    const val RENEWALS_COUNT = "subs_renewals_count"
    fun title(i: Int) = "subs_renewal_${i}_title"
    fun amount(i: Int) = "subs_renewal_${i}_amount"
    fun days(i: Int) = "subs_renewal_${i}_days"
    fun id(i: Int) = "subs_renewal_${i}_id"
}

class SubscriptionsWidget(
    private val getAppStarter: () -> AppStarter,
) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val renewalsCount = prefs[intPreferencesKey(SubscriptionsPrefsKey.RENEWALS_COUNT)] ?: 0
            val items = (0 until renewalsCount.coerceIn(0, 3)).map { index ->
                WidgetRenewal(
                    id = prefs[stringPreferencesKey(SubscriptionsPrefsKey.id(index))].orEmpty(),
                    title = prefs[stringPreferencesKey(SubscriptionsPrefsKey.title(index))].orEmpty(),
                    amount = prefs[stringPreferencesKey(SubscriptionsPrefsKey.amount(index))].orEmpty(),
                    daysLabel = prefs[stringPreferencesKey(SubscriptionsPrefsKey.days(index))].orEmpty(),
                )
            }
            val total = prefs[intPreferencesKey(SubscriptionsPrefsKey.TOTAL)] ?: 0
            val currency = prefs[stringPreferencesKey(SubscriptionsPrefsKey.CURRENCY)] ?: "INR"

            SubscriptionsWidgetContent(
                totalCount = total,
                currency = currency,
                items = items,
                onWidgetClick = actionStartActivity(getAppStarter().defaultStartIntent()),
                onRowClick = { ruleId ->
                    actionStartActivity(
                        getAppStarter().getRootIntent().apply {
                            putExtra(SubscriptionsReminderWorker.EXTRA_OPEN_SUBSCRIPTION_RULE, ruleId)
                        }
                    )
                },
            )
        }
    }
}

@Keep
@AndroidEntryPoint
class SubscriptionsWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        fun updateBroadcast(context: Context) {
            WidgetBase.updateBroadcast(context, SubscriptionsWidgetReceiver::class.java)
        }
    }

    override val glanceAppWidget: GlanceAppWidget = SubscriptionsWidget(
        getAppStarter = { appStarter }
    )
    private val coroutineScope = MainScope()

    @Inject
    lateinit var appStarter: AppStarter

    @Inject
    lateinit var plannedPaymentRuleDao: PlannedPaymentRuleDao

    @Inject
    lateinit var settingsAct: SettingsAct

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateData(context)
    }

    private fun updateData(context: Context) {
        coroutineScope.launch {
            val rules = ioThread { plannedPaymentRuleDao.findAll() }
                .filter { isSubscriptionRule(it) }
            val now = Instant.now()
            val today = LocalDate.now(ZoneOffset.UTC)

            val renewals = rules.mapNotNull { rule ->
                val next = nextOccurrence(rule.startDate, rule.intervalN, rule.intervalType, now)
                    ?: return@mapNotNull null
                val date = LocalDate.ofInstant(Instant.ofEpochMilli(next), ZoneOffset.UTC)
                val days = ChronoUnit.DAYS.between(today, date)
                rule to days
            }.sortedBy { it.second }.take(3)

            val currency = settingsAct(Unit).baseCurrency
            val total = rules.count {
                val remaining = it.remainingOccurrences
                !it.paused && !it.archived && (remaining == null || remaining > 0)
            }

            GlanceAppWidgetManager(context).getGlanceIds(SubscriptionsWidget::class.java).forEach { id ->
                updateAppWidgetState(
                    context,
                    PreferencesGlanceStateDefinition,
                    id
                ) { pref ->
                    pref.toMutablePreferences().apply {
                        this[intPreferencesKey(SubscriptionsPrefsKey.TOTAL)] = total
                        this[stringPreferencesKey(SubscriptionsPrefsKey.CURRENCY)] = currency
                        this[intPreferencesKey(SubscriptionsPrefsKey.RENEWALS_COUNT)] = renewals.size

                        renewals.forEachIndexed { index, (rule, days) ->
                            this[stringPreferencesKey(SubscriptionsPrefsKey.title(index))] =
                                rule.title?.takeIf { it.isNotBlank() } ?: "Subscription"
                            this[stringPreferencesKey(SubscriptionsPrefsKey.amount(index))] =
                                amountLabel(rule.amount, currency)
                            this[stringPreferencesKey(SubscriptionsPrefsKey.days(index))] =
                                dayLabel(days)
                            this[stringPreferencesKey(SubscriptionsPrefsKey.id(index))] =
                                rule.id.toString()
                        }
                    }
                }
                glanceAppWidget.update(context, id)
            }
        }
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
        now: Instant,
    ): Long? {
        if (start == null || intervalN == null || intervalType == null || intervalN <= 0) {
            return null
        }
        var t = start.toEpochMilli()
        var guard = 0
        while (t < now.toEpochMilli() && guard < 512) {
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

    private fun amountLabel(
        amount: Double,
        currency: String,
    ): String = when (currency.uppercase()) {
        "INR" -> "₹%,.0f".format(amount)
        else -> "%s %,.0f".format(currency, amount)
    }

    private fun dayLabel(days: Long): String = when (days) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> "in ${days}d"
    }
}
