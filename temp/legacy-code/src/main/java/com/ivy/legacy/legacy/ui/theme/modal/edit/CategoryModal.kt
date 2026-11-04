package com.ivy.wallet.ui.theme.modal.edit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arrow.core.raise.either
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.Category.Companion.ALL_TRANSACTION_TYPES
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.legacy.ui.IvyColorPicker
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.utils.hideKeyboard
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.legacy.utils.onScreenStart
import com.ivy.legacy.utils.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import com.ivy.wallet.ui.theme.Ivy
import com.ivy.wallet.ui.theme.components.ItemIconMDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyNameTextField
import com.ivy.wallet.ui.theme.dynamicContrast
import com.ivy.wallet.ui.theme.modal.ChooseIconModal
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalAddSave
import com.ivy.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

@Deprecated("Old design system. Use `:ivy-design` and Material3")
data class CategoryModalData(
    val category: Category?,
    val id: UUID = UUID.randomUUID(),
    val autoFocusKeyboard: Boolean = true,
    /** Prefilled name for the create flow (e.g. from "Add {search}" in the picker). */
    val initialName: String? = null,
)

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BoxWithConstraintsScope.CategoryModal(
    modal: CategoryModalData?,
    onCreateCategory: (CreateCategoryData) -> Unit,
    onEditCategory: (Category) -> Unit,
    dismiss: () -> Unit,
) {
    val initialCategory = modal?.category
    var nameTextFieldValue by remember(modal) {
        mutableStateOf(selectEndTextFieldValue(initialCategory?.name?.value ?: modal?.initialName))
    }
    var color by remember(modal) {
        mutableStateOf(initialCategory?.color?.let { Color(it.value) } ?: Ivy)
    }
    var icon by remember(modal) {
        mutableStateOf(initialCategory?.icon)
    }
    var types by remember(modal) {
        mutableStateOf(
            initialCategory?.transactionTypes
                ?.takeIf { it.isNotEmpty() }
                ?: ALL_TRANSACTION_TYPES
        )
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
                item = modal?.category,
                enabled = nameTextFieldValue.text.isNotNullOrBlank()
            ) {
                if (initialCategory != null) {
                    val updatedCategory = either {
                        initialCategory.copy(
                            name = NotBlankTrimmedString.from(nameTextFieldValue.text.trim()).bind(),
                            color = ColorInt(color.toArgb()),
                            icon = icon,
                            transactionTypes = types
                        )
                    }.getOrNull()

                    if (updatedCategory != null) {
                        onEditCategory(updatedCategory)
                    }
                } else {
                    onCreateCategory(
                        CreateCategoryData(
                            name = nameTextFieldValue.text.trim(),
                            color = color,
                            icon = icon?.id,
                            transactionTypes = types
                        )
                    )
                }

                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = if (modal?.category != null) {
                stringResource(R.string.edit_category)
            } else {
                stringResource(
                    R.string.create_category
                )
            }
        )

        Spacer(Modifier.height(24.dp))

        IconNameRow(
            hint = stringResource(R.string.category_name),
            defaultIcon = R.drawable.ic_custom_category_m,
            color = color,
            icon = icon?.id,

            autoFocusKeyboard = modal?.autoFocusKeyboard ?: true,

            nameTextFieldValue = nameTextFieldValue,
            setNameTextFieldValue = { nameTextFieldValue = it },
            showChooseIconModal = {
                chooseIconModalVisible = true
            }
        )

        Spacer(Modifier.height(32.dp))

        TransactionTypesSection(
            selected = types,
            onSelected = { types = it }
        )

        Spacer(Modifier.height(40.dp))

        IvyColorPicker(
            selectedColor = color,
            onColorSelected = { color = it }
        )

        Spacer(Modifier.height(48.dp))
    }

    ChooseIconModal(
        visible = chooseIconModalVisible,
        initialIcon = icon?.id ?: "category",
        color = color,
        dismiss = { chooseIconModalVisible = false }
    ) {
        icon = it?.let { iconId -> IconAsset.unsafe(iconId) }
    }
}

@Composable
fun IconNameRow(
    hint: String,
    @DrawableRes defaultIcon: Int,
    color: Color,
    icon: String?,

    autoFocusKeyboard: Boolean,

    nameTextFieldValue: TextFieldValue,
    setNameTextFieldValue: (TextFieldValue) -> Unit,

    showChooseIconModal: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val nameFocus = FocusRequester()

        onScreenStart {
            if (autoFocusKeyboard) {
                nameFocus.requestFocus()
            }
        }

        Spacer(Modifier.width(24.dp))

        ItemIconMDefaultIcon(
            modifier = Modifier
                .clip(CircleShape)
                .background(color, CircleShape)
                .clickable {
                    showChooseIconModal()
                }
                .testTag("modal_item_icon"),
            iconName = icon,
            tint = color.dynamicContrast(),
            defaultIcon = defaultIcon
        )

        val view = LocalView.current
        IvyNameTextField(
            modifier = Modifier
                .padding(start = 28.dp, end = 36.dp)
                .focusRequester(nameFocus),
            underlineModifier = Modifier.padding(start = 24.dp, end = 32.dp),
            value = nameTextFieldValue,
            hint = hint,
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
            setNameTextFieldValue(newValue)
        }
    }
}

@Composable
private fun TransactionTypesSection(
    selected: Set<TransactionType>,
    onSelected: (Set<TransactionType>) -> Unit,
) {
    val options = listOf(
        TransactionType.INCOME to stringResource(R.string.income),
        TransactionType.EXPENSE to stringResource(R.string.expense),
        TransactionType.TRANSFER to stringResource(R.string.transfer),
    )

    Column {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.transaction_types).uppercase(),
            style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (option, label) ->
                val isSelected = option in selected
                val background = if (isSelected) UI.colors.primary else UI.colors.medium
                val textColor = if (isSelected) {
                    findContrastTextColor(UI.colors.primary)
                } else {
                    UI.colors.gray
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(background, CircleShape)
                        .clickable {
                            // Keep at least one type selected - a category with
                            // no types would be hidden from every picker.
                            val next = if (isSelected) {
                                selected - option
                            } else {
                                selected + option
                            }
                            if (next.isNotEmpty()) {
                                onSelected(next)
                            }
                        }
                        .testTag("category_type_${option.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = ivyInter(14.sp, FontWeight.SemiBold),
                        color = textColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCategoryModal() {
    IvyWalletPreview {
        CategoryModal(
            modal = CategoryModalData(null),
            onCreateCategory = { },
            onEditCategory = { }
        ) {
        }
    }
}
