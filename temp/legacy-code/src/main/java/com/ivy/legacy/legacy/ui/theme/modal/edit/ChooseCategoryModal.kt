package com.ivy.wallet.ui.theme.modal.edit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.legacy.cupertino.CupertinoCategoryPicker
import com.ivy.legacy.utils.hideKeyboard
import com.ivy.legacy.utils.onScreenStart
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalSkip
import com.ivy.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Suppress("ParameterNaming")
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ChooseCategoryModal(
    id: UUID = UUID.randomUUID(),
    visible: Boolean,
    initialCategory: Category?,
    categories: List<Category>,
    // Transaction type of the screen that opened the picker. Used as the
    // initial segment of the type filter (All / Expenses / Income / Transfers).
    type: TransactionType? = null,

    showCategoryModal: (CategoryModalData) -> Unit,
    onCategoryChanged: (Category?) -> Unit,
    dismiss: () -> Unit
) {
    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        // The picker owns its scroll container (LazyColumn)
        scrollState = null,
        PrimaryAction = {
            // "Skip" clears the current category (same behavior as before)
            ModalSkip {
                save(
                    category = null,
                    onCategoryChanged = onCategoryChanged,
                    dismiss = dismiss
                )
            }
        }
    ) {
        val view = LocalView.current
        onScreenStart {
            hideKeyboard(view)
        }

        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = stringResource(R.string.choose_category)
        )

        Spacer(Modifier.height(24.dp))

        CupertinoCategoryPicker(
            categories = categories,
            type = type,
            selectedId = initialCategory?.id,
            onCategoryClick = { category ->
                save(
                    category = category,
                    onCategoryChanged = onCategoryChanged,
                    dismiss = dismiss
                )
            },
            onLongClickCategory = { category ->
                showCategoryModal(CategoryModalData(category))
            },
            onCreateCategory = {
                showCategoryModal(CategoryModalData(null))
            },
            onCreateNamedCategory = { name ->
                showCategoryModal(CategoryModalData(null, initialName = name))
            },
            modifier = Modifier.height(380.dp),
        )

        Spacer(Modifier.height(16.dp))
    }
}

private fun save(
    category: Category?,
    onCategoryChanged: (Category?) -> Unit,
    dismiss: () -> Unit
) {
    onCategoryChanged(category)
    dismiss()
}
