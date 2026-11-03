package com.ivy.importdata.csvimport.flow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.legacy.cupertino.CupertinoChildPage
import com.ivy.legacy.cupertino.CupertinoDockClearance
import com.ivy.legacy.cupertino.CupertinoNavRow
import com.ivy.legacy.cupertino.CupertinoRowsCard
import com.ivy.legacy.cupertino.CupertinoSectionHeader
import com.ivy.legacy.domain.deprecated.logic.csv.model.ImportType
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.theme.components.IvyIcon

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ImportFrom(
    hasSkip: Boolean,
    launchedFromOnboarding: Boolean,

    onBack: () -> Unit = {},
    onOpenCsv: (() -> Unit)? = null,
    onSkip: () -> Unit = {},
    onImportFrom: (ImportType) -> Unit = {},
) {
    val importTypes = ImportType.values()

    CupertinoChildPage(
        title = stringResource(R.string.import_data),
        onClose = onBack,
        backLabel = if (launchedFromOnboarding) {
            stringResource(R.string.back)
        } else {
            stringResource(R.string.settings)
        },
        onSkip = if (hasSkip) onSkip else null,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "manual_csv") {
                CupertinoRowsCard(
                    rows = listOf(
                        {
                            CupertinoNavRow(
                                icon = R.drawable.ic_export_csv,
                                title = stringResource(R.string.manual_csv_import),
                                external = false,
                                onClick = {
                                    onOpenCsv?.invoke()
                                }
                            )
                        }
                    )
                )
            }

            item(key = "sources_header") {
                CupertinoSectionHeader(
                    text = stringResource(R.string.import_from)
                )
            }

            items(importTypes) { importType ->
                ImportOption(
                    importType = importType,
                    onImportFrom = onImportFrom
                )
            }

            item(key = "dock_clearance") {
                Spacer(Modifier.height(CupertinoDockClearance))
            }
        }
    }
}

@Composable
private fun ImportOption(
    importType: ImportType,
    onImportFrom: (ImportType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .clickable { onImportFrom(importType) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IvyIcon(
            modifier = Modifier.size(32.dp),
            icon = importType.logo(),
            tint = Color.Unspecified
        )

        Spacer(Modifier.width(14.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = importType.listName(),
            style = ivyInter(17.sp, FontWeight.Medium),
            color = UI.colors.pureInverse
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = UI.colors.gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
