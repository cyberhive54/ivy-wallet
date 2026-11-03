package com.ivy.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.frp.forward
import com.ivy.frp.then2
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.wallet.domain.data.IvyCurrency
import com.ivy.wallet.ui.theme.modal.BufferModal
import com.ivy.wallet.ui.theme.modal.BufferModalData
import com.ivy.wallet.ui.theme.modal.CurrencyModal

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.MenuTab() {
    val viewModel: HomeViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    var bufferModalData: BufferModalData? by remember { mutableStateOf(null) }
    var currencyModalVisible by remember { mutableStateOf(false) }

    val baseCurrency = uiState.baseData.baseCurrency

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        MoreMenuContent(
            theme = uiState.theme,
            onSwitchTheme = { viewModel.onEvent(HomeEvent.SwitchTheme) },
            balance = uiState.balance.toDouble(),
            buffer = uiState.buffer.amount.toDouble(),
            currency = baseCurrency,
            onBufferClick = {
                bufferModalData = BufferModalData(
                    balance = uiState.balance.toDouble(),
                    currency = baseCurrency,
                    buffer = uiState.buffer.amount.toDouble()
                )
            },
            onCurrencyClick = {
                currencyModalVisible = true
            }
        )

        Spacer(Modifier.height(110.dp))
    }

    BufferModal(
        modal = bufferModalData,
        dismiss = { bufferModalData = null },
        onBufferChanged = forward<Double>() then2 {
            HomeEvent.SetBuffer(it)
        } then2 viewModel::onEvent
    )

    CurrencyModal(
        title = stringResource(R.string.set_currency),
        initialCurrency = IvyCurrency.fromCode(baseCurrency),
        visible = currencyModalVisible,
        dismiss = { currencyModalVisible = false },
        onSetCurrency = forward<String>() then2 {
            HomeEvent.SetCurrency(it)
        } then2 viewModel::onEvent
    )
}
