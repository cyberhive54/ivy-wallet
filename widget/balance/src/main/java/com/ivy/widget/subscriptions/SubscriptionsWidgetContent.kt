package com.ivy.widget.subscriptions

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.R

data class WidgetRenewal(
    val id: String,
    val title: String,
    val amount: String,
    val daysLabel: String,
)

private val WHITE = ColorProvider(androidx.compose.ui.graphics.Color.White)
private val OFF_WHITE = ColorProvider(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.66f))
private val AMBER = ColorProvider(androidx.compose.ui.graphics.Color(0xFFFFD60A))

@Composable
fun SubscriptionsWidgetContent(
    totalCount: Int,
    currency: String,
    items: List<WidgetRenewal>,
    onWidgetClick: Action,
    onRowClick: (String) -> Action,
) {
    Box(
        GlanceModifier
            .background(ImageProvider(R.drawable.shape_widget_background))
            .clickable(onWidgetClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header: title + count
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUBSCRIPTIONS",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OFF_WHITE,
                    )
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = "· $totalCount active",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = WHITE,
                    )
                )
            }

            Spacer(GlanceModifier.height(6.dp))

            if (items.isEmpty()) {
                Text(
                    text = "No upcoming renewals",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = OFF_WHITE,
                    )
                )
            } else {
                items.forEachIndexed { index, item ->
                    val rowModifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(onRowClick(item.id))
                        .padding(vertical = 5.dp)
                    Row(
                        modifier = rowModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.daysLabel,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AMBER
                            ),
                            modifier = GlanceModifier.width(58.dp)
                        )
                        Text(
                            text = item.title,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = WHITE
                            ),
                            modifier = GlanceModifier.defaultWeight(),
                            maxLines = 1
                        )
                        Text(
                            text = item.amount,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WHITE
                            )
                        )
                    }
                    if (index < items.lastIndex) {
                        Box(
                            GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(OFF_WHITE)
                        ) {}
                    }
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            Text(
                text = "Tap to open Ivy Expand",
                style = TextStyle(
                    fontSize = 9.sp,
                    color = OFF_WHITE
                )
            )
        }
    }
}
