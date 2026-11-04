package com.ivy

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.ivy.attributions.AttributionsScreenImpl
import com.ivy.balance.BalanceScreen
import com.ivy.budgets.BudgetScreen
import com.ivy.categories.CategoriesScreen
import com.ivy.tags.TagsScreen
import com.ivy.contributors.ContributorsScreenImpl
import com.ivy.design.l0_system.UI
import com.ivy.disclaimer.DisclaimerScreenImpl
import com.ivy.exchangerates.ExchangeRatesScreen
import com.ivy.features.FeaturesScreenImpl
import com.ivy.importdata.csv.CSVScreen
import com.ivy.importdata.csvimport.ImportCSVScreen
import com.ivy.loans.loan.LoansScreen
import com.ivy.loans.loandetails.LoanDetailsScreen
import com.ivy.main.MainScreen
import com.ivy.main.BottomBar
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.ContextualDock
import com.ivy.navigation.AttributionsScreen
import com.ivy.navigation.BalanceScreen
import com.ivy.navigation.BudgetScreen
import com.ivy.navigation.CSVScreen
import com.ivy.navigation.CategoriesScreen
import com.ivy.navigation.TagsScreen
import com.ivy.navigation.ContributorsScreen
import com.ivy.navigation.DisclaimerScreen
import com.ivy.navigation.EditPlannedScreen
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.ExchangeRatesScreen
import com.ivy.navigation.FeaturesScreen
import com.ivy.navigation.ImportScreen
import com.ivy.navigation.LoanDetailsScreen
import com.ivy.navigation.LoansScreen
import com.ivy.navigation.MainScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.OnboardingScreen
import com.ivy.navigation.PieChartStatisticScreen
import com.ivy.navigation.PlannedPaymentsScreen
import com.ivy.navigation.PollScreen
import com.ivy.navigation.ReleasesScreen
import com.ivy.navigation.ReportScreen
import com.ivy.navigation.Screen
import com.ivy.navigation.SearchScreen
import com.ivy.navigation.SettingsScreen
import com.ivy.navigation.TransactionsScreen
import com.ivy.onboarding.OnboardingScreen
import com.ivy.piechart.PieChartStatisticScreen
import com.ivy.planned.edit.EditPlannedScreen
import com.ivy.planned.list.PlannedPaymentsScreen
import com.ivy.poll.impl.ui.PollScreen
import com.ivy.releases.ReleasesScreenImpl
import com.ivy.reports.ReportScreen
import com.ivy.search.SearchScreen
import com.ivy.settings.SettingsScreen
import com.ivy.transaction.EditTransactionScreen
import com.ivy.transactions.TransactionsScreen

/**
 * Screens that are presented as a "fluid sheet" rising from the bottom edge
 * (the add/edit transaction flow opened from the radial wedge menu).
 */
private val Screen?.isFluidAddScreen: Boolean
    get() = this is EditTransactionScreen || this is EditPlannedScreen

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
@Suppress("CyclomaticComplexMethod", "FunctionNaming", "LongMethod")
fun BoxWithConstraintsScope.IvyNavGraph(screen: Screen?) {
    // Pushed "list" screens (Categories, Tags) show the floating dock with a
    // contextual "+" (add category / add tag) instead of the radial add menu.
    val showContextualDock = screen is CategoriesScreen || screen is TagsScreen
    val ivyContext = ivyWalletCtx()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
        targetState = screen,
        transitionSpec = { navTransition() },
        contentKey = { it?.javaClass },
        label = "IvyNavGraph",
    ) { currentScreen ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (currentScreen.isFluidAddScreen) 1f else 0f)
        ) {
            if (currentScreen.isFluidAddScreen) {
                // Opaque backdrop so the rising sheet fully covers the base screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(UI.colors.pure)
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    null -> {
                        // show nothing
                    }

                    is MainScreen -> MainScreen(screen = currentScreen)
                    is OnboardingScreen -> OnboardingScreen(screen = currentScreen)
                    is ExchangeRatesScreen -> ExchangeRatesScreen()
                    is EditTransactionScreen -> EditTransactionScreen(screen = currentScreen)
                    is TransactionsScreen -> TransactionsScreen(screen = currentScreen)
                    is PieChartStatisticScreen -> PieChartStatisticScreen(screen = currentScreen)
                    is CategoriesScreen -> CategoriesScreen(screen = currentScreen)
                    is TagsScreen -> TagsScreen(screen = currentScreen)
                    is SettingsScreen -> SettingsScreen()
                    is PlannedPaymentsScreen -> PlannedPaymentsScreen(screen = currentScreen)
                    is EditPlannedScreen -> EditPlannedScreen(screen = currentScreen)
                    is BalanceScreen -> BalanceScreen(screen = currentScreen)
                    is ImportScreen -> ImportCSVScreen(screen = currentScreen)
                    is ReportScreen -> ReportScreen(screen = currentScreen)
                    is BudgetScreen -> BudgetScreen(screen = currentScreen)
                    is LoansScreen -> LoansScreen(screen = currentScreen)
                    is LoanDetailsScreen -> LoanDetailsScreen(screen = currentScreen)
                    is SearchScreen -> SearchScreen(screen = currentScreen)
                    is CSVScreen -> CSVScreen(screen = currentScreen)
                    FeaturesScreen -> FeaturesScreenImpl()
                    AttributionsScreen -> AttributionsScreenImpl()
                    ContributorsScreen -> ContributorsScreenImpl()
                    ReleasesScreen -> ReleasesScreenImpl()
                    DisclaimerScreen -> DisclaimerScreenImpl()
                    PollScreen -> PollScreen()
                }
            }
        }
        }

        if (showContextualDock) {
            val nav = navigation()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    BottomBar(
                        tab = ivyContext.mainTab,
                        selectTab = { tab ->
                            ivyContext.selectMainTab(tab)
                            nav.resetBackStack()
                            nav.navigateTo(MainScreen)
                        },
                        onAddIncome = {
                            nav.navigateTo(
                                EditTransactionScreen(
                                    initialTransactionId = null,
                                    type = com.ivy.base.model.TransactionType.INCOME
                                )
                            )
                        },
                        onAddExpense = {
                            nav.navigateTo(
                                EditTransactionScreen(
                                    initialTransactionId = null,
                                    type = com.ivy.base.model.TransactionType.EXPENSE
                                )
                            )
                        },
                        onAddTransfer = {
                            nav.navigateTo(
                                EditTransactionScreen(
                                    initialTransactionId = null,
                                    type = com.ivy.base.model.TransactionType.TRANSFER
                                )
                            )
                        },
                        onAddPlannedPayment = {
                            nav.navigateTo(
                                EditPlannedScreen(
                                    type = com.ivy.base.model.TransactionType.EXPENSE,
                                    plannedPaymentRuleId = null
                                )
                            )
                        },
                        // Disabled here - the contextual "+" is used instead
                        showAddAccountModal = {},
                        overrideAddAction = {
                            // Categories/Tags screens collect this and open their add modal
                            ContextualDock.requestAdd()
                        },
                    )
                }
            }
        }
    }
}

/**
 * Fluid motion language for the add-transaction flow:
 * - Opening: the add/edit sheet rises from the bottom edge with a slight
 *   bounce while the base screen scales back (iOS "pushed under" feel).
 * - Back: the sheet sinks off-screen and the base screen springs back to 1.
 * - Every other navigation keeps the legacy hard cut (no regression).
 */
private fun AnimatedContentTransitionScope<Screen?>.navTransition(): ContentTransform {
    val targetFluid = targetState.isFluidAddScreen
    val initialFluid = initialState.isFluidAddScreen
    return when {
        targetFluid && !initialFluid -> {
            (slideInVertically(
                animationSpec = spring(dampingRatio = 0.86f, stiffness = 380f),
                initialOffsetY = { it },
            ) + fadeIn(animationSpec = spring(stiffness = 380f))) togetherWith
                (scaleOut(
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
                    targetScale = 0.94f,
                ) + fadeOut(animationSpec = spring(stiffness = 300f)))
        }

        initialFluid && !targetFluid -> {
            (scaleIn(
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 330f),
                initialScale = 0.94f,
            ) + fadeIn(animationSpec = spring(stiffness = 330f))) togetherWith
                slideOutVertically(
                    animationSpec = spring(dampingRatio = 0.88f, stiffness = 400f),
                    targetOffsetY = { it },
                )
        }

        else -> EnterTransition.None togetherWith ExitTransition.None
    }
}
