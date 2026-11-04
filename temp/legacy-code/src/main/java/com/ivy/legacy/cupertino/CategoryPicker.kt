package com.ivy.legacy.cupertino

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.design.l0_system.UI
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.toComposeColor

/**
 * iOS-style category picker shared by the Choose category modal and the
 * Categories page.
 *
 * Layout (top to bottom):
 * 1. Full-width segmented type filter: All / Expenses / Income / Transfers
 *    (clears the "which category is for which type" cluster)
 * 2. Search row with the add (+) button side by side
 * 3. Category rows, or when searching: "No such category" and an "Add {name}" row
 *
 * [type] selects the initial segment. Categories without any matching type
 * (legacy/all-typed) are always shown.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CupertinoCategoryPicker(
    categories: List<Category>,
    type: TransactionType?,
    selectedId: CategoryId? = null,
    onCategoryClick: (Category) -> Unit,
    onLongClickCategory: ((Category) -> Unit)? = null,
    onCreateCategory: () -> Unit,
    onCreateNamedCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filterType by remember(type) { mutableStateOf(type) }
    var query by remember { mutableStateOf("") }

    val visible = remember(categories, filterType, query) {
        categories.filter { category ->
            val matchesType = filterType == null || filterType in category.transactionTypes
            val matchesQuery = query.isBlank() ||
                category.name.value.contains(query.trim(), ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    Column(modifier = modifier) {
        CupertinoCategoryTypeFilterBar(
            selected = filterType,
            onSelected = { filterType = it }
        )

        Spacer(Modifier.height(16.dp))

        CupertinoCategorySearchAddRow(
            query = query,
            onQueryChange = { query = it },
            onAddClick = onCreateCategory,
        )

        Spacer(Modifier.height(16.dp))

        when {
            query.isNotBlank() && visible.isEmpty() -> {
                CupertinoNoCategoryFoundState(query = query)

                Spacer(Modifier.height(8.dp))

                CupertinoCategoryAddRow(
                    text = stringResource(R.string.add_named_category, query.trim()),
                    onClick = { onCreateNamedCategory(query.trim()) },
                    testTag = "add_named_category_row",
                )
            }

            visible.isEmpty() -> {
                CupertinoEmptyCategoriesState()
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visible, key = { it.id.value.toString() }) { category ->
                        CategoryRow(
                            category = category,
                            selected = selectedId == category.id,
                            onClick = { onCategoryClick(category) },
                            onLongClick = onLongClickCategory?.let { callback ->
                                { callback(category) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CupertinoCategoryTypeFilterBar(
    selected: TransactionType?,
    onSelected: (TransactionType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        null to stringResource(R.string.all),
        TransactionType.EXPENSE to stringResource(R.string.expenses),
        TransactionType.INCOME to stringResource(R.string.income),
        TransactionType.TRANSFER to stringResource(R.string.transfers),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (option, label) ->
            val isSelected = selected == option
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
                    .clickable { onSelected(if (isSelected) null else option) }
                    .testTag(
                        if (option == null) "category_filter_all"
                        else "category_filter_${option.name.lowercase()}"
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.2.sp),
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun CupertinoCategorySearchAddRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(CircleShape)
                .background(UI.colors.medium, CircleShape)
                .testTag("category_search_field"),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 44.dp),
                decorationBox = { inner ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.padding(horizontal = 44.dp, vertical = 14.dp)
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_category),
                                style = ivyInter(16.sp),
                                color = UI.colors.gray
                            )
                        }
                        inner()
                    }
                }
            )

            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = UI.colors.gray,
                modifier = Modifier
                    .padding(start = 14.dp)
                    .size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(UI.colors.pureInverse, CircleShape)
                .clickable(onClick = onAddClick)
                .testTag("add_category_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = stringResource(R.string.add_category),
                tint = UI.colors.pure,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {
    val categoryColor = category.color.value.toComposeColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("choose_category_button"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(categoryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ItemIconSDefaultIcon(
                iconName = category.icon?.id,
                defaultIcon = R.drawable.ic_custom_category_s,
                tint = findContrastTextColor(categoryColor)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = category.name.value,
            style = ivyInter(16.sp, FontWeight.Medium, letterSpacing = (-0.2).sp),
            color = UI.colors.pureInverse,
            maxLines = 1
        )

        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = UI.colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    CategoryHairline()
}

@Composable
fun CupertinoCategoryAddRow(
    text: String,
    onClick: () -> Unit,
    testTag: String = "add_category_row",
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(UI.colors.medium, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = null,
                tint = UI.colors.pureInverse,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = text,
            style = ivyInter(16.sp, FontWeight.Medium, letterSpacing = (-0.2).sp),
            color = UI.colors.pureInverse
        )
    }
}

@Composable
fun CupertinoNoCategoryFoundState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_such_category),
            style = ivyInter(17.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "“${query.trim()}”",
            style = ivyInter(15.sp),
            color = UI.colors.gray
        )
    }
}

@Composable
fun CupertinoEmptyCategoriesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_categories_yet),
            style = ivyInter(15.sp),
            color = UI.colors.gray
        )
    }
}

@Composable
private fun CategoryHairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(UI.colors.pureInverse.copy(alpha = 0.06f))
    )
}
