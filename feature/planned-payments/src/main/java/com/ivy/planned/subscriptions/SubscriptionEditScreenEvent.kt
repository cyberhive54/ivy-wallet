package com.ivy.planned.subscriptions

import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.IntervalType
import com.ivy.legacy.datamodel.Account
import com.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import com.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import com.ivy.wallet.ui.theme.modal.RecurringRuleModalData
import com.ivy.wallet.ui.theme.modal.edit.AccountModalData
import com.ivy.wallet.ui.theme.modal.edit.CategoryModalData
import java.time.LocalDateTime

sealed interface SubscriptionEditScreenEvent {
    data class OnRuleChanged(
        val startDate: LocalDateTime,
        val oneTime: Boolean,
        val intervalN: Int?,
        val intervalType: IntervalType?
    ) : SubscriptionEditScreenEvent

    data class OnAmountChanged(val newAmount: Double) : SubscriptionEditScreenEvent
    data class OnTitleChanged(val newTitle: String?) : SubscriptionEditScreenEvent
    data class OnDescriptionChanged(val newDescription: String?) : SubscriptionEditScreenEvent
    data class OnCategoryChanged(val newCategory: Category?) : SubscriptionEditScreenEvent
    data class OnAccountChanged(val newAccount: Account) : SubscriptionEditScreenEvent

    // --- Subscription-specific ---
    data class OnIconChanged(val newIconKey: String?) : SubscriptionEditScreenEvent
    data class OnPaymentMethodNoteChanged(val newNote: String?) : SubscriptionEditScreenEvent
    data class OnReminderLeadDaysChanged(val days: Int) : SubscriptionEditScreenEvent
    data class OnTrialChanged(val isTrial: Boolean) : SubscriptionEditScreenEvent
    data class OnTrialEndsChanged(val newTrialEndsAt: LocalDateTime?) : SubscriptionEditScreenEvent
    data class OnRemainingOccurrencesChanged(val newRemaining: Int?) : SubscriptionEditScreenEvent
    data class OnPausedChanged(val paused: Boolean) : SubscriptionEditScreenEvent
    data class OnArchivedChanged(val archived: Boolean) : SubscriptionEditScreenEvent

    data class OnSave(val closeScreen: Boolean = true) : SubscriptionEditScreenEvent
    data object OnDelete : SubscriptionEditScreenEvent
    data class OnEditCategory(val updatedCategory: Category) : SubscriptionEditScreenEvent
    data class OnCreateCategory(val data: CreateCategoryData) : SubscriptionEditScreenEvent
    data class OnCreateAccount(val data: CreateAccountData) : SubscriptionEditScreenEvent
    data class OnCategoryModalVisible(val visible: Boolean) : SubscriptionEditScreenEvent
    data class OnDescriptionModalVisible(val visible: Boolean) : SubscriptionEditScreenEvent
    data class OnDeleteTransactionModalVisible(val visible: Boolean) : SubscriptionEditScreenEvent
    data class OnAmountModalVisible(val visible: Boolean) : SubscriptionEditScreenEvent
    data class OnCategoryModalDataChanged(val categoryModalData: CategoryModalData?) :
        SubscriptionEditScreenEvent

    data class OnRecurringRuleModalDataChanged(val recurringRuleModalData: RecurringRuleModalData?) :
        SubscriptionEditScreenEvent

    data class OnAccountModalDataChanged(val accountModalData: AccountModalData?) :
        SubscriptionEditScreenEvent
}
