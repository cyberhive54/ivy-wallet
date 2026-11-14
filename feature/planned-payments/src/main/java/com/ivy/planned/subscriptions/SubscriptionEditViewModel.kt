package com.ivy.planned.subscriptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeConverter
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WritePlannedPaymentPriceHistoryDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.db.entity.PlannedPaymentPriceHistoryEntity
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.IntervalType
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.TransactionRepository
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.datamodel.PlannedPaymentRule
import com.ivy.legacy.datamodel.temp.toLegacyDomain
import com.ivy.legacy.domain.deprecated.logic.AccountCreator
import com.ivy.legacy.utils.ioThread
import com.ivy.navigation.Navigation
import com.ivy.navigation.SubscriptionEditScreen
import com.ivy.planned.subscriptions.SubscriptionEditScreenEvent.*
import com.ivy.ui.ComposeViewModel
import com.ivy.wallet.domain.action.account.AccountsAct
import com.ivy.wallet.domain.deprecated.logic.CategoryCreator
import com.ivy.wallet.domain.deprecated.logic.PlannedPaymentsGenerator
import com.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import com.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import com.ivy.wallet.ui.theme.modal.RecurringRuleModalData
import com.ivy.wallet.ui.theme.modal.edit.AccountModalData
import com.ivy.wallet.ui.theme.modal.edit.CategoryModalData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Dedicated subscription editor. A subscription IS a recurring EXPENSE planned
 * payment; this viewmodel writes price-history rows on final save so the board
 * can surface hike badges.
 */
@Stable
@HiltViewModel
class SubscriptionEditViewModel @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryRepository: CategoryRepository,
    private val settingsDao: SettingsDao,
    private val nav: Navigation,
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val plannedPaymentPriceHistoryWriter: WritePlannedPaymentPriceHistoryDao,
    private val plannedPaymentsGenerator: PlannedPaymentsGenerator,
    private val categoryCreator: CategoryCreator,
    private val accountCreator: AccountCreator,
    private val accountsAct: AccountsAct,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val transactionRepository: TransactionRepository,
    private val timeConverter: TimeConverter,
) : ComposeViewModel<SubscriptionEditScreenState, SubscriptionEditScreenEvent>() {

    private var transactionType by mutableStateOf(TransactionType.EXPENSE)
    private var startDate by mutableStateOf<LocalDateTime?>(null)
    private var intervalN by mutableStateOf<Int?>(null)
    private var intervalType by mutableStateOf<IntervalType?>(null)
    private var oneTime by mutableStateOf(false)
    private var initialTitle by mutableStateOf<String?>(null)
    private var description by mutableStateOf<String?>(null)
    private var account by mutableStateOf<Account?>(null)
    private var category by mutableStateOf<Category?>(null)
    private var amount by mutableDoubleStateOf(0.0)
    private var currency by mutableStateOf("")
    private var categories by mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private var accounts by mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private var categoryModalVisible by mutableStateOf(false)
    private var descriptionModalVisible by mutableStateOf(false)
    private var deleteTransactionModalVisible by mutableStateOf(false)
    private var amountModalVisible by mutableStateOf(false)
    private var recurringRuleModalData by mutableStateOf<RecurringRuleModalData?>(null)
    private var categoryModalData by mutableStateOf<CategoryModalData?>(null)
    private var accountModalData by mutableStateOf<AccountModalData?>(null)

    // --- Subscription-specific ---
    private var iconKey by mutableStateOf<String?>(null)
    private var paymentMethodNote by mutableStateOf<String?>(null)
    private var reminderLeadDays by mutableIntStateOf(7)
    private var isTrial by mutableStateOf(false)
    private var trialEndsAt by mutableStateOf<LocalDateTime?>(null)
    private var remainingOccurrences by mutableStateOf<Int?>(null)
    private var paused by mutableStateOf(false)
    private var archived by mutableStateOf(false)

    private var loadedRule: PlannedPaymentRule? = null
    private var editMode = false
    private var title: String? = null

    /** Amount as last written to the database — used for price-history write-back. */
    private var lastSavedAmount: Double = 0.0

    @Composable
    override fun uiState(): SubscriptionEditScreenState {
        return SubscriptionEditScreenState(
            currency = getCurrency(),
            transactionType = getTransactionType(),
            categories = getCategories(),
            accounts = getAccounts(),
            startDate = getStartDate(),
            intervalN = getIntervalN(),
            intervalType = getIntervalType(),
            oneTime = getOneTime(),
            initialTitle = getInitialTitle(),
            description = getDescription(),
            account = getAccount(),
            category = getCategory(),
            amount = getAmount(),
            categoryModalVisible = getCategoryModalVisibility(),
            categoryModalData = getCategoryModalData(),
            accountModalData = getAccountModalData(),
            deleteTransactionModalVisible = getDeleteTransactionModalVisibility(),
            descriptionModalVisible = getDescriptionModalVisibility(),
            amountModalVisible = getAmountModalVisibility(),
            recurringRuleModalData = getRecurringRuleModalData(),
            iconKey = getIconKey(),
            paymentMethodNote = getPaymentMethodNote(),
            reminderLeadDays = getReminderLeadDays(),
            isTrial = getIsTrial(),
            trialEndsAt = getTrialEndsAt(),
            remainingOccurrences = getRemainingOccurrences(),
            paused = getPaused(),
            archived = getArchived(),
        )
    }

    @Composable
    private fun getCurrency(): String = currency

    @Composable
    private fun getCategories(): ImmutableList<Category> = categories

    @Composable
    private fun getAccounts(): ImmutableList<Account> = accounts

    @Composable
    private fun getTransactionType(): TransactionType = transactionType

    @Composable
    private fun getStartDate(): LocalDateTime? = startDate

    @Composable
    private fun getIntervalN(): Int? = intervalN

    @Composable
    private fun getIntervalType(): IntervalType? = intervalType

    @Composable
    private fun getOneTime(): Boolean = oneTime

    @Composable
    private fun getInitialTitle(): String? = initialTitle

    @Composable
    private fun getDescription(): String? = description

    @Composable
    private fun getAccount(): Account? = account

    @Composable
    private fun getCategory(): Category? = category

    @Composable
    private fun getAmount(): Double = amount

    @Composable
    private fun getIconKey(): String? = iconKey

    @Composable
    private fun getPaymentMethodNote(): String? = paymentMethodNote

    @Composable
    private fun getReminderLeadDays(): Int = reminderLeadDays

    @Composable
    private fun getIsTrial(): Boolean = isTrial

    @Composable
    private fun getTrialEndsAt(): LocalDateTime? = trialEndsAt

    @Composable
    private fun getRemainingOccurrences(): Int? = remainingOccurrences

    @Composable
    private fun getPaused(): Boolean = paused

    @Composable
    private fun getArchived(): Boolean = archived

    @Composable
    private fun getCategoryModalVisibility(): Boolean = categoryModalVisible

    @Composable
    private fun getDescriptionModalVisibility(): Boolean = descriptionModalVisible

    @Composable
    private fun getDeleteTransactionModalVisibility(): Boolean = deleteTransactionModalVisible

    @Composable
    private fun getAmountModalVisibility(): Boolean = amountModalVisible

    @Composable
    private fun getCategoryModalData(): CategoryModalData? = categoryModalData

    @Composable
    private fun getAccountModalData(): AccountModalData? = accountModalData

    @Composable
    private fun getRecurringRuleModalData(): RecurringRuleModalData? = recurringRuleModalData

    override fun onEvent(event: SubscriptionEditScreenEvent) {
        when (event) {
            is OnSave -> save(closeScreen = event.closeScreen)
            is OnDelete -> delete()
            is OnDescriptionChanged -> updateDescription(event.newDescription)
            is OnCreateAccount -> createAccount(event.data)
            is OnCreateCategory -> createCategory(event.data)
            is OnAccountChanged -> updateAccount(event.newAccount)
            is OnAmountChanged -> updateAmount(event.newAmount)
            is OnTitleChanged -> updateTitle(event.newTitle)
            is OnRuleChanged -> updateRule(
                event.startDate,
                event.oneTime,
                event.intervalN,
                event.intervalType,
            )
            is OnCategoryChanged -> updateCategory(event.newCategory)
            is OnEditCategory -> editCategory(event.updatedCategory)
            is OnCategoryModalVisible -> categoryModalVisible = event.visible
            is OnCategoryModalDataChanged -> categoryModalData = event.categoryModalData
            is OnAccountModalDataChanged -> accountModalData = event.accountModalData
            is OnDescriptionModalVisible -> descriptionModalVisible = event.visible
            is OnAmountModalVisible -> amountModalVisible = event.visible
            is OnDeleteTransactionModalVisible -> deleteTransactionModalVisible = event.visible
            is OnRecurringRuleModalDataChanged -> recurringRuleModalData = event.recurringRuleModalData

            is OnIconChanged -> updateIcon(event.newIconKey)
            is OnPaymentMethodNoteChanged -> updatePaymentMethodNote(event.newNote)
            is OnReminderLeadDaysChanged -> updateReminderLeadDays(event.days)
            is OnTrialChanged -> updateTrial(event.isTrial)
            is OnTrialEndsChanged -> updateTrialEnds(event.newTrialEndsAt)
            is OnRemainingOccurrencesChanged -> updateRemainingOccurrences(event.newRemaining)
            is OnPausedChanged -> updatePaused(event.paused)
            is OnArchivedChanged -> updateArchived(event.archived)
        }
    }

    fun start(screen: SubscriptionEditScreen) {
        viewModelScope.launch {
            transactionType = TransactionType.EXPENSE
            editMode = screen.plannedPaymentRuleId != null

            val accounts = accountsAct(Unit)
            if (accounts.isEmpty()) {
                nav.back()
                return@launch
            }
            this@SubscriptionEditViewModel.accounts = accounts
            categories = categoryRepository.findAll().toImmutableList()

            reset()

            loadedRule = screen.plannedPaymentRuleId?.let {
                ioThread { plannedPaymentRuleDao.findById(it)!!.toLegacyDomain() }
            } ?: PlannedPaymentRule(
                startDate = null,
                intervalN = null,
                intervalType = null,
                oneTime = false,
                type = TransactionType.EXPENSE,
                amount = screen.amount ?: 0.0,
                accountId = screen.accountId ?: accounts.first().id,
                categoryId = screen.categoryId,
                title = screen.title,
                description = screen.description,
            )

            display(loadedRule!!)
        }
    }

    private suspend fun display(rule: PlannedPaymentRule) {
        this.title = rule.title

        transactionType = TransactionType.EXPENSE
        startDate = with(timeConverter) { rule.startDate?.toLocalDateTime() }
        intervalN = rule.intervalN
        oneTime = rule.oneTime
        intervalType = rule.intervalType
        initialTitle = rule.title
        description = rule.description
        val selectedAccount = ioThread { accountDao.findById(rule.accountId)!!.toLegacyDomain() }
        account = selectedAccount
        category = rule.categoryId?.let {
            ioThread { categoryRepository.findById(CategoryId(it)) }
        }
        amount = rule.amount
        lastSavedAmount = rule.amount

        iconKey = rule.icon
        paymentMethodNote = rule.paymentMethodNote
        reminderLeadDays = rule.reminderLeadDays
        isTrial = rule.isTrial
        trialEndsAt = with(timeConverter) { rule.trialEndsAt?.toLocalDateTime() }
        remainingOccurrences = rule.remainingOccurrences
        paused = rule.paused
        archived = rule.archived

        updateCurrency(account = selectedAccount)
    }

    private suspend fun updateCurrency(account: Account) {
        currency = account.currency ?: baseCurrency()
    }

    private suspend fun baseCurrency(): String = ioThread { settingsDao.findFirst().currency }

    private fun updateRule(
        startDate: LocalDateTime,
        oneTime: Boolean,
        intervalN: Int?,
        intervalType: IntervalType?,
    ) {
        loadedRule = loadedRule().copy(
            startDate = with(timeConverter) { startDate.toUTC() },
            intervalN = intervalN,
            intervalType = intervalType,
            oneTime = oneTime,
        )
        this@SubscriptionEditViewModel.startDate = startDate
        this@SubscriptionEditViewModel.intervalN = intervalN
        this@SubscriptionEditViewModel.intervalType = intervalType
        this@SubscriptionEditViewModel.oneTime = oneTime

        saveIfEditMode()
    }

    private fun updateAmount(newAmount: Double) {
        loadedRule = loadedRule().copy(amount = newAmount)
        this@SubscriptionEditViewModel.amount = newAmount

        saveIfEditMode()
    }

    private fun updateTitle(newTitle: String?) {
        loadedRule = loadedRule().copy(title = newTitle)
        this.title = newTitle

        saveIfEditMode()
    }

    private fun updateDescription(newDescription: String?) {
        loadedRule = loadedRule().copy(description = newDescription)
        this@SubscriptionEditViewModel.description = newDescription

        saveIfEditMode()
    }

    private fun updateCategory(newCategory: Category?) {
        loadedRule = loadedRule().copy(categoryId = newCategory?.id?.value)
        this@SubscriptionEditViewModel.category = newCategory

        saveIfEditMode()
    }

    private fun updateAccount(newAccount: Account) {
        loadedRule = loadedRule().copy(accountId = newAccount.id)
        this@SubscriptionEditViewModel.account = newAccount

        viewModelScope.launch { updateCurrency(account = newAccount) }

        saveIfEditMode()
    }

    // --- Subscription-specific updaters ---

    private fun updateIcon(newIconKey: String?) {
        iconKey = newIconKey
        loadedRule = loadedRule().copy(icon = newIconKey)

        saveIfEditMode()
    }

    private fun updatePaymentMethodNote(newNote: String?) {
        val trimmed = newNote?.trim()?.take(48)
        paymentMethodNote = trimmed
        loadedRule = loadedRule().copy(paymentMethodNote = trimmed)

        saveIfEditMode()
    }

    private fun updateReminderLeadDays(days: Int) {
        val clamped = days.coerceIn(0, 14)
        reminderLeadDays = clamped
        loadedRule = loadedRule().copy(reminderLeadDays = clamped)

        saveIfEditMode()
    }

    private fun updateTrial(trial: Boolean) {
        isTrial = trial
        loadedRule = loadedRule().copy(isTrial = trial)

        saveIfEditMode()
    }

    private fun updateTrialEnds(newTrialEndsAt: LocalDateTime?) {
        trialEndsAt = newTrialEndsAt
        loadedRule = loadedRule().copy(
            trialEndsAt = with(timeConverter) { newTrialEndsAt?.toUTC() }
        )

        saveIfEditMode()
    }

    private fun updateRemainingOccurrences(newRemaining: Int?) {
        remainingOccurrences = newRemaining
        loadedRule = loadedRule().copy(remainingOccurrences = newRemaining)

        saveIfEditMode()
    }

    private fun updatePaused(newPaused: Boolean) {
        paused = newPaused
        loadedRule = loadedRule().copy(paused = newPaused)

        saveIfEditMode()
    }

    private fun updateArchived(newArchived: Boolean) {
        archived = newArchived
        loadedRule = loadedRule().copy(archived = newArchived)

        saveIfEditMode()
    }

    private fun saveIfEditMode() {
        if (editMode) {
            save(closeScreen = false)
        }
    }

    private fun save(closeScreen: Boolean = true) {
        if (!validate()) {
            return
        }

        viewModelScope.launch {
            try {
                ioThread {
                    val isCreating = !editMode
                    val historyBaseline = lastSavedAmount

                    loadedRule = loadedRule().copy(
                        type = TransactionType.EXPENSE,
                        startDate = with(timeConverter) { startDate?.toUTC() }
                            ?: error("no startDate"),
                        intervalN = intervalN ?: error("no intervalN"),
                        intervalType = intervalType ?: error("no intervalType"),
                        oneTime = false,
                        categoryId = category?.id?.value,
                        accountId = account?.id ?: error("no accountId"),
                        title = title?.trim(),
                        description = description?.trim(),
                        amount = amount ?: error("no amount"),
                        icon = iconKey,
                        paymentMethodNote = paymentMethodNote?.takeIf { it.isNotBlank() },
                        reminderLeadDays = reminderLeadDays,
                        isTrial = isTrial,
                        trialEndsAt = with(timeConverter) { trialEndsAt?.toUTC() },
                        remainingOccurrences = remainingOccurrences,
                        isSynced = false,
                    )

                    plannedPaymentRuleWriter.save(loadedRule().toEntity())
                    plannedPaymentsGenerator.generate(loadedRule())

                    // Price history for hike detection — only on the explicit
                    // final save, comparing against what was last persisted.
                    if (closeScreen) {
                        val previousAmount = if (isCreating) null else historyBaseline
                        val ruleId = loadedRule().id
                        when {
                            isCreating -> {
                                plannedPaymentPriceHistoryWriter.save(
                                    priceHistory(ruleId = ruleId, amount = amount, source = "created")
                                )
                            }

                            previousAmount != null && previousAmount != amount -> {
                                plannedPaymentPriceHistoryWriter.save(
                                    priceHistory(ruleId = ruleId, amount = amount, source = "edited")
                                )
                            }
                        }
                        lastSavedAmount = amount
                    }
                }

                if (closeScreen) {
                    nav.back()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun priceHistory(
        ruleId: java.util.UUID,
        amount: Double,
        source: String,
    ) = PlannedPaymentPriceHistoryEntity(
        ruleId = ruleId,
        amount = amount,
        source = source,
    )

    private fun validate(): Boolean {
        if (amount == 0.0) return false

        // Subscriptions are always recurring
        if (startDate == null) return false
        if (intervalN == null || intervalN!! <= 0) return false
        if (intervalType == null) return false
        if (oneTime) return false

        // Trial requires an end date
        if (isTrial && trialEndsAt == null) return false

        return true
    }

    private fun delete() {
        viewModelScope.launch {
            deleteTransactionModalVisible = false
            ioThread {
                loadedRule?.let {
                    plannedPaymentRuleWriter.deleteById(it.id)
                    transactionRepository.deletedByRecurringRuleIdAndNoDateTime(
                        recurringRuleId = it.id,
                    )
                    plannedPaymentPriceHistoryWriter.deleteByRuleId(it.id)
                }
                nav.back()
            }
        }
    }

    private fun createCategory(data: CreateCategoryData) {
        viewModelScope.launch {
            categoryCreator.createCategory(data) {
                categories = categoryRepository.findAll().toImmutableList()

                updateCategory(it)
            }
        }
    }

    private fun editCategory(updatedCategory: Category) {
        viewModelScope.launch {
            categoryCreator.editCategory(updatedCategory) {
                categories = categoryRepository.findAll().toImmutableList()
            }
        }
    }

    private fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            accountCreator.createAccount(data) {
                accounts = accountsAct(Unit)
            }
        }
    }

    private fun reset() {
        loadedRule = null

        initialTitle = null
        description = null
        category = null
        iconKey = null
        paymentMethodNote = null
        reminderLeadDays = 7
        isTrial = false
        trialEndsAt = null
        remainingOccurrences = null
        paused = false
        archived = false
    }

    private fun loadedRule() = loadedRule ?: error("Loaded subscription is null")
}
