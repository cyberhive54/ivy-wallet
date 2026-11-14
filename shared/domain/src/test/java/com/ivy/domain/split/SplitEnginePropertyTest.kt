package com.ivy.domain.split

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class SplitEnginePropertyTest {

    private fun memberId(seed: Int): UUID = UUID(0, seed.toLong())

    private fun money(value: Double): BigDecimal =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)

    @Test
    fun `property - the shares always add up to the total`() = runTest {
        checkAll(
            Arb.double(1.0, 1_000_000.0),
            Arb.list(Arb.int(1, 5), 1..5)
        ) { rawTotal, memberSeeds ->
            val total = money(rawTotal)
            val participants = memberSeeds.distinct().map { seed ->
                SplitParticipant(memberId = memberId(seed))
            }

            val shares = SplitEngine.split(total, participants)

            val sum = shares.values.fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
            sum.setScale(2, RoundingMode.HALF_UP) shouldBe total
        }
    }

    @Test
    fun `property - equal splits differ by at most a cent`() = runTest {
        checkAll(
            Arb.double(1.0, 1_000_000.0),
            Arb.list(Arb.int(1, 5), 1..5)
        ) { rawTotal, memberSeeds ->
            val total = money(rawTotal)
            val participants = memberSeeds.distinct().map { seed ->
                SplitParticipant(memberId = memberId(seed))
            }

            val shares = SplitEngine.split(total, participants)

            val amounts = shares.values.map { it.setScale(2, RoundingMode.HALF_UP) }
            val min = amounts.min()
            val max = amounts.max()
            (max.subtract(min).compareTo(BigDecimal("0.01")) <= 0) shouldBe true
        }
    }

    @Test
    fun `property - multi payer balances always add up to zero`() = runTest {
        checkAll(
            Arb.double(1.0, 100_000.0),
            Arb.list(Arb.int(1, 4), 1..4)
        ) { rawTotal, memberSeeds ->
            val total = money(rawTotal)
            val participants = memberSeeds.distinct().map { seed ->
                SplitParticipant(memberId = memberId(seed))
            }
            // The payers cover the whole expense, split at random
            val payerCount = participants.size
            val payerMembers = participants.map { it.memberId }
            val payers = payerMembers.mapIndexed { index, member ->
                val share = total.divide(
                    BigDecimal.valueOf(payerCount.toLong()),
                    2,
                    RoundingMode.HALF_UP
                )
                val amount = if (index == payerCount - 1) {
                    total.subtract(share.multiply(BigDecimal.valueOf(payerCount - 1L)))
                } else {
                    share
                }
                Payer(memberId = member, amount = amount)
            }

            val balances = SplitEngine.multiPayerBalances(total, payers, participants)

            val sum = balances.values.fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
            sum.setScale(2, RoundingMode.HALF_UP) shouldBe BigDecimal("0.00")
        }
    }

    @Test
    fun `property - simplifying clears every balance`() = runTest {
        checkAll(
            Arb.list(Arb.double(-5000.0, 5000.0), 2..6)
        ) { amounts ->
            // A group's net always sums to zero - the last member absorbs the rest
            val rounded = amounts.map { money(it) }
            val last = rounded.size - 1
            val corrected = rounded.toMutableList()
            corrected[last] = rounded.dropLast(1)
                .fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }
                .negate()
            val net = corrected.mapIndexed { index, amount ->
                memberId(index) to amount
            }.toMap()

            val transfers = SplitEngine.simplify(net)

            // Applying the transfers must zero out every balance
            val after = net.toMutableMap()
            transfers.forEach { transfer ->
                after[transfer.fromMemberId] = (after[transfer.fromMemberId] ?: BigDecimal.ZERO)
                    .add(transfer.amount)
                after[transfer.toMemberId] = (after[transfer.toMemberId] ?: BigDecimal.ZERO)
                    .subtract(transfer.amount)
            }
            after.values.forEach { value ->
                value.abs().setScale(2, RoundingMode.HALF_UP) shouldBe BigDecimal("0.00")
            }
        }
    }

    @Test
    fun `round trip - equal split of 100 between 3 members`() {
        val a = memberId(1)
        val b = memberId(2)
        val c = memberId(3)

        val shares = SplitEngine.split(
            BigDecimal("100.00"),
            listOf(
                SplitParticipant(a),
                SplitParticipant(b),
                SplitParticipant(c),
            ),
        )

        shares[a] shouldBe BigDecimal("33.34")
        shares[b] shouldBe BigDecimal("33.33")
        shares[c] shouldBe BigDecimal("33.33")
    }

    @Test
    fun `round trip - exact plus equal shares`() {
        val a = memberId(1)
        val b = memberId(2)
        val c = memberId(3)

        val shares = SplitEngine.split(
            BigDecimal("100.00"),
            listOf(
                SplitParticipant(a, ShareMode.EXACT, exactAmount = BigDecimal("40.00")),
                SplitParticipant(b),
                SplitParticipant(c),
            ),
        )

        shares[a] shouldBe BigDecimal("40.00")
        shares[b] shouldBe BigDecimal("30.00")
        shares[c] shouldBe BigDecimal("30.00")
    }

    @Test
    fun `round trip - percentages`() {
        val a = memberId(1)
        val b = memberId(2)

        val shares = SplitEngine.split(
            BigDecimal("200.00"),
            listOf(
                SplitParticipant(a, ShareMode.PERCENT, percent = 60.0),
                SplitParticipant(b, ShareMode.PERCENT, percent = 40.0),
            ),
        )

        shares[a] shouldBe BigDecimal("120.00")
        shares[b] shouldBe BigDecimal("80.00")
    }

    @Test
    fun `round trip - weighted shares`() {
        val a = memberId(1)
        val b = memberId(2)
        val c = memberId(3)

        val shares = SplitEngine.split(
            BigDecimal("120.00"),
            listOf(
                SplitParticipant(a, ShareMode.SHARES, shares = 2),
                SplitParticipant(b, ShareMode.SHARES, shares = 1),
                SplitParticipant(c, ShareMode.SHARES, shares = 1),
            ),
        )

        shares[a] shouldBe BigDecimal("60.00")
        shares[b] shouldBe BigDecimal("30.00")
        shares[c] shouldBe BigDecimal("30.00")
    }

    @Test
    fun `round trip - adjustment deviates from the equal share`() {
        val a = memberId(1)
        val b = memberId(2)
        val c = memberId(3)

        val shares = SplitEngine.split(
            BigDecimal("90.00"),
            listOf(
                SplitParticipant(a),
                SplitParticipant(b),
                SplitParticipant(c, ShareMode.ADJUSTMENT, adjustment = BigDecimal("10.00")),
            ),
        )

        // The 10 deviation is taken from the other two equal members
        shares[c] shouldBe BigDecimal("40.00")
        shares[a] shouldBe BigDecimal("25.00")
        shares[b] shouldBe BigDecimal("25.00")
    }

    @Test
    fun `round trip - only a subset of the group takes part`() {
        val a = memberId(1)
        val b = memberId(2)
        val notInvolved = memberId(3)

        val shares = SplitEngine.split(
            BigDecimal("50.00"),
            listOf(
                SplitParticipant(a),
                SplitParticipant(b),
            ),
        )

        shares.size shouldBe 2
        shares.containsKey(notInvolved) shouldBe false
        shares[a] shouldBe BigDecimal("25.00")
        shares[b] shouldBe BigDecimal("25.00")
    }

    @Test
    fun `round trip - multi payer with two payers`() {
        val payerA = memberId(1)
        val payerB = memberId(2)
        val participantC = memberId(3)

        val balances = SplitEngine.multiPayerBalances(
            total = BigDecimal("100.00"),
            payers = listOf(
                Payer(payerA, BigDecimal("60.00")),
                Payer(payerB, BigDecimal("40.00")),
            ),
            participants = listOf(
                SplitParticipant(payerA),
                SplitParticipant(payerB),
                SplitParticipant(participantC),
            ),
        )

        // A paid 60 and owes 33.34 -> the group owes A 26.66
        balances[payerA] shouldBe BigDecimal("26.66")
        // B paid 40 and owes 33.33 -> the group owes B 6.67
        balances[payerB] shouldBe BigDecimal("6.67")
        // C owes 33.33
        balances[participantC] shouldBe BigDecimal("-33.33")
    }

    @Test
    fun `simplify - three members collapse into two transfers`() {
        val a = memberId(1)
        val b = memberId(2)
        val c = memberId(3)

        val transfers = SplitEngine.simplify(
            mapOf(
                a to BigDecimal("2100.00"),
                b to BigDecimal("-200.00"),
                c to BigDecimal("-1900.00"),
            )
        )

        transfers.size shouldBe 2
        transfers.map { it.amount }.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) } shouldBe
            BigDecimal("2100.00")
    }
}
