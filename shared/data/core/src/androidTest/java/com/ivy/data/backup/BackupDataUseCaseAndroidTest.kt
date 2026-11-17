package com.ivy.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ivy.base.TestDispatchersProvider
import com.ivy.base.di.KotlinxSerializationModule
import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.model.TransactionType
import com.ivy.data.DataObserver
import com.ivy.data.db.IvyRoomDatabase
import com.ivy.data.db.entity.GroupEntity
import com.ivy.data.db.entity.GroupMemberEntity
import com.ivy.data.db.entity.LoanEntity
import com.ivy.data.db.entity.LoanRecordEntity
import com.ivy.data.db.entity.PlannedPaymentPriceHistoryEntity
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.db.entity.SplitCommentEntity
import com.ivy.data.db.entity.SplitExpenseEntity
import com.ivy.data.db.entity.SplitExpenseParticipantEntity
import com.ivy.data.db.entity.SplitSettlementEntity
import com.ivy.data.file.FileSystem
import com.ivy.data.model.IntervalType
import com.ivy.data.model.LoanType
import com.ivy.base.model.LoanRecordType
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.fake.fakeRepositoryMemoFactory
import com.ivy.data.repository.mapper.AccountMapper
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BackupDataUseCaseAndroidTest {

    private lateinit var db: IvyRoomDatabase
    private lateinit var useCase: BackupDataUseCase
    private lateinit var appContext: Context

    @Before
    fun createDb() {
        buildDb()
    }

    private fun buildDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        appContext = context
        db = Room.inMemoryDatabaseBuilder(context, IvyRoomDatabase::class.java).build()
        val instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        val accountMapper = AccountMapper(
            currencyRepository = CurrencyRepository(
                settingsDao = db.settingsDao,
                writeSettingsDao = db.writeSettingsDao,
                dispatchersProvider = TestDispatchersProvider,
            )
        )
        useCase = BackupDataUseCase(
            accountDao = db.accountDao,
            budgetDao = db.budgetDao,
            categoryDao = db.categoryDao,
            loanRecordDao = db.loanRecordDao,
            loanDao = db.loanDao,
            plannedPaymentRuleDao = db.plannedPaymentRuleDao,
            plannedPaymentPriceHistoryDao = db.plannedPaymentPriceHistoryDao,
            groupDao = db.groupDao,
            groupMemberDao = db.groupMemberDao,
            splitExpenseDao = db.splitExpenseDao,
            splitExpenseParticipantDao = db.splitExpenseParticipantDao,
            splitExpensePayerDao = db.splitExpensePayerDao,
            splitSettlementDao = db.splitSettlementDao,
            splitCommentDao = db.splitCommentDao,
            settingsDao = db.settingsDao,
            transactionDao = db.transactionDao,
            transactionWriter = db.writeTransactionDao,
            sharedPrefs = SharedPrefs(appContext),
            accountRepository = AccountRepository(
                accountDao = db.accountDao,
                writeAccountDao = db.writeAccountDao,
                mapper = accountMapper,
                dispatchersProvider = TestDispatchersProvider,
                memoFactory = fakeRepositoryMemoFactory(),
            ),
            accountMapper = accountMapper,
            categoryWriter = db.writeCategoryDao,
            settingsWriter = db.writeSettingsDao,
            budgetWriter = db.writeBudgetDao,
            loanWriter = db.writeLoanDao,
            loanRecordWriter = db.writeLoanRecordDao,
            plannedPaymentRuleWriter = db.writePlannedPaymentRuleDao,
            plannedPaymentPriceHistoryWriter = db.writePlannedPaymentPriceHistoryDao,
            groupWriter = db.writeGroupDao,
            groupMemberWriter = db.writeGroupMemberDao,
            splitExpenseWriter = db.writeSplitExpenseDao,
            splitExpenseParticipantWriter = db.writeSplitExpenseParticipantDao,
            splitExpensePayerWriter = db.writeSplitExpensePayerDao,
            splitSettlementWriter = db.writeSplitSettlementDao,
            splitCommentWriter = db.writeSplitCommentDao,
            context = instrumentationContext,
            json = KotlinxSerializationModule.provideJson(),
            dispatchersProvider = TestDispatchersProvider,
            fileSystem = FileSystem(instrumentationContext),
            dataObserver = DataObserver(),
            tagsReader = db.tagDao,
            tagAssociationReader = db.tagAssociationDao,
            tagsWriter = db.writeTagDao,
            tagAssociationWriter = db.writeTagAssociationDao
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun backup450_150(): Unit = runBlocking {
        backupTestCase("450-150")
    }

    @Test
    fun backup132_expand_suite_roundtrip(): Unit = runBlocking {
        // ── seed ──────────────────────────────────────────
        val accountId = UUID.randomUUID()
        val catId = UUID.randomUUID()
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        val subscription = PlannedPaymentRuleEntity(
            startDate = now,
            intervalN = 1,
            intervalType = IntervalType.MONTH,
            oneTime = false,
            type = TransactionType.EXPENSE,
            accountId = accountId,
            amount = 999.0,
            categoryId = catId,
            title = "Netflix",
            remainingOccurrences = 3,
            icon = "film",
            paymentMethodNote = "HDFC 4932",
            cardOrMethod = "HDFC",
            isTrial = true,
            trialEndsAt = now.plus(12, ChronoUnit.DAYS),
            reminderLeadDays = 3,
            paused = false,
            archived = false,
        )
        db.writePlannedPaymentRuleDao.save(subscription)
        db.writePlannedPaymentPriceHistoryDao.saveMany(
            listOf(
                PlannedPaymentPriceHistoryEntity(
                    ruleId = subscription.id, amount = 899.0,
                    effectiveFrom = now, source = "created",
                ),
                PlannedPaymentPriceHistoryEntity(
                    ruleId = subscription.id, amount = 999.0,
                    effectiveFrom = now.plus(1, ChronoUnit.DAYS), source = "edited",
                ),
            )
        )

        val loan = LoanEntity(
            name = "Rahul",
            amount = 500.0,
            type = LoanType.LEND,
            accountId = accountId,
            customerPhone = "9876543210",
            counterpartyName = "Rahul S",
        )
        db.writeLoanDao.save(loan)
        db.loanRecordDao.findAll() // exercise the read DAO to be safe
        val loanRecord = LoanRecordEntity(
            loanId = loan.id,
            amount = 500.0,
            dateTime = now,
            loanRecordType = LoanRecordType.INCREASE,
            recordDueDate = now.plus(7, ChronoUnit.DAYS),
            recordPhone = "9990001111",
            attachmentUrl = "receipts/test.jpg",
        )
        db.writeLoanRecordDao.save(loanRecord)

        val groupId = UUID.randomUUID()
        val meId = UUID.randomUUID()
        val friendId = UUID.randomUUID()
        val expenseId = UUID.randomUUID()
        db.writeGroupDao.save(
            GroupEntity(
                name = "Goa Trip",
                type = "TRIP",
                currency = "INR",
                id = groupId,
            )
        )
        db.writeGroupMemberDao.saveMany(
            listOf(
                GroupMemberEntity(
                    groupId = groupId,
                    memberName = "You",
                    memberIsYou = true,
                    id = meId,
                ),
                GroupMemberEntity(
                    groupId = groupId,
                    memberName = "Priya",
                    id = friendId,
                ),
            )
        )
        db.writeSplitExpenseDao.save(
            SplitExpenseEntity(
                groupId = groupId,
                paidByMemberId = meId,
                amount = 12000.0,
                currency = "INR",
                note = "Hotel",
                date = now,
                id = expenseId,
            )
        )
        db.writeSplitExpenseParticipantDao.save(
            SplitExpenseParticipantEntity(
                expenseId = expenseId,
                memberId = meId,
                shareMode = "EQUAL",
                owedAmount = 6000.0,
            )
        )
        db.writeSplitExpensePayerDao.save(
            com.ivy.data.db.entity.SplitExpensePayerEntity(
                expenseId = expenseId,
                memberId = meId,
                amount = 12000.0,
            )
        )
        db.writeSplitSettlementDao.save(
            SplitSettlementEntity(
                groupId = groupId,
                fromMemberId = friendId,
                toMemberId = meId,
                amount = 6000.0,
                currency = "INR",
                createdAt = now,
            )
        )
        db.writeSplitCommentDao.save(
            SplitCommentEntity(
                expenseId = expenseId,
                authorMemberId = friendId,
                text = "huge thanks",
                createdAt = now,
                visibility = "PUBLIC",
            )
        )

        val prefs = SharedPrefs(appContext)
        prefs.putString(SharedPrefs.WORKSPACE, "LEDGER")
        prefs.putBoolean(SharedPrefs.LEDGER_WORKSPACE_ENABLED, false)
        prefs.putBoolean(SharedPrefs.SPLITLY_WORKSPACE_ENABLED, true)

        // ── export ────────────────────────────────────────
        val exported = tempAndroidFile("v132-roundtrip", ".zip").toUri()
        useCase.exportToFile(exported)

        // ── restore into a fresh db ───────────────────────
        db.close()
        buildDb()

        // wipe toggles so the restore genuinely rewrites them
        prefs.putString(SharedPrefs.WORKSPACE, "FINANCE")
        prefs.putBoolean(SharedPrefs.LEDGER_WORKSPACE_ENABLED, true)
        prefs.putBoolean(SharedPrefs.SPLITLY_WORKSPACE_ENABLED, false)

        val res = useCase.importBackupFile(exported, onProgress = {})
        res.failedRows.shouldBeEmpty()

        // ── assert ────────────────────────────────────────
        db.plannedPaymentRuleDao.findById(subscription.id).shouldNotBeNull()
        val restoredRule = db.plannedPaymentRuleDao.findById(subscription.id)!!
        restoredRule.icon shouldBe "film"
        restoredRule.paymentMethodNote shouldBe "HDFC 4932"
        restoredRule.cardOrMethod shouldBe "HDFC"
        restoredRule.reminderLeadDays shouldBe 3
        restoredRule.remainingOccurrences shouldBe 3
        restoredRule.isTrial shouldBe true

        val history = db.plannedPaymentPriceHistoryDao.findByRuleId(subscription.id)
        history.size shouldBe 2
        history.map { it.amount } shouldBe listOf(999.0, 899.0) // ordered by effectiveFrom DESC

        val restoredLoan = db.loanDao.findById(loan.id).shouldNotBeNull()
        restoredLoan.customerPhone shouldBe "9876543210"
        restoredLoan.counterpartyName shouldBe "Rahul S"
        val restoredRecords = db.loanRecordDao.findAll().filter { it.loanId == loan.id }
        restoredRecords.size shouldBe 1
        restoredRecords[0].recordPhone shouldBe "9990001111"
        restoredRecords[0].attachmentUrl shouldBe "receipts/test.jpg"

        db.groupDao.findById(groupId).shouldNotBeNull()
        db.groupMemberDao.findByGroupId(groupId).size shouldBe 2
        val expenses = db.splitExpenseDao.findByGroupId(groupId)
        expenses.size shouldBe 1
        expenses[0].note shouldBe "Hotel"
        db.splitExpenseParticipantDao.findAll().size shouldBe 1
        db.splitExpensePayerDao.findByExpenseId(expenseId).size shouldBe 1
        db.splitSettlementDao.findByGroupId(groupId).size shouldBe 1
        val comments = db.splitCommentDao.findAll().filter { it.expenseId == expenseId }
        comments.size shouldBe 1

        // toggle persistence (sharedPrefs whitelist round-trip)
        prefs.getString(SharedPrefs.WORKSPACE, "FINANCE") shouldBe "LEDGER"
        prefs.getBoolean(SharedPrefs.LEDGER_WORKSPACE_ENABLED, true) shouldBe false
        prefs.getBoolean(SharedPrefs.SPLITLY_WORKSPACE_ENABLED, false) shouldBe true
    }

    private suspend fun backupTestCase(version: String) {
        importBackupZipTestCase(version)
        importBackupJsonTestCase(version)

        // close and re-open the db to ensure fresh data
        closeDb()
        createDb()
        exportsAndImportsTestCase(version)
    }

    private suspend fun importBackupZipTestCase(version: String) {
        // given
        val backupUri = copyTestResourceToInternalStorage("backups/$version.zip")

        // when
        val res = useCase.importBackupFile(backupUri, onProgress = {})

        // then
        res.shouldBeSuccessful()
    }

    private suspend fun importBackupJsonTestCase(version: String) {
        // given
        val backupUri = copyTestResourceToInternalStorage("backups/$version.json")

        // when
        val res = useCase.importBackupFile(backupUri, onProgress = {})

        // then
        res.shouldBeSuccessful()
    }

    private suspend fun exportsAndImportsTestCase(version: String) {
        // given
        val backupUri = copyTestResourceToInternalStorage("backups/$version.zip")
        // preload data
        useCase.importBackupFile(backupUri, onProgress = {}).shouldBeSuccessful()
        val exportedFileUri = tempAndroidFile("exported", ".zip").toUri()

        // then
        useCase.exportToFile(exportedFileUri)
        val reImportRes = useCase.importBackupFile(backupUri, onProgress = {})

        // then
        reImportRes.shouldBeSuccessful()
    }

    private fun copyTestResourceToInternalStorage(resPath: String): Uri {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assetManager = context.assets
        val inputStream = assetManager.open(resPath)
        val outputFile = tempAndroidFile("temp-backup", resPath.split(".").last())
        outputFile.outputStream().use { fileOut ->
            fileOut.write(inputStream.readBytes())
        }
        return Uri.fromFile(outputFile)
    }

    private fun tempAndroidFile(prefix: String, suffix: String): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return File.createTempFile(prefix, suffix, context.filesDir)
    }

    private fun ImportResult.shouldBeSuccessful() {
        failedRows.shouldBeEmpty()
        categoriesImported shouldBeGreaterThan 0
        accountsImported shouldBeGreaterThan 0
        transactionsImported shouldBeGreaterThan 0
    }
}
