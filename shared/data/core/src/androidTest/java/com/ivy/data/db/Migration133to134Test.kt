package com.ivy.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ivy.data.db.migration.Migration133to134_LedgerEntryCategory
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies the 133 -> 134 Ledger entry category column migration. */
@RunWith(AndroidJUnit4::class)
class Migration133to134Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        IvyRoomDatabase::class.java,
        listOf(IvyRoomDatabase.DeleteSEMigration()),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate133to134_addsNullableCategoryId() {
        helper.createDatabase(TestDb, 133).close()

        val db = helper.runMigrationsAndValidate(
            TestDb,
            134,
            true,
            Migration133to134_LedgerEntryCategory()
        )

        db.query("PRAGMA table_info(loan_records)").use { cursor ->
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == "categoryId") {
                    found = true
                    cursor.getInt(3) shouldBe 0 // notNull == false
                }
            }
            found shouldBe true
        }
        db.close()
    }

    companion object {
        private const val TestDb = "migration-133-134-test"
    }
}
