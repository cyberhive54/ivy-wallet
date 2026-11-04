package com.ivy.base.model

/**
 * Optional necessity classification for EXPENSE transactions.
 *
 * - [UNKNOWN] The user hasn't classified the expense yet.
 * - [MUST]    Essential (rent, food, medicine, bills).
 * - [NEED]    Important but arguably optional.
 * - [WANT]    Discretionary spending.
 *
 * Stored on [com.ivy.data.db.entity.TransactionEntity] as the enum name.
 * Null means "not classified" (e.g. income/transfer transactions, or legacy rows).
 */
enum class Necessity {
    UNKNOWN,
    MUST,
    NEED,
    WANT,
    ;

    companion object {
        val ALL = listOf(UNKNOWN, MUST, NEED, WANT)

        fun from(raw: String?): Necessity? {
            if (raw == null) return null
            val normalized = raw.trim()
            if (normalized.isEmpty()) return null
            return entries.firstOrNull {
                it.name.equals(normalized, ignoreCase = true)
            }
        }
    }
}
