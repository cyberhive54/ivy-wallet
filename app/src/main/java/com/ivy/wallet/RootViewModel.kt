package com.ivy.wallet

import android.content.Intent
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.base.legacy.Accent
import com.ivy.base.legacy.Look
import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.legacy.Theme
import com.ivy.base.legacy.stringRes
import com.ivy.base.model.TransactionType
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.repository.LegalRepository
import com.ivy.frp.test.TestIdlingResource
import com.ivy.legacy.IvyWalletCtx
import com.ivy.legacy.utils.ioThread
import com.ivy.legacy.utils.readOnly
import com.ivy.navigation.DisclaimerScreen
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.MainScreen
import com.ivy.navigation.Navigation
import com.ivy.navigation.SubscriptionEditScreen
import com.ivy.navigation.OnboardingScreen
import com.ivy.ui.R
import com.ivy.wallet.domain.deprecated.logic.notification.SubscriptionsReminderWorker
import com.ivy.wallet.domain.deprecated.logic.notification.LedgerReminderLogic
import com.ivy.wallet.domain.deprecated.logic.notification.LedgerReminderWorker
import com.ivy.wallet.domain.deprecated.logic.notification.SubscriptionsReminderLogic
import com.ivy.wallet.domain.deprecated.logic.notification.TransactionReminderLogic
import com.ivy.wallet.migrations.MigrationsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions")
class RootViewModel @Inject constructor(
    private val ivyContext: IvyWalletCtx,
    private val nav: Navigation,
    private val settingsDao: SettingsDao,
    private val sharedPrefs: SharedPrefs,
    private val transactionReminderLogic: TransactionReminderLogic,
    private val subscriptionsReminderLogic: SubscriptionsReminderLogic,
    private val ledgerReminderLogic: LedgerReminderLogic,
    private val migrationsManager: MigrationsManager,
    private val legalRepo: LegalRepository,
) : ViewModel() {

    companion object {
        const val EXTRA_ADD_TRANSACTION_TYPE = "add_transaction_type_extra"

        const val USER_INACTIVITY_TIME_LIMIT = 60 // Time in seconds
    }

    private var appLockEnabled = false

    private val _appLocked = MutableStateFlow<Boolean?>(null)
    val appLocked = _appLocked.readOnly()

    fun start(systemDarkMode: Boolean, intent: Intent) {
        viewModelScope.launch {
            TestIdlingResource.increment()

            ioThread {
                val theme = settingsDao.findAll().firstOrNull()?.theme
                    ?: if (systemDarkMode) Theme.DARK else Theme.LIGHT
                ivyContext.switchTheme(theme)

                // Look (material) axis - persisted in SharedPrefs. Migrates the old
                // mock-theme key for upgrades.
                val look = Look.parse(sharedPrefs.getString(SharedPrefs.SETTINGS_LOOK, null))
                    ?: Look.parse(sharedPrefs.getString(SharedPrefs.SETTINGS_MOCK_THEME, null))
                    ?: Look.IVY
                ivyContext.switchLook(look)

                // Accent axis - DEFAULT keeps each material's signature accent. Older builds
                // persisted "ivy" as the implicit default; parsed directly, same behavior.
                val accent = Accent.parse(
                    sharedPrefs.getString(SharedPrefs.SETTINGS_ACCENT_COLOR, null)
                ) ?: Accent.DEFAULT
                ivyContext.switchAccent(accent)

                ivyContext.switchDockTranslucent(
                    sharedPrefs.getBoolean(SharedPrefs.SETTINGS_DOCK_TRANSLUCENT, false)
                )

                ivyContext.initStartDayOfMonthInMemory(sharedPrefs = sharedPrefs)
                ivyContext.initWorkspaceInMemory(sharedPrefs)
            }

            TestIdlingResource.decrement()
        }

        viewModelScope.launch {
            TestIdlingResource.increment()

            ioThread {
                appLockEnabled = sharedPrefs.getBoolean(SharedPrefs.APP_LOCK_ENABLED, false)
                // initial app locked state
                _appLocked.value = appLockEnabled

                if (isOnboardingCompleted()) {
                    navigateOnboardedUser(intent)
                } else {
                    nav.navigateTo(OnboardingScreen)
                }
                if (!legalRepo.isDisclaimerAccepted()) {
                    nav.navigateTo(DisclaimerScreen)
                }
            }

            TestIdlingResource.decrement()
        }

        viewModelScope.launch {
            migrationsManager.executeMigrations()
        }
    }

    private fun navigateOnboardedUser(intent: Intent) {
        if (!handleSpecialStart(intent)) {
            nav.navigateTo(MainScreen)
            transactionReminderLogic.scheduleReminder()
            subscriptionsReminderLogic.scheduleReminder()
            ledgerReminderLogic.scheduleReminder()
        }
    }

    @Suppress("SwallowedException")
    private fun handleSpecialStart(intent: Intent): Boolean {
        // Tapped a ledger reminder — open the Ledger workspace directly
        val openLedger = intent.getBooleanExtra(LedgerReminderWorker.EXTRA_OPEN_LEDGER_WORKSPACE, false)
        if (openLedger) {
            ivyContext.switchWorkspace(
                com.ivy.base.model.Workspace.LEDGER,
                sharedPrefs
            )
            nav.navigateTo(MainScreen)
            return true
        }

        // Tapped a subscription reminder — main screen first, editor on top
        val subRuleId = intent.getStringExtra(SubscriptionsReminderWorker.EXTRA_OPEN_SUBSCRIPTION_RULE)
            ?.let { runCatching { java.util.UUID.fromString(it) }.getOrNull() }
        if (subRuleId != null) {
            nav.navigateTo(MainScreen)
            nav.navigateTo(SubscriptionEditScreen(plannedPaymentRuleId = subRuleId))
            return true
        }

        val addTrnType: TransactionType? = try {
            intent.getSerializableExtra(EXTRA_ADD_TRANSACTION_TYPE) as? TransactionType
                ?: TransactionType.valueOf(intent.getStringExtra(EXTRA_ADD_TRANSACTION_TYPE) ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }

        if (addTrnType != null) {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = addTrnType
                )
            )

            return true
        }

        return false
    }

    @Suppress("EmptyFunctionBlock")
    fun handleBiometricAuthResult(
        onAuthSuccess: () -> Unit = {}
    ): BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Timber.d(stringRes(R.string.authentication_succeeded))
                unlockApp()
                onAuthSuccess()
            }

            override fun onAuthenticationFailed() {
                Timber.d(stringRes(R.string.authentication_failed))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            }
        }
    }

    private fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean(SharedPrefs.ONBOARDING_COMPLETED, false)
    }

    // App Lock & UserInactivity --------------------------------------------------------------------
    fun isAppLockEnabled(): Boolean {
        return appLockEnabled
    }

    fun isAppLocked(): Boolean {
        // by default we assume that the app is locked
        return appLocked.value ?: true
    }

    fun lockApp() {
        _appLocked.value = true
    }

    fun unlockApp() {
        _appLocked.value = false
    }

    private val userInactiveTime = AtomicLong(0)
    private var userInactiveJob: Job? = null

    @Suppress("MagicNumber")
    fun startUserInactiveTimeCounter() {
        if (userInactiveJob != null && userInactiveJob!!.isActive) return

        userInactiveJob = viewModelScope.launch(Dispatchers.IO) {
            while (userInactiveTime.get() < USER_INACTIVITY_TIME_LIMIT &&
                userInactiveJob != null && !userInactiveJob?.isCancelled!!
            ) {
                delay(1000)
                userInactiveTime.incrementAndGet()
            }

            if (!isAppLocked()) {
                lockApp()
            }

            cancel()
        }
    }

    fun checkUserInactiveTimeStatus() {
        if (userInactiveTime.get() < USER_INACTIVITY_TIME_LIMIT) {
            if (userInactiveJob != null && userInactiveJob?.isCancelled == false) {
                userInactiveJob?.cancel()
                resetUserInactiveTimer()
            }
        }
    }

    fun resetUserInactiveTimer() {
        userInactiveTime.set(0)
    }
}
