package com.ivy.importdata.csvimport.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.cupertino.cupertinoHairline
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter

@Composable
fun ImportProcessing(
    progressPercent: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        Text(
            text = stringResource(R.string.please_wait),
            style = ivyInter(28.sp, FontWeight.Bold),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "$progressPercent%",
            style = ivyInter(15.sp, FontWeight.SemiBold),
            color = UI.colors.gray
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.importing_the_csv_file),
            style = ivyInter(15.sp, FontWeight.Medium),
            color = UI.colors.pureInverse
        )

        Spacer(Modifier.height(16.dp))

        ProgressBar(
            progressPercent = progressPercent
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ProgressBar(
    progressPercent: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .padding(horizontal = 24.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(100))
            .background(UI.colors.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (progressPercent > 0) {
            Spacer(
                modifier = Modifier
                    .weight(progressPercent.toFloat())
                    .height(8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(100))
                    .background(UI.colors.pureInverse),
            )
        }

        val uncompletedPercent = 100 - progressPercent
        if (uncompletedPercent > 0) {
            Spacer(
                modifier = Modifier
                    .weight(uncompletedPercent.toFloat())
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    IvyWalletPreview {
        ImportProcessing(
            progressPercent = 49
        )
    }
}
