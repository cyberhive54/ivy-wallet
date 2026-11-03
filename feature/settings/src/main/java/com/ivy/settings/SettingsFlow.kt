package com.ivy.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.ivy.attributions.AttributionsScreenImpl
import com.ivy.contributors.ContributorsScreenImpl
import com.ivy.exchangerates.ExchangeRatesScreen
import com.ivy.features.FeaturesScreenImpl
import com.ivy.importdata.csv.CSVScreen as CsvScreenUi
import com.ivy.importdata.csvimport.ImportCSVScreen
import com.ivy.legacy.ivyWalletCtx
import com.ivy.navigation.CSVScreen
import com.ivy.navigation.ImportScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.releases.ReleasesScreenImpl

/**
 * Pages reachable from the Settings root.
 *
 * They are rendered INSIDE the Settings dock tab (in-tab child stack) instead of the
 * global navigation graph, so the bottom dock stays visible with "Settings" selected
 * and system back pops one level back to the Settings root.
 */
internal enum class SettingsChildPage {
    ExchangeRates,
    Features,
    Attributions,
    Releases,
    Contributors,
    Import,
    Csv,
}

/**
 * Hosts the Settings root list and the [SettingsChildPage] child pages in a local
 * back stack inside the Settings tab slot. The dock (rendered by MainScreen) is
 * therefore always visible on every settings page.
 *
 * Back behavior:
 * - child page → pops one level back to the Settings root (never switches dock tab)
 * - Settings root → back is not intercepted (system default applies)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BoxWithConstraintsScope.SettingsFlowHost() {
    val nav = navigation()
    val viewModel: SettingsViewModel = screenScopedViewModel()
    val ivyContext = ivyWalletCtx()

    // Persisted as a CSV string for robust saveable state across process death.
    var stackRaw by rememberSaveable { mutableStateOf("") }
    var pushing by remember { mutableStateOf(true) }

    // Hoisted ABOVE the child-page stack so the Settings root list keeps its scroll
    // position when a child page is pushed on top of it (the root content is disposed
    // while a child page is shown). Initial values come from the Activity-scoped
    // ViewModel, which also preserves the position across dock tab switches.
    val rootListState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.rootListScrollIndex.intValue,
        initialFirstVisibleItemScrollOffset = viewModel.rootListScrollOffset.intValue
    )

    // Continuously mirror the scroll position into the ViewModel.
    LaunchedEffect(rootListState) {
        snapshotFlow {
            rootListState.firstVisibleItemIndex to rootListState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.saveRootListScroll(index, offset)
        }
    }

    // iOS convention: tapping the already-active Settings dock tab pops the child
    // stack back to the root and scrolls the list to the top.
    var lastSeenTabReclick by remember { mutableIntStateOf(ivyContext.mainTabReclick) }
    LaunchedEffect(ivyContext.mainTabReclick) {
        if (ivyContext.mainTabReclick == lastSeenTabReclick) return@LaunchedEffect
        lastSeenTabReclick = ivyContext.mainTabReclick

        if (stackRaw.isNotBlank()) {
            pushing = false
            stackRaw = ""
        }
        viewModel.saveRootListScroll(0, 0)
        rootListState.animateScrollToItem(0)
    }

    val stack: List<SettingsChildPage> = remember(stackRaw) {
        if (stackRaw.isBlank()) {
            emptyList()
        } else {
            stackRaw.split(",")
                .mapNotNull { name ->
                    runCatching { SettingsChildPage.valueOf(name.trim()) }.getOrNull()
                }
        }
    }

    fun push(page: SettingsChildPage) {
        pushing = true
        stackRaw = (stack + page).joinToString(",") { it.name }
    }

    fun pop() {
        pushing = false
        stackRaw = stack.dropLast(1).joinToString(",") { it.name }
    }

    // Intercepts back only when a child page is open; at the Settings root the
    // system default applies (handled by RootActivity's back callback).
    BackHandler(enabled = stack.isNotEmpty()) {
        // A legacy modal (IvyModal) opened on top of a child page must close first.
        val lastModal = nav.modalBackHandling.lastOrNull()
        if (lastModal != null) {
            lastModal.onBackPressed()
        } else {
            pop()
        }
    }

    val top: SettingsChildPage? = stack.lastOrNull()

    AnimatedContent(
        targetState = top,
        transitionSpec = {
            if (pushing) {
                slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it / 4 } + fadeOut()
            } else {
                slideInHorizontally { -it / 4 } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "settings_child_stack",
        contentKey = { page -> page?.ordinal ?: -1 },
    ) { page ->
        when (page) {
            null -> SettingsRootContent(
                onOpenPage = { push(it) },
                listState = rootListState
            )

            SettingsChildPage.ExchangeRates -> ExchangeRatesScreen(
                onClose = { pop() }
            )

            SettingsChildPage.Features -> FeaturesScreenImpl(
                onClose = { pop() }
            )

            SettingsChildPage.Attributions -> AttributionsScreenImpl(
                onClose = { pop() }
            )

            SettingsChildPage.Releases -> ReleasesScreenImpl(
                onClose = { pop() }
            )

            SettingsChildPage.Contributors -> ContributorsScreenImpl(
                onClose = { pop() }
            )

            SettingsChildPage.Import -> ImportCSVScreen(
                screen = ImportScreen(
                    launchedFromOnboarding = false,
                    inlineHosted = true
                ),
                onClose = { pop() },
                onOpenCsv = { push(SettingsChildPage.Csv) }
            )

            SettingsChildPage.Csv -> CsvScreenUi(
                screen = CSVScreen(launchedFromOnboarding = false),
                onClose = { pop() }
            )
        }
    }
}
