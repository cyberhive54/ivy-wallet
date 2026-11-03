package com.ivy.tags

import com.ivy.data.model.Tag
import com.ivy.data.model.TagId

sealed interface TagsScreenEvent {
    data class OnSearchQueryUpdate(val queryString: String) : TagsScreenEvent
    data class OnTagModalVisible(val tagModalData: TagModalData?) : TagsScreenEvent
    data class OnCreateTag(val tag: Tag) : TagsScreenEvent
    data class OnEditTag(val tag: Tag) : TagsScreenEvent
    data class OnDeleteTagConfirmation(val tagData: TagData?) : TagsScreenEvent
    data class OnDeleteTag(val tagId: TagId) : TagsScreenEvent
}
