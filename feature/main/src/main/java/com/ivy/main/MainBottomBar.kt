package com.ivy.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.base.legacy.Look
import com.ivy.design.l0_system.TrueBlack
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.utils.thenIf
import com.ivy.legacy.data.model.MainTab
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.densityScope
import com.ivy.legacy.utils.drawColoredShadow
import com.ivy.legacy.utils.lerp
import com.ivy.legacy.utils.navigationBarInset
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.springBounceFast
import com.ivy.legacy.utils.toDensityDp
import com.ivy.legacy.utils.toDensityPx
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gradient
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.GradientIvy
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.IvyCircleButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton
import com.ivy.wallet.ui.theme.gradientExpenses
import com.ivy.wallet.ui.theme.modal.AddModalBackHandling
import com.ivy.wallet.ui.theme.pureBlur
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

val TRN_BUTTON_CLICK_AREA_HEIGHT = 150.dp
val FAB_BUTTON_SIZE = 56.dp
val DOCK_HEIGHT = 68.dp
val DOCK_BOTTOM_MARGIN = 14.dp
val DOCK_HORIZONTAL_MARGIN = 16.dp
val CENTER_FAB_SIZE = 52.dp

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BoxWithConstraintsScope.BottomBar(
    tab: MainTab,
    selectTab: (MainTab) -> Unit,

    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTransfer: () -> Unit,
    onAddPlannedPayment: () -> Unit,

    showAddAccountModal: () -> Unit,
) {
    val ivyContext = ivyWalletCtx()

    var expanded by remember { mutableStateOf(false) }

    val modalId = remember { UUID.randomUUID() }

    AddModalBackHandling(
        modalId = modalId,
        visible = expanded
    ) {
        expanded = false
    }

    val screenHeightDp = densityScope { ivyContext.screenHeight.toDp() }
    val expandedBackgroundOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else screenHeightDp,
        animationSpec = springBounceFast()
    )

    val fabRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = springBounceFast()
    )

    val buttonsShownPercent by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = springBounceFast()
    )

    // Material-aware dock colors. isLight comes from the token (not a color equality
    // check) so the Forest / Graphite light palettes keep their light dock.
    // The glass material is the Look.GLASS signature OR the user's Translucent-dock
    // override - in both cases the surface recipe is glass while colors still follow
    // the active material, mode and accent (pill/active tab read the primary tokens).
    val isLight = UI.colors.isLight
    val isAmoled = UI.colors.pure == TrueBlack
    val isGlass = UI.colors.look == Look.GLASS || ivyContext.dockTranslucent

    val dockBg = when {
        isGlass && isLight -> Color(0xFFF6F8FC).copy(alpha = 0.72f)
        isGlass -> Color(0xFF161C29).copy(alpha = 0.74f)
        isLight -> Color(0xFFFFFFFF).copy(alpha = 0.96f)
        isAmoled -> Color(0xFF141418).copy(alpha = 0.97f)
        else -> Color(0xFF262732).copy(alpha = 0.95f)
    }

    val dockBorderColor = when {
        isGlass && isLight -> Color(0xFFDDE3EE).copy(alpha = 0.70f)
        isGlass -> Color.White.copy(alpha = 0.16f)
        isLight -> Color(0xFFE2E4EB).copy(alpha = 0.85f)
        isAmoled -> Color.White.copy(alpha = 0.18f)
        else -> Color.White.copy(alpha = 0.14f)
    }

    val shadowColor = when {
        isGlass && isLight -> Color(0xFF0A1220)
        isGlass -> Color.Black
        isLight -> Color(0xFF0F172A)
        isAmoled -> Color.Black
        else -> Color.Black
    }

    val shadowAlpha = when {
        isGlass && isLight -> 0.13f
        isGlass -> 0.40f
        isLight -> 0.18f
        isAmoled -> 0.22f
        else -> 0.50f
    }

    // The liquid pill and the active tab read the accent-reactive primary tokens:
    // primary in light mode, the readable companion (primary1) in dark/AMOLED.
    val pillBg = if (isLight) {
        UI.colors.primary.copy(alpha = 0.13f)
    } else {
        UI.colors.primary.copy(alpha = if (isAmoled) 0.28f else 0.24f)
    }

    val activeTabColor = if (isLight) UI.colors.primary else UI.colors.primary1

    val unselectedTabColor = when {
        isGlass && isLight -> Color(0xFF5C6470)
        isGlass -> Color.White.copy(alpha = 0.58f)
        isLight -> Color(0xFF525763)
        isAmoled -> Color.White.copy(alpha = 0.65f)
        else -> Color.White.copy(alpha = 0.55f)
    }

    val targetSlot = when (tab) {
        MainTab.HOME -> 0f
        MainTab.ACCOUNTS -> 1f
        MainTab.MENU -> 3f
        MainTab.SETTINGS -> 4f
    }

    val animatedSlot by animateFloatAsState(
        targetValue = targetSlot,
        animationSpec = spring(
            dampingRatio = 0.74f,
            stiffness = 380f
        ),
        label = "dock_sliding_pill"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .alpha(1f - buttonsShownPercent)
            .navigationBarsPadding()
            .padding(
                bottom = DOCK_BOTTOM_MARGIN,
                start = DOCK_HORIZONTAL_MARGIN,
                end = DOCK_HORIZONTAL_MARGIN
            )
            .drawColoredShadow(
                color = shadowColor,
                alpha = shadowAlpha,
                borderRadius = 35.dp,
                shadowRadius = 24.dp,
                offsetY = 6.dp
            )
            .shadow(
                elevation = if (isLight) 10.dp else 12.dp,
                shape = RoundedCornerShape(35.dp),
                clip = false,
                spotColor = Color.Black.copy(alpha = if (isLight) 0.20f else 0.45f),
                ambientColor = Color.Black.copy(alpha = if (isLight) 0.08f else 0.25f)
            )
            .clip(RoundedCornerShape(35.dp))
            .background(dockBg)
            .then(
                if (isGlass) {
                    // Lit pane: a top sheen over the translucent surface.
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (isLight) 0.24f else 0.09f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(35.dp)
                    )
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.dp,
                brush = if (isGlass) {
                    // The glass signature: bright top edge fading down.
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isLight) 0.85f else 0.34f),
                            Color.White.copy(alpha = if (isLight) 0.10f else 0.04f)
                        )
                    )
                } else {
                    SolidColor(dockBorderColor)
                },
                shape = RoundedCornerShape(35.dp)
            )
            .height(DOCK_HEIGHT)
            .clickableNoIndication(rememberInteractionSource()) {
                // consume click
            }
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val totalHeightPx = constraints.maxHeight.toFloat()
            val slotWidthPx = totalWidthPx / 5f
            val slotWidthDp = densityScope { slotWidthPx.toDensityDp() }
            val pillWidth = (slotWidthDp - 4.dp).coerceAtLeast(42.dp)
            val pillHeight = 58.dp

            // Liquid Sliding Pill Indicator
            Box(
                modifier = Modifier
                    .offset {
                        val pillWidthPx = pillWidth.toPx()
                        val pillHeightPx = pillHeight.toPx()
                        val x = (animatedSlot * slotWidthPx + (slotWidthPx - pillWidthPx) / 2f).roundToInt()
                        val y = ((totalHeightPx - pillHeightPx) / 2f).roundToInt()
                        IntOffset(x, y)
                    }
                    .size(width = pillWidth, height = pillHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(pillBg)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockTab(
                    activeIcon = R.drawable.ic_home_filled,
                    inactiveIcon = R.drawable.ic_home,
                    name = stringResource(R.string.home),
                    selected = tab == MainTab.HOME,
                    selectedColor = activeTabColor,
                    unselectedColor = unselectedTabColor
                ) {
                    selectTab(MainTab.HOME)
                }

                DockTab(
                    activeIcon = R.drawable.ic_accounts_filled,
                    inactiveIcon = R.drawable.ic_accounts,
                    name = stringResource(R.string.accounts),
                    selected = tab == MainTab.ACCOUNTS,
                    selectedColor = activeTabColor,
                    unselectedColor = unselectedTabColor
                ) {
                    selectTab(MainTab.ACCOUNTS)
                }

                Spacer(modifier = Modifier.weight(1f))

                DockTab(
                    activeIcon = R.drawable.ic_menu_filled,
                    inactiveIcon = R.drawable.ic_menu,
                    name = stringResource(R.string.menu),
                    selected = tab == MainTab.MENU,
                    selectedColor = activeTabColor,
                    unselectedColor = unselectedTabColor
                ) {
                    selectTab(MainTab.MENU)
                }

                DockTab(
                    activeIcon = R.drawable.ic_settings_filled,
                    inactiveIcon = R.drawable.ic_settings,
                    name = stringResource(R.string.settings),
                    selected = tab == MainTab.SETTINGS,
                    selectedColor = activeTabColor,
                    unselectedColor = unselectedTabColor
                ) {
                    selectTab(MainTab.SETTINGS)
                }
            }
        }
    }

    if (expandedBackgroundOffset < screenHeightDp) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = expandedBackgroundOffset)
                .background(UI.colors.pure.copy(alpha = 0.95f))
                .clickableNoIndication(rememberInteractionSource()) {
                    // consume click, do nothing
                }
                .zIndex(100f)
        )
    }

    // ------------------------------------ BUTTONS--------------------------------------------------
    val dockBottomMarginPx = densityScope { DOCK_BOTTOM_MARGIN.toDensityPx() }
    val dockHeightPx = densityScope { DOCK_HEIGHT.toDensityPx() }
    val centerFabSizePx = densityScope { CENTER_FAB_SIZE.toDensityPx() }

    val fabStartX = ivyContext.screenWidth / 2f - centerFabSizePx / 2f
    val fabStartY = ivyContext.screenHeight - navigationBarInset() -
            dockBottomMarginPx - (dockHeightPx / 2f) - (centerFabSizePx / 2f)

    TransactionButtons(
        buttonsShownPercent = buttonsShownPercent,

        fabStartX = fabStartX,
        fabStartY = fabStartY,

        onAddIncome = onAddIncome,
        onAddExpense = onAddExpense,
        onAddTransfer = onAddTransfer,
        onAddPlannedPayment = onAddPlannedPayment
    )

    var dragOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    // + & x button
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = fabStartX.roundToInt(),
                        y = fabStartY.roundToInt()
                    )
                }
            }
            .size(CENTER_FAB_SIZE)
            .rotate(fabRotation)
            .zIndex(200f)
            .thenIf(tab != MainTab.ACCOUNTS) {
                pointerInput(Unit) {
                    detectDragGestures(
                        onDragCancel = {
                            dragOffset = Offset.Zero
                        },
                        onDragEnd = {
                            dragOffset = Offset.Zero
                        },
                        onDrag = { _, dragAmount ->
                            dragOffset += dragAmount

                            val horizontalThreshold = 40
                            val verticalThreshold = 60

                            when {
                                abs(dragOffset.x) < horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    onAddExpense()
                                }

                                dragOffset.x < -horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up left
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    onAddIncome()
                                }

                                dragOffset.x > horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up right
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    onAddTransfer()
                                }
                            }
                        }
                    )
                }
            }
            .testTag("fab_add"),
        backgroundPadding = 6.dp,
        icon = R.drawable.ic_add,
        backgroundGradient = when (tab) {
            MainTab.ACCOUNTS -> {
                GradientGreen
            }

            else -> {
                if (!expanded) GradientIvy else Gradient.solid(UI.colors.gray)
            }
        },
        hasShadow = !expanded,
        tint = White
    ) {
        when (tab) {
            MainTab.ACCOUNTS -> {
                showAddAccountModal()
            }

            else -> {
                expanded = !expanded
            }
        }
    }
}

@Composable
private fun TransactionButtons(
    buttonsShownPercent: Float,

    fabStartX: Float,
    fabStartY: Float,

    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTransfer: () -> Unit,
    onAddPlannedPayment: () -> Unit,
) {
    val ivyContext = ivyWalletCtx()

    val bH = 48.dp
    val bV = 20.dp // 24.dp
    val bCenterV = 74.dp // 80.dp

    if (buttonsShownPercent > 0.01f) {
        val buttonLeftX = bH.toDensityPx()
        val buttonRightX = ivyContext.screenWidth - bH.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx()

        val sideButtonsY = fabStartY - bV.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx()
        val buttonCenterY = fabStartY - bCenterV.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx()

        val clickAreaWidth = ivyContext.screenWidth / 3

        IvyOutlinedButton(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placealbe = measurable.measure(constraints)
                    layout(placealbe.width, placealbe.height) {
                        placealbe.place(
                            x = ivyContext.screenWidth / 2 - placealbe.width / 2,
                            y = buttonCenterY.roundToInt() - 48.dp.roundToPx() - placealbe.height - FAB_BUTTON_SIZE.roundToPx()
                        )
                    }
                }
                .padding(horizontal = 8.dp)
                .alpha(buttonsShownPercent)
                .zIndex(200f),
            iconStart = R.drawable.ic_planned_payments,
            text = stringResource(R.string.add_planned_payment),
            solidBackground = true
        ) {
            onAddPlannedPayment()
        }

        // Add INCOME ------------------------------------------------------------------------------
        AddIncomeButton(
            buttonsShownPercent = buttonsShownPercent,
            fabStartX = fabStartX,
            fabStartY = fabStartY,
            buttonLeftX = buttonLeftX,
            sideButtonsY = sideButtonsY,
            clickAreaWidth = clickAreaWidth,
            onAddIncome = onAddIncome
        )
        // Add INCOME ------------------------------------------------------------------------------

        // Add EXPENSE ------------------------------------------------------------------------------
        AddExpenseButton(
            buttonsShownPercent = buttonsShownPercent,
            fabStartX = fabStartX,
            fabStartY = fabStartY,
            buttonCenterY = buttonCenterY,
            clickAreaWidth = clickAreaWidth,
            onAddExpense = onAddExpense
        )
        // Add EXPENSE ------------------------------------------------------------------------------

        // Add TRANSFER ----------------------------------------------------------------------------
        AddTransferButton(
            buttonsShownPercent = buttonsShownPercent,
            fabStartX = fabStartX,
            fabStartY = fabStartY,
            buttonRightX = buttonRightX,
            sideButtonsY = sideButtonsY,
            clickAreaWidth = clickAreaWidth,
            onAddTransfer = onAddTransfer
        )
        // Add TRANSFER ----------------------------------------------------------------------------
    }
}

@Composable
private fun AddIncomeButton(
    buttonsShownPercent: Float,
    fabStartX: Float,
    fabStartY: Float,
    buttonLeftX: Float,
    sideButtonsY: Float,
    clickAreaWidth: Int,
    onAddIncome: () -> Unit,
) {
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val x = lerp(fabStartX, buttonLeftX, buttonsShownPercent)
                val y = lerp(
                    fabStartY,
                    sideButtonsY - FAB_BUTTON_SIZE.roundToPx(),
                    buttonsShownPercent
                )

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = x.roundToInt(),
                        y = y.roundToInt()
                    )
                }
            }
            .size(FAB_BUTTON_SIZE)
            .zIndex(200f),
        icon = R.drawable.ic_income,
        backgroundGradient = GradientGreen,
        tint = White,
        onClick = onAddIncome
    )

    Text(
        modifier = Modifier
            .width(FAB_BUTTON_SIZE + 16.dp)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = buttonLeftX.roundToInt() - 8.dp.roundToPx(),
                        y = (sideButtonsY + 12.dp.toPx()).roundToInt()
                    )
                }
            }
            .alpha(buttonsShownPercent)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddIncome()
            }
            .zIndex(200f),
        text = stringResource(R.string.add_income_uppercase),
        style = UI.typo.c.style(
            color = UI.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )

    // Click area
    Spacer(
        modifier = Modifier
            .size(
                width = clickAreaWidth.toDensityDp(),
                height = TRN_BUTTON_CLICK_AREA_HEIGHT
            )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = 0,
                        y = sideButtonsY.roundToInt() - FAB_BUTTON_SIZE.roundToPx() - 16.dp.roundToPx()
                    )
                }
            }
            .zIndex(199f)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddIncome()
            }
    )
}

@Composable
private fun AddExpenseButton(
    buttonsShownPercent: Float,
    fabStartX: Float,
    fabStartY: Float,
    buttonCenterY: Float,
    clickAreaWidth: Int,
    onAddExpense: () -> Unit,
) {
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val y =
                    lerp(
                        fabStartY,
                        buttonCenterY - FAB_BUTTON_SIZE.roundToPx(),
                        buttonsShownPercent
                    )

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = fabStartX.roundToInt(),
                        y = y.roundToInt()
                    )
                }
            }
            .size(FAB_BUTTON_SIZE)
            .zIndex(200f),
        icon = R.drawable.ic_expense,
        backgroundGradient = gradientExpenses(),
        horizontalGradient = false,
        tint = White,
        onClick = onAddExpense
    )

    Text(
        modifier = Modifier
            .width(FAB_BUTTON_SIZE + 16.dp)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = fabStartX.roundToInt() - 8.dp.roundToPx(),
                        y = (buttonCenterY + 12.dp.toPx()).roundToInt()
                    )
                }
            }
            .alpha(buttonsShownPercent)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddExpense()
            }
            .zIndex(200f),
        text = stringResource(R.string.add_expense_uppercase),
        style = UI.typo.c.style(
            color = UI.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )

    // Click area
    Spacer(
        modifier = Modifier
            .size(
                width = clickAreaWidth.toDensityDp(),
                height = TRN_BUTTON_CLICK_AREA_HEIGHT
            )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = clickAreaWidth,
                        y = buttonCenterY.roundToInt() - FAB_BUTTON_SIZE.roundToPx() - 16.dp.roundToPx()
                    )
                }
            }
            .zIndex(199f)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddExpense()
            }
    )
}

@Composable
private fun AddTransferButton(
    buttonsShownPercent: Float,
    fabStartX: Float,
    fabStartY: Float,
    buttonRightX: Float,
    sideButtonsY: Float,
    clickAreaWidth: Int,
    onAddTransfer: () -> Unit,
) {
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val x = lerp(fabStartX, buttonRightX, buttonsShownPercent)
                val y = lerp(
                    fabStartY,
                    sideButtonsY - FAB_BUTTON_SIZE.roundToPx(),
                    buttonsShownPercent
                )

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = x.roundToInt(),
                        y = y.roundToInt()
                    )
                }
            }
            .size(FAB_BUTTON_SIZE)
            .zIndex(200f),
        icon = R.drawable.ic_transfer,
        backgroundGradient = GradientIvy,
        tint = White,
        onClick = onAddTransfer
    )

    Text(
        modifier = Modifier
            .width(FAB_BUTTON_SIZE + 16.dp)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = buttonRightX.roundToInt() - 8.dp.roundToPx(),
                        y = (sideButtonsY + 12.dp.toPx()).roundToInt()
                    )
                }
            }
            .alpha(buttonsShownPercent)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddTransfer()
            }
            .zIndex(200f),
        text = stringResource(R.string.account_transfer),
        style = UI.typo.c.style(
            color = UI.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )

    // Click area
    Spacer(
        modifier = Modifier
            .size(
                width = clickAreaWidth.toDensityDp(),
                height = TRN_BUTTON_CLICK_AREA_HEIGHT
            )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = 2 * clickAreaWidth,
                        y = sideButtonsY.roundToInt() - FAB_BUTTON_SIZE.roundToPx() - 16.dp.roundToPx()
                    )
                }
            }
            .zIndex(199f)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddTransfer()
            }
    )
}

@Composable
private fun RowScope.DockTab(
    @DrawableRes activeIcon: Int,
    @DrawableRes inactiveIcon: Int,
    name: String,
    selected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 400f
        ),
        label = "dock_tab_scale"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(percent = 50))
            .clickableNoIndication(rememberInteractionSource()) {
                onClick()
            }
            .testTag(name.lowercase()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IvyIcon(
            modifier = Modifier
                .size(24.dp)
                .scale(iconScale),
            icon = if (selected) activeIcon else inactiveIcon,
            tint = if (selected) selectedColor else unselectedColor
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = name,
            style = UI.typo.c.style(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) selectedColor else unselectedColor,
                textAlign = TextAlign.Center
            )
        )
    }
}
