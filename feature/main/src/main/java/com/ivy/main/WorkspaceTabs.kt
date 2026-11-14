package com.ivy.main

import com.ivy.base.model.Workspace
import com.ivy.legacy.data.model.MainTab

/**
 * Coerces a persisted/dock-selected [MainTab] into a tab that is valid for the
 * given workspace. Defense-in-depth alongside the coercion in `IvyWalletCtx` —
 * the dock renders with this value so a stale tab can never produce a blank screen.
 */
internal fun effectiveTab(workspace: Workspace, tab: MainTab): MainTab = when (workspace) {
    Workspace.FINANCE -> when (tab) {
        MainTab.LEDGER_CONTACTS, MainTab.SPLITLY_GROUPS, MainTab.SPLITLY_ACTIVITY -> MainTab.HOME
        else -> tab
    }
    Workspace.LEDGER -> when (tab) {
        MainTab.ACCOUNTS, MainTab.SPLITLY_GROUPS, MainTab.SPLITLY_ACTIVITY -> MainTab.HOME
        else -> tab
    }
    Workspace.SPLITLY -> when (tab) {
        MainTab.ACCOUNTS, MainTab.LEDGER_CONTACTS -> MainTab.HOME
        else -> tab
    }
}

/**
 * Dock pill target slot for the given tab within a workspace.
 *
 * Layout (5 equal slots):
 *  - Finance: Home · Transactions(action) · + · Accounts · Settings
 *  - Ledger:  Home · Transactions(action) · + · Contacts · Settings
 *  - Splitly: Home · Groups(tab)          · + · Activity · Settings
 */
internal fun targetSlotFor(workspace: Workspace, tab: MainTab): Float {
    val effective = effectiveTab(workspace, tab)
    return when (workspace) {
        Workspace.FINANCE -> when (effective) {
            MainTab.HOME -> 0f
            MainTab.ACCOUNTS -> 3f
            else -> 4f // SETTINGS / legacy MENU
        }
        Workspace.LEDGER -> when (effective) {
            MainTab.HOME -> 0f
            MainTab.LEDGER_CONTACTS -> 3f
            else -> 4f // SETTINGS
        }
        Workspace.SPLITLY -> when (effective) {
            MainTab.HOME -> 0f
            MainTab.SPLITLY_GROUPS -> 1f
            MainTab.SPLITLY_ACTIVITY -> 3f
            else -> 4f // SETTINGS
        }
    }
}
