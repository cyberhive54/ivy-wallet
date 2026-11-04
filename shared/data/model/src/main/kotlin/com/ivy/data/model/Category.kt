package com.ivy.data.model

import com.ivy.base.model.TransactionType
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.sync.Identifiable
import com.ivy.data.model.sync.UniqueId
import java.util.UUID

@JvmInline
value class CategoryId(override val value: UUID) : UniqueId

data class Category(
    override val id: CategoryId,
    val name: NotBlankTrimmedString,
    val color: ColorInt,
    val icon: IconAsset?,
    override val orderNum: Double,
    /**
     * Transaction types this category can be used with.
     * A category can belong to any combination of Income/Expense/Transfer.
     * Defaults to all types (legacy behavior).
     */
    val transactionTypes: Set<TransactionType> = ALL_TRANSACTION_TYPES,
) : Identifiable<CategoryId>, Reorderable {
    companion object {
        val ALL_TRANSACTION_TYPES = setOf(
            TransactionType.INCOME,
            TransactionType.EXPENSE,
            TransactionType.TRANSFER,
        )
    }
}