package com.ivy.importdata.csvimport

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.importdata.csvimport.flow.ImportFrom
import com.ivy.importdata.csvimport.flow.ImportProcessing
import com.ivy.importdata.csvimport.flow.ImportResultUI
import com.ivy.importdata.csvimport.flow.instructions.ImportInstructions
import com.ivy.legacy.domain.deprecated.logic.csv.model.ImportType
import com.ivy.navigation.CSVScreen
import com.ivy.navigation.ImportScreen
import com.ivy.navigation.navigation
import com.ivy.onboarding.viewmodel.OnboardingViewModel
import com.ivy.data.backup.ImportResult

@OptIn(ExperimentalStdlibApi::class)
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ImportCSVScreen(
    screen: ImportScreen,
    onClose: (() -> Unit)? = null,
    onOpenCsv: (() -> Unit)? = null,
) {
    val viewModel: ImportViewModel = viewModel()

    val importStep by viewModel.importStep.observeAsState(ImportStep.IMPORT_FROM)
    val importType by viewModel.importType.observeAsState()
    val importProgressPercent by viewModel.importProgressPercent.observeAsState(0)
    val importResult by viewModel.importResult.observeAsState()

    // In-tab hosting: back goes one wizard step back; on the first step the host pops the page.
    BackHandler(enabled = screen.inlineHosted && importStep != ImportStep.IMPORT_FROM) {
        viewModel.backFromStep()
    }

    val onboardingViewModel: OnboardingViewModel = viewModel()

    com.ivy.legacy.utils.onScreenStart {
        viewModel.start(screen)
    }
    val context = LocalContext.current

    val nav = navigation()
    val exitPage: () -> Unit = {
        if (screen.inlineHosted) {
            onClose?.invoke()
        } else {
            nav.back()
        }
    }
    val openCsv: () -> Unit = {
        onOpenCsv?.invoke()
            ?: nav.navigateTo(
                CSVScreen(launchedFromOnboarding = screen.launchedFromOnboarding)
            )
    }

    UI(
        screen = screen,
        importStep = importStep,
        importType = importType,
        importProgressPercent = importProgressPercent,
        importResult = importResult,
        exitPage = exitPage,
        openCsv = openCsv,

        onChooseImportType = viewModel::setImportType,
        onUploadCSVFile = { viewModel.uploadFile(context) },
        onStepBack = { viewModel.backFromStep() },
        onSkip = {
            viewModel.skip(
                screen = screen,
                onboardingViewModel = onboardingViewModel,
                exit = { onClose?.invoke() }
            )
        },
        onFinish = {
            viewModel.finish(
                screen = screen,
                onboardingViewModel = onboardingViewModel,
                exit = { onClose?.invoke() }
            )
        }
    )
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: ImportScreen,
    importStep: ImportStep,
    importType: ImportType?,
    importProgressPercent: Int,
    importResult: ImportResult?,
    exitPage: () -> Unit,
    openCsv: () -> Unit,

    onChooseImportType: (ImportType) -> Unit = {},
    onUploadCSVFile: () -> Unit = {},
    onStepBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onFinish: () -> Unit = {},
) {
    when (importStep) {
        ImportStep.IMPORT_FROM -> {
            ImportFrom(
                hasSkip = screen.launchedFromOnboarding,
                launchedFromOnboarding = screen.launchedFromOnboarding,
                onBack = exitPage,
                onOpenCsv = openCsv,
                onSkip = onSkip,
                onImportFrom = onChooseImportType
            )
        }

        ImportStep.INSTRUCTIONS -> {
            ImportInstructions(
                hasSkip = screen.launchedFromOnboarding,
                importType = importType!!,
                onBack = onStepBack,
                onSkip = onSkip,
                onUploadClick = onUploadCSVFile
            )
        }

        ImportStep.LOADING -> {
            ImportProcessing(
                progressPercent = importProgressPercent
            )
        }

        ImportStep.RESULT -> {
            ImportResultUI(
                result = importResult!!,
                launchedFromOnboarding = screen.launchedFromOnboarding,
                onBack = onStepBack,
                onOpenCsv = openCsv,
            ) {
                onFinish()
            }
        }
    }
}

@ExperimentalFoundationApi
@Preview
@Composable
private fun Preview() {
    com.ivy.legacy.IvyWalletPreview {
        UI(
            screen = ImportScreen(launchedFromOnboarding = true),
            importStep = ImportStep.IMPORT_FROM,
            importType = null,
            importProgressPercent = 0,
            importResult = null,
            exitPage = {},
            openCsv = {}
        )
    }
}
