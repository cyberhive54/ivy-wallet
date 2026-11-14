package com.ivy.legacy.data.model

import androidx.compose.runtime.Immutable

@Immutable
enum class MainTab {
    HOME,
    ACCOUNTS,
    MENU,
    SETTINGS,

    // Ledger workspace mid-tab (contacts/loans people list)
    LEDGER_CONTACTS,

    // Splitly workspace mid-tabs (group list / activity feed)
    SPLITLY_GROUPS,
    SPLITLY_ACTIVITY
}
