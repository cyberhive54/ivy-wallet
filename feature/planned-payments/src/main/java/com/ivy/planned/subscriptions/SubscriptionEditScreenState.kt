package com.ivy.planned.subscriptions

import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.IntervalType
import com.ivy.legacy.datamodel.Account
import com.ivy.wallet.ui.theme.modal.RecurringRuleModalData
import com.ivy.wallet.ui.theme.modal.edit.AccountModalData
import com.ivy.wallet.ui.theme.modal.edit.CategoryModalData
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDateTime
import javax.annotation.concurrent.Immutable

@Immutable
data class SubscriptionEditScreenState(
    val currency: String,
    val transactionType: TransactionType,
    val startDate: LocalDateTime?,
    val intervalN: Int?,
    val intervalType: IntervalType?,
    val oneTime: Boolean,
    val initialTitle: String?,
    val description: String?,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<Account>,
    val account: Account?,
    val category: Category?,
    val amount: Double,
    val categoryModalVisible: Boolean,
    val descriptionModalVisible: Boolean,
    val deleteTransactionModalVisible: Boolean,
    val categoryModalData: CategoryModalData?,
    val accountModalData: AccountModalData?,
    val recurringRuleModalData: RecurringRuleModalData?,
    val amountModalVisible: Boolean,

    // --- Subscription-specific ---
    val iconKey: String?,
    val paymentMethodNote: String?,
    val reminderLeadDays: Int,
    val isTrial: Boolean,
    val trialEndsAt: LocalDateTime?,
    /** null = never ends; otherwise cycles remaining. */
    val remainingOccurrences: Int?,
    val paused: Boolean,
    val archived: Boolean,
)
