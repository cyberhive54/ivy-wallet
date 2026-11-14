package com.ivy.planned.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.base.model.TransactionType
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.read.PlannedPaymentPriceHistoryDao
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WritePlannedPaymentPriceHistoryDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.db.entity.PlannedPaymentPriceHistoryEntity
import com.ivy.data.model.Category
import com.ivy.data.model.IntervalType
import com.ivy.data.repository.CategoryRepository
import com.ivy.legacy.forDisplay
import com.ivy.legacy.incrementDate
import com.ivy.legacy.datamodel.temp.toLegacyDomain
import com.ivy.legacy.utils.ioThread
import com.ivy.wallet.domain.deprecated.logic.PlannedPaymentsGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

data class SubscriptionItem(
    val ruleId: java.util.UUID,
    val title: String,
    val amount: Double,
    val currency: String,
    val iconKey: String?,
    val methodNote: String?,
    val cardOrMethod: String?,
    val categoryId: java.util.UUID?,
    val categoryName: String?,
    val categoryColor: Int?,
    val monthsPerOccurrence: Double,
    val monthlyAmount: Double,
    val intervalLabel: String,
    val nextDate: LocalDate?,
    val remaining: Int?,
    val isTrial: Boolean,
    val trialEnds: LocalDate?,
    val hikePercent: Double?,
    val hikePreviousAmount: Double?,
    /** Paused — hidden from totals/timeline, dimmed on the board (still listed). */
    val isPaused: Boolean,
)

data class SubscriptionTimelineEntry(
    val id: java.util.UUID,
    val date: LocalDate,
    val title: String,
    val amount: Double,
    val currency: String,
)

data class CategoryChip(
    /** null = the implicit "All" chip */
    val id: java.util.UUID?,
    val label: String,
    val count: Int,
)

data class SubscriptionsState(
    val loading: Boolean = true,
    val currency: String = "INR",
    val totalCount: Int = 0,
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val chips: List<CategoryChip> = emptyList(),
    val selectedChipId: java.util.UUID? = null,
    val items: List<SubscriptionItem> = emptyList(),
    val timeline: List<SubscriptionTimelineEntry> = emptyList(),
    /** Archived rules — render in the Archived segment instead of the main board. */
    val archivedItems: List<SubscriptionItem> = emptyList(),
) {
    val isEmpty: Boolean get() = !loading && totalCount == 0 && archivedItems.isEmpty()
}

/**
 * Drives the dedicated Subscriptions board. A subscription IS a recurring
 * (non-oneTime) expense planned payment — the board adds monthly-normalized
 * cost, next-occurrence, price-hike badges and a 30-day timeline on top of it.
 */
@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val plannedPaymentPriceHistoryDao: PlannedPaymentPriceHistoryDao,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val plannedPaymentPriceHistoryWriter: WritePlannedPaymentPriceHistoryDao,
    private val plannedPaymentsGenerator: PlannedPaymentsGenerator,
    private val categoriesRepository: CategoryRepository,
    private val settingsDao: SettingsDao,
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionsState())
    val state: StateFlow<SubscriptionsState> = _state.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    fun selectChip(chipId: java.util.UUID?) {
        val current = _state.value
        if (current.selectedChipId == chipId) return
        _state.value = current.copy(
            selectedChipId = chipId,
            items = filterItems(currentRulesSnapshot, chipId),
        )
    }

    private var currentRulesSnapshot: List<SubscriptionItem> = emptyList()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)

            _state.value = withContext(Dispatchers.Default) {
                val currency = ioThread { settingsDao.findFirst() }.currency
                val categories: List<Category> = categoriesRepository.findAll()
                val categoryById = categories.associateBy { it.id.value }

                val now = Instant.now()
                val today: LocalDate = LocalDate.now(ZoneOffset.UTC)

                val rules = plannedPaymentRuleDao.findAll()
                    .filter { rule ->
                        val intervalN = rule.intervalN
                        intervalN != null &&
                            rule.intervalType != null &&
                            intervalN > 0 &&
                            rule.type == TransactionType.EXPENSE &&
                            !rule.oneTime
                    }

                suspend fun mapRule(
                    rule: com.ivy.data.db.entity.PlannedPaymentRuleEntity,
                ): SubscriptionItem {
                    val category = rule.categoryId?.let { id -> categoryById[id] }

                    val nextDate = nextOccurrence(rule.startDate, rule.intervalN, rule.intervalType, now)
                        ?.atZone(ZoneOffset.UTC)?.toLocalDate()

                    val monthsPerOccurrence = monthsPerOccurrence(rule.intervalN!!, rule.intervalType!!)
                    val monthly = rule.amount / monthsPerOccurrence

                    val hike = ioThread {
                        priceHike(rule.id)
                    }

                    val intervalLabel = if (rule.intervalN == 1) {
                        perIntervalLabel(rule.intervalType!!)
                    } else {
                        "Every ${rule.intervalN} ${rule.intervalType!!.forDisplay(rule.intervalN!!)}"
                    }

                    return@mapRule SubscriptionItem(
                        ruleId = rule.id,
                        title = rule.title?.takeIf { it.isNotBlank() }
                            ?: category?.name?.value
                            ?: "Subscription",
                        amount = rule.amount,
                        currency = currency,
                        iconKey = rule.icon,
                        methodNote = rule.paymentMethodNote,
                        cardOrMethod = rule.cardOrMethod,
                        categoryId = rule.categoryId,
                        categoryName = category?.name?.value,
                        categoryColor = category?.color?.value,
                        monthsPerOccurrence = monthsPerOccurrence,
                        monthlyAmount = monthly,
                        intervalLabel = intervalLabel,
                        nextDate = nextDate,
                        remaining = rule.remainingOccurrences,
                        isTrial = rule.isTrial,
                        trialEnds = rule.trialEndsAt?.atZone(ZoneOffset.UTC)?.toLocalDate(),
                        hikePercent = hike?.percent,
                        hikePreviousAmount = hike?.previousAmount,
                        isPaused = rule.paused,
                    )
                }

                val activeRules = rules.filter { !it.archived }
                val archivedRules = rules.filter { it.archived }

                val activeMappedList = mutableListOf<SubscriptionItem>()
                for (rule in activeRules) {
                    activeMappedList.add(mapRule(rule))
                }
                val archivedMappedList = mutableListOf<SubscriptionItem>()
                for (rule in archivedRules) {
                    archivedMappedList.add(mapRule(rule))
                }

                val activeMapped = activeMappedList.sortedWith(
                    compareBy({ it.isPaused }, { it.nextDate ?: LocalDate.MAX }, { -it.amount })
                )
                val archivedMapped = archivedMappedList.sortedBy { it.title.lowercase() }

                // Paused subscriptions stay visible but contribute nothing to totals/timeline
                val effective = activeMapped.filter { !it.isPaused }
                val mapped = activeMapped

                currentRulesSnapshot = mapped

                val chips = buildList {
                    add(CategoryChip(null, "All", mapped.size))
                    val byCategory = mapped.filter { it.categoryId != null }
                        .groupBy { it.categoryId!! }
                    for ((catId, catItems) in byCategory) {
                        val name = catItems.first().categoryName ?: "Category"
                        add(CategoryChip(catId, name, catItems.size))
                    }
                }

                // 30-day timeline: next occurrence for every active, non-paused rule in the window
                val windowEnd = today.plusDays(30)
                val timeline = effective.mapNotNull { item ->
                    val next = item.nextDate ?: return@mapNotNull null
                    if (next.isBefore(today) || next.isAfter(windowEnd)) {
                        return@mapNotNull null
                    }
                    SubscriptionTimelineEntry(
                        id = item.ruleId,
                        date = next,
                        title = item.title,
                        amount = item.amount,
                        currency = item.currency,
                    )
                }.sortedWith(compareBy({ it.date }, { -it.amount }))

                val monthlyTotal = effective.sumOf { it.monthlyAmount }

                SubscriptionsState(
                    loading = false,
                    currency = currency,
                    totalCount = mapped.size,
                    monthlyTotal = monthlyTotal,
                    yearlyTotal = monthlyTotal * 12.0,
                    chips = chips,
                    selectedChipId = null,
                    items = mapped,
                    timeline = timeline,
                    archivedItems = archivedMapped,
                )
            }
        }
    }

    private fun filterItems(
        items: List<SubscriptionItem>,
        chipId: java.util.UUID?,
    ): List<SubscriptionItem> =
        if (chipId == null) items else items.filter { it.categoryId == chipId }

    private suspend fun priceHike(
        ruleId: java.util.UUID,
    ): HikeInfo? {
        val history = plannedPaymentPriceHistoryDao.findByRuleId(ruleId)
        if (history.size < 2) return null
        val latest = history[0]
        val previous = history[1]
        if (previous.amount <= 0.0) return null
        val delta = (latest.amount - previous.amount) / previous.amount * 100.0
        return if (delta >= 3.0) HikeInfo(percent = delta, previousAmount = previous.amount) else null
    }

    /** Keep an expired trial as a paid recurring rule (logs conversion to history). */
    fun convertTrialToPaid(ruleId: java.util.UUID) {
        viewModelScope.launch {
            ioThread {
                val entity = plannedPaymentRuleDao.findById(ruleId) ?: return@ioThread
                if (!entity.isTrial) return@ioThread
                val updated = entity.copy(isTrial = false, trialEndsAt = null)
                plannedPaymentRuleWriter.save(updated)
                plannedPaymentPriceHistoryWriter.save(
                    PlannedPaymentPriceHistoryEntity(
                        ruleId = ruleId,
                        amount = updated.amount,
                        source = "converted",
                    )
                )
                plannedPaymentsGenerator.generate(updated.toLegacyDomain())
            }
            refresh()
        }
    }

    /** End a subscription (used by the trial keep/cancel prompt). */
    fun endSubscription(ruleId: java.util.UUID) {
        viewModelScope.launch {
            ioThread {
                plannedPaymentRuleWriter.deleteById(ruleId)
                plannedPaymentPriceHistoryWriter.deleteByRuleId(ruleId)
            }
            refresh()
        }
    }

    /** Pause/resume a subscription — paused items stay visible but dimmed, zero cost. */
    fun setPaused(ruleId: java.util.UUID, paused: Boolean) {
        viewModelScope.launch {
            ioThread {
                val rule = plannedPaymentRuleDao.findById(ruleId) ?: return@ioThread
                plannedPaymentRuleWriter.save(rule.copy(paused = paused))
            }
            refresh()
        }
    }

    /** Archive/restore a subscription — archived rules leave the board, restore brings them back. */
    fun archive(ruleId: java.util.UUID, archived: Boolean) {
        viewModelScope.launch {
            ioThread {
                val rule = plannedPaymentRuleDao.findById(ruleId) ?: return@ioThread
                plannedPaymentRuleWriter.save(rule.copy(archived = archived))
            }
            refresh()
        }
    }

    fun restore(ruleId: java.util.UUID) = archive(ruleId, archived = false)

    private data class HikeInfo(
        val percent: Double,
        val previousAmount: Double,
    )

    private fun nextOccurrence(
        start: Instant?,
        intervalN: Int?,
        intervalType: IntervalType?,
        now: Instant,
    ): Instant? {
        val n = intervalN
        val type = intervalType
        val s = start ?: return null
        if (n == null || type == null || n <= 0) {
            return null
        }
        var t = s
        var guard = 0
        while (t.isBefore(now) && guard < 512) {
            t = type.incrementDate(t, n.toLong())
            guard++
        }
        return t
    }

    private fun monthsPerOccurrence(
        n: Int,
        type: IntervalType,
    ): Double = when (type) {
        IntervalType.DAY -> n / 30.44
        IntervalType.WEEK -> n / 4.345
        IntervalType.MONTH -> n.toDouble()
        IntervalType.YEAR -> n * 12.0
    }

    private fun perIntervalLabel(
        type: IntervalType,
    ): String = when (type) {
        IntervalType.DAY -> "Daily"
        IntervalType.WEEK -> "Weekly"
        IntervalType.MONTH -> "Monthly"
        IntervalType.YEAR -> "Yearly"
    }
}
