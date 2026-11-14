package com.ivy.planned.subscriptions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.IntervalType
import com.ivy.design.l0_system.UI
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.onScreenStart
import com.ivy.navigation.SubscriptionEditScreen
import com.ivy.navigation.screenScopedViewModel
import androidx.compose.ui.graphics.Color
import com.ivy.planned.edit.RecurringRule
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.edit.core.Category
import com.ivy.legacy.ui.component.edit.core.Description
import com.ivy.wallet.ui.edit.core.EditBottomSheet
import com.ivy.wallet.ui.edit.core.Title
import com.ivy.wallet.ui.edit.core.Toolbar
import com.ivy.wallet.ui.theme.modal.ModalSet
import com.ivy.wallet.ui.theme.modal.RecurringRuleModal
import com.ivy.wallet.ui.theme.modal.RecurringRuleModalData
import com.ivy.wallet.ui.theme.modal.edit.AccountModal
import com.ivy.wallet.ui.theme.modal.edit.AccountModalData
import com.ivy.wallet.ui.theme.modal.edit.CategoryModal
import com.ivy.wallet.ui.theme.modal.edit.CategoryModalData
import com.ivy.wallet.ui.theme.modal.edit.ChooseCategoryModal
import com.ivy.wallet.ui.theme.modal.edit.DescriptionModal
import java.time.LocalDateTime
import kotlinx.collections.immutable.persistentListOf

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.SubscriptionEditScreen(screen: SubscriptionEditScreen) {
    val viewModel: SubscriptionEditViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()
    LaunchedEffect(Unit) {
        viewModel.start(screen)
    }

    UI(
        screen = screen,
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

/**
 * Subscription editor — the dedicated create/edit sheet for subscription rules
 * (recurring EXPENSE planned payments). Fork language mirrors EditPlannedScreen;
 * the new sections (icon palette, method note, lead time, trial, end-after-N)
 * ship after ActionButton's bar.
 */
@Suppress("LongMethod")
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: SubscriptionEditScreen,
    state: SubscriptionEditScreenState,
    onEvent: (SubscriptionEditScreenEvent) -> Unit,
) {
    val ivyContext = ivyWalletCtx()

    var titleTextFieldValue by remember(state.initialTitle) {
        mutableStateOf(TextFieldValue(state.initialTitle.orEmpty()))
    }
    val titleFocus = androidx.compose.ui.focus.FocusRequester()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        Toolbar(
            // TRANSFER suppresses the type pill: subscriptions are always EXPENSE,
            // so offering an income/expense switch here would be a dead button.
            type = TransactionType.TRANSFER,
            initialTransactionId = screen.plannedPaymentRuleId,
            onDeleteTrnModal = {
                onEvent(SubscriptionEditScreenEvent.OnDeleteTransactionModalVisible(true))
            },
            onChangeTransactionTypeModal = {},
            showDuplicateButton = false,
            onDuplicate = {}
        )

        Spacer(Modifier.height(32.dp))

        Title(
            type = state.transactionType,
            titleFocus = titleFocus,
            initialTransactionId = screen.plannedPaymentRuleId,
            titleTextFieldValue = titleTextFieldValue,
            setTitleTextFieldValue = {
                titleTextFieldValue = it
            },
            suggestions = emptySet(),
            onTitleChanged = { onEvent(SubscriptionEditScreenEvent.OnTitleChanged(it)) },
            onNext = {
                if (hasRecurringRule(
                        state.startDate,
                        state.intervalN,
                        state.intervalType,
                        state.oneTime
                    )
                ) {
                    onEvent(SubscriptionEditScreenEvent.OnSave())
                } else {
                    onEvent(
                        SubscriptionEditScreenEvent.OnRecurringRuleModalDataChanged(
                            RecurringRuleModalData(
                                initialStartDate = state.startDate,
                                initialIntervalN = state.intervalN,
                                initialIntervalType = state.intervalType,
                                initialOneTime = state.oneTime
                            )
                        )
                    )
                }
            }
        )

        Spacer(Modifier.height(32.dp))

        // Category
        Category(
            category = state.category,
            onChooseCategory = {
                onEvent(SubscriptionEditScreenEvent.OnCategoryModalVisible(true))
            }
        )

        Spacer(Modifier.height(32.dp))

        // Recurring rule (start date + cycle) — subscriptions always recur
        RecurringRule(
            startDate = state.startDate,
            intervalN = state.intervalN,
            intervalType = state.intervalType,
            oneTime = state.oneTime,
            onShowRecurringRuleModal = {
                onEvent(
                    SubscriptionEditScreenEvent.OnRecurringRuleModalDataChanged(
                        RecurringRuleModalData(
                            initialStartDate = state.startDate,
                            initialIntervalN = state.intervalN,
                            initialIntervalType = state.intervalType,
                            initialOneTime = state.oneTime
                        )
                    )
                )
            }
        )

        Spacer(Modifier.height(24.dp))

        SectionLabel(stringResource(R.string.subscriptions_section_subscription))

        // Icon palette
        IconPalette(
            selected = state.iconKey,
            onSelect = { key ->
                onEvent(
                    SubscriptionEditScreenEvent.OnIconChanged(
                        if (state.iconKey == key) null else key
                    )
                )
            }
        )

        Spacer(Modifier.height(14.dp))

        // Card / method note (never a PAN)
        PaymentMethodNoteField(
            value = state.paymentMethodNote ?: "",
            onValueChange = {
                onEvent(SubscriptionEditScreenEvent.OnPaymentMethodNoteChanged(it))
            }
        )

        Spacer(Modifier.height(14.dp))

        // Reminder lead — 0/2/3/7/14 days before renewal
        Text(
            text = stringResource(R.string.subscriptions_remind_label),
            style = ivyInter(11.sp, FontWeight.Bold, letterSpacing = 1.1.sp),
            color = UI.colors.gray,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
        )
        Spacer(Modifier.height(8.dp))
        LeadTimeChips(
            selected = state.reminderLeadDays,
            onSelect = { onEvent(SubscriptionEditScreenEvent.OnReminderLeadDaysChanged(it)) }
        )

        Spacer(Modifier.height(24.dp))

        SectionLabel(stringResource(R.string.subscriptions_section_trial))

        // Trial toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable { onEvent(SubscriptionEditScreenEvent.OnTrialChanged(!state.isTrial)) }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.subscriptions_trial_toggle),
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse,
                modifier = Modifier.weight(1f)
            )
            com.ivy.wallet.ui.theme.components.IvySwitch(enabled = state.isTrial) {
                onEvent(SubscriptionEditScreenEvent.OnTrialChanged(it))
            }
        }

        // Trial end date — only when trial is on
        if (state.isTrial) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable {
                        ivyContext.datePicker(
                            minDate = java.time.LocalDate.now(),
                            initialDate = state.trialEndsAt?.toLocalDate()
                                ?: java.time.LocalDate.now().plusDays(14),
                        ) { picked ->
                            onEvent(SubscriptionEditScreenEvent.OnTrialEndsChanged(picked.atTime(12, 0)))
                        }
                    }
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    tint = UI.colors.gray
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = state.trialEndsAt?.toLocalDate()
                        ?.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))
                        ?: stringResource(R.string.subscriptions_trial_pick_date),
                    style = ivyInter(15.sp, FontWeight.Medium),
                    color = UI.colors.pureInverse,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // End after N cycles
        Text(
            text = stringResource(R.string.subscriptions_end_after_label),
            style = ivyInter(11.sp, FontWeight.Bold, letterSpacing = 1.1.sp),
            color = UI.colors.gray,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
        )
        Spacer(Modifier.height(8.dp))
        EndAfterRow(
            remaining = state.remainingOccurrences,
            onSetRemaining = { onEvent(SubscriptionEditScreenEvent.OnRemainingOccurrencesChanged(it)) }
        )

        Spacer(Modifier.height(4.dp))

        SectionLabel(stringResource(R.string.subscriptions_section_manage))
        ManageRow(
            paused = state.paused,
            archived = state.archived,
            onPauseToggle = { onEvent(SubscriptionEditScreenEvent.OnPausedChanged(!state.paused)) },
            onArchiveToggle = { onEvent(SubscriptionEditScreenEvent.OnArchivedChanged(!state.archived)) },
        )

        Spacer(Modifier.height(24.dp))

        Description(
            description = state.description,
            onAddDescription = { onEvent(SubscriptionEditScreenEvent.OnDescriptionModalVisible(true)) },
            onEditDescription = { onEvent(SubscriptionEditScreenEvent.OnDescriptionModalVisible(true)) }
        )

        Spacer(Modifier.height(600.dp)) // scroll hack (fork pattern)
    }

    onScreenStart {
        if (screen.plannedPaymentRuleId == null && screen.mandatoryFilled()) {
            onEvent(
                SubscriptionEditScreenEvent.OnRecurringRuleModalDataChanged(
                    RecurringRuleModalData(
                        initialStartDate = state.startDate,
                        initialIntervalN = state.intervalN,
                        initialIntervalType = state.intervalType,
                        initialOneTime = state.oneTime
                    )
                )
            )
        }
    }

    EditBottomSheet(
        initialTransactionId = screen.plannedPaymentRuleId,
        type = state.transactionType,
        accounts = state.accounts,
        selectedAccount = state.account,
        toAccount = null,
        amount = state.amount,
        currency = state.currency,

        ActionButton = {
            ModalSet(
                modifier = Modifier.testTag("subscriptionEditScreen_set")
            ) {
                onEvent(SubscriptionEditScreenEvent.OnSave())
            }
        },

        amountModalShown = state.amountModalVisible,
        setAmountModalShown = {
            onEvent(SubscriptionEditScreenEvent.OnAmountModalVisible(it))
        },

        onAmountChanged = {
            onEvent(SubscriptionEditScreenEvent.OnAmountChanged(it))
            if (shouldFocusCategory(state.category, state.transactionType)) {
                onEvent(SubscriptionEditScreenEvent.OnCategoryModalVisible(true))
            } else if (
                shouldFocusRecurring(
                    state.startDate,
                    state.intervalN,
                    state.intervalType,
                    state.oneTime
                )
            ) {
                onEvent(
                    SubscriptionEditScreenEvent.OnRecurringRuleModalDataChanged(
                        RecurringRuleModalData(
                            initialStartDate = state.startDate,
                            initialIntervalN = state.intervalN,
                            initialIntervalType = state.intervalType,
                            initialOneTime = state.oneTime
                        )
                    )
                )
            }
        },
        onSelectedAccountChanged = { onEvent(SubscriptionEditScreenEvent.OnAccountChanged(it)) },
        onToAccountChanged = { },
        onAddNewAccount = {
            onEvent(
                SubscriptionEditScreenEvent.OnAccountModalDataChanged(
                    AccountModalData(
                        account = null,
                        baseCurrency = state.currency,
                        balance = 0.0
                    )
                )
            )
        }
    )

    // ------------------------------------ Modals ------------------------------------

    ChooseCategoryModal(
        visible = state.categoryModalVisible,
        initialCategory = state.category,
        categories = state.categories,
        type = state.transactionType,
        showCategoryModal = { data ->
            onEvent(SubscriptionEditScreenEvent.OnCategoryModalDataChanged(data))
        },
        onCategoryChanged = {
            onEvent(SubscriptionEditScreenEvent.OnCategoryChanged(it))
            onEvent(
                SubscriptionEditScreenEvent.OnRecurringRuleModalDataChanged(
                    RecurringRuleModalData(
                        initialStartDate = state.startDate,
                        initialIntervalN = state.intervalN,
                        initialIntervalType = state.intervalType,
                        initialOneTime = state.oneTime
                    )
                )
            )
        },
        dismiss = {
            onEvent(SubscriptionEditScreenEvent.OnCategoryModalVisible(false))
        }
    )

    CategoryModal(
        modal = state.categoryModalData,
        onCreateCategory = { onEvent(SubscriptionEditScreenEvent.OnCreateCategory(it)) },
        onEditCategory = {
            onEvent(SubscriptionEditScreenEvent.OnEditCategory(it))
        },
        dismiss = {
            onEvent(SubscriptionEditScreenEvent.OnCategoryModalDataChanged(null))
        }
    )

    AccountModal(
        modal = state.accountModalData,
        onCreateAccount = { onEvent(SubscriptionEditScreenEvent.OnCreateAccount(it)) },
        onEditAccount = { _, _ -> },
        dismiss = {
            onEvent(SubscriptionEditScreenEvent.OnAccountModalDataChanged(null))
        }
    )

    DescriptionModal(
        visible = state.descriptionModalVisible,
        description = state.description,
        onDescriptionChanged = { onEvent(SubscriptionEditScreenEvent.OnDescriptionChanged(it)) },
        dismiss = {
            onEvent(SubscriptionEditScreenEvent.OnDescriptionModalVisible(false))
        }
    )

    DeleteSubscriptionDialog(
        visible = state.deleteTransactionModalVisible,
        title = titleTextFieldValue.text.trim().ifBlank { null },
        onCancel = {
            onEvent(SubscriptionEditScreenEvent.OnDeleteTransactionModalVisible(false))
        },
        onConfirm = { onEvent(SubscriptionEditScreenEvent.OnDelete) }
    )

    RecurringRuleModal(
        modal = state.recurringRuleModalData,
        onRuleChanged = { newStartDate, newOneTime, newIntervalN, newIntervalType ->
            onEvent(
                SubscriptionEditScreenEvent.OnRuleChanged(
                    newStartDate,
                    newOneTime,
                    newIntervalN,
                    newIntervalType
                )
            )

            if (shouldFocusCategory(state.category, state.transactionType)) {
                onEvent(SubscriptionEditScreenEvent.OnCategoryModalVisible(true))
            } else if (titleTextFieldValue.text.isBlank()) {
                titleFocus.requestFocus()
            }
        },
        dismiss = {
            onEvent(SubscriptionEditScreenEvent.OnRecurringRuleModalDataChanged(null))
        }
    )
}

private fun shouldFocusCategory(
    category: Category?,
    type: TransactionType,
): Boolean = category == null && type != TransactionType.TRANSFER

private fun shouldFocusRecurring(
    startDate: LocalDateTime?,
    intervalN: Int?,
    intervalType: IntervalType?,
    oneTime: Boolean,
): Boolean = !com.ivy.planned.edit.hasRecurringRule(startDate, intervalN, intervalType, oneTime)

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = ivyInter(11.sp, FontWeight.Bold, letterSpacing = 1.1.sp),
        color = UI.colors.gray,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
    )
}

@Composable
private fun IconPalette(
    selected: String?,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        items(SUBSCRIPTION_ICON_KEYS) { key ->
            val isSelected = selected == key
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            androidx.compose.ui.graphics.Color(0xFF5E5CE6).copy(alpha = 0.22f)
                        } else {
                            UI.colors.pureInverse.copy(alpha = 0.06f)
                        },
                        CircleShape
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) {
                            androidx.compose.ui.graphics.Color(0xFF5E5CE6)
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        },
                        shape = CircleShape
                    )
                    .clickable { onSelect(key) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(SUBSCRIPTION_ICONS[key]!!),
                    contentDescription = key,
                    tint = if (isSelected) {
                        androidx.compose.ui.graphics.Color(0xFF5E5CE6)
                    } else {
                        UI.colors.pureInverse
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodNoteField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_vue_money_card),
                contentDescription = null,
                tint = UI.colors.gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.subscriptions_method_hint),
                        style = ivyInter(15.sp),
                        color = UI.colors.gray
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = ivyInter(15.sp).copy(color = UI.colors.pureInverse),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LeadTimeChips(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        val options = listOf(0, 2, 3, 7, 14)
        items(options) { days ->
            val isSelected = selected == days
            Text(
                text = if (days == 0) {
                    stringResource(R.string.subscriptions_lead_day0)
                } else {
                    stringResource(R.string.subscriptions_lead_days, days)
                },
                style = ivyInter(13.sp, FontWeight.SemiBold),
                color = if (isSelected) {
                    androidx.compose.ui.graphics.Color.White
                } else {
                    UI.colors.pureInverse
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        if (isSelected) {
                            androidx.compose.ui.graphics.Color(0xFF5E5CE6)
                        } else {
                            UI.colors.pureInverse.copy(alpha = 0.06f)
                        },
                        RoundedCornerShape(percent = 50)
                    )
                    .clickable { onSelect(days) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun EndAfterRow(
    remaining: Int?,
    onSetRemaining: (Int?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val chips: List<Pair<Int?, String>> = listOf(
            3 to "3",
            6 to "6",
            12 to "12",
            null to stringResource(R.string.subscriptions_never),
        )
        chips.forEach { (value, label) ->
            val isSelected = remaining == value
            Text(
                text = label,
                style = ivyInter(14.sp, FontWeight.SemiBold),
                color = if (isSelected) {
                    androidx.compose.ui.graphics.Color.White
                } else {
                    UI.colors.pureInverse
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        if (isSelected) {
                            androidx.compose.ui.graphics.Color(0xFF5E5CE6)
                        } else {
                            UI.colors.pureInverse.copy(alpha = 0.06f)
                        },
                        RoundedCornerShape(percent = 50)
                    )
                    .clickable { onSetRemaining(value) }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        if (remaining != null) {
            Text(
                text = stringResource(R.string.subscriptions_remaining_hint, remaining),
                style = ivyInter(12.sp),
                color = UI.colors.gray
            )
        }
    }
}

private fun hasRecurringRule(
    startDate: LocalDateTime?,
    intervalN: Int?,
    intervalType: IntervalType?,
    oneTime: Boolean,
): Boolean = com.ivy.planned.edit.hasRecurringRule(startDate, intervalN, intervalType, oneTime)

@ExperimentalFoundationApi
@Preview
@Composable
private fun Preview() {
    IvyWalletPreview {
        UI(
            screen = SubscriptionEditScreen(plannedPaymentRuleId = null),
            state = SubscriptionEditScreenState(
                currency = "INR",
                transactionType = TransactionType.EXPENSE,
                startDate = null,
                intervalN = null,
                intervalType = null,
                oneTime = false,
                initialTitle = "",
                description = null,
                categories = persistentListOf(),
                accounts = persistentListOf(),
                account = null,
                category = null,
                amount = 0.0,
                categoryModalVisible = false,
                descriptionModalVisible = false,
                deleteTransactionModalVisible = false,
                categoryModalData = null,
                accountModalData = null,
                recurringRuleModalData = null,
                amountModalVisible = false,
                iconKey = "film",
                paymentMethodNote = "HDFC 4932",
                reminderLeadDays = 7,
                isTrial = false,
                trialEndsAt = null,
                remainingOccurrences = null,
                paused = false,
                archived = false,
            ),
            onEvent = {}
        )
    }
}

@Composable
private fun ManageRow(
    paused: Boolean,
    archived: Boolean,
    onPauseToggle: () -> Unit,
    onArchiveToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        ManageChip(
            icon = if (paused) R.drawable.ic_vue_media_play else R.drawable.ic_time_tracking_pause,
            labelRes = if (paused) R.string.subscriptions_resume else R.string.subscriptions_pause,
            onClick = onPauseToggle,
        )
        ManageChip(
            icon = if (archived) R.drawable.ic_check else R.drawable.ic_vue_main_archive,
            labelRes = if (archived) R.string.subscriptions_restore else R.string.subscriptions_archive,
            onClick = onArchiveToggle,
        )
    }
}

@Composable
private fun ManageChip(
    icon: Int,
    labelRes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(UI.colors.pureInverse.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = UI.colors.pureInverse,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(labelRes),
            style = ivyInter(14.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )
    }
}

/**
 * Destructive confirm — the subscription name must be typed to enable Delete.
 */
@Composable
private fun DeleteSubscriptionDialog(
    visible: Boolean,
    title: String?,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return

    var typed by remember { mutableStateOf("") }
    val expected = title?.trim().orEmpty()
    val canDelete = expected.isNotEmpty() && typed.trim().equals(expected, ignoreCase = true)

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(UI.colors.pureInverse, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.subscriptions_delete_confirm_title),
                style = ivyInter(20.sp, FontWeight.ExtraBold),
                color = Color(0xFFFF6B5B)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.subscriptions_delete_confirm_body,
                    title ?: "this subscription"
                ),
                style = ivyInter(14.sp),
                color = UI.colors.pure.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(14.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = typed,
                onValueChange = { typed = it },
                singleLine = true,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pure),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(UI.colors.pure.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                decorationBox = { inner ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (typed.isEmpty()) {
                            Text(
                                text = stringResource(R.string.subscriptions_delete_confirm_hint),
                                style = ivyInter(16.sp),
                                color = UI.colors.pure.copy(alpha = 0.45f)
                            )
                        }
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = ivyInter(14.sp, FontWeight.SemiBold),
                    color = UI.colors.pure,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(UI.colors.pure.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 16.dp, vertical = 11.dp)
                )
                Text(
                    text = stringResource(R.string.subscriptions_delete_confirm_action),
                    style = ivyInter(14.sp, FontWeight.Bold),
                    color = Color(0xFFFF6B5B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Color(0xFFFF6B5B).copy(alpha = if (canDelete) 0.22f else 0.06f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = canDelete, onClick = onConfirm)
                        .padding(horizontal = 16.dp, vertical = 11.dp)
                )
            }
        }
    }
}
