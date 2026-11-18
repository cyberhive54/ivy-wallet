package com.ivy.main.workspace

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter

/**
 * Ivy Expand design contract (see DESIGN.md).
 *
 * Quality pass: one accent used once, semantic colour only for money direction,
 * hierarchy through scale and spacing rather than weight, and no decorative
 * motion. Feedback is a quiet surface change, never a scale bounce.
 */
object ExpandDesign {
    val Accent = Color(0xFF5E5CE6)
    val Receive = Color(0xFF30D158)
    val Pay = Color(0xFFFF9F0A)
    val Danger = Color(0xFFFF6B5B)

    val CardRadius = 18.dp
    val Hairline = 1.dp

    /** Critically damped spring for state transitions only (sheets, expansions). */
    fun <T> spring(visibilityThreshold: T? = null) = spring<T>(
        dampingRatio = 1.0f,
        stiffness = 320f,
        visibilityThreshold = visibilityThreshold,
    )
}

/** Neutral text roles — secondary text is readable, not decorative gray. */
private val TextPrimary: Color @Composable get() = UI.colors.pureInverse
private val TextSecondary: Color @Composable get() = UI.colors.pureInverse.copy(alpha = 0.72f)
private val Surface: Color @Composable get() = UI.colors.pureInverse.copy(alpha = 0.04f)
private val HairlineColor: Color @Composable get() = UI.colors.pureInverse.copy(alpha = 0.10f)

/** A quiet surface for grouped content. No press animation. */
@Composable
fun ExpandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ExpandDesign.CardRadius))
            .background(Surface, RoundedCornerShape(ExpandDesign.CardRadius))
            .border(ExpandDesign.Hairline, HairlineColor, RoundedCornerShape(ExpandDesign.CardRadius))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

/** Section heading, sentence case, no exaggerated tracking. */
@Composable
fun ExpandSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = ivyInter(13.sp, FontWeight.Medium),
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = ivyInter(13.sp, FontWeight.Medium),
                color = TextPrimary,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

/** Primary action: filled only for the one main action on a screen. */
@Composable
fun ExpandPillAction(
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = ExpandDesign.Accent,
    filled: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val background = when {
        !enabled -> Surface
        filled -> accent
        else -> Surface
    }
    Text(
        text = label,
        style = ivyInter(13.sp, FontWeight.Medium),
        color = when {
            !enabled -> TextSecondary
            filled -> Color.White
            else -> TextPrimary
        },
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background, RoundedCornerShape(50))
            .border(ExpandDesign.Hairline, HairlineColor, RoundedCornerShape(50))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}

/**
 * One authoritative balance band. Two figures remain because Splitly/Ledger
 * genuinely track both directions, but they are no longer competing hero numerals.
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
            .background(Surface, RoundedCornerShape(ExpandDesign.CardRadius))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BalanceCell(receiveLabel, receiveAmount, currency, ExpandDesign.Receive)
        Box(
            Modifier
                .width(1.dp)
                .height(40.dp)
                .background(HairlineColor)
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
        Text(text = label, style = ivyInter(12.sp), color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (currency.isEmpty()) "₹${"%,.2f".format(amount)}" else "$currency ${"%,.2f".format(amount)}",
            style = ivyInter(20.sp, FontWeight.SemiBold),
            color = if (amount > 0.0) accent else TextSecondary,
        )
    }
}

/** Empty state with a single quiet affordance. */
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
                .size(52.dp)
                .clip(RoundedCornerShape(50))
                .border(ExpandDesign.Hairline, HairlineColor, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = glyph, style = ivyInter(20.sp, FontWeight.Medium), color = TextSecondary)
        }
        Spacer(Modifier.height(18.dp))
        Text(text = title, style = ivyInter(17.sp, FontWeight.SemiBold), color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = ivyInter(14.sp),
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        ExpandPillAction(label = actionLabel, onClick = onAction)
    }
}
