package com.ivy.base.model

/**
 * The three user-facing workspaces of the fork. Personal Finance is always on;
 * Ledger and Splitly hide entirely from the rail + dock unless enabled by the user
 * (zero-records guard blocks hiding when data exists).
 */
enum class Workspace(val code: String) {
    FINANCE("FINANCE"),
    LEDGER("LEDGER"),
    SPLITLY("SPLITLY"),
    ;

    companion object {
        fun parse(raw: String?): Workspace =
            entries.firstOrNull { it.code == raw } ?: FINANCE
    }
}
