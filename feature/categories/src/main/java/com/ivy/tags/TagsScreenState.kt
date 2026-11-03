package com.ivy.tags

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Tag
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class TagData(
    val tag: Tag,
    val usageCount: Int,
)

@Immutable
data class TagsScreenState(
    val tags: ImmutableList<TagData> = persistentListOf(),
    val searchQuery: String = "",
    val tagModalData: TagModalData? = null,
    val deleteTagConfirmation: TagData? = null,
)
