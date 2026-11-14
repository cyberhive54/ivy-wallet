package com.ivy.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.accounts.AccountsTab
import com.ivy.base.model.TransactionType
import com.ivy.base.model.Workspace
import com.ivy.home.HomeTab
import com.ivy.settings.SettingsScreen
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.data.model.MainTab
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.onScreenStart
import com.ivy.main.workspace.LedgerContacts
import com.ivy.main.workspace.LedgerLanding
import com.ivy.main.workspace.SplitlyActivity
import com.ivy.main.workspace.SplitlyGroups
import com.ivy.main.workspace.SplitlyLanding
import com.ivy.main.workspace.WorkspaceRail
import com.ivy.navigation.EditPlannedScreen
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.LoansScreen
import com.ivy.navigation.MainScreen
import com.ivy.navigation.SearchScreen
import com.ivy.navigation.navigation
import com.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import com.ivy.wallet.ui.theme.modal.edit.AccountModal
import com.ivy.wallet.ui.theme.modal.edit.AccountModalData

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.MainScreen(screen: MainScreen) {
    val viewModel: MainViewModel = viewModel()

    val currency by viewModel.currency.observeAsState("")

    onScreenStart {
        viewModel.start(screen)
    }

    val ivyContext = ivyWalletCtx()
    UI(
        screen = screen,
        tab = ivyContext.mainTab,
        baseCurrency = currency,
        selectTab = viewModel::selectTab,
        onCreateAccount = viewModel::createAccount
    )
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: MainScreen,
    tab: MainTab,

    baseCurrency: String,

    selectTab: (MainTab) -> Unit,
    onCreateAccount: (CreateAccountData) -> Unit,
) {
    val ivyContext = ivyWalletCtx()
    val workspace = ivyContext.activeWorkspace
    val effective = effectiveTab(workspace, tab)

    Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        // Rail shows on workspace home tabs only (inner tab screens sit under it)
        if (effective == MainTab.HOME) {
            with(this@UI) { WorkspaceRail() }
        }

        when (workspace) {
            Workspace.FINANCE -> when (effective) {
                MainTab.HOME -> with(this@UI) { HomeTab() }
                MainTab.ACCOUNTS -> with(this@UI) { AccountsTab() }
                MainTab.MENU -> with(this@UI) { SettingsScreen() } // guard; dock no longer opens this tab
                MainTab.SETTINGS -> with(this@UI) { SettingsScreen() }
                // Home coercion handles the workspace-specific tabs
                else -> with(this@UI) { HomeTab() }
            }
            Workspace.LEDGER -> when (effective) {
                MainTab.LEDGER_CONTACTS -> with(this@UI) { LedgerContacts() }
                else -> with(this@UI) { LedgerLanding() }
            }
            Workspace.SPLITLY -> when (effective) {
                MainTab.SPLITLY_GROUPS -> with(this@UI) { SplitlyGroups() }
                MainTab.SPLITLY_ACTIVITY -> with(this@UI) { SplitlyActivity() }
                else -> with(this@UI) { SplitlyLanding() }
            }
        }
    }

    var accountModalData: AccountModalData? by remember { mutableStateOf(null) }

    val nav = navigation()
    BottomBar(
        tab = tab,
        selectTab = selectTab,

        onAddIncome = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.INCOME
                )
            )
        },
        onAddExpense = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.EXPENSE
                )
            )
        },
        onAddTransfer = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionType.TRANSFER
                )
            )
        },
        onAddPlannedPayment = {
            nav.navigateTo(
                EditPlannedScreen(
                    type = TransactionType.EXPENSE,
                    plannedPaymentRuleId = null
                )
            )
        },

        showAddAccountModal = {
            accountModalData = AccountModalData(
                account = null,
                balance = 0.0,
                baseCurrency = baseCurrency
            )
        },

        onTransactionsTab = {
            // Dock "Transactions" shows the full transactions history via the fork's
            // Search screen with an empty query (matches everything).
            nav.navigateTo(SearchScreen)
        },

        onWorkspacePlus = {
            when (ivyContext.activeWorkspace) {
                Workspace.LEDGER -> nav.navigateTo(LoansScreen)
                Workspace.SPLITLY -> ivyContext.selectMainTab(MainTab.SPLITLY_GROUPS)
                Workspace.FINANCE -> Unit // handled by the radial add menu
            }
        }
    )

    // Quick links live in the dock itself (Flipkart-style inline pill)

    AccountModal(
        modal = accountModalData,
        onCreateAccount = onCreateAccount,
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalData = null
        }
    )
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Preview
@Composable
private fun PreviewMainScreen() {
    IvyWalletPreview {
        UI(
            screen = MainScreen,
            tab = MainTab.HOME,
            baseCurrency = "BGN",
            selectTab = {},
            onCreateAccount = { }
        )
    }
}
