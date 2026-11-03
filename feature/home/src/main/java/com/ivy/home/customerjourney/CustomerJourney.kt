package com.ivy.home.customerjourney

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.dynamicContrast
import com.ivy.domain.RootScreen
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.rootScreen
import com.ivy.navigation.IvyPreview
import com.ivy.navigation.navigation
import com.ivy.home.homeHairline
import com.ivy.home.homeInter
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CustomerJourney(
    customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,
    modifier: Modifier = Modifier,
    onDismiss: (CustomerJourneyCardModel) -> Unit,
) {
    val ivyContext = ivyWalletCtx()
    val nav = navigation()
    // Check is added for Paparazzi Test where context is different
    if (LocalContext.current is RootScreen) {
        val rootScreen = rootScreen()

        if (customerJourneyCards.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
        }

        for (card in customerJourneyCards) {
            Spacer(Modifier.height(10.dp))

            CustomerJourneyCard(
                modifier = modifier,
                cardData = card,
                onDismiss = {
                    onDismiss(card)
                }
            ) {
                card.onAction(nav, ivyContext, rootScreen)
            }
        }
    } else {
        Box(modifier)
    }
}

@Composable
fun CustomerJourneyCard(
    cardData: CustomerJourneyCardModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onCTA: () -> Unit,
) {
    // The card's signature color is kept, but demoted from full-bleed background to an
    // accent: tinted CTA pill on a token-driven Cupertino card.
    val accent = cardData.background.startColor.dynamicContrast()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .border(1.dp, homeHairline(), RoundedCornerShape(20.dp))
            .clickable {
                onCTA()
            }
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp, end = 8.dp),
                text = cardData.title,
                style = homeInter(16.sp, FontWeight.SemiBold, letterSpacing = (-0.2).sp),
                color = UI.colors.pureInverse,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (cardData.hasDismiss) {
                IvyIcon(
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                        }
                        .padding(8.dp), // enlarge click area
                    icon = R.drawable.ic_dismiss_close,
                    tint = UI.colors.gray,
                    contentDescription = "prompt_dismiss",
                )

                Spacer(Modifier.width(10.dp))
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 24.dp),
            text = cardData.description,
            style = homeInter(14.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(14.dp))

        if (cardData.cta != null) {
            Row(
                modifier = Modifier
                    .padding(start = 18.dp, end = 18.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(accent.copy(alpha = 0.14f))
                        .clickable {
                            onCTA()
                        }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .testTag("cta_prompt_${cardData.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IvyIcon(
                        icon = cardData.ctaIcon,
                        tint = accent,
                        modifier = Modifier.height(16.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = cardData.cta,
                        style = homeInter(14.sp, FontWeight.SemiBold),
                        color = accent
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCard() {
    IvyPreview {
        CustomerJourneyCard(
            cardData = CustomerJourneyCardsProvider.adjustBalanceCard(),
            onCTA = { },
            onDismiss = {}
        )
    }
}
