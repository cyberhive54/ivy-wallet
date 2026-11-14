package com.ivy.domain.split

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/** How a member's share of an expense is defined. */
enum class ShareMode {
    /** Equal share of what is left. */
    EQUAL,

    /** A fixed amount. */
    EXACT,

    /** A percentage (0..100) of what is left. */
    PERCENT,

    /** A number of shares of what is left. */
    SHARES,

    /** An explicit delta added to their share (can be negative). */
    ADJUSTMENT
}

/**
 * One participant of a split.
 *
 * Only the field matching [mode] is used; the others are ignored.
 * A group member that is not in the list does not take part in the split -
 * this is how "equal between a subset of the group" is expressed.
 */
data class SplitParticipant(
    val memberId: UUID,
    val mode: ShareMode = ShareMode.EQUAL,
    /** EXACT: the amount owed to this member. */
    val exactAmount: BigDecimal? = null,
    /** PERCENT: percent (0..100) of the remaining amount. */
    val percent: Double? = null,
    /** SHARES: the number of shares. */
    val shares: Int? = null,
    /** ADJUSTMENT: a delta added to the member's share (can be negative). */
    val adjustment: BigDecimal? = null,
)

/** A member who paid part of the expense. */
data class Payer(
    val memberId: UUID,
    val amount: BigDecimal,
)

/** A suggested money movement that settles a group. */
data class Transfer(
    val fromMemberId: UUID,
    val toMemberId: UUID,
    val amount: BigDecimal,
)

/**
 * Pure split engine - no Android, no IO, fully unit-testable.
 *
 * Guarantees:
 * - the shares of one expense always add up to the expense total (exact, to the cent)
 * - two equal members never differ by more than one cent
 * - the net balances of a multi-payer expense always add up to zero
 *
 * Allocation rules (in order):
 * 1. EXACT members take their fixed amount off the top.
 * 2. PERCENT / SHARES members take their weighted slice of what is left.
 * 3. EQUAL / ADJUSTMENT members split what is left equally; an ADJUSTMENT member
 *    additionally gets its delta, which the other equal members absorb.
 *
 * Rounding is deterministic: the leftover cents are handed out one by one,
 * starting with the equal members, so the total always matches exactly.
 */
object SplitEngine {

    val ZERO: BigDecimal = BigDecimal.ZERO
    private const val SCALE = 2
    private const val PRECISION = 10
    private val HUNDRED = BigDecimal.valueOf(100L)
    private val CENT = BigDecimal("0.01")

    /**
     * Splits [total] between [participants].
     *
     * @return the amount owed to each member (positive = the group owes them).
     */
    fun split(
        total: BigDecimal,
        participants: List<SplitParticipant>,
    ): Map<UUID, BigDecimal> {
        require(participants.isNotEmpty()) { "A split needs at least one participant" }
        require(total.signum() >= 0) { "A split total cannot be negative" }

        val raw = LinkedHashMap<UUID, BigDecimal>()

        // 1. Exact amounts take their slice first
        var pool = total
        participants.filter { it.mode == ShareMode.EXACT }.forEach { participant ->
            val amount = participant.exactAmount ?: ZERO
            raw[participant.memberId] = amount
            pool = pool.subtract(amount)
        }

        // 2. Percent / shares members take their weighted slice of the pool
        val weighted = participants.filter {
            it.mode == ShareMode.PERCENT || it.mode == ShareMode.SHARES
        }
        val totalShares = weighted
            .filter { it.mode == ShareMode.SHARES }
            .sumOf { it.shares ?: 0 }
        weighted.forEach { participant ->
            val amount = when (participant.mode) {
                ShareMode.PERCENT -> {
                    val percent = participant.percent ?: 0.0
                    require(percent >= 0.0 && percent <= 100.0) { "Percent must be 0..100" }
                    pool.multiply(BigDecimal.valueOf(percent))
                        .divide(HUNDRED, PRECISION, RoundingMode.HALF_UP)
                }

                ShareMode.SHARES -> {
                    val shares = participant.shares ?: 0
                    require(totalShares > 0) { "Share weights must add up to more than zero" }
                    pool.multiply(BigDecimal.valueOf(shares.toLong()))
                        .divide(BigDecimal.valueOf(totalShares.toLong()), PRECISION, RoundingMode.HALF_UP)
                }

                else -> ZERO
            }
            raw[participant.memberId] = amount
        }
        val weightedTotal = weighted.fold(ZERO) { acc, participant ->
            acc.add(raw[participant.memberId] ?: ZERO)
        }

        // 3. Equal / adjustment members split the rest equally
        val equalFamily = participants.filter {
            it.mode == ShareMode.EQUAL || it.mode == ShareMode.ADJUSTMENT
        }
        val adjustments = equalFamily.filter { it.mode == ShareMode.ADJUSTMENT }
        val pureEqual = equalFamily.filter { it.mode == ShareMode.EQUAL }
        val remaining = pool.subtract(weightedTotal)
        if (equalFamily.isNotEmpty()) {
            val base = remaining.divide(
                BigDecimal.valueOf(equalFamily.size.toLong()),
                PRECISION,
                RoundingMode.HALF_UP
            )
            var adjustmentsTotal = ZERO
            adjustments.forEach { participant ->
                val amount = base.add(participant.adjustment ?: ZERO)
                raw[participant.memberId] = amount
                adjustmentsTotal = adjustmentsTotal.add(amount)
            }
            val purePool = remaining.subtract(adjustmentsTotal)
            if (pureEqual.isNotEmpty()) {
                val each = purePool.divide(
                    BigDecimal.valueOf(pureEqual.size.toLong()),
                    PRECISION,
                    RoundingMode.HALF_UP
                )
                pureEqual.forEach { participant ->
                    raw[participant.memberId] = each
                }
            } else if (purePool.signum() != 0) {
                // Every equal member has an adjustment - the leftover goes to the first one
                val first = adjustments.first().memberId
                raw[first] = (raw[first] ?: ZERO).add(purePool)
            }
        }

        // 4. Round to cents and hand out the leftover cents one by one
        val rounded = LinkedHashMap<UUID, BigDecimal>()
        raw.forEach { (memberId, value) ->
            rounded[memberId] = value.setScale(SCALE, RoundingMode.HALF_UP)
        }
        var diff = total
            .subtract(rounded.values.fold(ZERO) { acc, value -> acc.add(value) })
            .setScale(SCALE, RoundingMode.HALF_UP)
        if (diff.signum() != 0 && rounded.isNotEmpty()) {
            // Equal members absorb the rounding first, exact amounts last
            val priority = buildList {
                addAll(pureEqual.map { it.memberId })
                addAll(adjustments.map { it.memberId })
                addAll(weighted.map { it.memberId })
                addAll(participants.filter { it.mode == ShareMode.EXACT }.map { it.memberId })
            }.filter { rounded.containsKey(it) }
            var index = 0
            var guard = 0
            val maxSteps = priority.size * 2 + 2
            while (diff.signum() != 0 && priority.isNotEmpty() && guard < maxSteps) {
                val key = priority[index % priority.size]
                val step = if (diff.signum() > 0) CENT else CENT.negate()
                rounded[key] = rounded.getValue(key).add(step)
                diff = diff.subtract(step)
                index++
                guard++
            }
        }
        return rounded
    }

    /**
     * Net balances for a multi-payer expense.
     *
     * A member's balance is "what they paid" minus "what they owe".
     * Positive = the group owes them.
     *
     * @return amount owed to each member (positive = the group owes them).
     */
    fun multiPayerBalances(
        total: BigDecimal,
        payers: List<Payer>,
        participants: List<SplitParticipant>,
    ): Map<UUID, BigDecimal> {
        require(payers.isNotEmpty()) { "An expense needs at least one payer" }
        val paidTotal = payers.fold(ZERO) { acc, payer -> acc.add(payer.amount) }
        val effectiveTotal = if (total.signum() > 0) total else paidTotal

        // How much of the expense each participant is responsible for
        val owed = split(effectiveTotal, participants)

        val balances = LinkedHashMap<UUID, BigDecimal>()
        payers.forEach { payer ->
            balances[payer.memberId] = (balances[payer.memberId] ?: ZERO).add(payer.amount)
        }
        owed.forEach { (memberId, owedAmount) ->
            balances[memberId] = (balances[memberId] ?: ZERO).subtract(owedAmount)
        }

        // The group's net must always be zero - if the payers didn't cover the
        // tracked total, the difference lands on the first payer.
        val sum = balances.values.fold(ZERO) { acc, value -> acc.add(value) }
        if (sum.signum() != 0 && balances.isNotEmpty()) {
            val firstKey = balances.keys.first()
            balances[firstKey] = balances.getValue(firstKey).subtract(sum)
                .setScale(SCALE, RoundingMode.HALF_UP)
        }
        return balances
    }

    /**
     * Greedy min-cash-flow: the fewest transfers that clear every balance.
     *
     * @param net amount owed to each member (positive = they should receive it).
     */
    fun simplify(net: Map<UUID, BigDecimal>): List<Transfer> {
        val creditors = net.filterValues { it > ZERO }
            .map { it.key to it.value }
            .toMutableList()
        val debtors = net.filterValues { it < ZERO }
            .map { it.key to it.value }
            .toMutableList()
        val transfers = mutableListOf<Transfer>()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val creditor = creditors.maxByOrNull { it.second } ?: break
            val debtor = debtors.minByOrNull { it.second } ?: break

            val amount = minOf(creditor.second, debtor.second.negate())
            if (amount > ZERO) {
                transfers.add(
                    Transfer(
                        fromMemberId = debtor.first,
                        toMemberId = creditor.first,
                        amount = amount.setScale(SCALE, RoundingMode.HALF_UP)
                    )
                )
            }

            val updatedCreditor = creditor.second.subtract(amount)
            val updatedDebtor = debtor.second.add(amount)
            if (updatedCreditor <= ZERO) {
                creditors.remove(creditor)
            } else {
                creditors[creditors.indexOf(creditor)] = creditor.first to updatedCreditor
            }
            if (updatedDebtor >= ZERO) {
                debtors.remove(debtor)
            } else {
                debtors[debtors.indexOf(debtor)] = debtor.first to updatedDebtor
            }
        }
        return transfers
    }
}
