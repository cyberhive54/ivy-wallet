package com.ivy.wallet.domain.deprecated.logic.model

import androidx.compose.ui.graphics.Color
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category.Companion.ALL_TRANSACTION_TYPES

data class CreateCategoryData(
    val name: String,
    val color: Color,
    val icon: String?,
    /** Transaction types this category can be used with. Defaults to all types. */
    val transactionTypes: Set<TransactionType> = ALL_TRANSACTION_TYPES,
)
