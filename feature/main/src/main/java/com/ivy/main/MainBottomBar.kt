package com.ivy.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import com.ivy.design.api.systems.AccentFamilies
import com.ivy.design.l0_system.Black
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
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.IvyCircleButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.AddModalBackHandling
import java.util.UUID
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

val DOCK_HEIGHT = 68.dp
val DOCK_BOTTOM_MARGIN = 14.dp
val DOCK_HORIZONTAL_MARGIN = 16.dp
val CENTER_FAB_SIZE = 52.dp

// Radial add menu geometry - all angles are in Compose canvas space
// (0 deg = east, 90 deg = south, 270 deg = straight up).
private val RING_RADIUS = 128.dp
private val BAND_WIDTH = 60.dp
private const val WEDGE_SWEEP_DEG = 41f
private const val INCOME_CENTER_DEG = 226.5f
private const val EXPENSE_CENTER_DEG = 270f
private const val TRANSFER_CENTER_DEG = 313.5f

/**
 * The bottom dock. The + button blooms a radial add menu: three sector wedges
 * (Income / Expense / Transfer) fanning out of the + with a bouncy spring and a
 * Planned Payment capsule riding above the fan. The dock stays visible on top of
 * the opaque scrim - tapping the scrim, the X (morphed +) or anywhere outside a
 * wedge collapses the menu.
 */
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
    // When set (pushed pages like Categories/Tags), the + runs this action
    // directly instead of blooming the radial add menu.
    overrideAddAction: (() -> Unit)? = null,
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
            // The dock rides above the radial menu's opaque scrim and stays
            // fully visible while the add menu is open.
            .zIndex(110f)
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
                // Tapping the dock body (outside a tab) collapses the add menu.
                expanded = false
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
                    expanded = false
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
                    expanded = false
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
                    expanded = false
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
                    expanded = false
                    selectTab(MainTab.SETTINGS)
                }
            }
        }
    }

    // ------------------------------------ Radial add menu ----------------------------------------
    val dockBottomMarginPx = densityScope { DOCK_BOTTOM_MARGIN.toDensityPx() }
    val dockHeightPx = densityScope { DOCK_HEIGHT.toDensityPx() }
    val centerFabSizePx = densityScope { CENTER_FAB_SIZE.toDensityPx() }

    val fabStartX = ivyContext.screenWidth / 2f - centerFabSizePx / 2f
    val fabStartY = ivyContext.screenHeight - navigationBarInset() -
            dockBottomMarginPx - (dockHeightPx / 2f) - (centerFabSizePx / 2f)

    val fabCenterXPx = fabStartX + centerFabSizePx / 2f
    val fabCenterYPx = fabStartY + centerFabSizePx / 2f

    RadialAddMenu(
        expanded = expanded,
        fabCenterXPx = fabCenterXPx,
        fabCenterYPx = fabCenterYPx,
        // The scrim is fully opaque (not translucent) - it reads as the screen
        // darkening to a near-black canvas while the dock and the menu stay lit.
        scrimColor = when {
            isAmoled -> TrueBlack
            isLight -> Color(0xFF15171E)
            else -> Color(0xFF08090D)
        },
        // The menu lives on a dark canvas, so the accent uses the night shade -
        // it follows the user's accent pick in every Look and mode.
        accentColor = AccentFamilies.primaryColor(
            accent = ivyContext.accent,
            isLight = false
        ),
        incomeColor = UI.colors.green,
        expenseColor = UI.colors.red,
        onDismiss = { expanded = false },
        onIncome = {
            expanded = false
            onAddIncome()
        },
        onExpense = {
            expanded = false
            onAddExpense()
        },
        onTransfer = {
            expanded = false
            onAddTransfer()
        },
        onPlanned = {
            expanded = false
            onAddPlannedPayment()
        }
    )

    // ------------------------------------ + & x button -------------------------------------------
    var dragOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    // + morphs into x with a bouncy rotation + scale pop.
    val fabRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 420f
        ),
        label = "fab_morph"
    )

    val fabScale by animateFloatAsState(
        targetValue = if (expanded) 1.06f else 1f,
        animationSpec = springBounceFast(),
        label = "fab_pop"
    )

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
            .scale(fabScale)
            .rotate(fabRotation)
            .zIndex(200f)
            .thenIf(tab != MainTab.ACCOUNTS && overrideAddAction == null) {
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
                                    expanded = false
                                    onAddExpense()
                                }

                                dragOffset.x < -horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up left
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    expanded = false
                                    onAddIncome()
                                }

                                dragOffset.x > horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up right
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    expanded = false
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
            MainTab.ACCOUNTS -> GradientGreen
            else -> if (!expanded) {
                // Accent-driven: the + follows the user's accent pick.
                Gradient.solid(UI.colors.primary)
            } else {
                // The x reads as a bright close control on the dark canvas.
                Gradient.solid(White)
            }
        },
        hasShadow = !expanded,
        tint = when (tab) {
            MainTab.ACCOUNTS -> White
            else -> if (!expanded) White else Black
        }
    ) {
        val override = overrideAddAction
        if (override != null) {
            override()
        } else {
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
}

/**
 * The radial add menu: an opaque full-screen scrim, three sector wedges fanning
 * out of the + button and the Planned Payment capsule riding above the fan.
 *
 * Layering (z-order): scrim 100 -> wedge canvas 105 -> dock 110 ->
 * wedge labels 155 -> planned capsule 160 -> + button 200.
 * The canvas sits below the dock so the dock stays fully interactive while
 * the menu is open (tapping a tab collapses the menu and switches instantly).
 */
@Composable
private fun BoxWithConstraintsScope.RadialAddMenu(
    expanded: Boolean,
    fabCenterXPx: Float,
    fabCenterYPx: Float,
    scrimColor: Color,
    accentColor: Color,
    incomeColor: Color,
    expenseColor: Color,
    onDismiss: () -> Unit,
    onIncome: () -> Unit,
    onExpense: () -> Unit,
    onTransfer: () -> Unit,
    onPlanned: () -> Unit,
) {
    // The wedges fan out of the + with overshoot; collapsing folds them back in.
    val fan by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 215f
        ),
        label = "radial_fan"
    )

    val scrimAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.95f,
            stiffness = 460f
        ),
        label = "radial_scrim"
    )

    // The capsule lags the fan slightly for a choreographed entrance.
    val plannedProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 185f
        ),
        label = "radial_planned"
    )

    if (fan <= 0.004f && scrimAlpha <= 0.004f) return

    val isLight = UI.colors.isLight

    // 1) Opaque full-screen scrim - tap anywhere to close.
    if (scrimAlpha > 0.004f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .graphicsLayer { alpha = scrimAlpha }
                .background(scrimColor)
                .clickableNoIndication(rememberInteractionSource()) {
                    onDismiss()
                }
                .testTag("radial_scrim")
        )
    }

    if (fan <= 0.004f) return

    val ringRadiusPx = densityScope { RING_RADIUS.toDensityPx() }
    val bandWidthPx = densityScope { BAND_WIDTH.toDensityPx() }
    val startRingPx = densityScope { (CENTER_FAB_SIZE / 2f + 6.dp).toDensityPx() }
    val capsuleTravelPx = densityScope { (RING_RADIUS + BAND_WIDTH / 2f + 34.dp).toDensityPx() }

    // The ring grows out of the + while the fan spreads.
    val ringPx = lerp(startRingPx, ringRadiusPx, fan)
    val bandAlpha = fan.coerceIn(0f, 1f)
    val contentAlpha = (fan * 1.6f).coerceAtMost(1f)
    val contentScale = 0.7f + 0.3f * fan

    // 2) Wedge sectors - crisp annular sectors with hairline gaps.
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(105f)
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    val dx = pos.x - fabCenterXPx
                    val dy = pos.y - fabCenterYPx
                    val dist = hypot(dx, dy)
                    val innerPx = ringRadiusPx - bandWidthPx / 2f
                    val outerPx = ringRadiusPx + bandWidthPx / 2f

                    if (dist < innerPx - 16f || dist > outerPx + 16f) {
                        onDismiss()
                        return@detectTapGestures
                    }

                    var angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
                    if (angle < 0f) angle += 360f

                    val halfSweep = WEDGE_SWEEP_DEG / 2f + 2f
                    when {
                        abs(angle - INCOME_CENTER_DEG) <= halfSweep -> onIncome()
                        abs(angle - EXPENSE_CENTER_DEG) <= halfSweep -> onExpense()
                        abs(angle - TRANSFER_CENTER_DEG) <= halfSweep -> onTransfer()
                        else -> onDismiss()
                    }
                }
            }
            .testTag("radial_menu")
    ) {
        val wedges = listOf(
            INCOME_CENTER_DEG to incomeColor,
            EXPENSE_CENTER_DEG to expenseColor,
            TRANSFER_CENTER_DEG to accentColor
        )

        wedges.forEach { (centerDeg, color) ->
            val animatedCenter = lerp(EXPENSE_CENTER_DEG, centerDeg, fan)
            val sweep = (WEDGE_SWEEP_DEG * fan).coerceAtLeast(0.5f)
            val innerR = ringPx - bandWidthPx / 2f
            val outerR = ringPx + bandWidthPx / 2f

            val sector = Path()
            sector.arcTo(
                rect = Rect(
                    left = fabCenterXPx - outerR,
                    top = fabCenterYPx - outerR,
                    right = fabCenterXPx + outerR,
                    bottom = fabCenterYPx + outerR
                ),
                startAngleDegrees = animatedCenter - sweep / 2f,
                sweepAngleDegrees = sweep,
                forceMoveTo = true
            )
            sector.arcTo(
                rect = Rect(
                    left = fabCenterXPx - innerR,
                    top = fabCenterYPx - innerR,
                    right = fabCenterXPx + innerR,
                    bottom = fabCenterYPx + innerR
                ),
                startAngleDegrees = animatedCenter + sweep / 2f,
                sweepAngleDegrees = -sweep,
                forceMoveTo = false
            )
            sector.close()

            drawPath(
                path = sector,
                color = color.copy(alpha = bandAlpha)
            )
        }
    }

    // 3) Wedge icon + label overlays riding the animated polar positions.
    WedgeContent(
        icon = R.drawable.ic_income,
        label = stringResource(R.string.income),
        alphaFactor = contentAlpha,
        scaleFactor = contentScale,
        angleDeg = lerp(EXPENSE_CENTER_DEG, INCOME_CENTER_DEG, fan),
        radiusPx = ringPx,
        centerX = fabCenterXPx,
        centerY = fabCenterYPx,
        testTag = "radial_income"
    )

    WedgeContent(
        icon = R.drawable.ic_expense,
        label = stringResource(R.string.expense),
        alphaFactor = contentAlpha,
        scaleFactor = contentScale,
        angleDeg = lerp(EXPENSE_CENTER_DEG, EXPENSE_CENTER_DEG, fan),
        radiusPx = ringPx,
        centerX = fabCenterXPx,
        centerY = fabCenterYPx,
        testTag = "radial_expense"
    )

    WedgeContent(
        icon = R.drawable.ic_transfer,
        label = stringResource(R.string.transfer),
        alphaFactor = contentAlpha,
        scaleFactor = contentScale,
        angleDeg = lerp(EXPENSE_CENTER_DEG, TRANSFER_CENTER_DEG, fan),
        radiusPx = ringPx,
        centerX = fabCenterXPx,
        centerY = fabCenterYPx,
        testTag = "radial_transfer"
    )

    // 4) Planned Payment capsule above the fan.
    PlannedPaymentCapsule(
        progress = plannedProgress,
        centerX = fabCenterXPx,
        fromY = fabCenterYPx - startRingPx,
        toY = fabCenterYPx - capsuleTravelPx,
        accentColor = accentColor,
        isLight = isLight,
        onClick = onPlanned
    )
}

@Composable
private fun BoxWithConstraintsScope.WedgeContent(
    @DrawableRes icon: Int,
    label: String,
    alphaFactor: Float,
    scaleFactor: Float,
    angleDeg: Float,
    radiusPx: Float,
    centerX: Float,
    centerY: Float,
    testTag: String,
) {
    if (alphaFactor <= 0.01f) return

    val radians = Math.toRadians(angleDeg.toDouble())
    val x = centerX + radiusPx * cos(radians).toFloat()
    val y = centerY + radiusPx * sin(radians).toFloat()

    Column(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = (x - placeable.width / 2f).roundToInt(),
                        y = (y - placeable.height / 2f).roundToInt()
                    )
                }
            }
            .graphicsLayer {
                this.alpha = alphaFactor
                this.scaleX = scaleFactor
                this.scaleY = scaleFactor
            }
            .width(78.dp)
            .zIndex(155f)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IvyIcon(
            modifier = Modifier.size(22.dp),
            icon = icon,
            tint = White
        )

        Spacer(Modifier.height(3.dp))

        Text(
            text = label,
            style = UI.typo.c.style(
                color = White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.PlannedPaymentCapsule(
    progress: Float,
    centerX: Float,
    fromY: Float,
    toY: Float,
    accentColor: Color,
    isLight: Boolean,
    onClick: () -> Unit,
) {
    if (progress <= 0.004f) return

    Row(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = (centerX - placeable.width / 2f).roundToInt(),
                        y = lerp(fromY, toY, progress).roundToInt()
                    )
                }
            }
            .graphicsLayer {
                this.alpha = progress.coerceIn(0f, 1f)
                this.scaleX = 0.78f + 0.22f * progress
                this.scaleY = 0.78f + 0.22f * progress
            }
            .zIndex(160f)
            .clip(RoundedCornerShape(percent = 50))
            .background(accentColor.copy(alpha = if (isLight) 0.16f else 0.26f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = if (isLight) 0.42f else 0.55f),
                shape = RoundedCornerShape(percent = 50)
            )
            .clickableNoIndication(rememberInteractionSource()) {
                onClick()
            }
            .testTag("radial_planned")
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IvyIcon(
            modifier = Modifier.size(18.dp),
            icon = R.drawable.ic_planned_payments,
            tint = accentColor
        )

        Spacer(Modifier.width(7.dp))

        Text(
            text = stringResource(R.string.add_planned_payment),
            style = UI.typo.b2.style(
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1
        )
    }
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
