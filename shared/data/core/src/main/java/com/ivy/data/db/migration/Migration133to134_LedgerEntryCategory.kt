package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds category tagging to Ledger (udhaar) entries: a nullable category id on
 * loan_records, with no backfill (existing entries remain untagged).
 */
class Migration133to134_LedgerEntryCategory : Migration(133, 134) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE loan_records ADD COLUMN categoryId TEXT")
    }
}
