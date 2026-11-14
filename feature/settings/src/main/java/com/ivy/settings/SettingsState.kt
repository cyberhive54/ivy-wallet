package com.ivy.settings

import com.ivy.base.legacy.Theme

data class SettingsState(
    val currencyCode: String,
    val name: String,
    val currentTheme: Theme,
    val lockApp: Boolean,
    val showNotifications: Boolean,
    val hideCurrentBalance: Boolean,
    val hideIncome: Boolean,
    val treatTransfersAsIncomeExpense: Boolean,
    val startDateOfMonth: String,
    val progressState: Boolean,
    val languageOptionVisible: Boolean,
    val look: String,
    val accentColor: String,
    val dockTranslucent: Boolean,
    val ledgerWorkspaceEnabled: Boolean,
    val splitlyWorkspaceEnabled: Boolean,
    /** "ledger" | "splitly" while a zero-records block dialog should be shown; null otherwise. */
    val workspaceBlockedKind: String?,
    /** Number of records in the blocked workspace, for the dialog message. */
    val workspaceBlockedCount: Int
)
