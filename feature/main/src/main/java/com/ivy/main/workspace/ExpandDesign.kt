package com.ivy.main.workspace

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter

/**
 * Ivy Expand design tokens (see DESIGN.md).
 *
 * One accent, one green (receivable), one amber (payable), one red (destructive).
 * Surfaces are hairline-bordered, not shadow-stacked. Motion is critically damped
 * springs; the only bounce is on gesture-driven dismissal.
 */
object ExpandDesign {
    val Accent = Color(0xFF5E5CE6)
    val AccentSoft = Color(0xFF7C7AF0)
    val Receive = Color(0xFF30D158)
    val Pay = Color(0xFFFF9F0A)
    val Danger = Color(0xFFFF6B5B)

    val CardRadius = 18.dp
    val PillRadius = percent50
    val Hairline = 1.dp

    /** Spring used for sheets / expansions — no overshoot. */
    fun <T> spring(visibilityThreshold: T? = null) = spring<T>(
        dampingRatio = 1.0f,
        stiffness = 320f,
        visibilityThreshold = visibilityThreshold,
    )
}

private val percent50 = 50

/** A calm, hairline-bordered surface for grouped content. */
@Composable
fun ExpandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.985f else 1f,
        animationSpec = ExpandDesign.spring(),
        label = "card_press",
    )
    val border by animateColorAsState(
        targetValue = UI.colors.pureInverse.copy(alpha = if (pressed && onClick != null) 0.16f else 0.08f),
        animationSpec = ExpandDesign.spring(),
        label = "card_border",
    )
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(ExpandDesign.CardRadius))
            .background(UI.colors.pureInverse.copy(alpha = 0.035f), RoundedCornerShape(ExpandDesign.CardRadius))
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
                } else Modifier
            )
            .border(ExpandDesign.Hairline, border, RoundedCornerShape(ExpandDesign.CardRadius))
            .padding(16.dp),
        content = content,
    )
}

/** Section heading — more space above than below (Apple spacing rhythm). */
@Composable
fun ExpandSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = ivyInter(12.sp, FontWeight.SemiBold, letterSpacing = 0.7.sp),
            color = UI.colors.gray,
            modifier = Modifier.weight(1f),
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = ivyInter(12.sp, FontWeight.SemiBold),
                color = ExpandDesign.Accent,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

/** Pill action button with press feedback on touch-down. */
@Composable
fun ExpandPillAction(
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = ExpandDesign.Accent,
    filled: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = ExpandDesign.spring(),
        label = "pill_press",
    )
    Text(
        text = label,
        style = ivyInter(13.sp, FontWeight.SemiBold),
        color = if (filled) Color.White else accent,
        textAlign = TextAlign.Center,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(
                if (filled) {
                    if (enabled) accent else accent.copy(alpha = 0.35f)
                } else {
                    accent.copy(alpha = 0.10f)
                },
                RoundedCornerShape(50)
            )
            .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}

/**
 * The single authoritative balance band. Positive/negative carry semantic color;
 * the numerals read as tabular through plain formatting.
 */
@Composable
fun ExpandBalanceBand(
    receiveLabel: String,
    receiveAmount: Double,
    payLabel: String,
    payAmount: Double,
    currency: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ExpandDesign.CardRadius))
            .background(UI.colors.pureInverse.copy(alpha = 0.04f), RoundedCornerShape(ExpandDesign.CardRadius))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BalanceCell(receiveLabel, receiveAmount, currency, ExpandDesign.Receive)
        Box(
            Modifier
                .width(1.dp)
                .height(44.dp)
                .background(UI.colors.pureInverse.copy(alpha = 0.08f))
        )
        BalanceCell(payLabel, payAmount, currency, ExpandDesign.Pay, alignEnd = true)
    }
}

@Composable
private fun BalanceCell(
    label: String,
    amount: Double,
    currency: String,
    accent: Color,
    alignEnd: Boolean = false,
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(text = label, style = ivyInter(12.sp, FontWeight.Medium), color = UI.colors.gray)
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (currency.isEmpty()) "₹${"%,.2f".format(amount)}" else "$currency ${"%,.2f".format(amount)}",
            style = ivyInter(22.sp, FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
            color = if (amount > 0.0) accent else UI.colors.pureInverse.copy(alpha = 0.5f),
        )
    }
}

/** Empty state with an affordance — never a bare sentence. */
@Composable
fun ExpandEmptyState(
    glyph: String,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ExpandDesign.Accent.copy(alpha = 0.12f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = glyph, style = ivyInter(24.sp, FontWeight.Bold), color = ExpandDesign.Accent)
        }
        Spacer(Modifier.height(16.dp))
        Text(text = title, style = ivyInter(17.sp, FontWeight.Bold), color = UI.colors.pureInverse)
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = ivyInter(14.sp),
            color = UI.colors.gray,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        ExpandPillAction(label = actionLabel, onClick = onAction)
    }
}
