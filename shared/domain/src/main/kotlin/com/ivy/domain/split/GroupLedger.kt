package com.ivy.domain.split

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/** One expense as seen by the ledger (already converted to the group currency). */
data class LedgerExpense(
    val id: UUID,
    val currency: String,
    val amount: BigDecimal,
    /** memberId -> how much they paid (multi-payer supported). */
    val paidBy: Map<UUID, BigDecimal>,
    /** memberId -> how much of the expense they owe. */
    val owed: Map<UUID, BigDecimal>,
)

/** One settlement between two members. */
data class LedgerSettlement(
    val id: UUID,
    val currency: String,
    val amount: BigDecimal,
    val fromMemberId: UUID,
    val toMemberId: UUID,
)

/**
 * Pure group ledger.
 *
 * A member's balance is positive when the group owes them money and negative
 * when they owe the group. All amounts are converted into the group currency
 * with the bundled [FxTable] (offline, no network).
 */
object GroupLedger {

    /**
     * Computes the net balance of every member of a group.
     *
     * @param groupCurrency the currency the group is settled in.
     */
    fun balances(
        groupCurrency: String,
        expenses: List<LedgerExpense>,
        settlements: List<LedgerSettlement>,
    ): Map<UUID, BigDecimal> {
        val balances = LinkedHashMap<UUID, BigDecimal>()

        fun add(memberId: UUID, amount: BigDecimal) {
            balances[memberId] = (balances[memberId] ?: BigDecimal.ZERO).add(amount)
        }

        expenses.forEach { expense ->
            expense.paidBy.forEach { (memberId, paid) ->
                add(memberId, convert(paid, expense.currency, groupCurrency))
            }
            expense.owed.forEach { (memberId, owed) ->
                add(memberId, -convert(owed, expense.currency, groupCurrency))
            }
        }

        settlements.forEach { settlement ->
            val amount = convert(settlement.amount, settlement.currency, groupCurrency)
            // the payer gave the money, so their debt shrinks
            add(settlement.fromMemberId, amount)
            // the receiver got the money, so their credit shrinks
            add(settlement.toMemberId, -amount)
        }

        return balances.mapValues { (_, value) ->
            value.setScale(2, RoundingMode.HALF_UP)
        }
    }

    /** Converts [amount] with the bundled FX table (settle-time rates). */
    fun convert(
        amount: BigDecimal,
        from: String,
        to: String,
    ): BigDecimal = FxTable.convert(amount, from, to)
}
