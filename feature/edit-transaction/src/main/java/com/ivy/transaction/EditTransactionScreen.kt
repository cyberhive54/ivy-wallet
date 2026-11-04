package com.ivy.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Theme
import com.ivy.base.model.Necessity
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.l0_system.Orange
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.utils.thenIf
import com.ivy.design.utils.hideKeyboard
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.data.EditTransactionDisplayLoan
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.ui.component.edit.TransactionDateTime
import com.ivy.legacy.ui.component.edit.core.Description
import com.ivy.legacy.ui.component.tags.AddTagButton
import com.ivy.legacy.ui.component.tags.ShowTagModal
import com.ivy.legacy.utils.onScreenStart
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.frp.test.TestingContext
import com.ivy.navigation.EditPlannedScreen
import com.ivy.navigation.EditTransactionScreen
import com.ivy.navigation.IvyPreview
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.domain.data.CustomExchangeRateState
import com.ivy.wallet.domain.data.IvyCurrency
import com.ivy.wallet.domain.deprecated.logic.model.CreateAccountData
import com.ivy.wallet.domain.deprecated.logic.model.CreateCategoryData
import com.ivy.wallet.ui.edit.core.Category
import com.ivy.wallet.ui.edit.core.DueDate
import com.ivy.wallet.ui.edit.core.Title
import com.ivy.wallet.ui.theme.components.AddPrimaryAttributeButton
import com.ivy.wallet.ui.theme.components.ChangeTransactionTypeModal
import com.ivy.wallet.ui.theme.components.CustomExchangeRateCard
import com.ivy.wallet.ui.theme.components.CircleButtonFilled
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.toComposeColor
import com.ivy.wallet.ui.theme.modal.DeleteModal
import com.ivy.wallet.ui.theme.modal.ProgressModal
import com.ivy.wallet.ui.theme.modal.edit.AccountModal
import com.ivy.wallet.ui.theme.modal.edit.AccountModalData
import com.ivy.wallet.ui.theme.modal.edit.AmountModal
import com.ivy.wallet.ui.theme.modal.edit.CategoryModal
import com.ivy.wallet.ui.theme.modal.edit.CategoryModalData
import com.ivy.wallet.ui.theme.modal.edit.ChooseCategoryModal
import com.ivy.wallet.ui.theme.modal.edit.DescriptionModal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.EditTransactionScreen(screen: EditTransactionScreen) {
    val viewModel: EditTransactionViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    LaunchedEffect(Unit) {
        viewModel.start(screen)
    }

    val view = LocalView.current

    UI(
        screen = screen,
        transactionType = uiState.transactionType,
        baseCurrency = uiState.currency,
        initialTitle = uiState.initialTitle,
        titleSuggestions = uiState.titleSuggestions,
        necessity = uiState.necessity,
        description = uiState.description,
        dateTime = uiState.dateTime,
        category = uiState.category,
        account = uiState.account,
        toAccount = uiState.toAccount,
        dueDate = uiState.dueDate,
        amount = uiState.amount,
        loanData = uiState.displayLoanHelper,
        backgroundProcessing = uiState.backgroundProcessingStarted,
        customExchangeRateState = uiState.customExchangeRateState,

        categories = uiState.categories,
        accounts = uiState.accounts,
        tags = uiState.tags,
        transactionAssociatedTags = uiState.transactionAssociatedTags,
        hasChanges = uiState.hasChanges,
        onSetDate = {
            viewModel.onEvent(EditTransactionViewEvent.OnChangeDate)
        },
        onSetTime = {
            viewModel.onEvent(EditTransactionViewEvent.OnChangeTime)
        },
        onTitleChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnTitleChanged(it))
        },
        onDescriptionChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnDescriptionChanged(it))
        },
        onAmountChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnAmountChanged(it))
        },
        onCategoryChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnCategoryChanged(it))
        },
        onAccountChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnAccountChanged(it))
        },
        onToAccountChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnToAccountChanged(it))
        },
        onSetNecessity = {
            viewModel.onEvent(EditTransactionViewEvent.OnSetNecessity(it))
        },
        onDueDateChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnDueDateChanged(it))
        },
        onSetTransactionType = {
            viewModel.onEvent(EditTransactionViewEvent.OnSetTransactionType(it))
        },
        onCreateCategory = {
            viewModel.onEvent(EditTransactionViewEvent.CreateCategory(it))
        },
        onEditCategory = {
            viewModel.onEvent(EditTransactionViewEvent.EditCategory(it))
        },
        onPayPlannedPayment = {
            viewModel.onEvent(EditTransactionViewEvent.OnPayPlannedPayment)
        },
        onSave = {
            view.hideKeyboard()
            viewModel.onEvent(EditTransactionViewEvent.Save(it))
        },
        onSetHasChanges = {
            viewModel.onEvent(EditTransactionViewEvent.SetHasChanges(it))
        },
        onDelete = {
            viewModel.onEvent(EditTransactionViewEvent.Delete)
        },
        onDuplicate = {
            viewModel.onEvent(EditTransactionViewEvent.Duplicate)
        },
        onCreateAccount = {
            viewModel.onEvent(EditTransactionViewEvent.CreateAccount(it))
        },
        onExchangeRateChange = {
            viewModel.onEvent(EditTransactionViewEvent.UpdateExchangeRate(it))
        },
        onTagOperation = {
            viewModel.onEvent(it)
        }
    )
}

@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: EditTransactionScreen,
    transactionType: TransactionType,
    baseCurrency: String,
    initialTitle: String?,
    titleSuggestions: ImmutableSet<String>,
    necessity: Necessity?,
    description: String?,
    category: Category?,
    dateTime: Instant?,
    account: Account?,
    toAccount: Account?,
    dueDate: Instant?,
    amount: Double,

    customExchangeRateState: CustomExchangeRateState,
    categories: ImmutableList<Category>,
    accounts: ImmutableList<Account>,
    tags: ImmutableList<Tag>,
    transactionAssociatedTags: ImmutableList<TagId>,
    onTitleChange: (String?) -> Unit,
    onDescriptionChange: (String?) -> Unit,
    onAmountChange: (Double) -> Unit,
    onCategoryChange: (Category?) -> Unit,
    onAccountChange: (Account) -> Unit,
    onToAccountChange: (Account) -> Unit,
    onSetNecessity: (Necessity?) -> Unit,
    onDueDateChange: (LocalDateTime?) -> Unit,
    onSetDate: () -> Unit,
    onSetTime: () -> Unit,
    onSetTransactionType: (TransactionType) -> Unit,

    onCreateCategory: (CreateCategoryData) -> Unit,
    onEditCategory: (Category) -> Unit,
    onPayPlannedPayment: () -> Unit,
    onSave: (closeScreen: Boolean) -> Unit,
    onSetHasChanges: (hasChanges: Boolean) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onCreateAccount: (CreateAccountData) -> Unit,
    onExchangeRateChange: (Double?) -> Unit = { },
    onTagOperation: (EditTransactionViewEvent.TagEvent) -> Unit = {},
    loanData: EditTransactionDisplayLoan = EditTransactionDisplayLoan(),
    backgroundProcessing: Boolean = false,
    hasChanges: Boolean = false,

    ) {
    var chooseCategoryModalVisible by remember { mutableStateOf(false) }
    var tagModelVisible by remember { mutableStateOf(false) }
    var categoryModalData: CategoryModalData? by remember { mutableStateOf(null) }
    var accountModalData: AccountModalData? by remember { mutableStateOf(null) }
    var descriptionModalVisible by remember { mutableStateOf(false) }
    var deleteTrnModalVisible by remember { mutableStateOf(false) }
    var changeTransactionTypeModalVisible by remember { mutableStateOf(false) }
    var amountModalShown by remember { mutableStateOf(false) }
    var exchangeRateAmountModalShown by remember { mutableStateOf(false) }
    var accountChangeModal by remember { mutableStateOf(false) }
    val waitModalVisible by remember(backgroundProcessing) {
        mutableStateOf(backgroundProcessing)
    }
    var selectedAcc by remember(account) {
        mutableStateOf(account)
    }

    val amountModalId =
        remember(screen.initialTransactionId, customExchangeRateState.exchangeRate) {
            UUID.randomUUID()
        }

    // Loan guard - changing the source account of a loan to a different currency needs confirming
    val onFromAccountSelected: (Account) -> Unit = { newAccount ->
        if (loanData.isLoan && account?.currency != newAccount.currency) {
            selectedAcc = newAccount
            accountChangeModal = true
        } else {
            onAccountChange(newAccount)
        }
    }

    var titleTextFieldValue by remember(initialTitle) {
        mutableStateOf(
            TextFieldValue(
                initialTitle ?: ""
            )
        )
    }
    val titleFocus = FocusRequester()
    val scrollState = rememberScrollState()

    // This is to scroll the column to the customExchangeCard composable when it is shown
    var customExchangeRatePosition by remember { mutableFloatStateOf(0F) }
    LaunchedEffect(key1 = customExchangeRateState.showCard) {
        val scrollInt =
            if (customExchangeRateState.showCard) customExchangeRatePosition.roundToInt() else 0
        scrollState.animateScrollTo(scrollInt)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UI.colors.pure)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        TopBar(
            // Setting the transaction type to TransactionType.TRANSFER for transactions associated
            // with loan record to hide the ChangeTransactionType Button
            type = if (loanData.isLoanRecord) TransactionType.TRANSFER else transactionType,
            initialTransactionId = screen.initialTransactionId,
            onDeleteTrnModal = {
                deleteTrnModalVisible = true
            },
            onChangeTransactionTypeModal = {
                changeTransactionTypeModalVisible = true
            },
            showDuplicateButton = true,
            onDuplicate = onDuplicate
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(12.dp))

            // Amount hero - tap to open the keypad
            AmountHero(
                amount = amount,
                currency = baseCurrency,
                showConvertedAmountText = if (transactionType == TransactionType.TRANSFER &&
                    customExchangeRateState.convertedAmount != null &&
                    customExchangeRateState.toCurrencyCode != null
                ) {
                    "${
                        customExchangeRateState.convertedAmount!!.format(
                            IvyCurrency.getDecimalPlaces(
                                customExchangeRateState.toCurrencyCode!!
                            )
                        )
                    } ${customExchangeRateState.toCurrencyCode}"
                } else {
                    null
                },
                onClick = {
                    amountModalShown = true
                }
            )

            Spacer(Modifier.height(20.dp))

            // Account chips (From/To for transfers)
            if (transactionType == TransactionType.TRANSFER) {
                LabeledAccountsSection(
                    label = stringResource(R.string.from),
                    accounts = accounts,
                    selectedAccount = account,
                    childrenTestTag = "from_account",
                    onAddNewAccount = {
                        accountModalData = AccountModalData(
                            account = null, baseCurrency = baseCurrency, balance = 0.0
                        )
                    },
                    onAccountSelected = onFromAccountSelected
                )

                Spacer(Modifier.height(8.dp))

                LabeledAccountsSection(
                    label = stringResource(R.string.to),
                    accounts = accounts,
                    selectedAccount = toAccount,
                    childrenTestTag = "to_account",
                    onAddNewAccount = {
                        accountModalData = AccountModalData(
                            account = null, baseCurrency = baseCurrency, balance = 0.0
                        )
                    },
                    onAccountSelected = onToAccountChange
                )
            } else {
                val label = when (transactionType) {
                    TransactionType.INCOME -> stringResource(R.string.add_money_to)
                    TransactionType.EXPENSE -> stringResource(R.string.pay_with)
                    TransactionType.TRANSFER -> stringResource(R.string.from)
                }

                LabeledAccountsSection(
                    label = label,
                    accounts = accounts,
                    selectedAccount = account,
                    childrenTestTag = "from_account",
                    onAddNewAccount = {
                        accountModalData = AccountModalData(
                            account = null, baseCurrency = baseCurrency, balance = 0.0
                        )
                    },
                    onAccountSelected = onFromAccountSelected
                )
            }

            // Necessity (expenses only)
            if (transactionType == TransactionType.EXPENSE) {
                Spacer(Modifier.height(8.dp))
                NecessitySection(
                    selected = necessity,
                    onSelected = onSetNecessity
                )
            }

            SectionHairline()

            Spacer(Modifier.height(16.dp))

            Title(
                type = transactionType,
                titleFocus = titleFocus,
                initialTransactionId = screen.initialTransactionId,

                titleTextFieldValue = titleTextFieldValue,
                setTitleTextFieldValue = {
                    titleTextFieldValue = it
                },
                suggestions = titleSuggestions,
                scrollState = scrollState,

                onTitleChanged = onTitleChange,
                onNext = {
                    when {
                        shouldFocusAmount(amount = amount) -> {
                            amountModalShown = true
                        }

                        else -> {
                            onSave(true)
                        }
                    }
                }
            )

            if (loanData.loanCaption != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = loanData.loanCaption!!,
                    style = UI.typo.nB2.style(
                        color = UI.colors.mediumInverse,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Category(category = category, onChooseCategory = {
                chooseCategoryModalVisible = true
            })

            Spacer(Modifier.height(16.dp))

            AddTagButton(transactionAssociatedTags = transactionAssociatedTags, onClick = {
                tagModelVisible = true
            })

            Spacer(Modifier.height(32.dp))

            val ivyContext = ivyWalletCtx()

            val timeConverter = LocalTimeConverter.current
            if (dueDate != null) {
                DueDate(dueDate = dueDate) {
                    ivyContext.datePicker(
                        initialDate = with(timeConverter) {
                            dueDate.toLocalDate()
                        }
                    ) {
                        onDueDateChange(it.atTime(12, 0))
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            Description(
                description = description,
                onAddDescription = { descriptionModalVisible = true },
                onEditDescription = { descriptionModalVisible = true }
            )

            TransactionDateTime(
                dateTime = dateTime,
                dueDateTime = dueDate,
                onEditDate = onSetDate,
                onEditTime = onSetTime,
            )

            if (transactionType == TransactionType.TRANSFER && customExchangeRateState.showCard) {
                Spacer(Modifier.height(12.dp))
                CustomExchangeRateCard(
                    fromCurrencyCode = baseCurrency,
                    toCurrencyCode = customExchangeRateState.toCurrencyCode ?: baseCurrency,
                    exchangeRate = customExchangeRateState.exchangeRate,
                    onRefresh = {
                        // Set exchangeRate to null to reset
                        onExchangeRateChange(null)
                    },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        customExchangeRatePosition = coordinates.positionInParent().y * 0.3f
                    }
                ) {
                    exchangeRateAmountModalShown = true
                }
            }

            if (dueDate == null && transactionType != TransactionType.TRANSFER && dateTime == null) {
                Spacer(Modifier.height(12.dp))

                val nav = navigation()
                AddPrimaryAttributeButton(
                    icon = R.drawable.ic_planned_payments,
                    text = stringResource(R.string.add_planned_date_payment),
                    onClick = {
                        nav.back()
                        nav.navigateTo(
                            EditPlannedScreen(
                                plannedPaymentRuleId = null,
                                type = transactionType,
                                amount = amount,
                                accountId = account?.id,
                                categoryId = category?.id?.value,
                                title = titleTextFieldValue.text,
                                description = description,
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // Bottom action bar
        BottomActionBar(
            initialTransactionId = screen.initialTransactionId,
            type = transactionType,
            dueDate = dueDate,
            hasChanges = hasChanges,
            onSave = onSave,
            onSetHasChanges = onSetHasChanges,
            onPayPlannedPayment = onPayPlannedPayment,
        )
    }

    onScreenStart {
        if (screen.initialTransactionId == null) {
            amountModalShown = true
        }
    }

    // Amount keypad modal (numpad + calculator)
    AmountModal(
        id = amountModalId,
        visible = amountModalShown,
        currency = baseCurrency,
        initialAmount = amount.takeIf { it > 0 },
        Header = {
            Spacer(Modifier.height(24.dp))

            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = stringResource(R.string.account),
                style = UI.typo.b1.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(16.dp))

            AccountsRow(
                accounts = accounts,
                selectedAccount = account,
                onSelectedAccountChanged = {
                    if (loanData.isLoan && account?.currency != it.currency) {
                        selectedAcc = it
                        accountChangeModal = true
                    } else {
                        onAccountChange(it)
                    }
                },
                onAddNewAccount = {
                    accountModalData = AccountModalData(
                        account = null, baseCurrency = baseCurrency, balance = 0.0
                    )
                },
                childrenTestTag = "amount_modal_account"
            )
        },
        amountSpacerTop = 48.dp,
        dismiss = {
            amountModalShown = false
        }
    ) {
        onAmountChange(it)
        if (shouldFocusCategory(category)) {
            chooseCategoryModalVisible = true
        } else if (shouldFocusTitle(titleTextFieldValue, transactionType)) {
            titleFocus.requestFocus()
        }
    }

    // Modals
    ChooseCategoryModal(
        visible = chooseCategoryModalVisible,
        initialCategory = category,
        categories = categories,
        type = transactionType,
        showCategoryModal = { categoryModalData = it },
        onCategoryChanged = {
            onCategoryChange(it)
            if (shouldFocusTitle(titleTextFieldValue, transactionType)) {
                titleFocus.requestFocus()
            } else if (shouldFocusAmount(amount = amount)) {
                amountModalShown = true
            }
        },
        dismiss = {
            chooseCategoryModalVisible = false
        }
    )

    CategoryModal(modal = categoryModalData, onCreateCategory = { createData ->
        onCreateCategory(createData)
        chooseCategoryModalVisible = false
    }, onEditCategory = onEditCategory, dismiss = {
        categoryModalData = null
    })

    AccountModal(
        modal = accountModalData,
        onCreateAccount = onCreateAccount,
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalData = null
        }
    )

    DescriptionModal(
        visible = descriptionModalVisible,
        description = description,
        onDescriptionChanged = onDescriptionChange,
        dismiss = {
            descriptionModalVisible = false
        }
    )

    DeleteModal(
        visible = deleteTrnModalVisible,
        title = stringResource(R.string.confirm_deletion),
        description = stringResource(R.string.transaction_confirm_deletion_description),
        dismiss = { deleteTrnModalVisible = false }
    ) {
        onDelete()
    }

    ChangeTransactionTypeModal(
        visible = changeTransactionTypeModalVisible,
        includeTransferType = true,
        initialType = transactionType,
        dismiss = {
            changeTransactionTypeModalVisible = false
        }
    ) {
        onSetTransactionType(it)
    }

    DeleteModal(
        visible = accountChangeModal,
        title = stringResource(R.string.confirm_account_change),
        description = stringResource(R.string.confirm_account_change_description),
        buttonText = stringResource(R.string.confirm),
        iconStart = R.drawable.ic_agreed,
        dismiss = {
            accountChangeModal = false
        }
    ) {
        selectedAcc?.let { onAccountChange(it) }
        accountChangeModal = false
    }

    ProgressModal(
        title = stringResource(R.string.confirm_account_change),
        description = stringResource(R.string.confirm_account_loan_change),
        visible = waitModalVisible
    )

    AmountModal(
        id = amountModalId,
        visible = exchangeRateAmountModalShown,
        currency = "",
        initialAmount = customExchangeRateState.exchangeRate,
        dismiss = { exchangeRateAmountModalShown = false },
        decimalCountMax = IvyCurrency.getDecimalPlaces(
            customExchangeRateState.toCurrencyCode ?: baseCurrency
        ),
        onAmountChanged = {
            onExchangeRateChange(it)
        }
    )

    ShowTagModal(
        visible = tagModelVisible,
        onDismiss = {
            tagModelVisible = false
            // Reset TagList, avoids showing incorrect tag list when user has searched for a tag
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagSearch(""))
        },
        allTagList = tags,
        selectedTagList = transactionAssociatedTags,
        onTagAdd = {
            onTagOperation(EditTransactionViewEvent.TagEvent.SaveTag(name = it))
        },
        onTagEdit = { oldTag, newTag ->
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagEdit(oldTag, newTag))
        },
        onTagDelete = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagDelete(it))
        },
        onTagSelected = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagSelect(it))
        },
        onTagDeSelected = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagDeSelect(it))
        },
        onTagSearch = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagSearch(it))
        }
    )
}

// -----------------------------------------------------------------------------------------
// New full-sheet ("amount-first") components - Cupertino style, themed, accent driven.
// These mirror the legacy edit components but use the Inter typeface + hairlines.
// -----------------------------------------------------------------------------------------

@Suppress("ParameterNaming")
@Composable
private fun TopBar(
    type: TransactionType,
    initialTransactionId: UUID?,
    onDeleteTrnModal: () -> Unit,
    onChangeTransactionTypeModal: () -> Unit,
    showDuplicateButton: Boolean,
    onDuplicate: () -> Unit,
) {
    val nav = navigation()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleButtonFilled(
            icon = R.drawable.ic_dismiss,
            tint = UI.colors.pureInverse,
            backgroundColor = UI.colors.medium,
            clickAreaPadding = 10.dp,
        ) {
            nav.back()
        }

        Spacer(Modifier.weight(1f))

        when (type) {
            TransactionType.INCOME -> TypePill(
                text = stringResource(R.string.income),
                icon = R.drawable.ic_income
            ) {
                onChangeTransactionTypeModal()
            }

            TransactionType.EXPENSE -> TypePill(
                text = stringResource(R.string.expense),
                icon = R.drawable.ic_expense
            ) {
                onChangeTransactionTypeModal()
            }

            TransactionType.TRANSFER -> {
                // No type pill for transfers (same as before)
            }
        }

        Spacer(Modifier.weight(1f))

        if (initialTransactionId != null) {
            if (showDuplicateButton) {
                CircleButtonFilled(
                    icon = R.drawable.ic_duplicate,
                    tint = UI.colors.pureInverse,
                    backgroundColor = UI.colors.medium,
                    clickAreaPadding = 10.dp,
                ) {
                    onDuplicate()
                }

                Spacer(Modifier.width(12.dp))
            }

            CircleButtonFilled(
                icon = R.drawable.ic_delete,
                tint = UI.colors.red,
                backgroundColor = UI.colors.medium,
                clickAreaPadding = 10.dp,
            ) {
                onDeleteTrnModal()
            }
        } else {
            // Balance the pill so it stays centered
            Spacer(Modifier.width(44.dp))
        }
    }
}

@Composable
private fun TypePill(
    text: String,
    icon: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(UI.colors.medium, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("change_type_pill"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = UI.colors.pureInverse,
            modifier = Modifier.padding(end = 6.dp)
        )

        Text(
            text = text,
            style = ivyInter(15.sp, FontWeight.SemiBold, letterSpacing = (-0.2).sp),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.width(6.dp))

        Icon(
            painter = painterResource(R.drawable.ic_expand_more),
            contentDescription = "change type",
            tint = UI.colors.gray,
            modifier = Modifier.width(18.dp)
        )
    }
}

@Composable
private fun AmountHero(
    amount: Double,
    currency: String,
    showConvertedAmountText: String?,
    onClick: () -> Unit,
) {
    val interactionSource = rememberInteractionSource()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(interactionSource) { onClick() }
            .padding(vertical = 8.dp, horizontal = 24.dp)
            .testTag("edit_amount_balance_row"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currency,
                style = ivyInter(22.sp, FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
                color = UI.colors.gray,
                modifier = Modifier.padding(end = 10.dp)
            )

            Text(
                text = if (currency.isBlank()) amount.format(2) else amount.format(currency),
                style = ivyInter(
                    size = 52.sp,
                    weight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = UI.colors.pureInverse,
                maxLines = 1
            )
        }

        if (showConvertedAmountText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = showConvertedAmountText,
                style = ivyInter(17.sp, FontWeight.SemiBold),
                color = UI.colors.gray
            )
        }
    }
}

@Composable
private fun LabeledAccountsSection(
    label: String,
    accounts: List<Account>,
    selectedAccount: Account?,
    childrenTestTag: String,
    onAddNewAccount: () -> Unit,
    onAccountSelected: (Account) -> Unit,
) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = label.uppercase(),
            style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(8.dp))

        AccountsRow(
            accounts = accounts,
            selectedAccount = selectedAccount,
            onSelectedAccountChanged = onAccountSelected,
            onAddNewAccount = onAddNewAccount,
            childrenTestTag = childrenTestTag
        )
    }
}

@Composable
private fun NecessitySection(
    selected: Necessity?,
    onSelected: (Necessity?) -> Unit,
) {
    val options = listOf(
        Necessity.UNKNOWN to stringResource(R.string.necessity_unknown),
        Necessity.MUST to stringResource(R.string.necessity_must),
        Necessity.NEED to stringResource(R.string.necessity_need),
        Necessity.WANT to stringResource(R.string.necessity_want),
    )

    Column {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.necessity).uppercase(),
            style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
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
                        .testTag("necessity_${option.name.lowercase()}"),
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

@Composable
private fun SectionHairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(UI.colors.pureInverse.copy(alpha = 0.06f))
    )
}

@Composable
private fun BottomActionBar(
    initialTransactionId: UUID?,
    type: TransactionType,
    dueDate: Instant?,
    hasChanges: Boolean,
    onSave: (closeScreen: Boolean) -> Unit,
    onSetHasChanges: (hasChangesValue: Boolean) -> Unit,
    onPayPlannedPayment: () -> Unit,
) {
    SectionHairline()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        when {
            initialTransactionId != null -> {
                when (dueDate != null) {
                    true -> {
                        if (hasChanges) {
                            PrimaryActionButton(
                                label = stringResource(R.string.save),
                                icon = R.drawable.ic_save,
                            ) {
                                onSave(false)
                                onSetHasChanges(false)
                            }
                        } else {
                            PrimaryActionButton(
                                label = if (type == TransactionType.EXPENSE) {
                                    stringResource(R.string.pay)
                                } else {
                                    stringResource(R.string.get)
                                },
                                icon = R.drawable.ic_check,
                                onClick = onPayPlannedPayment
                            )
                        }
                    }

                    false -> {
                        PrimaryActionButton(
                            label = stringResource(R.string.save),
                            icon = R.drawable.ic_save,
                        ) {
                            onSave(true)
                        }
                    }
                }
            }

            else -> {
                PrimaryActionButton(
                    label = stringResource(R.string.add),
                    icon = R.drawable.ic_plus,
                    testTag = "modal_add",
                ) {
                    onSave(true)
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    label: String,
    icon: Int,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val foreground = findContrastTextColor(UI.colors.primary)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(UI.colors.primary, RoundedCornerShape(16.dp))
            .testTag(testTag ?: "primary_action")
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = foreground,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .width(20.dp)
            )

            Text(
                text = label,
                style = ivyInter(17.sp, FontWeight.SemiBold, letterSpacing = (-0.2).sp),
                color = foreground
            )
        }
    }
}

@Composable
private fun AccountsRow(
    accounts: List<Account>,
    selectedAccount: Account?,
    onSelectedAccountChanged: (Account) -> Unit,
    modifier: Modifier = Modifier,
    childrenTestTag: String? = null,
    onAddNewAccount: () -> Unit,
) {
    val lazyState = rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(accounts, selectedAccount) {
        if (selectedAccount != null) {
            val selectedIndex = accounts.indexOf(selectedAccount)
            if (selectedIndex != -1) {
                launch {
                    if (TestingContext.inTest) return@launch // breaks UI tests

                    lazyState.scrollToItem(selectedIndex)
                }
            }
        }
    }

    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        state = lazyState
    ) {
        item {
            Spacer(Modifier.width(24.dp))
        }

        itemsIndexed(accounts) { _, account ->
            AccountChip(
                account = account,
                selected = selectedAccount == account,
                testTag = childrenTestTag ?: "account"
            ) {
                onSelectedAccountChanged(account)
            }
            Spacer(Modifier.width(8.dp))
        }

        item {
            AddAccountChip {
                onAddNewAccount()
            }
        }

        item {
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun AccountChip(
    account: Account,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val accountColor = account.color.toComposeColor()
    val textColor =
        if (selected) findContrastTextColor(accountColor) else UI.colors.pureInverse

    val medium = UI.colors.medium
    val rFull = CircleShape

    Row(
        modifier = Modifier
            .clip(rFull)
            .thenIf(!selected) {
                border(2.dp, medium, rFull)
            }
            .thenIf(selected) {
                background(accountColor, rFull)
            }
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        ItemIconSDefaultIcon(
            iconName = account.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = textColor
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = account.name,
            style = ivyInter(15.sp, FontWeight.SemiBold, letterSpacing = (-0.2).sp),
            color = textColor
        )

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun AddAccountChip(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .border(2.dp, UI.colors.medium, CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        IvyIcon(
            icon = R.drawable.ic_plus,
            tint = UI.colors.pureInverse
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = stringResource(R.string.add_account),
            style = ivyInter(15.sp, FontWeight.SemiBold, letterSpacing = (-0.2).sp),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.width(24.dp))
    }
}

private fun shouldFocusCategory(
    category: Category?,
): Boolean = category == null

private fun shouldFocusTitle(
    titleTextFieldValue: TextFieldValue,
    type: TransactionType
): Boolean = titleTextFieldValue.text.isBlank() && type != TransactionType.TRANSFER

private fun shouldFocusAmount(amount: Double) = amount == 0.0

/** For Preview purpose **/
private val testDateTime = LocalDateTime.of(2023, 4, 27, 0, 35)
    .toInstant(ZoneOffset.UTC)

@ExperimentalFoundationApi
@Preview
@Composable
private fun BoxWithConstraintsScope.Preview(isDark: Boolean = false) {
    IvyPreview(isDark) {
        UI(
            screen = EditTransactionScreen(null, TransactionType.EXPENSE),
            initialTitle = "",
            titleSuggestions = persistentSetOf(),
            tags = persistentListOf(),
            transactionAssociatedTags = persistentListOf(),
            baseCurrency = "BGN",
            dateTime = testDateTime,
            description = null,
            category = null,
            account = Account(name = "phyre", Orange.toArgb()),
            toAccount = null,
            amount = 0.0,
            dueDate = null,
            transactionType = TransactionType.INCOME,
            customExchangeRateState = CustomExchangeRateState(),

            categories = persistentListOf(),
            accounts = persistentListOf(),

            onDueDateChange = {},
            onCategoryChange = {},
            onAccountChange = {},
            onToAccountChange = {},
            onDescriptionChange = {},
            onTitleChange = {},
            onAmountChange = {},
            necessity = null,
            onSetNecessity = {},

            onCreateCategory = { },
            onEditCategory = {},
            onPayPlannedPayment = {},
            onSave = {},
            onSetHasChanges = {},
            onDelete = {},
            onDuplicate = {},
            onCreateAccount = { },
            onSetDate = {},
            onSetTime = {},
            onSetTransactionType = {}
        )
    }
}

/** For screenshot testing */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTransactionScreenUiTest(isDark: Boolean) {
    val theme = when (isDark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    IvyWalletPreview(theme) {
        Preview(isDark)
    }
}
