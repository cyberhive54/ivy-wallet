package com.ivy.domain.split

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

class GroupLedgerTest {

    private fun id(seed: Int): UUID = UUID(0, seed.toLong())

    @Test
    fun `balances - the payer is owed what the others owe`() {
        val you = id(1)
        val rahul = id(2)

        val balances = GroupLedger.balances(
            groupCurrency = "INR",
            expenses = listOf(
                LedgerExpense(
                    id = id(10),
                    currency = "INR",
                    amount = BigDecimal("100.00"),
                    paidBy = mapOf(you to BigDecimal("100.00")),
                    owed = mapOf(
                        you to BigDecimal("50.00"),
                        rahul to BigDecimal("50.00"),
                    ),
                )
            ),
            settlements = emptyList(),
        )

        balances[you] shouldBe BigDecimal("50.00")
        balances[rahul] shouldBe BigDecimal("-50.00")
    }

    @Test
    fun `balances - a settlement clears the balance`() {
        val you = id(1)
        val rahul = id(2)

        val balances = GroupLedger.balances(
            groupCurrency = "INR",
            expenses = listOf(
                LedgerExpense(
                    id = id(10),
                    currency = "INR",
                    amount = BigDecimal("100.00"),
                    paidBy = mapOf(you to BigDecimal("100.00")),
                    owed = mapOf(
                        you to BigDecimal("50.00"),
                        rahul to BigDecimal("50.00"),
                    ),
                )
            ),
            settlements = listOf(
                LedgerSettlement(
                    id = id(20),
                    currency = "INR",
                    amount = BigDecimal("50.00"),
                    fromMemberId = rahul,
                    toMemberId = you,
                )
            ),
        )

        balances[you] shouldBe BigDecimal("0.00")
        balances[rahul] shouldBe BigDecimal("0.00")
    }

    @Test
    fun `balances - foreign currencies are converted with the bundled table`() {
        val you = id(1)
        val rahul = id(2)

        val balances = GroupLedger.balances(
            groupCurrency = "USD",
            expenses = listOf(
                LedgerExpense(
                    id = id(10),
                    currency = "EUR",
                    amount = BigDecimal("100.00"),
                    paidBy = mapOf(you to BigDecimal("100.00")),
                    owed = mapOf(
                        you to BigDecimal("50.00"),
                        rahul to BigDecimal("50.00"),
                    ),
                )
            ),
            settlements = emptyList(),
        )

        // 50 EUR -> USD = 50 / 0.92 = 54.35
        balances[rahul] shouldBe BigDecimal("-54.35")
        balances[you] shouldBe BigDecimal("54.35")
    }

    @Test
    fun `fx - the bundled table converts between 25 currencies`() {
        FxTable.currencies.size shouldBe 25
        FxTable.supports("inr") shouldBe true
        FxTable.supports("XXX") shouldBe false

        // 100 INR -> USD = 100 / 83.20 = 1.20
        FxTable.convert(BigDecimal("100.00"), "INR", "USD") shouldBe BigDecimal("1.20")
        // same currency is a no-op
        FxTable.convert(BigDecimal("100.00"), "INR", "INR") shouldBe BigDecimal("100.00")
        // unknown currency falls back to 1:1
        FxTable.convert(BigDecimal("100.00"), "XXX", "USD") shouldBe BigDecimal("100.00")
    }
}
