package com.ivy.legacy

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ivy.design.IvyContext
import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.model.Workspace
import com.ivy.data.model.Category
import com.ivy.legacy.datamodel.Account
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("Legacy code. Don't use it, please.")
@Singleton
class IvyWalletCtx @Inject constructor() : IvyContext() {
    // ------------------------------------------ State ---------------------------------------------
    /**
     * The currently visible workspace. Observing a mutableStateOf directly; switching
     * resets the rail selection on every screen that consumes it.
     */
    @Deprecated("Legacy code. Don't use it, please.")
    var activeWorkspace by mutableStateOf(Workspace.FINANCE)
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    var ledgerWorkspaceEnabled by mutableStateOf(true)
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    var splitlyWorkspaceEnabled by mutableStateOf(false)
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    fun switchWorkspace(
        workspace: Workspace,
        sharedPrefs: SharedPrefs,
    ) {
        if (workspace == Workspace.LEDGER && !ledgerWorkspaceEnabled) return
        if (workspace == Workspace.SPLITLY && !splitlyWorkspaceEnabled) return
        activeWorkspace = workspace
        mainTab = coerceMainTab(workspace, mainTab)
        sharedPrefs.putString(SharedPrefs.WORKSPACE, workspace.code)
    }

    @Deprecated("Legacy code. Don't use it, please.")
    fun setLedgerWorkspaceEnabled(enabled: Boolean, sharedPrefs: SharedPrefs) {
        ledgerWorkspaceEnabled = enabled
        sharedPrefs.putBoolean(SharedPrefs.LEDGER_WORKSPACE_ENABLED, enabled)
        if (!enabled && activeWorkspace == Workspace.LEDGER) {
            activeWorkspace = Workspace.FINANCE
            mainTab = coerceMainTab(Workspace.FINANCE, mainTab)
            sharedPrefs.putString(SharedPrefs.WORKSPACE, Workspace.FINANCE.code)
        }
    }

    @Deprecated("Legacy code. Don't use it, please.")
    fun setSplitlyWorkspaceEnabled(enabled: Boolean, sharedPrefs: SharedPrefs) {
        splitlyWorkspaceEnabled = enabled
        sharedPrefs.putBoolean(SharedPrefs.SPLITLY_WORKSPACE_ENABLED, enabled)
        if (!enabled && activeWorkspace == Workspace.SPLITLY) {
            activeWorkspace = Workspace.FINANCE
            mainTab = coerceMainTab(Workspace.FINANCE, mainTab)
            sharedPrefs.putString(SharedPrefs.WORKSPACE, Workspace.FINANCE.code)
        }
    }

    @Deprecated("Legacy code. Don't use it, please.")
    fun initWorkspaceInMemory(sharedPrefs: SharedPrefs) {
        ledgerWorkspaceEnabled = sharedPrefs.getBoolean(SharedPrefs.LEDGER_WORKSPACE_ENABLED, true)
        splitlyWorkspaceEnabled = sharedPrefs.getBoolean(SharedPrefs.SPLITLY_WORKSPACE_ENABLED, false)
        activeWorkspace = Workspace.parse(
            sharedPrefs.getString(SharedPrefs.WORKSPACE, Workspace.FINANCE.code)
        )
        mainTab = coerceMainTab(activeWorkspace, mainTab)
    }

    private fun coerceMainTab(
        workspace: Workspace,
        tab: com.ivy.legacy.data.model.MainTab,
    ): com.ivy.legacy.data.model.MainTab {
        return when (workspace) {
            Workspace.FINANCE -> when (tab) {
                com.ivy.legacy.data.model.MainTab.LEDGER_CONTACTS,
                com.ivy.legacy.data.model.MainTab.SPLITLY_GROUPS,
                com.ivy.legacy.data.model.MainTab.SPLITLY_ACTIVITY ->
                    com.ivy.legacy.data.model.MainTab.HOME
                else -> tab
            }
            Workspace.LEDGER -> when (tab) {
                com.ivy.legacy.data.model.MainTab.ACCOUNTS,
                com.ivy.legacy.data.model.MainTab.SPLITLY_GROUPS,
                com.ivy.legacy.data.model.MainTab.SPLITLY_ACTIVITY ->
                    com.ivy.legacy.data.model.MainTab.HOME
                else -> tab
            }
            Workspace.SPLITLY -> when (tab) {
                com.ivy.legacy.data.model.MainTab.ACCOUNTS,
                com.ivy.legacy.data.model.MainTab.LEDGER_CONTACTS ->
                    com.ivy.legacy.data.model.MainTab.HOME
                else -> tab
            }
        }
    }

    @Deprecated("Legacy code. Don't use it, please.")
    var startDayOfMonth = 1
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    fun setStartDayOfMonth(day: Int) {
        startDayOfMonth = day
    }

    // ---------------------- Optimization  ----------------------------
    @Deprecated("Legacy code. Don't use it, please.")
    val categoryMap: MutableMap<UUID, Category> = mutableMapOf()

    @Deprecated("Legacy code. Don't use it, please.")
    val accountMap: MutableMap<UUID, Account> = mutableMapOf()
    // ---------------------- Optimization  ----------------------------

    @Deprecated("Legacy code. Don't use it, please.")
    var dataBackupCompleted = false

    @Deprecated("Legacy code. Don't use it, please.")
    fun initStartDayOfMonthInMemory(sharedPrefs: SharedPrefs): Int {
        startDayOfMonth = sharedPrefs.getInt(SharedPrefs.START_DATE_OF_MONTH, 1)
        return startDayOfMonth
    }

    @Deprecated("Legacy code. Don't use it, please.")
    var selectedPeriod: com.ivy.legacy.data.model.TimePeriod =
        com.ivy.legacy.data.model.TimePeriod.currentMonth(
            startDayOfMonth = startDayOfMonth // this is default value
        )

    @Deprecated("Legacy code. Don't use it, please.")
    private var selectedPeriodInitialized = false

    @Deprecated("Legacy code. Don't use it, please.")
    fun initSelectedPeriodInMemory(
        startDayOfMonth: Int,
        forceReinitialize: Boolean = false
    ): com.ivy.legacy.data.model.TimePeriod {
        if (!selectedPeriodInitialized || forceReinitialize) {
            selectedPeriod = com.ivy.legacy.data.model.TimePeriod.currentMonth(
                startDayOfMonth = startDayOfMonth
            )
            selectedPeriodInitialized = true
        }

        return selectedPeriod
    }

    @Deprecated("Legacy code. Don't use it, please.")
    fun updateSelectedPeriodInMemory(period: com.ivy.legacy.data.model.TimePeriod) {
        selectedPeriod = period
    }

    @Deprecated("Legacy code. Don't use it, please.")
    var transactionsListState: LazyListState? = null

    @Deprecated("Legacy code. Don't use it, please.")
    var categoriesListState: LazyListState? = null

    @Deprecated("Legacy code. Don't use it, please.")
    var accountsListState: LazyListState? = null

    @Deprecated("Legacy code. Don't use it, please.")
    var loanListState: LazyListState? = null

    @Deprecated("Legacy code. Don't use it, please.")
    var mainTab by mutableStateOf(com.ivy.legacy.data.model.MainTab.HOME)
        private set

    /**
     * Increments every time the dock re-selects the tab that is already active
     * (e.g. tapping "Settings" while already in Settings). Screens observe this
     * counter to implement the iOS convention "tap current tab -> scroll to top".
     */
    var mainTabReclick by mutableStateOf(0)
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    fun selectMainTab(tab: com.ivy.legacy.data.model.MainTab) {
        if (tab == mainTab) {
            mainTabReclick += 1
        }
        mainTab = tab
    }

    @Deprecated("Legacy code. Don't use it, please.")
    var moreMenuExpanded = false
        private set

    @Deprecated("Legacy code. Don't use it, please.")
    fun setMoreMenuExpanded(expanded: Boolean) {
        moreMenuExpanded = expanded
    }
    // ------------------------------------------ State ---------------------------------------------

    // Activity help -------------------------------------------------------------------------------
    @Deprecated("Legacy code. Don't use it, please.")
    lateinit var onShowDatePicker: (
        minDate: LocalDate?,
        maxDate: LocalDate?,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    ) -> Unit
    lateinit var onShowTimePicker: (
        initialTime: LocalTime?,
        onDatePicked: (LocalTime) -> Unit
    ) -> Unit

    @Deprecated("Legacy code. Don't use it, please.")
    fun datePicker(
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    ) {
        onShowDatePicker(minDate, maxDate, initialDate, onDatePicked)
    }

    @Deprecated("Legacy code. Don't use it, please.")
    fun timePicker(
        initialTime: LocalTime?,
        onTimePicked: (LocalTime) -> Unit
    ) {
        onShowTimePicker(initialTime, onTimePicked)
    }
    // Activity help -------------------------------------------------------------------------------

    // Billing -------------------------------------------------------------------------------------
    @Deprecated("Legacy code. Don't use it, please.")
    var isPremium = true // if (BuildConfig.DEBUG) Constants.PREMIUM_INITIAL_VALUE_DEBUG else false
    // Billing -------------------------------------------------------------------------------------

    @Deprecated("Legacy code. Don't use it, please.")
    lateinit var googleSignIn: (idTokenResult: (String?) -> Unit) -> Unit

    @Deprecated("Legacy code. Don't use it, please.")
    lateinit var createNewFile: (fileName: String, onCreated: (Uri) -> Unit) -> Unit

    @Deprecated("Legacy code. Don't use it, please.")
    lateinit var openFile: (onOpened: (Uri) -> Unit) -> Unit

    // Testing --------------------------------------------------------------------------------------
    @Deprecated("Legacy code. Don't use it, please.")
    fun reset() {
        mainTab = com.ivy.legacy.data.model.MainTab.HOME
        startDayOfMonth = 1
        isPremium = true
        transactionsListState = null
        categoriesListState = null
        accountsListState = null
        activeWorkspace = Workspace.FINANCE
        ledgerWorkspaceEnabled = true
        splitlyWorkspaceEnabled = false
    }
}
