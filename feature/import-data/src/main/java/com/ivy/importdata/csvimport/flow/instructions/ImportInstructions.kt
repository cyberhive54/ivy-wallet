package com.ivy.importdata.csvimport.flow.instructions

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.importdata.csvimport.flow.ImportSteps
import com.ivy.legacy.cupertino.CupertinoChildPage
import com.ivy.legacy.cupertino.CupertinoDockClearance
import com.ivy.legacy.cupertino.CupertinoFilledButton
import com.ivy.legacy.domain.deprecated.logic.csv.model.ImportType
import com.ivy.legacy.utils.drawColoredShadow
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.theme.components.IvyIcon

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ImportInstructions(
    hasSkip: Boolean,
    importType: ImportType,

    onBack: () -> Unit = {},
    onSkip: () -> Unit,
    onUploadClick: () -> Unit,
) {
    CupertinoChildPage(
        title = importType.listName(),
        subtitle = stringResource(R.string.how_to_import),
        onClose = onBack,
        backLabel = if (hasSkip) {
            stringResource(R.string.back)
        } else {
            stringResource(R.string.settings)
        },
        onSkip = if (hasSkip) onSkip else null,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "app_row") {
                App(importType = importType)
            }

            item(key = "steps_header") {
                Text(
                    modifier = Modifier.padding(start = 32.dp, top = 16.dp, bottom = 8.dp),
                    text = stringResource(R.string.steps),
                    style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
                    color = UI.colors.gray
                )
            }

            item(key = "steps") {
                importType.ImportSteps(onUploadClick = onUploadClick)
            }

            item(key = "dock_clearance") {
                Spacer(Modifier.height(CupertinoDockClearance))
            }
        }
    }
}

@Composable
fun VideoArticleRow(
    videoUrl: String?,
    articleUrl: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
    ) {
        if (videoUrl != null) {
            VideoArticleAction(
                icon = R.drawable.ic_import_video,
                title = stringResource(R.string.video),
                subtitle = stringResource(R.string.how_to),
                external = true,
                onClick = {
                    val rootScreen = com.ivy.legacy.rootScreen()
                    rootScreen.openUrlInBrowser(videoUrl)
                }
            )
        }

        if (videoUrl != null && articleUrl != null) {
            CupertinoSheetSeparator()
        }

        if (articleUrl != null) {
            VideoArticleAction(
                icon = R.drawable.ic_import_web,
                title = stringResource(R.string.article),
                subtitle = stringResource(R.string.how_to),
                external = true,
                onClick = {
                    val rootScreen = com.ivy.legacy.rootScreen()
                    rootScreen.openUrlInBrowser(articleUrl)
                }
            )
        }
    }

    Spacer(Modifier.height(16.dp))
}

@Composable
private fun VideoArticleAction(
    @DrawableRes icon: Int?,
    title: String,
    subtitle: String,
    external: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            IvyIcon(
                modifier = Modifier
                    .background(UI.colors.pure, CircleShape)
                    .size(28.dp),
                icon = icon,
                tint = Color.Unspecified
            )

            Spacer(Modifier.width(14.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = ivyInter(16.sp, FontWeight.Medium),
                color = UI.colors.pureInverse
            )

            Text(
                text = subtitle,
                style = ivyInter(12.sp),
                color = UI.colors.gray
            )
        }

        Icon(
            painter = painterResource(
                if (external) {
                    R.drawable.ic_arrow_up_right
                } else {
                    R.drawable.ic_arrow_right
                }
            ),
            contentDescription = stringResource(R.string.opens_externally),
            tint = UI.colors.gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Deprecated(
    "Replaced by VideoArticleAction. Kept for compatibility with per-source step lists."
)
@Composable
fun InstructionButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int?,
    caption: String,
    text: String,

    onClick: () -> Unit
) {
    VideoArticleAction(
        icon = icon,
        title = text,
        subtitle = caption,
        onClick = onClick
    )
}

@Composable
fun UploadFileStep(
    stepNumber: Int,
    text: String = stringResource(R.string.upload_csv_file),
    btnTitle: String = text,
    onUploadClick: () -> Unit
) {
    StepTitle(
        number = stepNumber,
        title = text
    )

    Spacer(Modifier.height(16.dp))

    CupertinoFilledButton(
        label = btnTitle,
        onClick = onUploadClick,
        modifier = Modifier.padding(horizontal = 20.dp)
    )

    Spacer(Modifier.height(16.dp))
}

@Suppress("MultipleEmitters")
@Composable
fun StepTitle(
    number: Int,
    title: String,
    description: String? = null,
) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .size(24.dp)
                .background(UI.colors.pureInverse, CircleShape),
            text = number.toString(),
            style = ivyInter(12.sp, FontWeight.SemiBold),
            color = UI.colors.pure,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.width(10.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = ivyInter(15.sp, FontWeight.SemiBold),
            color = UI.colors.pureInverse
        )
    }

    if (description != null) {
        Spacer(Modifier.height(4.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = description,
            style = ivyInter(12.sp),
            color = UI.colors.gray
        )
    }

    Spacer(Modifier.height(10.dp))
}

@Composable
private fun App(
    importType: ImportType
) {
    val rootScreen = com.ivy.legacy.rootScreen()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .clickable {
                rootScreen.openGooglePlayAppPage(
                    appId = importType.appId()
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IvyIcon(
            modifier = Modifier
                .drawColoredShadow(importType.color())
                .size(48.dp),
            icon = importType.logo(),
            tint = Color.Unspecified
        )

        Spacer(Modifier.width(14.dp))

        Text(
            text = importType.appName(),
            style = ivyInter(17.sp, FontWeight.Medium),
            color = UI.colors.pureInverse,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_up_right),
            contentDescription = stringResource(R.string.opens_externally),
            tint = UI.colors.gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
