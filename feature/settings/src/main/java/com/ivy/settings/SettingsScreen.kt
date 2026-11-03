package com.ivy.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Accent
import com.ivy.base.legacy.Theme
import com.ivy.design.api.systems.AccentFamilies
import com.ivy.design.l0_system.UI
import com.ivy.legacy.Constants
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.rootScreen
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.components.IvySwitch
import java.util.Locale

@Composable
fun BoxWithConstraintsScope.SettingsScreen() {
    SettingsFlowHost()
}

@Composable
internal fun BoxWithConstraintsScope.SettingsRootContent(
    onOpenPage: (SettingsChildPage) -> Unit,
    listState: LazyListState,
) {
    val viewModel: SettingsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()
    val rootScreen = rootScreen()

    UI(
        currencyCode = uiState.currencyCode,
        theme = uiState.currentTheme,
        look = uiState.look,
        accentColor = uiState.accentColor,
        dockTranslucent = uiState.dockTranslucent,
        lockApp = uiState.lockApp,
        showNotifications = uiState.showNotifications,
        hideCurrentBalance = uiState.hideCurrentBalance,
        hideIncome = uiState.hideIncome,
        progressState = uiState.progressState,
        treatTransfersAsIncomeExpense = uiState.treatTransfersAsIncomeExpense,
        nameLocalAccount = uiState.name,
        startDateOfMonth = uiState.startDateOfMonth.toInt(),
        languageOptionVisible = uiState.languageOptionVisible,
        onSetCurrency = {
            viewModel.onEvent(SettingsEvent.SetCurrency(it))
        },
        onSetName = {
            viewModel.onEvent(SettingsEvent.SetName(it))
        },
        onBackupData = {
            viewModel.onEvent(SettingsEvent.BackupData(rootScreen))
        },
        onExportToCSV = {
            viewModel.onEvent(SettingsEvent.ExportToCsv(rootScreen))
        },
        onSetTheme = {
            viewModel.onEvent(SettingsEvent.SetTheme(it))
        },
        onSetLook = {
            viewModel.onEvent(SettingsEvent.SetLook(it))
        },
        onSetAccentColor = {
            viewModel.onEvent(SettingsEvent.SetAccentColor(it))
        },
        onSetDockTranslucent = {
            viewModel.onEvent(SettingsEvent.SetDockTranslucent(it))
        },
        onSetLockApp = {
            viewModel.onEvent(SettingsEvent.SetLockApp(it))
        },
        onSetShowNotifications = {
            viewModel.onEvent(SettingsEvent.SetShowNotifications(it))
        },
        onSetHideCurrentBalance = {
            viewModel.onEvent(SettingsEvent.SetHideCurrentBalance(it))
        },
        onSetHideIncome = {
            viewModel.onEvent(SettingsEvent.SetHideIncome(it))
        },
        onSetStartDateOfMonth = {
            viewModel.onEvent(SettingsEvent.SetStartDateOfMonth(it))
        },
        onSetTreatTransfersAsIncExp = {
            viewModel.onEvent(SettingsEvent.SetTransfersAsIncomeExpense(it))
        },
        onDeleteAllUserData = {
            viewModel.onEvent(SettingsEvent.DeleteAllUserData)
        },
        onSwitchLanguage = {
            viewModel.onEvent(SettingsEvent.SwitchLanguage)
        },
        onOpenPage = onOpenPage,
        listState = listState
    )
}

@Suppress("LongMethod")
@Composable
private fun BoxWithConstraintsScope.UI(
    currencyCode: String,
    theme: Theme,
    look: String,
    accentColor: String,
    dockTranslucent: Boolean,
    lockApp: Boolean,
    nameLocalAccount: String?,
    languageOptionVisible: Boolean,
    onSetCurrency: (String) -> Unit,
    startDateOfMonth: Int = 1,
    showNotifications: Boolean = true,
    hideCurrentBalance: Boolean = false,
    hideIncome: Boolean = false,
    progressState: Boolean = false,
    treatTransfersAsIncomeExpense: Boolean = false,
    onSetName: (String) -> Unit = {},
    onBackupData: () -> Unit = {},
    onExportToCSV: () -> Unit = {},
    onSetTheme: (Theme) -> Unit = {},
    onSetLook: (String) -> Unit = {},
    onSetAccentColor: (String) -> Unit = {},
    onSetDockTranslucent: (Boolean) -> Unit = {},
    onSetLockApp: (Boolean) -> Unit = {},
    onSetShowNotifications: (Boolean) -> Unit = {},
    onSetTreatTransfersAsIncExp: (Boolean) -> Unit = {},
    onSetHideCurrentBalance: (Boolean) -> Unit = {},
    onSetHideIncome: (Boolean) -> Unit = {},
    onSetStartDateOfMonth: (Int) -> Unit = {},
    onDeleteAllUserData: () -> Unit = {},
    onSwitchLanguage: () -> Unit = {},
    onOpenPage: (SettingsChildPage) -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    var currencyModalVisible by remember { mutableStateOf(false) }
    var nameModalVisible by remember { mutableStateOf(false) }
    var chooseStartDateOfMonthVisible by remember { mutableStateOf(false) }
    var deleteAllDataModalVisible by remember { mutableStateOf(false) }
    var deleteAllDataModalFinalVisible by remember { mutableStateOf(false) }
    var helpFeedbackVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_lazy_column")
    ) {
        item {
            SettingsHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
        }

        item {
            AppearanceSection(
                query = searchQuery,
                theme = theme,
                look = look,
                accentColor = accentColor,
                onSetTheme = onSetTheme,
                onSetLook = onSetLook,
                onSetAccentColor = onSetAccentColor
            )
        }

        item {
            PreferencesSection(
                query = searchQuery,
                nameLocalAccount = nameLocalAccount,
                currencyCode = currencyCode,
                startDateOfMonth = startDateOfMonth,
                languageOptionVisible = languageOptionVisible,
                onOpenNameModal = { nameModalVisible = true },
                onOpenCurrencyModal = { currencyModalVisible = true },
                onOpenStartDateModal = { chooseStartDateOfMonthVisible = true },
                onSwitchLanguage = onSwitchLanguage
            )
        }

        item {
            PrivacySection(
                query = searchQuery,
                lockApp = lockApp,
                hideCurrentBalance = hideCurrentBalance,
                hideIncome = hideIncome,
                onSetLockApp = onSetLockApp,
                onSetHideCurrentBalance = onSetHideCurrentBalance,
                onSetHideIncome = onSetHideIncome
            )
        }

        item {
            DataSection(
                query = searchQuery,
                onBackupData = onBackupData,
                onExportToCSV = onExportToCSV,
                onImportData = {
                    onOpenPage(SettingsChildPage.Import)
                },
                onExchangeRates = {
                    onOpenPage(SettingsChildPage.ExchangeRates)
                }
            )
        }

        item {
            BehaviorSection(
                query = searchQuery,
                showNotifications = showNotifications,
                treatTransfersAsIncomeExpense = treatTransfersAsIncomeExpense,
                dockTranslucent = dockTranslucent,
                onSetShowNotifications = onSetShowNotifications,
                onSetTreatTransfersAsIncExp = onSetTreatTransfersAsIncExp,
                onSetDockTranslucent = onSetDockTranslucent,
                onAdvancedFeatures = {
                    onOpenPage(SettingsChildPage.Features)
                }
            )
        }

        item {
            SupportSection(
                query = searchQuery,
                onHelpFeedback = { helpFeedbackVisible = true }
            )
        }

        item {
            AboutSection(
                query = searchQuery,
                onReleases = {
                    onOpenPage(SettingsChildPage.Releases)
                },
                onContributors = {
                    onOpenPage(SettingsChildPage.Contributors)
                },
                onAttributions = {
                    onOpenPage(SettingsChildPage.Attributions)
                }
            )
        }

        item {
            DangerZoneSection(
                query = searchQuery,
                onDeleteAllUserData = { deleteAllDataModalVisible = true }
            )
        }

        item {
            SettingsFooter()
        }
    }

    SettingsCurrencySheet(
        visible = currencyModalVisible,
        initialCurrencyCode = currencyCode,
        dismiss = { currencyModalVisible = false },
        onSetCurrency = onSetCurrency
    )

    SettingsNameSheet(
        visible = nameModalVisible,
        initialName = nameLocalAccount ?: "",
        dismiss = { nameModalVisible = false },
        onSetName = onSetName
    )

    SettingsStartDaySheet(
        visible = chooseStartDateOfMonthVisible,
        selectedDay = startDateOfMonth,
        dismiss = { chooseStartDateOfMonthVisible = false },
        onSelectDay = onSetStartDateOfMonth
    )

    CupertinoAlert(
        visible = deleteAllDataModalVisible,
        title = stringResource(R.string.delete_all_user_data_question),
        message = stringResource(
            R.string.delete_all_user_data_warning,
            stringResource(R.string.your_account)
        ),
        confirmLabel = stringResource(R.string.delete),
        dismissLabel = stringResource(R.string.cancel),
        destructive = true,
        onConfirm = {
            deleteAllDataModalVisible = false
            deleteAllDataModalFinalVisible = true
        },
        onDismiss = { deleteAllDataModalVisible = false }
    )

    CupertinoAlert(
        visible = deleteAllDataModalFinalVisible,
        title = stringResource(
            R.string.confirm_all_userd_data_deletion,
            stringResource(R.string.all_of_your_data)
        ),
        message = stringResource(R.string.final_deletion_warning),
        confirmLabel = stringResource(R.string.delete),
        dismissLabel = stringResource(R.string.cancel),
        destructive = true,
        onConfirm = {
            onDeleteAllUserData()
        },
        onDismiss = { deleteAllDataModalFinalVisible = false }
    )

    CupertinoProgressCard(
        visible = progressState,
        title = stringResource(R.string.exporting_data),
        message = stringResource(R.string.exporting_data_description)
    )

    HelpFeedbackSheet(
        visible = helpFeedbackVisible,
        dismiss = { helpFeedbackVisible = false }
    )
}

// -----------------------------------------------------------------------------------------
// Header
// -----------------------------------------------------------------------------------------

@Composable
private fun SettingsHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    Spacer(Modifier.height(24.dp))

    Text(
        modifier = Modifier.padding(start = 24.dp),
        text = stringResource(R.string.settings),
        style = inter(34.sp, FontWeight.Bold, letterSpacing = (-0.5).sp),
        color = UI.colors.pureInverse
    )

    Spacer(Modifier.height(16.dp))

    SettingsSearchField(
        query = searchQuery,
        onQueryChange = onSearchQueryChange
    )

    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SettingsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(UI.colors.medium)
            .border(1.dp, hairlineColor(), RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(14.dp))

        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = UI.colors.gray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_search_hint),
                    style = inter(16.sp),
                    color = UI.colors.gray
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = inter(16.sp).copy(color = UI.colors.pureInverse),
                cursorBrush = SolidColor(UI.colors.pureInverse),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (query.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_dismiss_close),
                contentDescription = stringResource(R.string.settings_clear_search),
                tint = UI.colors.gray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onQueryChange("") }
            )
        }

        Spacer(Modifier.width(14.dp))
    }
}

// -----------------------------------------------------------------------------------------
// Appearance
// -----------------------------------------------------------------------------------------

@Composable
private fun AppearanceSection(
    query: String,
    theme: Theme,
    look: String,
    accentColor: String,
    onSetTheme: (Theme) -> Unit,
    onSetLook: (String) -> Unit,
    onSetAccentColor: (String) -> Unit,
) {
    if (!matches(
            query,
            "appearance", "theme", "mode", "dark", "light", "system",
            "amoled", "color", "accent", "ivy", "forest", "glass", "graphite"
        )
    ) {
        return
    }

    val lookIndex = SettingsAppearance.LOOKS
        .indexOf(look)
        .coerceAtLeast(0)

    val modeIndex = when (theme) {
        Theme.AUTO -> 0
        Theme.LIGHT -> 1
        Theme.DARK, Theme.AMOLED_DARK -> 2
    }

    SectionHeader(text = stringResource(R.string.settings_section_appearance))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .border(1.dp, hairlineColor(), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_theme),
            style = inter(15.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.height(10.dp))

        // Picker options are derived from LOOKS so labels and indices can never diverge.
        val lookLabels = mapOf(
            SettingsAppearance.LOOK_IVY to stringResource(R.string.settings_theme_ivy),
            SettingsAppearance.LOOK_FOREST to stringResource(R.string.settings_theme_forest),
            SettingsAppearance.LOOK_GLASS to stringResource(R.string.settings_theme_glass),
            SettingsAppearance.LOOK_GRAPHITE to stringResource(R.string.settings_theme_graphite)
        )

        SegmentedPill(
            options = SettingsAppearance.LOOKS.map { look -> lookLabels[look].orEmpty() },
            selectedIndex = lookIndex,
            onSelect = { index ->
                onSetLook(SettingsAppearance.LOOKS[index])
            }
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.settings_mode),
            style = inter(15.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.height(10.dp))

        SegmentedPill(
            options = listOf(
                stringResource(R.string.settings_mode_system),
                stringResource(R.string.settings_mode_light),
                stringResource(R.string.settings_mode_dark)
            ),
            selectedIndex = modeIndex,
            onSelect = { index ->
                when (index) {
                    0 -> onSetTheme(Theme.AUTO)
                    1 -> onSetTheme(Theme.LIGHT)
                    else -> onSetTheme(Theme.DARK)
                }
            }
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_amoled_dark),
                    style = inter(15.sp, FontWeight.Medium),
                    color = UI.colors.pureInverse
                )
                Text(
                    text = stringResource(R.string.settings_amoled_dark_description),
                    style = inter(12.sp),
                    color = UI.colors.gray
                )
            }

            IvySwitch(enabled = theme == Theme.AMOLED_DARK) { amoledOn ->
                onSetTheme(if (amoledOn) Theme.AMOLED_DARK else Theme.DARK)
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.settings_accent_color),
            style = inter(15.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                SettingsAppearance.ACCENT_DEFAULT to AccentFamilies.signature(
                    UI.colors.look,
                    UI.colors.isLight
                ),
                SettingsAppearance.ACCENT_IVY to AccentFamilies.primaryColor(Accent.IVY, UI.colors.isLight),
                SettingsAppearance.ACCENT_GREEN to AccentFamilies.primaryColor(Accent.GREEN, UI.colors.isLight),
                SettingsAppearance.ACCENT_BLUE to AccentFamilies.primaryColor(Accent.BLUE, UI.colors.isLight),
                SettingsAppearance.ACCENT_ORANGE to AccentFamilies.primaryColor(Accent.ORANGE, UI.colors.isLight),
                SettingsAppearance.ACCENT_PINK to AccentFamilies.primaryColor(Accent.PINK, UI.colors.isLight)
            ).forEach { (accent, color) ->
                AccentSwatch(
                    color = color,
                    selected = accentColor == accent,
                    contentDescription = accent,
                    onClick = { onSetAccentColor(accent) }
                )
            }
        }
    }
}

@Composable
private fun SegmentedPill(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .border(1.dp, hairlineColor(), RoundedCornerShape(50))
    ) {
        options.forEachIndexed { index, label ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(UI.colors.pureInverse.copy(alpha = 0.10f))
                )
            }

            val selected = index == selectedIndex
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (selected) {
                            UI.colors.pureInverse.copy(alpha = 0.08f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = UI.colors.pureInverse,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(Modifier.width(4.dp))
                }

                Text(
                    text = label,
                    style = inter(14.sp, FontWeight.Medium),
                    color = if (selected) UI.colors.pureInverse else UI.colors.gray,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    color: Color,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    UI.colors.pureInverse
                } else {
                    UI.colors.pureInverse.copy(alpha = 0.10f)
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Sections
// -----------------------------------------------------------------------------------------

@Composable
private fun PreferencesSection(
    query: String,
    nameLocalAccount: String?,
    currencyCode: String,
    startDateOfMonth: Int,
    languageOptionVisible: Boolean,
    onOpenNameModal: () -> Unit,
    onOpenCurrencyModal: () -> Unit,
    onOpenStartDateModal: () -> Unit,
    onSwitchLanguage: () -> Unit,
) {
    val accountRow: (@Composable () -> Unit)? = if (matches(
            query, "account", "name", "profile"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_local_account,
                title = stringResource(R.string.settings_account),
                value = if (!nameLocalAccount.isNullOrBlank()) {
                    nameLocalAccount
                } else {
                    stringResource(R.string.anonymous)
                },
                onClick = onOpenNameModal
            )
        }
    } else {
        null
    }

    val currencyRow: (@Composable () -> Unit)? = if (matches(
            query, "currency", "money"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_currency,
                title = stringResource(R.string.set_currency),
                value = currencyCode,
                onClick = onOpenCurrencyModal
            )
        }
    } else {
        null
    }

    val languageRow: (@Composable () -> Unit)? = if (languageOptionVisible && matches(
            query, "language", "locale"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_location_global,
                title = stringResource(R.string.language),
                value = Locale.getDefault().displayName,
                external = true,
                onClick = onSwitchLanguage
            )
        }
    } else {
        null
    }

    val startDateRow: (@Composable () -> Unit)? = if (matches(
            query, "start", "date", "month"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_custom_calendar_m,
                title = stringResource(R.string.start_date_of_month),
                value = startDateOfMonth.toString(),
                onClick = onOpenStartDateModal
            )
        }
    } else {
        null
    }

    RowsCard(
        header = stringResource(R.string.settings_section_preferences),
        rows = listOf(accountRow, currencyRow, languageRow, startDateRow)
    )
}

@Composable
private fun PrivacySection(
    query: String,
    lockApp: Boolean,
    hideCurrentBalance: Boolean,
    hideIncome: Boolean,
    onSetLockApp: (Boolean) -> Unit,
    onSetHideCurrentBalance: (Boolean) -> Unit,
    onSetHideIncome: (Boolean) -> Unit,
) {
    val lockRow: (@Composable () -> Unit)? = if (matches(
            query, "lock", "app lock", "privacy", "fingerprint", "biometric", "security"
        )
    ) {
        {
            SettingSwitchRow(
                icon = R.drawable.ic_custom_fingerprint_m,
                title = stringResource(R.string.lock_app),
                checked = lockApp,
                onToggle = onSetLockApp
            )
        }
    } else {
        null
    }

    val hideBalanceRow: (@Composable () -> Unit)? = if (matches(
            query, "hide", "balance", "privacy"
        )
    ) {
        {
            SettingSwitchRow(
                icon = R.drawable.ic_hide_m,
                title = stringResource(R.string.hide_balance),
                description = stringResource(R.string.hide_balance_description),
                checked = hideCurrentBalance,
                onToggle = onSetHideCurrentBalance
            )
        }
    } else {
        null
    }

    val hideIncomeRow: (@Composable () -> Unit)? = if (matches(
            query, "hide", "income", "privacy"
        )
    ) {
        {
            SettingSwitchRow(
                icon = R.drawable.ic_hide_m,
                title = stringResource(R.string.hide_income),
                description = stringResource(R.string.hide_income_description),
                checked = hideIncome,
                onToggle = onSetHideIncome
            )
        }
    } else {
        null
    }

    RowsCard(
        header = stringResource(R.string.settings_section_privacy),
        rows = listOf(lockRow, hideBalanceRow, hideIncomeRow)
    )
}

@Composable
private fun DataSection(
    query: String,
    onBackupData: () -> Unit,
    onExportToCSV: () -> Unit,
    onImportData: () -> Unit,
    onExchangeRates: () -> Unit,
) {
    val backupRow: (@Composable () -> Unit)? = if (matches(
            query, "backup", "zip", "export"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_security_shield,
                title = stringResource(R.string.backup_data),
                onClick = onBackupData
            )
        }
    } else {
        null
    }

    val exportCsvRow: (@Composable () -> Unit)? = if (matches(
            query, "export", "csv"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_pc_printer,
                title = stringResource(R.string.export_to_csv),
                description = stringResource(R.string.do_not_use_for_backup_purposes),
                onClick = onExportToCSV
            )
        }
    } else {
        null
    }

    val importRow: (@Composable () -> Unit)? = if (matches(
            query, "import", "csv", "transfer data"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_export_csv,
                title = stringResource(R.string.import_data),
                onClick = onImportData
            )
        }
    } else {
        null
    }

    val exchangeRatesRow: (@Composable () -> Unit)? = if (matches(
            query, "exchange", "rates", "currency"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_currency,
                title = stringResource(R.string.exchange_rates),
                onClick = onExchangeRates
            )
        }
    } else {
        null
    }

    RowsCard(
        header = stringResource(R.string.settings_section_data),
        rows = listOf(backupRow, exportCsvRow, importRow, exchangeRatesRow)
    )
}

@Composable
private fun BehaviorSection(
    query: String,
    showNotifications: Boolean,
    treatTransfersAsIncomeExpense: Boolean,
    dockTranslucent: Boolean,
    onSetShowNotifications: (Boolean) -> Unit,
    onSetTreatTransfersAsIncExp: (Boolean) -> Unit,
    onSetDockTranslucent: (Boolean) -> Unit,
    onAdvancedFeatures: () -> Unit,
) {
    val notificationsRow: (@Composable () -> Unit)? = if (matches(
            query, "notification", "reminder"
        )
    ) {
        {
            SettingSwitchRow(
                icon = R.drawable.ic_notification_m,
                title = stringResource(R.string.show_notifications),
                checked = showNotifications,
                onToggle = onSetShowNotifications
            )
        }
    } else {
        null
    }

    val transfersRow: (@Composable () -> Unit)? = if (matches(
            query, "transfer", "income", "expense"
        )
    ) {
        {
            SettingSwitchRow(
                icon = R.drawable.ic_custom_transfer_m,
                title = stringResource(R.string.transfers_as_income_expense),
                description = stringResource(R.string.transfers_as_income_expense_description),
                checked = treatTransfersAsIncomeExpense,
                onToggle = onSetTreatTransfersAsIncExp
            )
        }
    } else {
        null
    }

    val advancedFeaturesRow: (@Composable () -> Unit)? = if (matches(
            query, "advanced", "features", "experimental", "flags"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_custom_programming_m,
                title = stringResource(R.string.advanced_features),
                onClick = onAdvancedFeatures
            )
        }
    } else {
        null
    }

    val dockTranslucentRow: (@Composable () -> Unit)? = if (matches(
            query, "dock", "translucent", "glass", "blur", "navbar", "pill"
        )
    ) {
        {
            SettingSwitchRow(
                icon = R.drawable.ic_vue_design_color_swatch,
                title = stringResource(R.string.settings_dock_translucent),
                description = stringResource(R.string.settings_dock_translucent_description),
                checked = dockTranslucent,
                onToggle = onSetDockTranslucent
            )
        }
    } else {
        null
    }

    RowsCard(
        header = stringResource(R.string.settings_section_behavior),
        rows = listOf(notificationsRow, transfersRow, dockTranslucentRow, advancedFeaturesRow)
    )
}

@Composable
private fun SupportSection(
    query: String,
    onHelpFeedback: () -> Unit,
) {
    val helpFeedbackRow: (@Composable () -> Unit)? = if (matches(
            query,
            "help", "feedback", "support", "bug", "report", "feature", "community", "contact", "telegram"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_support,
                title = stringResource(R.string.settings_help_feedback),
                value = null,
                onClick = onHelpFeedback
            )
        }
    } else {
        null
    }

    RowsCard(
        header = stringResource(R.string.settings_section_support),
        rows = listOf(helpFeedbackRow)
    )
}

@Composable
private fun AboutSection(
    query: String,
    onReleases: () -> Unit,
    onContributors: () -> Unit,
    onAttributions: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val rootScreen = rootScreen()

    val rateRow: (@Composable () -> Unit)? = if (matches(
            query, "rate", "review", "play", "star"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_custom_star_m,
                title = stringResource(R.string.rate_us_on_google_play),
                external = true,
                onClick = { rootScreen.reviewIvyWallet(dismissReviewCard = false) }
            )
        }
    } else {
        null
    }

    val shareRow: (@Composable () -> Unit)? = if (matches(
            query, "share", "invite", "friend"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_custom_family_m,
                title = stringResource(R.string.share_ivy_wallet),
                onClick = { rootScreen.shareIvyWallet() }
            )
        }
    } else {
        null
    }

    val openSourceRow: (@Composable () -> Unit)? = if (matches(
            query, "open", "source", "github", "code"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.github_logo,
                title = stringResource(R.string.ivy_wallet_is_opensource),
                external = true,
                onClick = {
                    rootScreen.openUrlInBrowser(Constants.URL_IVY_WALLET_REPO)
                }
            )
        }
    } else {
        null
    }

    val releasesRow: (@Composable () -> Unit)? = if (matches(
            query, "release", "version", "changelog", "update"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_money_tag,
                title = stringResource(R.string.releases),
                onClick = onReleases
            )
        }
    } else {
        null
    }

    val contributorsRow: (@Composable () -> Unit)? = if (matches(
            query, "contributor", "people", "team"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_people_people,
                title = stringResource(R.string.project_contributors),
                onClick = onContributors
            )
        }
    } else {
        null
    }

    val attributionsRow: (@Composable () -> Unit)? = if (matches(
            query, "attribution", "license", "credits"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_location_global,
                title = stringResource(R.string.attributions),
                onClick = onAttributions
            )
        }
    } else {
        null
    }

    val tcRow: (@Composable () -> Unit)? = if (matches(
            query, "terms", "conditions", "legal"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_security_shield,
                title = stringResource(R.string.terms_conditions),
                external = true,
                onClick = { uriHandler.openUri(Constants.URL_TC) }
            )
        }
    } else {
        null
    }

    val privacyPolicyRow: (@Composable () -> Unit)? = if (matches(
            query, "privacy", "policy", "legal"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_vue_security_shield,
                title = stringResource(R.string.privacy_policy),
                external = true,
                onClick = { uriHandler.openUri(Constants.URL_PRIVACY_POLICY) }
            )
        }
    } else {
        null
    }

    RowsCard(
        header = stringResource(R.string.settings_section_about),
        rows = listOf(
            rateRow,
            shareRow,
            openSourceRow,
            releasesRow,
            contributorsRow,
            attributionsRow,
            tcRow,
            privacyPolicyRow
        )
    )
}

@Composable
private fun DangerZoneSection(
    query: String,
    onDeleteAllUserData: () -> Unit,
) {
    val deleteRow: (@Composable () -> Unit)? = if (matches(
            query, "delete", "danger", "data", "reset", "wipe"
        )
    ) {
        {
            SettingNavRow(
                icon = R.drawable.ic_delete,
                title = stringResource(R.string.delete_all_user_data),
                tint = Red,
                onClick = onDeleteAllUserData
            )
        }
    } else {
        null
    }

    RowsCard(
        header = stringResource(R.string.danger_zone),
        rows = listOf(deleteRow),
        headerColor = Red
    )
}

@Composable
private fun SettingsFooter() {
    val rootScreen = rootScreen()

    Spacer(Modifier.height(40.dp))

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_version_footer"),
        text = "Ivy Wallet ${rootScreen.buildVersionName} " +
            "(${rootScreen.buildVersionCode})",
        style = inter(12.sp),
        color = UI.colors.gray,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(150.dp))
}

// -----------------------------------------------------------------------------------------
// Reusable building blocks
// -----------------------------------------------------------------------------------------

@Composable
private fun RowsCard(
    header: String,
    rows: List<(@Composable () -> Unit)?>,
    headerColor: Color = UI.colors.gray,
) {
    val visibleRows = rows.filterNotNull()
    if (visibleRows.isEmpty()) return

    SectionHeader(text = header, color = headerColor)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .border(1.dp, hairlineColor(), RoundedCornerShape(20.dp))
    ) {
        visibleRows.forEachIndexed { index, row ->
            if (index > 0) {
                RowSeparator()
            }
            row()
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    color: Color = UI.colors.gray,
) {
    Spacer(Modifier.height(32.dp))

    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = text.uppercase(Locale.getDefault()),
        style = inter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
        color = color
    )

    Spacer(Modifier.height(10.dp))
}

@Composable
private fun RowSeparator() {
    Box(
        modifier = Modifier
            .padding(start = 54.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(UI.colors.pureInverse.copy(alpha = 0.06f))
    )
}

@Composable
private fun SettingNavRow(
    @DrawableRes icon: Int,
    title: String,
    description: String? = null,
    value: String? = null,
    tint: Color = UI.colors.pureInverse,
    external: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = inter(17.sp, FontWeight.Medium),
                color = tint
            )

            if (!description.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))

                Text(
                    text = description,
                    style = inter(13.sp),
                    color = UI.colors.gray
                )
            }
        }

        if (!value.isNullOrEmpty()) {
            Spacer(Modifier.width(8.dp))

            Text(
                text = value,
                style = inter(16.sp),
                color = UI.colors.gray
            )
        }

        Spacer(Modifier.width(10.dp))

        Icon(
            painter = painterResource(
                if (external) {
                    R.drawable.ic_arrow_up_right
                } else {
                    R.drawable.ic_arrow_right
                }
            ),
            contentDescription = if (external) {
                stringResource(R.string.opens_externally)
            } else {
                null
            },
            tint = UI.colors.gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingSwitchRow(
    @DrawableRes icon: Int,
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    description: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = UI.colors.pureInverse,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = inter(17.sp, FontWeight.Medium),
                color = UI.colors.pureInverse
            )

            if (!description.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))

                Text(
                    text = description,
                    style = inter(13.sp),
                    color = UI.colors.gray
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        IvySwitch(enabled = checked) {
            onToggle(it)
        }
    }
}

@Composable
private fun hairlineColor(): Color = UI.colors.pureInverse.copy(alpha = 0.08f)

private fun matches(query: String, vararg keywords: String): Boolean {
    if (query.isBlank()) return true
    val trimmed = query.trim()
    return keywords.any { it.contains(trimmed, ignoreCase = true) }
}

@Preview
@Composable
private fun Preview(theme: Theme = Theme.LIGHT) {
    IvyWalletPreview(theme) {
        UI(
            nameLocalAccount = null,
            theme = theme,
            look = SettingsAppearance.LOOK_IVY,
            accentColor = SettingsAppearance.ACCENT_DEFAULT,
            dockTranslucent = false,
            lockApp = false,
            currencyCode = "BGN",
            onSetCurrency = {},
            languageOptionVisible = true
        )
    }
}

/** For screenshot testing */
@Composable
fun SettingsUiTest(isDark: Boolean) {
    val theme = when (isDark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    Preview(theme)
}
