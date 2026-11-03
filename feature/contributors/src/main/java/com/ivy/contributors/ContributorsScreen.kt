package com.ivy.contributors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ivy.base.legacy.Theme
import com.ivy.design.l0_system.UI
import com.ivy.legacy.Constants
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.cupertino.CupertinoChildPage
import com.ivy.legacy.cupertino.CupertinoDockClearance
import com.ivy.legacy.cupertino.CupertinoNavRow
import com.ivy.legacy.cupertino.CupertinoRowsCard
import com.ivy.legacy.cupertino.CupertinoSectionHeader
import com.ivy.legacy.cupertino.cupertinoHairline
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import kotlinx.collections.immutable.persistentListOf

@Composable
fun BoxWithConstraintsScope.ContributorsScreenImpl(
    onClose: (() -> Unit)? = null,
    viewModel: ContributorsViewModel = screenScopedViewModel(),
) {
    val uiState = viewModel.uiState()

    ContributorsUi(
        uiState = uiState,
        onEvent = { viewModel.onEvent(it) },
        onClose = onClose ?: {}
    )
}

@Composable
private fun BoxWithConstraintsScope.ContributorsUi(
    uiState: ContributorsState,
    onEvent: (ContributorsEvent) -> Unit,
    onClose: () -> Unit,
) {
    val browser = LocalUriHandler.current

    val title = when (val response = uiState.contributorsResponse) {
        is ContributorsResponse.Success ->
            stringResource(R.string.contributors_number, response.contributors.size)

        else -> stringResource(R.string.project_contributors)
    }

    CupertinoChildPage(
        title = title,
        onClose = onClose,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            when (uiState.contributorsResponse) {
                is ContributorsResponse.Error -> item(key = "error") {
                    ContributorsErrorState(
                        message = uiState.contributorsResponse.errorMessage
                    ) {
                        onEvent(ContributorsEvent.TryAgainButtonClicked)
                    }
                }

                ContributorsResponse.Loading -> item(key = "loading") {
                    CupertinoSectionHeader(text = stringResource(R.string.loading))
                }

                is ContributorsResponse.Success -> {
                    if (uiState.projectResponse is ProjectResponse.Success) {
                        item(key = "project_info") {
                            ProjectInfoCard(
                                projectInfo = uiState.projectResponse.projectInfo,
                                onOpenFork = {
                                    browser.openUri("${Constants.URL_IVY_WALLET_REPO}/fork")
                                },
                                onOpenRepo = {
                                    browser.openUri(Constants.URL_IVY_WALLET_REPO)
                                }
                            )
                        }
                    }

                    items(
                        items = uiState.contributorsResponse.contributors
                    ) { contributor ->
                        ContributorRow(
                            contributor = contributor,
                            onClick = {
                                if (contributor.githubProfileUrl.isNotBlank()) {
                                    browser.openUri(contributor.githubProfileUrl)
                                }
                            }
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
private fun ProjectInfoCard(
    projectInfo: ProjectRepositoryInfo,
    onOpenFork: () -> Unit,
    onOpenRepo: () -> Unit,
) {
    CupertinoRowsCard(
        header = "GitHub",
        separatorStartIndent = 16.dp,
        rows = listOf(
            {
                CupertinoNavRow(
                    icon = null,
                    title = "${projectInfo.stars} stars",
                    external = true,
                    onClick = onOpenRepo
                )
            },
            {
                CupertinoNavRow(
                    icon = null,
                    title = "${projectInfo.forks} forks",
                    external = true,
                    onClick = onOpenFork
                )
            }
        )
    )
}

@Composable
private fun ContributorRow(
    contributor: Contributor,
    onClick: () -> Unit,
) {
    CupertinoRowsCard(
        rows = listOf(
            {
                CupertinoNavRow(
                    icon = null,
                    title = contributor.name,
                    description = when (contributor.contributionsCount.toInt()) {
                        1 -> stringResource(R.string.one_contribution)
                        else -> stringResource(
                            R.string.contributions_number,
                            contributor.contributionsCount
                        )
                    },
                    onClick = onClick,
                    trailing = {
                        AsyncImage(
                            model = contributor.photoUrl,
                            contentDescription = contributor.name,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(UI.colors.medium)
                                .background(cupertinoHairline())
                        )
                    }
                )
            }
        )
    )
}

@Composable
private fun ContributorsErrorState(
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

@Preview
@Composable
private fun PreviewSuccess(isDark: Boolean = false) {
    PreviewSuccessContent()
}

/** For screenshot testing */
@Composable
fun BoxWithConstraintsScope.ContributorScreenUiTest(isDark: Boolean) {
    IvyWalletPreview(theme = if (isDark) Theme.DARK else Theme.LIGHT) {
        PreviewSuccessContent()
    }
}

@Composable
private fun BoxWithConstraintsScope.PreviewSuccessContent() {
    ContributorsUi(
        uiState = ContributorsState(
            projectResponse = ProjectResponse.Success(
                projectInfo = ProjectRepositoryInfo(
                    forks = "259",
                    stars = "1524",
                    url = ""
                )
            ),
            contributorsResponse = ContributorsResponse.Success(
                contributors = persistentListOf(
                    Contributor(
                        name = "Iliyan",
                        photoUrl = "",
                        contributionsCount = "567",
                        githubProfileUrl = ""
                    )
                )
            )
        ),
        onEvent = {},
        onClose = {}
    )
}
