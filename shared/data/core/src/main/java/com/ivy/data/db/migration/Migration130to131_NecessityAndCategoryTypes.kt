package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the optional "necessity" classification to transactions and the
 * "transactionTypes" association to categories.
 *
 * - transactions.necessity: nullable TEXT (UNKNOWN/MUST/NEED/WANT). Null for
 *   income/transfer transactions and unclassified rows.
 * - categories.transactionTypes: nullable TEXT (comma-separated TransactionType names).
 *   Null/blank = usable with all types (legacy behavior).
 */
class Migration130to131_NecessityAndCategoryTypes : Migration(130, 131) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN necessity TEXT")
        db.execSQL("ALTER TABLE categories ADD COLUMN transactionTypes TEXT")
    }
}
