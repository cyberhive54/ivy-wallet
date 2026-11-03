package com.ivy.features

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ivy.legacy.cupertino.CupertinoChildPage
import com.ivy.legacy.cupertino.CupertinoDockClearance
import com.ivy.legacy.cupertino.CupertinoRowsCard
import com.ivy.legacy.cupertino.CupertinoSwitchRow
import com.ivy.navigation.screenScopedViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BoxWithConstraintsScope.FeaturesScreenImpl(
    onClose: (() -> Unit)? = null,
    viewModel: FeaturesViewModel = screenScopedViewModel(),
) {
    FeaturesUi(
        uiState = viewModel.uiState(),
        onEvent = viewModel::onEvent,
        onClose = onClose ?: {}
    )
}

@Composable
private fun BoxWithConstraintsScope.FeaturesUi(
    uiState: FeaturesUiState,
    onEvent: (FeaturesUiEvent) -> Unit,
    onClose: () -> Unit,
) {
    CupertinoChildPage(
        title = "Features",
        onClose = onClose,
    ) {
        val groups = remember(uiState.featureItemViewStates) {
            groupFeatureItems(uiState.featureItemViewStates)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            groups.forEach { group ->
                item(key = group.header) {
                    CupertinoRowsCard(
                        header = group.header,
                        separatorStartIndent = 16.dp,
                        rows = group.features.map { feature ->
                            {
                                CupertinoSwitchRow(
                                    icon = null,
                                    title = feature.name,
                                    description = feature.description,
                                    checked = feature.enabled,
                                    onToggle = { onEvent(FeaturesUiEvent.ToggleFeature(feature.key)) }
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

private data class FeatureGroup(
    val header: String?,
    val features: List<FeatureItemViewState.FeatureToggleViewState>,
)

private fun groupFeatureItems(
    items: ImmutableList<FeatureItemViewState>,
): List<FeatureGroup> {
    val groups = mutableListOf<FeatureGroup>()
    var currentHeader: String? = null
    var currentFeatures = mutableListOf<FeatureItemViewState.FeatureToggleViewState>()

    fun flush() {
        if (currentFeatures.isNotEmpty()) {
            groups.add(FeatureGroup(header = currentHeader, features = currentFeatures.toList()))
        }
        currentFeatures = mutableListOf()
    }

    items.forEach { item ->
        when (item) {
            is FeatureItemViewState.FeatureHeaderViewState -> {
                flush()
                currentHeader = item.name
            }

            is FeatureItemViewState.FeatureToggleViewState -> {
                currentFeatures.add(item)
            }
        }
    }
    flush()

    return groups
}
