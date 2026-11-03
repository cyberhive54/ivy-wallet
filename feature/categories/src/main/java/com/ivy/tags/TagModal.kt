package com.ivy.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.legacy.ui.IvyColorPicker
import com.ivy.legacy.utils.hideKeyboard
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.legacy.utils.onScreenStart
import com.ivy.legacy.utils.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Ivy
import com.ivy.wallet.ui.theme.components.ItemIconMDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyNameTextField
import com.ivy.wallet.ui.theme.dynamicContrast
import com.ivy.wallet.ui.theme.modal.ChooseIconModal
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalAddSave
import com.ivy.wallet.ui.theme.modal.ModalTitle
import java.time.Instant
import java.util.UUID

@Deprecated("Old design system. Use `:ivy-design` and Material3")
data class TagModalData(
    val tag: Tag?,
    val id: UUID = UUID.randomUUID(),
    val autoFocusKeyboard: Boolean = true,
)

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BoxWithConstraintsScope.TagModal(
    modal: TagModalData?,
    onCreateTag: (Tag) -> Unit,
    onEditTag: (Tag) -> Unit,
    dismiss: () -> Unit,
) {
    val initialTag = modal?.tag
    var nameTextFieldValue by remember(modal) {
        mutableStateOf(selectEndTextFieldValue(initialTag?.name?.value))
    }
    var color by remember(modal) {
        mutableStateOf(
            initialTag?.color?.takeIf { it.value != 0 }
                ?.let { Color(it.value) } ?: Ivy
        )
    }
    var icon by remember(modal) {
        mutableStateOf(initialTag?.icon)
    }

    var chooseIconModalVisible by remember(modal) {
        mutableStateOf(false)
    }

    IvyModal(
        id = modal?.id,
        visible = modal != null,
        dismiss = dismiss,
        PrimaryAction = {
            ModalAddSave(
                item = modal?.tag,
                enabled = nameTextFieldValue.text.isNotNullOrBlank()
            ) {
                val name = NotBlankTrimmedString.from(nameTextFieldValue.text.trim())
                    .getOrNull()
                if (name != null) {
                    if (initialTag != null) {
                        val updatedTag = initialTag.copy(
                            name = name,
                            color = ColorInt(color.toArgb()),
                            icon = icon
                        )

                        onEditTag(updatedTag)
                    } else {
                        onCreateTag(
                            Tag(
                                id = TagId(UUID.randomUUID()),
                                name = name,
                                description = null,
                                color = ColorInt(color.toArgb()),
                                icon = icon,
                                orderNum = 0.0,
                                creationTimestamp = Instant.now()
                            )
                        )
                    }

                    dismiss()
                }
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = if (initialTag != null) {
                stringResource(R.string.edit_tag)
            } else {
                stringResource(R.string.create_tag)
            }
        )

        Spacer(Modifier.height(24.dp))

        val nameFocus = FocusRequester()

        onScreenStart {
            if (modal?.autoFocusKeyboard == true) {
                nameFocus.requestFocus()
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(24.dp))

            ItemIconMDefaultIcon(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color, CircleShape)
                    .clickable {
                        chooseIconModalVisible = true
                    },
                iconName = icon?.id,
                tint = color.dynamicContrast(),
                defaultIcon = R.drawable.ic_custom_category_m
            )

            val view = LocalView.current
            IvyNameTextField(
                modifier = Modifier
                    .padding(start = 28.dp, end = 36.dp)
                    .focusRequester(nameFocus),
                underlineModifier = Modifier.padding(start = 24.dp, end = 32.dp),
                value = nameTextFieldValue,
                hint = stringResource(R.string.tag_name),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text,
                    autoCorrect = true
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        hideKeyboard(view)
                    }
                ),
            ) { newValue ->
                nameTextFieldValue = newValue
            }
        }

        Spacer(Modifier.height(40.dp))

        IvyColorPicker(
            selectedColor = color,
            onColorSelected = { color = it }
        )

        Spacer(Modifier.height(48.dp))
    }

    ChooseIconModal(
        visible = chooseIconModalVisible,
        initialIcon = icon?.id ?: "tag",
        color = color,
        dismiss = { chooseIconModalVisible = false }
    ) {
        icon = it?.let { iconId -> IconAsset.unsafe(iconId) }
    }
}
