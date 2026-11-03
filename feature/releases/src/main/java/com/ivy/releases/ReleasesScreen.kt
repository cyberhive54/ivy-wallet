package com.ivy.releases

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.design.l0_system.UI
import com.ivy.legacy.Constants
import com.ivy.legacy.cupertino.CupertinoChildPage
import com.ivy.legacy.cupertino.CupertinoDockClearance
import com.ivy.legacy.cupertino.CupertinoRowsCard
import com.ivy.legacy.cupertino.CupertinoSectionHeader
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter

@Composable
fun BoxWithConstraintsScope.ReleasesScreenImpl(
    onClose: (() -> Unit)? = null,
) {
    val viewModel: ReleasesViewModel = viewModel()
    val uiState = viewModel.uiState()

    ReleasesUi(
        uiState = uiState,
        onEvent = { viewModel.onEvent(it) },
        onClose = onClose ?: {}
    )
}

@Composable
private fun BoxWithConstraintsScope.ReleasesUi(
    uiState: ReleasesState,
    onEvent: (ReleasesEvent) -> Unit,
    onClose: () -> Unit,
) {
    val browser = LocalUriHandler.current

    CupertinoChildPage(
        title = stringResource(R.string.releases),
        onClose = onClose,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "github_repo") {
                CupertinoRowsCard(
                    rows = listOf(
                        {
                            CupertinoRepoRow(onClick = {
                                browser.openUri(Constants.URL_IVY_WALLET_REPO)
                            })
                        }
                    )
                )
            }

            when (uiState) {
                is ReleasesState.Error -> {
                    item(key = "error") {
                        ReleasesErrorState(
                            message = uiState.errorMessage,
                            onClick = { onEvent(ReleasesEvent.OnTryAgainClick) }
                        )
                    }
                }

                is ReleasesState.Loading -> {
                    item(key = "loading") {
                        CupertinoSectionHeader(text = uiState.loadingMessage)
                    }
                }

                is ReleasesState.Success -> {
                    items(
                        items = uiState.releasesInfo
                    ) { releaseInfo ->
                        ReleaseCard(
                            releaseInfo = releaseInfo,
                            onClick = { browser.openUri(releaseInfo.releaseUrl) }
                        )
                    }
                }
            }

            item(key = "dock_clearance") {
                Spacer(Modifier.height(CupertinoDockClearance))
            }
        }
    }
}

@Composable
private fun CupertinoRepoRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ivy-Apps/ivy-wallet",
            style = ivyInter(16.sp, FontWeight.Medium),
            color = UI.colors.pureInverse,
            modifier = Modifier.weight(1f)
        )

        ExternalArrow()
    }
}

@Composable
private fun ReleaseCard(
    releaseInfo: ReleaseInfo,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = releaseInfo.releaseName,
                style = ivyInter(16.sp, FontWeight.SemiBold),
                color = UI.colors.pureInverse,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = releaseInfo.releaseDate,
                style = ivyInter(12.sp),
                color = UI.colors.gray
            )

            Spacer(Modifier.width(10.dp))

            ExternalArrow()
        }

        if (releaseInfo.releaseCommits.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp)
            ) {
                releaseInfo.releaseCommits.take(6).forEach { commit ->
                    Text(
                        text = "• $commit",
                        style = ivyInter(13.sp),
                        color = UI.colors.gray,
                        maxLines = 1
                    )
                }
            }
        } else {
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun ExternalArrow() {
    Icon(
        painter = painterResource(R.drawable.ic_arrow_up_right),
        contentDescription = stringResource(R.string.opens_externally),
        tint = UI.colors.gray,
        modifier = Modifier.height(18.dp)
    )
}

@Composable
private fun ReleasesErrorState(
    message: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = ivyInter(15.sp, FontWeight.Medium),
            color = UI.colors.red,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(UI.colors.pureInverse)
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.try_again),
                style = ivyInter(15.sp, FontWeight.SemiBold),
                color = UI.colors.pure
            )
        }
    }
}
