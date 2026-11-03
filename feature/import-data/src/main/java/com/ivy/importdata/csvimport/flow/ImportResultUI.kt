package com.ivy.importdata.csvimport.flow

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.backup.ImportResult
import com.ivy.design.l0_system.UI
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.cupertino.CupertinoBackRow
import com.ivy.legacy.cupertino.CupertinoFilledButton
import com.ivy.legacy.cupertino.CupertinoNavRow
import com.ivy.legacy.cupertino.CupertinoRowsCard
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import kotlinx.collections.immutable.persistentListOf

@SuppressLint("ComposeModifierMissing")
@Composable
fun BoxWithConstraintsScope.ImportResultUI(
    result: ImportResult,
    launchedFromOnboarding: Boolean,
    isManualCsvImport: Boolean = false,
    onTryAgain: (() -> Unit)? = null,
    onBack: () -> Unit = {},
    onOpenCsv: (() -> Unit)? = null,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UI.colors.pure)
            .systemBarsPadding()
    ) {
        CupertinoBackRow(onClose = onBack)

        Spacer(Modifier.height(8.dp))

        val importSuccess = result.transactionsImported > 0 &&
                result.transactionsImported > result.rowsFound / 2
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = if (importSuccess) stringResource(R.string.success) else stringResource(R.string.failure),
            style = ivyInter(34.sp, FontWeight.Bold, letterSpacing = (-0.5).sp),
            color = if (importSuccess) UI.colors.pureInverse else Red
        )

        Spacer(Modifier.height(16.dp))

        val successPercent = if (result.rowsFound > 0) {
            (result.transactionsImported / result.rowsFound.toDouble()) * 100
        } else {
            0.0
        }

        SuccessSectionUI(
            result = result,
            successPercent = successPercent,
        )

        FailedSectionUI(
            result = result,
            successPercent = successPercent,
        )

        if (!isManualCsvImport) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                text = stringResource(R.string.csv_import_failed),
                style = ivyInter(13.sp),
                color = UI.colors.gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            CupertinoRowsCard(
                rows = listOf(
                    {
                        CupertinoNavRow(
                            icon = null,
                            title = stringResource(R.string.manual_csv_import),
                            onClick = {
                                onOpenCsv?.invoke()
                            }
                        )
                    }
                )
            )
        }

        Spacer(Modifier.weight(1f))

        CupertinoFilledButton(
            label = stringResource(R.string.finish),
            enabled = true,
            onClick = onFinish,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        if (onTryAgain != null) {
            Spacer(Modifier.height(12.dp))

            CupertinoFilledButton(
                label = stringResource(R.string.try_again),
                enabled = true,
                onClick = onTryAgain,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SuccessSectionUI(
    result: ImportResult,
    successPercent: Double,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.imported),
            style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
            color = Green
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${successPercent.format(2)}%",
            style = ivyInter(28.sp, FontWeight.Bold),
            color = UI.colors.pureInverse
        )

        Text(
            text = stringResource(R.string.transactions_imported, result.transactionsImported),
            style = ivyInter(13.sp, FontWeight.Medium),
            color = Gray
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.accounts_imported, result.accountsImported),
            style = ivyInter(13.sp, FontWeight.Medium),
            color = Gray
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.categories_imported, result.categoriesImported),
            style = ivyInter(13.sp, FontWeight.Medium),
            color = Gray
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FailedSectionUI(
    result: ImportResult,
    successPercent: Double,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.failed),
            style = ivyInter(13.sp, FontWeight.SemiBold, letterSpacing = 0.6.sp),
            color = Red
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${(100 - successPercent).format(2)}%",
            style = ivyInter(28.sp, FontWeight.Bold),
            color = UI.colors.pureInverse
        )

        Text(
            text = stringResource(
                R.string.rows_from_csv_not_recognized,
                result.rowsFound - result.transactionsImported
            ),
            style = ivyInter(13.sp, FontWeight.Medium),
            color = Gray
        )
    }
}

@Preview(device = "id:pixel_3", showBackground = true, showSystemUi = true)
@Composable
private fun Preview() {
    IvyWalletPreview {
        ImportResultUI(
            result = ImportResult(
                rowsFound = 356,
                transactionsImported = 320,
                accountsImported = 4,
                categoriesImported = 13,
                failedRows = persistentListOf()
            ),
            launchedFromOnboarding = false,
        ) {
        }
    }
}
