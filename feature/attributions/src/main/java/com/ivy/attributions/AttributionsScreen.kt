package com.ivy.attributions

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.ivy.base.legacy.Theme
import com.ivy.legacy.cupertino.CupertinoChildPage
import com.ivy.legacy.cupertino.CupertinoDockClearance
import com.ivy.legacy.cupertino.CupertinoNavRow
import com.ivy.legacy.cupertino.CupertinoRowsCard
import com.ivy.legacy.cupertino.CupertinoSectionHeader
import com.ivy.navigation.screenScopedViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BoxWithConstraintsScope.AttributionsScreenImpl(
    onClose: (() -> Unit)? = null,
    viewModel: AttributionsViewModel = screenScopedViewModel(),
) {
    val uiState = viewModel.uiState()

    AttributionsUI(
        uiState = uiState,
        onClose = onClose ?: {}
    )
}

@Composable
private fun BoxWithConstraintsScope.AttributionsUI(
    uiState: AttributionsState,
    onClose: () -> Unit,
) {
    val browser = LocalUriHandler.current

    CupertinoChildPage(
        title = "Attributions",
        onClose = onClose,
    ) {
        val sections = remember(uiState.attributionItems) {
            groupAttributionItems(uiState.attributionItems)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            sections.forEach { section ->
                item(key = section.name) {
                    CupertinoSectionHeader(text = section.name)
                }

                item(key = "${section.name}_card") {
                    CupertinoRowsCard(
                        separatorStartIndent = 54.dp,
                        rows = section.attributions.map { attribution ->
                            {
                                CupertinoNavRow(
                                    icon = null,
                                    title = attribution.name,
                                    external = true,
                                    onClick = { browser.openUri(attribution.link) }
                                )
                            }
                        }
                    )
                }
            }

            item(key = "dock_clearance") {
                Spacer(Modifier.height(CupertinoDockClearance))
            }
        }
    }
}

private data class AttributionSection(
    val name: String,
    val attributions: List<AttributionItem.Attribution>,
)

/** For screenshot testing */
@Composable
fun BoxWithConstraintsScope.AttributionScreenUiTest(isDark: Boolean) {
    val items = remember(isDark) {
        kotlinx.collections.immutable.persistentListOf<AttributionItem>(
            AttributionItem.Divider(sectionName = "Icons"),
            AttributionItem.Attribution(name = "iconsax", link = "https://iconsax.io"),
            AttributionItem.Attribution(
                name = "Material Symbols Google",
                link = "https://fonts.google.com/icons"
            ),
            AttributionItem.Divider(sectionName = "Fonts"),
            AttributionItem.Attribution(
                name = "Inter",
                link = "https://fonts.google.com/specimen/Inter"
            )
        )
    }

    com.ivy.legacy.IvyWalletPreview(theme = if (isDark) Theme.DARK else Theme.LIGHT) {
        AttributionsUI(
            uiState = AttributionsState(attributionItems = items),
            onClose = {}
        )
    }
}

private fun groupAttributionItems(
    items: ImmutableList<AttributionItem>,
): List<AttributionSection> {
    val sections = mutableListOf<AttributionSection>()
    var currentName = ""
    var currentAttributions = mutableListOf<AttributionItem.Attribution>()

    fun flush() {
        if (currentAttributions.isNotEmpty()) {
            sections.add(
                AttributionSection(
                    name = currentName,
                    attributions = currentAttributions.toList()
                )
            )
        }
        currentAttributions = mutableListOf()
    }

    items.forEach { item ->
        when (item) {
            is AttributionItem.Attribution -> currentAttributions.add(item)

            is AttributionItem.Divider -> {
                flush()
                currentName = item.sectionName
            }
        }
    }
    flush()

    return sections
}
