package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds member archive + audit fields for Splitly group management.
 *
 * The defaults must match the @ColumnInfo declarations on GroupMemberEntity
 * exactly (Room certifies the default value during migration validation).
 */
class Migration132to133_SplitlyMembers : Migration(132, 133) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE group_members ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE group_members ADD COLUMN createdAt INTEGER")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `split_expense_payers` (
                `expenseId` TEXT NOT NULL,
                `memberId` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
    }
}
