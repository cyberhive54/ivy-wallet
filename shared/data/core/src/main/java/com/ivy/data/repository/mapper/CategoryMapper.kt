package com.ivy.data.repository.mapper

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.ivy.base.model.TransactionType
import com.ivy.data.db.entity.CategoryEntity
import com.ivy.data.model.Category
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import javax.inject.Inject

class CategoryMapper @Inject constructor() {
    fun CategoryEntity.toDomain(): Either<String, Category> = either {
        ensure(!isDeleted) { "Category is deleted" }

        Category(
            id = com.ivy.data.model.CategoryId(id),
            name = NotBlankTrimmedString.from(name).bind(),
            color = ColorInt(color),
            icon = icon?.let(IconAsset::from)?.getOrNull(),
            orderNum = orderNum,
            transactionTypes = parseTransactionTypes(transactionTypes)
        )
    }

    fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            name = name.value,
            color = color.value,
            icon = icon?.id,
            orderNum = orderNum,
            transactionTypes = formatTransactionTypes(transactionTypes),
            isSynced = true,
            id = id.value
        )
    }

    private fun parseTransactionTypes(raw: String?): Set<TransactionType> {
        if (raw.isNullOrBlank()) {
            // Legacy rows: usable with every type
            return Category.ALL_TRANSACTION_TYPES
        }
        val parsed = raw.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { name ->
                TransactionType.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
            }
            .toSet()

        // Defensive: an unreadable/empty value means "all types" rather than "no types",
        // which would hide the category everywhere.
        return parsed.ifEmpty { Category.ALL_TRANSACTION_TYPES }
    }

    private fun formatTransactionTypes(types: Set<TransactionType>): String? {
        // Store null for the default (all types) to keep legacy rows compact
        if (types.containsAll(Category.ALL_TRANSACTION_TYPES)) return null
        if (types.isEmpty()) return null
        return types.joinToString(",") { it.name }
    }
}
