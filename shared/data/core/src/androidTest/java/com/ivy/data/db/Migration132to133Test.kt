package com.ivy.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ivy.data.db.migration.Migration132to133_SplitlyMembers
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Verifies the 132 -> 133 Splitly member migration, including the new
 * multi-payer table. DDL must match the exported 133.json exactly.
 */
@RunWith(AndroidJUnit4::class)
class Migration132to133Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        IvyRoomDatabase::class.java,
        listOf(IvyRoomDatabase.DeleteSEMigration()),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate132to133_preservesMembersAndAddsPayersTable() {
        val groupId = UUID.randomUUID().toString()
        val memberId = UUID.randomUUID().toString()

        helper.createDatabase(TestDb, 132).apply {
            execSQL(
                """
                INSERT INTO groups (name, type, currency, archived, createdAt, id)
                VALUES ('Goa Trip', 'TRIP', 'INR', 0, 1700000000000, '$groupId')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO group_members
                    (groupId, memberName, memberIsYou, memberColor, orderNum, id)
                VALUES ('$groupId', 'You', 1, 0, 0.0, '$memberId')
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TestDb,
            133,
            true,
            Migration132to133_SplitlyMembers()
        )

        // Existing member row is preserved with defaults on the new columns
        db.query("SELECT memberName, archived FROM group_members WHERE id = '$memberId'").use { cursor ->
            cursor.moveToFirst() shouldBe true
            cursor.getString(0) shouldBe "You"
            cursor.getInt(1) shouldBe 0
        }

        // The new multi-payer table exists and round-trips a row
        val expenseId = UUID.randomUUID().toString()
        val payerId = UUID.randomUUID().toString()
        db.execSQL(
            """
            INSERT INTO split_expense_payers (expenseId, memberId, amount, id)
            VALUES ('$expenseId', '$payerId', 1200.0, '${UUID.randomUUID()}')
            """.trimIndent()
        )
        db.query("SELECT COUNT(*) FROM split_expense_payers").use { cursor ->
            cursor.moveToFirst() shouldBe true
            cursor.getInt(0) shouldBe 1
        }
        db.close()
    }

    companion object {
        private const val TestDb = "migration-132-133-test"
    }
}
