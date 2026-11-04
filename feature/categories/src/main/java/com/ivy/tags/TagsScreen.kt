package com.ivy.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.legacy.utils.ContextualDock
import com.ivy.ui.typography.ivyInter
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.ui.SearchInput
import com.ivy.legacy.utils.selectEndTextFieldValue
import com.ivy.navigation.IvyPreview
import com.ivy.navigation.TagsScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Ivy
import com.ivy.wallet.ui.theme.components.CircleButtonFilled
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.modal.DeleteModal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant

@Composable
fun BoxWithConstraintsScope.TagsScreen(screen: TagsScreen) {
    val viewModel: TagsViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

    // The floating dock "+" (contextual add) collects these requests when this page is shown
    LaunchedEffect(Unit) {
        ContextualDock.addRequests.collect {
            viewModel.onEvent(TagsScreenEvent.OnTagModalVisible(TagModalData(tag = null)))
        }
    }

    TagsUi(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.TagsUi(
    state: TagsScreenState,
    onEvent: (TagsScreenEvent) -> Unit,
) {
    val nav = navigation()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        state = rememberLazyListState()
    ) {
        item {
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleButtonFilled(
                    icon = R.drawable.ic_back,
                    onClick = {
                        nav.back()
                    },
                    clickAreaPadding = 10.dp
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.tags),
                    style = ivyInter(28.sp, FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
                    color = UI.colors.pureInverse
                )
            }

            Spacer(Modifier.height(16.dp))

            TagSearchField(
                onSearch = {
                    onEvent(TagsScreenEvent.OnSearchQueryUpdate(it))
                }
            )

            Spacer(Modifier.height(8.dp))
        }

        if (state.tags.isEmpty()) {
            item {
                TagEmptyState(
                    hasSearchQuery = state.searchQuery.isNotBlank()
                )
            }
        }

        items(state.tags, key = { it.tag.id.value }) { tagData ->
            TagRow(
                tagData = tagData,
                onEdit = {
                    onEvent(TagsScreenEvent.OnTagModalVisible(TagModalData(tag = tagData.tag)))
                },
                onRequestDelete = {
                    onEvent(TagsScreenEvent.OnDeleteTagConfirmation(tagData))
                }
            )
        }

        item {
            Spacer(Modifier.height(130.dp)) // dock clearance scroll hack
        }
    }

    TagModal(
        modal = state.tagModalData,
        onCreateTag = {
            onEvent(TagsScreenEvent.OnCreateTag(it))
        },
        onEditTag = {
            onEvent(TagsScreenEvent.OnEditTag(it))
        },
        dismiss = {
            onEvent(TagsScreenEvent.OnTagModalVisible(null))
        }
    )

    DeleteModal(
        title = stringResource(R.string.delete),
        description = stringResource(R.string.tag_confirm_deletion_description),
        visible = state.deleteTagConfirmation != null,
        dismiss = {
            onEvent(TagsScreenEvent.OnDeleteTagConfirmation(null))
        },
        onDelete = {
            state.deleteTagConfirmation?.let { tagData ->
                onEvent(TagsScreenEvent.OnDeleteTag(tagData.tag.id))
            }
            onEvent(TagsScreenEvent.OnDeleteTagConfirmation(null))
        }
    )
}

@Composable
private fun TagRow(
    tagData: TagData,
    onEdit: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    val tagColor = tagData.tag.color
        .takeIf { it.value != 0 }
        ?.let { Color(it.value) } ?: Ivy

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(UI.shapes.r4)
            .background(UI.colors.pure)
            .clickable {
                onEdit()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(tagColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val icon = tagData.tag.icon
            if (icon != null) {
                ItemIconSDefaultIcon(
                    iconName = icon.id,
                    tint = tagColor.dynamicContrastCompat(),
                    defaultIcon = R.drawable.ic_custom_category_m
                )
            } else {
                Text(
                    text = tagData.tag.name.value.trim().take(1)
                        .uppercase(),
                    style = UI.typo.b2.style(
                        color = findContrastTextColor(tagColor),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = tagData.tag.name.value,
                style = UI.typo.b1.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (tagData.usageCount > 0) {
                    stringResource(R.string.tag_used_times, tagData.usageCount)
                } else {
                    stringResource(R.string.tag_unused)
                },
                style = UI.typo.c.style(
                    color = UI.colors.gray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        IvyIcon(
            modifier = Modifier
                .clickable {
                    onRequestDelete()
                }
                .padding(6.dp),
            icon = R.drawable.ic_delete,
            tint = UI.colors.gray
        )
    }
}

@Composable
private fun TagEmptyState(
    hasSearchQuery: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (hasSearchQuery) {
                stringResource(R.string.no_search_results)
            } else {
                stringResource(R.string.create_tag)
            },
            style = UI.typo.b2.style(
                color = UI.colors.gray,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun TagSearchField(
    onSearch: (String) -> Unit,
) {
    var searchQueryTextFieldValue by remember {
        mutableStateOf(selectEndTextFieldValue(""))
    }

    SearchInput(
        searchQueryTextFieldValue = searchQueryTextFieldValue,
        hint = stringResource(R.string.tags),
        focus = false,
        showClearIcon = searchQueryTextFieldValue.text.isNotEmpty(),
        onSetSearchQueryTextField = {
            searchQueryTextFieldValue = it
            onSearch(it.text)
        }
    )
}

private fun Color.dynamicContrastCompat(): Color {
    // Local dynamic-contrast helper: a readable tint over the tag color.
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return if (luminance > 0.6) {
        Color.Black.copy(alpha = 0.75f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }
}

@Preview
@Composable
private fun BoxWithConstraintsScope.PreviewTagsScreen() {
    IvyPreview {
        TagsUi(
            state = TagsScreenState(
                tags = persistentTagPreviewData()
            ),
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun BoxWithConstraintsScope.PreviewTagsScreenDark() {
    IvyPreview(dark = true) {
        TagsUi(
            state = TagsScreenState(
                tags = persistentTagPreviewData()
            ),
            onEvent = {}
        )
    }
}

private fun persistentTagPreviewData(): ImmutableList<TagData> = persistentListOf(
    TagData(
        tag = Tag(
            id = TagId(java.util.UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Subscription"),
            description = null,
            color = ColorInt(Color.Red.toArgb()),
            icon = null,
            orderNum = 0.0,
            creationTimestamp = Instant.now()
        ),
        usageCount = 12
    ),
    TagData(
        tag = Tag(
            id = TagId(java.util.UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Business"),
            description = null,
            color = ColorInt(Color.Transparent.toArgb()),
            icon = null,
            orderNum = 1.0,
            creationTimestamp = Instant.now()
        ),
        usageCount = 0
    )
)
