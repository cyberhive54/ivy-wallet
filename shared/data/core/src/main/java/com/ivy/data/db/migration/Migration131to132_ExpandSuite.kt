package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the expand-suite columns and tables:
 *
 * Column additions (all nullable — no backfill) + NOT NULL-with-default columns
 * whose defaults declare through @ColumnInfo on the entity (Room certifies
 * defaultValue, so the ALTERs must carry that exact default):
 * - loans.customerPhone / counterpartyName
 * - loan_records.recordDueDate / recordPhone / attachmentUrl
 * - planned_payment_rules.remainingOccurrences / icon / paymentMethodNote / cardOrMethod
 * - planned_payment_rules.isTrial            — NOT NULL DEFAULT 0
 * - planned_payment_rules.trialEndsAt        — nullable
 * - planned_payment_rules.reminderLeadDays   — NOT NULL DEFAULT 7
 * - planned_payment_rules.paused             — NOT NULL DEFAULT 0
 * - planned_payment_rules.archived           — NOT NULL DEFAULT 0
 *
 * New tables (DDL exactly as Room generates for v132; no defaults, no indexes):
 * - planned_payment_price_history, groups, group_members, split_expenses,
 *   split_expense_participants, split_settlements, split_comments
 *
 * Schema JSON for 132 is exported by Room at build time; do not hand-edit.
 */
class Migration131to132_ExpandSuite : Migration(131, 132) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE loans ADD COLUMN customerPhone TEXT")
        db.execSQL("ALTER TABLE loans ADD COLUMN counterpartyName TEXT")

        db.execSQL("ALTER TABLE loan_records ADD COLUMN recordDueDate INTEGER")
        db.execSQL("ALTER TABLE loan_records ADD COLUMN recordPhone TEXT")
        db.execSQL("ALTER TABLE loan_records ADD COLUMN attachmentUrl TEXT")

        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN remainingOccurrences INTEGER")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN icon TEXT")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN paymentMethodNote TEXT")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN cardOrMethod TEXT")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN isTrial INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN trialEndsAt INTEGER")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN reminderLeadDays INTEGER NOT NULL DEFAULT 7")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN paused INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE planned_payment_rules ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `planned_payment_price_history` (
                `ruleId` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `effectiveFrom` INTEGER NOT NULL,
                `source` TEXT NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `groups` (
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `currency` TEXT NOT NULL,
                `archived` INTEGER NOT NULL,
                `note` TEXT,
                `createdAt` INTEGER NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `group_members` (
                `groupId` TEXT NOT NULL,
                `memberName` TEXT NOT NULL,
                `memberIsYou` INTEGER NOT NULL,
                `memberColor` INTEGER NOT NULL,
                `memberPhone` TEXT,
                `avatarId` TEXT,
                `orderNum` REAL NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `split_expenses` (
                `groupId` TEXT NOT NULL,
                `paidByMemberId` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `currency` TEXT NOT NULL,
                `note` TEXT,
                `date` INTEGER NOT NULL,
                `categoryId` TEXT,
                `attachmentUrl` TEXT,
                `groupRecurringRuleId` TEXT,
                `settled` INTEGER NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `split_expense_participants` (
                `expenseId` TEXT NOT NULL,
                `memberId` TEXT NOT NULL,
                `shareMode` TEXT NOT NULL,
                `shareValue` REAL,
                `owedAmount` REAL NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `split_settlements` (
                `groupId` TEXT NOT NULL,
                `fromMemberId` TEXT,
                `toMemberId` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `currency` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `linkedTransactionId` TEXT,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `split_comments` (
                `expenseId` TEXT NOT NULL,
                `authorMemberId` TEXT NOT NULL,
                `text` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `visibility` TEXT NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
    }
}
