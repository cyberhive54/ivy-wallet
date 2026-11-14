package com.ivy.domain.split

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Bundled static FX table for settle-time conversion.
 *
 * The app is offline-first, so no network call is ever needed to settle a
 * group. The table below is a bundled snapshot (rates per 1 [BASE]);
 * users may refresh it online, but that is never required.
 *
 * Currencies not in the table fall back to a 1:1 conversion with the base
 * currency, so an unknown currency never breaks a settlement.
 */
object FxTable {

    /** The currency all rates are relative to. */
    const val BASE = "USD"

    /** Units of the currency per 1 [BASE]. */
    private val rates: Map<String, BigDecimal> = mapOf(
        "USD" to BigDecimal("1"),
        "EUR" to BigDecimal("0.92"),
        "GBP" to BigDecimal("0.79"),
        "INR" to BigDecimal("83.20"),
        "JPY" to BigDecimal("151.40"),
        "AUD" to BigDecimal("1.51"),
        "CAD" to BigDecimal("1.36"),
        "CHF" to BigDecimal("0.88"),
        "CNY" to BigDecimal("7.24"),
        "HKD" to BigDecimal("7.82"),
        "SGD" to BigDecimal("1.34"),
        "NZD" to BigDecimal("1.65"),
        "SEK" to BigDecimal("10.55"),
        "NOK" to BigDecimal("10.80"),
        "DKK" to BigDecimal("6.87"),
        "PLN" to BigDecimal("3.98"),
        "CZK" to BigDecimal("23.10"),
        "RUB" to BigDecimal("92.50"),
        "TRY" to BigDecimal("33.40"),
        "ZAR" to BigDecimal("18.60"),
        "BRL" to BigDecimal("5.42"),
        "MXN" to BigDecimal("18.10"),
        "AED" to BigDecimal("3.67"),
        "SAR" to BigDecimal("3.75"),
        "THB" to BigDecimal("35.60"),
    )

    /** All currencies the bundled table can convert. */
    val currencies: List<String> = listOf(BASE) + rates.keys.filter { it != BASE }

    /** The bundled rate for [currency] (units per 1 [BASE]). */
    fun rateOf(currency: String): BigDecimal = rates[currency.uppercase()] ?: BigDecimal.ONE

    /** True when the bundled table knows [currency]. */
    fun supports(currency: String): Boolean = rates.containsKey(currency.uppercase())

    /**
     * Converts [amount] from [from] to [to] using the bundled rates.
     *
     * The rates are quoted as "units of the currency per 1 [BASE]", so the
     * conversion goes through the base currency:
     * `amount -> amount / rate[from] -> * rate[to]`.
     *
     * Unknown currencies are treated as 1:1 with the base currency.
     */
    fun convert(
        amount: BigDecimal,
        from: String,
        to: String,
    ): BigDecimal {
        val fromRate = rateOf(from)
        val toRate = rateOf(to)
        if (fromRate == BigDecimal.ONE && toRate == BigDecimal.ONE) return amount
        return amount
            .multiply(toRate)
            .divide(fromRate, 2, RoundingMode.HALF_UP)
    }
}
