package com.ivy.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.legacy.Constants
import com.ivy.legacy.domain.data.IvyCurrency
import com.ivy.ui.R

// -----------------------------------------------------------------------------------------
// Cupertino-style sheets that replace the legacy IvyModal implementations in Settings.
// -----------------------------------------------------------------------------------------

@Composable
internal fun SettingsCurrencySheet(
    visible: Boolean,
    initialCurrencyCode: String,
    dismiss: () -> Unit,
    onSetCurrency: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    val allCurrencies = remember(visible) {
        if (visible) IvyCurrency.getAvailable().sortedBy { it.code } else emptyList()
    }

    val filtered = remember(query, allCurrencies) {
        if (query.isBlank()) {
            allCurrencies
        } else {
            allCurrencies.filter {
                it.code.contains(query, ignoreCase = true) ||
                    it.name.contains(query, ignoreCase = true)
            }
        }
    }

    CupertinoSheet(
        visible = visible,
        title = stringResource(R.string.set_currency),
        onDismiss = dismiss,
    ) {
        SheetSearchField(
            query = query,
            onQueryChange = { query = it },
            hint = stringResource(R.string.settings_search_hint)
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            items(filtered) { currency ->
                val selected = currency.code == initialCurrencyCode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable {
                            onSetCurrency(currency.code)
                            dismiss()
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currency.code,
                        style = inter(16.sp, FontWeight.SemiBold),
                        color = UI.colors.pureInverse,
                        modifier = Modifier.width(64.dp)
                    )

                    Text(
                        text = currency.name,
                        style = inter(14.sp),
                        color = UI.colors.gray,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    if (selected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = UI.colors.pureInverse,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
internal fun SettingsNameSheet(
    visible: Boolean,
    initialName: String,
    dismiss: () -> Unit,
    onSetName: (String) -> Unit,
) {
    var name by remember(visible) { mutableStateOf(initialName) }

    CupertinoSheet(
        visible = visible,
        title = stringResource(R.string.settings_your_name),
        onDismiss = dismiss,
    ) {
        SheetTextField(
            value = name,
            onValueChange = { name = it },
            hint = stringResource(R.string.settings_your_name)
        )

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(UI.colors.pureInverse)
                .clickable {
                    onSetName(name.trim())
                    dismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.settings_save),
                style = inter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pure
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun SettingsStartDaySheet(
    visible: Boolean,
    selectedDay: Int,
    dismiss: () -> Unit,
    onSelectDay: (Int) -> Unit,
) {
    CupertinoSheet(
        visible = visible,
        title = stringResource(R.string.start_date_of_month),
        onDismiss = dismiss,
    ) {
        Text(
            text = stringResource(R.string.settings_select_start_day),
            style = inter(13.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (1..31).chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    week.forEach { day ->
                        DayCell(
                            day = day,
                            selected = day == selectedDay,
                            onClick = {
                                onSelectDay(day)
                                dismiss()
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DayCell(
    day: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (selected) UI.colors.pureInverse else UI.colors.medium
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = hairline(),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = inter(14.sp, FontWeight.Medium),
            color = if (selected) UI.colors.pure else UI.colors.pureInverse
        )
    }
}

/**
 * "Help & feedback" sheet: consolidates the formerly duplicated support rows.
 * All rows leave the app (marked with the external-link arrow).
 */
@Composable
internal fun HelpFeedbackSheet(
    visible: Boolean,
    dismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    CupertinoSheet(
        visible = visible,
        title = stringResource(R.string.settings_help_feedback),
        onDismiss = dismiss,
    ) {
        SheetRow(
            title = stringResource(R.string.report_bug),
            description = "GitHub · Ivy-Apps/ivy-wallet",
            onClick = {
                dismiss()
                uriHandler.openUri(Constants.URL_GITHUB_NEW_ISSUE)
            },
            trailing = {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_up_right),
                    contentDescription = null,
                    tint = UI.colors.gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        SheetSeparator()

        SheetRow(
            title = stringResource(R.string.request_a_feature),
            description = "GitHub · Ivy-Apps/ivy-wallet",
            onClick = {
                dismiss()
                uriHandler.openUri(Constants.URL_GITHUB_NEW_ISSUE)
            },
            trailing = {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_up_right),
                    contentDescription = null,
                    tint = UI.colors.gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        SheetSeparator()

        SheetRow(
            title = stringResource(R.string.settings_join_community),
            description = "Telegram",
            onClick = {
                dismiss()
                uriHandler.openUri(Constants.URL_IVY_TELEGRAM_INVITE)
            },
            trailing = {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_up_right),
                    contentDescription = null,
                    tint = UI.colors.gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

// -----------------------------------------------------------------------------------------
// Reusable sheet inputs
// -----------------------------------------------------------------------------------------

@Composable
private fun SheetSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
) {
    SheetInputContainer {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            tint = UI.colors.gray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(10.dp))

        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = hint,
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
    }
}

@Composable
private fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
) {
    SheetInputContainer {
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    style = inter(16.sp),
                    color = UI.colors.gray
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = inter(16.sp).copy(color = UI.colors.pureInverse),
                cursorBrush = SolidColor(UI.colors.pureInverse),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SheetInputContainer(
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(UI.colors.medium)
            .border(1.dp, hairline(), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
