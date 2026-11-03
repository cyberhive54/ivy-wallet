package com.ivy.tags

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.Tag
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.repository.TagRepository
import com.ivy.legacy.utils.ioThread
import com.ivy.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagRepository: TagRepository,
) : ComposeViewModel<TagsScreenState, TagsScreenEvent>() {

    private val tags = mutableStateOf<ImmutableList<TagData>>(persistentListOf())
    private val searchQuery = mutableStateOf("")
    private val tagModalData = mutableStateOf<TagModalData?>(null)
    private val deleteTagConfirmation = mutableStateOf<TagData?>(null)

    @Composable
    override fun uiState(): TagsScreenState {
        LaunchedEffect(Unit) {
            start()
        }

        return TagsScreenState(
            tags = getFilteredTags(),
            searchQuery = searchQuery.value,
            tagModalData = tagModalData.value,
            deleteTagConfirmation = deleteTagConfirmation.value
        )
    }

    @Composable
    private fun getFilteredTags(): ImmutableList<TagData> {
        val allTags = tags.value
        val query = searchQuery.value
        return remember(allTags, query) {
            allTags.filter {
                query.lowercase().trim() in it.tag.name.value.lowercase()
            }.toImmutableList()
        }
    }

    private fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            loadTags()
        }
    }

    private suspend fun loadTags() {
        ioThread {
            val associationMap = tagRepository.findByAllTagsForAssociations()
            val usagePerTag = associationMap.values
                .flatten()
                .groupingBy { it.id }
                .eachCount()

            val loaded = tagRepository.findAll()
                .map { TagData(tag = it, usageCount = usagePerTag[it.id] ?: 0) }
                .sortedWith(
                    compareBy<TagData> { it.tag.orderNum }
                        .thenBy { it.tag.name.value.lowercase() }
                )

            this.tags.value = loaded.toImmutableList()
        }
    }

    override fun onEvent(event: TagsScreenEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is TagsScreenEvent.OnSearchQueryUpdate -> {
                    searchQuery.value = event.queryString
                }

                is TagsScreenEvent.OnTagModalVisible -> {
                    tagModalData.value = event.tagModalData
                }

                is TagsScreenEvent.OnCreateTag -> {
                    ioThread {
                        val nextOrder = (tags.value.maxOfOrNull { it.tag.orderNum } ?: 0.0) + 1.0
                        tagRepository.save(event.tag.copy(orderNum = nextOrder))
                        loadTags()
                    }
                }

                is TagsScreenEvent.OnEditTag -> {
                    ioThread {
                        tagRepository.save(event.tag)
                        loadTags()
                    }
                }

                is TagsScreenEvent.OnDeleteTagConfirmation -> {
                    deleteTagConfirmation.value = event.tagData
                }

                is TagsScreenEvent.OnDeleteTag -> {
                    ioThread {
                        tagRepository.deleteById(event.tagId)
                        loadTags()
                    }
                }
            }
        }
    }
}
