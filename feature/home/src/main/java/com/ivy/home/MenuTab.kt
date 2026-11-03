package com.ivy.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.base.legacy.Theme
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.frp.forward
import com.ivy.frp.then2
import com.ivy.legacy.Constants
import com.ivy.legacy.rootScreen
import com.ivy.legacy.utils.openUrl
import com.ivy.navigation.BudgetScreen
import com.ivy.navigation.CategoriesScreen
import com.ivy.navigation.LoansScreen
import com.ivy.navigation.PlannedPaymentsScreen
import com.ivy.navigation.ReportScreen
import com.ivy.navigation.SearchScreen
import com.ivy.navigation.SettingsScreen
import com.ivy.navigation.TagsScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.wallet.domain.data.IvyCurrency
import com.ivy.wallet.ui.theme.components.BufferBattery
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.BufferModal
import com.ivy.wallet.ui.theme.modal.BufferModalData
import com.ivy.wallet.ui.theme.modal.CurrencyModal
import com.ivy.wallet.ui.theme.wallet.AmountCurrencyB1

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.MenuTab() {
    val viewModel: HomeViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    var bufferModalData: BufferModalData? by remember { mutableStateOf(null) }
    var currencyModalVisible by remember { mutableStateOf(false) }

    val baseCurrency = uiState.baseData.baseCurrency
    val nav = navigation()

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.menu),
            style = UI.typo.h1.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(20.dp))

        MenuSearchRow(
            onClick = {
                nav.navigateTo(SearchScreen)
            }
        )

        Spacer(Modifier.height(28.dp))

        SectionLabel(text = stringResource(R.string.menu_manage))

        MenuGroup {
            MenuRow(
                icon = R.drawable.home_more_menu_categories,
                label = stringResource(R.string.categories)
            ) {
                nav.navigateTo(CategoriesScreen)
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.ic_hashtag,
                label = stringResource(R.string.tags)
            ) {
                nav.navigateTo(TagsScreen)
            }
        }

        Spacer(Modifier.height(28.dp))

        SectionLabel(text = stringResource(R.string.quick_access))

        MenuGroup {
            MenuRow(
                icon = R.drawable.home_more_menu_budgets,
                label = stringResource(R.string.budgets)
            ) {
                nav.navigateTo(BudgetScreen)
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.home_more_menu_reports,
                label = stringResource(R.string.reports)
            ) {
                nav.navigateTo(ReportScreen)
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.home_more_menu_planned_payments,
                label = stringResource(R.string.planned_payments)
            ) {
                nav.navigateTo(PlannedPaymentsScreen)
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.home_more_menu_loans,
                label = stringResource(R.string.loans)
            ) {
                nav.navigateTo(LoansScreen)
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.home_more_menu_settings,
                label = stringResource(R.string.settings)
            ) {
                nav.navigateTo(SettingsScreen)
            }
        }

        Spacer(Modifier.height(28.dp))

        SectionLabel(text = stringResource(R.string.menu_preferences))

        MenuGroup {
            MenuRow(
                icon = when (uiState.theme) {
                    Theme.LIGHT -> R.drawable.home_more_menu_light_mode
                    Theme.DARK -> R.drawable.home_more_menu_dark_mode
                    Theme.AMOLED_DARK -> R.drawable.home_more_menu_amoled_dark_mode
                    Theme.AUTO -> R.drawable.home_more_menu_auto_mode
                },
                label = stringResource(R.string.appearance),
                iconChipBg = when (uiState.theme) {
                    Theme.LIGHT -> UI.colors.pure
                    Theme.DARK -> UI.colors.pureInverse
                    Theme.AMOLED_DARK -> UI.colors.pureInverse
                    Theme.AUTO -> UI.colors.pure
                },
                iconTint = when (uiState.theme) {
                    Theme.LIGHT -> UI.colors.pureInverse
                    Theme.DARK -> UI.colors.pure
                    Theme.AMOLED_DARK -> UI.colors.pure
                    Theme.AUTO -> UI.colors.pureInverse
                },
                trailing = {
                    Text(
                        text = when (uiState.theme) {
                            Theme.LIGHT -> stringResource(R.string.light_mode)
                            Theme.DARK -> stringResource(R.string.dark_mode)
                            Theme.AMOLED_DARK -> stringResource(R.string.amoled_mode)
                            Theme.AUTO -> stringResource(R.string.auto_mode)
                        },
                        style = UI.typo.c.style(
                            color = UI.colors.gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            ) {
                viewModel.onEvent(HomeEvent.SwitchTheme)
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.ic_currency,
                label = stringResource(R.string.base_currency),
                trailing = {
                    Text(
                        text = baseCurrency,
                        style = UI.typo.b1.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            ) {
                currencyModalVisible = true
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.home_more_menu_planned_payments,
                label = stringResource(R.string.savings_goal),
                trailing = {
                    AmountCurrencyB1(
                        amount = uiState.buffer.amount.toDouble(),
                        currency = baseCurrency,
                        amountFontWeight = FontWeight.SemiBold
                    )
                }
            ) {
                bufferModalData = BufferModalData(
                    balance = uiState.balance.toDouble(),
                    currency = baseCurrency,
                    buffer = uiState.buffer.amount.toDouble()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        BufferBattery(
            modifier = Modifier.padding(horizontal = 16.dp),
            buffer = uiState.buffer.amount.toDouble(),
            currency = baseCurrency,
            balance = uiState.balance.toDouble()
        ) {
            bufferModalData = BufferModalData(
                balance = uiState.balance.toDouble(),
                currency = baseCurrency,
                buffer = uiState.buffer.amount.toDouble()
            )
        }

        Spacer(Modifier.height(28.dp))

        SectionLabel(text = stringResource(R.string.menu_more))

        MenuGroup {
            val uriHandler = LocalUriHandler.current
            val rootScreen = rootScreen()

            MenuRow(
                icon = R.drawable.home_more_menu_share,
                label = stringResource(R.string.share_ivy)
            ) {
                rootScreen.shareIvyWallet()
            }

            MenuDivider()

            MenuRow(
                icon = R.drawable.github_logo,
                label = stringResource(R.string.ivy_wallet_open_source)
            ) {
                openUrl(
                    uriHandler = uriHandler,
                    url = Constants.URL_IVY_WALLET_REPO
                )
            }
        }

        Spacer(Modifier.height(150.dp))
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

@Composable
private fun MenuSearchRow(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UI.shapes.rFull)
            .background(UI.colors.pure)
            .border(1.dp, UI.colors.medium, UI.shapes.rFull)
            .clickable {
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IvyIcon(icon = R.drawable.ic_search, tint = UI.colors.pureInverse)

        Spacer(Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.search_transactions),
            style = UI.typo.b2.style(
                fontWeight = FontWeight.SemiBold,
                color = UI.colors.pureInverse
            )
        )

        Spacer(Modifier.weight(1f))

        IvyIcon(
            modifier = Modifier.size(16.dp),
            icon = R.drawable.ic_arrow_right,
            tint = UI.colors.gray
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        modifier = Modifier.padding(start = 24.dp, bottom = 10.dp),
        text = text,
        style = UI.typo.b2.style(
            color = UI.colors.gray,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun MenuGroup(
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UI.shapes.r4)
            .background(UI.colors.pure),
        content = content
    )
}

@Composable
private fun MenuDivider() {
    Spacer(
        modifier = Modifier
            .padding(start = 58.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(UI.colors.medium)
    )
}

@Composable
private fun MenuRow(
    @DrawableRes icon: Int,
    label: String,
    iconChipBg: Color = UI.colors.medium,
    iconTint: Color = UI.colors.pureInverse,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 54.dp)
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconChipBg),
            contentAlignment = Alignment.Center
        ) {
            IvyIcon(
                modifier = Modifier.size(18.dp),
                icon = icon,
                tint = iconTint
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = label,
            style = UI.typo.b1.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f)
        )

        trailing?.invoke(this)

        Spacer(Modifier.width(10.dp))

        IvyIcon(
            modifier = Modifier.size(15.dp),
            icon = R.drawable.ic_arrow_right,
            tint = UI.colors.gray
        )
    }
}
