package com.ivy.exchangerates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.base.legacy.Theme
import com.ivy.design.l0_system.UI
import com.ivy.exchangerates.data.RateUi
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.cupertino.CupertinoChildPage
import com.ivy.legacy.cupertino.CupertinoDockClearance
import com.ivy.legacy.cupertino.CupertinoFilledButton
import com.ivy.legacy.cupertino.CupertinoSectionHeader
import com.ivy.legacy.cupertino.cupertinoHairline
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.theme.components.DeleteButton

@Composable
fun BoxWithConstraintsScope.ExchangeRatesScreen(
    onClose: (() -> Unit)? = null,
) {
    val viewModel: ExchangeRatesViewModel = viewModel()
    val state = viewModel.uiState()

    UI(
        state = state,
        onEvent = viewModel::onEvent,
        onClose = onClose,
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: RatesState,
    onEvent: (RatesEvent) -> Unit,
    onClose: (() -> Unit)? = null,
) {
    var rateToUpdate by remember {
        mutableStateOf<RateUi?>(null)
    }
    var addRateModalVisible by remember {
        mutableStateOf(false)
    }

    CupertinoChildPage(
        title = stringResource(R.string.exchange_rates),
        onClose = onClose ?: {},
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "search") {
                    SearchField(
                        onSearch = { onEvent(RatesEvent.Search(it)) }
                    )
                }

                item(key = "manual_header") {
                    CupertinoSectionHeader(text = "Manual")
                }

                items(items = state.manual) { rate ->
                    RateRow(
                        rate = rate,
                        canDelete = true,
                        onDelete = { onEvent(RatesEvent.RemoveOverride(rate)) },
                        onClick = { rateToUpdate = rate }
                    )
                }

                item(key = "automatic_header") {
                    CupertinoSectionHeader(text = "Automatic")
                }

                items(items = state.automatic) { rate ->
                    RateRow(
                        rate = rate,
                        canDelete = false,
                        onDelete = null,
                        onClick = { rateToUpdate = rate }
                    )
                }

                item(key = "dock_clearance") {
                    Spacer(Modifier.height(CupertinoDockClearance + 72.dp))
                }
            }

            CupertinoFilledButton(
                label = stringResource(R.string.add_manual_exchange_rate),
                enabled = true,
                onClick = { addRateModalVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = CupertinoDockClearance)
            )
        }
    }

    AddRateSheet(
        visible = addRateModalVisible,
        baseCurrency = state.baseCurrency,
        dismiss = { addRateModalVisible = false },
        onAdd = { newRate ->
            onEvent(RatesEvent.AddRate(newRate))
            addRateModalVisible = false
        }
    )

    rateToUpdate?.let { rate ->
        EditRateSheet(
            rate = rate,
            dismiss = { rateToUpdate = null },
            onDone = { newRate ->
                onEvent(RatesEvent.UpdateRate(rate, newRate))
                rateToUpdate = null
            }
        )
    }
}

@Composable
private fun RateRow(
    rate: RateUi,
    canDelete: Boolean,
    onDelete: (() -> Unit)?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "${rate.from} → ${rate.to}",
                style = ivyInter(17.sp, FontWeight.Medium),
                color = UI.colors.pureInverse
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = rate.rate.format(currencyCode = rate.to),
                style = ivyInter(13.sp),
                color = UI.colors.gray
            )
        }

        if (canDelete && onDelete != null) {
            DeleteButton(onClick = onDelete)

            Spacer(Modifier.width(8.dp))
        }

        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = UI.colors.gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SearchField(
    onSearch: (String) -> Unit,
) {
    var query by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(UI.colors.medium)
            .border(1.dp, cupertinoHairline(), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                    text = "Search currency",
                    style = ivyInter(16.sp),
                    color = UI.colors.gray
                )
            }

            BasicTextField(
                value = query,
                onValueChange = {
                    query = it
                    onSearch(it)
                },
                singleLine = true,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
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
                    .clickable {
                        query = ""
                        onSearch("")
                    }
            )
        }
    }
}

@Preview
@Composable
private fun Preview(theme: Theme = Theme.LIGHT) {
    IvyWalletPreview(theme) {
        UI(
            state = RatesState(
                baseCurrency = "BGN",
                manual = kotlinx.collections.immutable.persistentListOf(
                    RateUi("BGN", "USD", 1.85),
                    RateUi("BGN", "EUR", 1.96),
                ),
                automatic = kotlinx.collections.immutable.persistentListOf(
                    RateUi("XXX", "YYY", 1.23),
                    RateUi("XXX", "YYY", 1.23),
                    RateUi("XXX", "YYY", 1.23),
                )
            ),
            onEvent = {},
            onClose = null,
        )
    }
}

/** For screenshot testing */
@Composable
fun ExchangeRatesScreenUiTest(isDark: Boolean) {
    Preview(theme = if (isDark) Theme.DARK else Theme.LIGHT)
}
