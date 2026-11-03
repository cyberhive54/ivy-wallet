package com.ivy.exchangerates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.exchangerates.data.RateUi
import com.ivy.legacy.cupertino.CupertinoFilledButton
import com.ivy.legacy.cupertino.CupertinoInputContainer
import com.ivy.legacy.cupertino.CupertinoSheetModal
import com.ivy.ui.R
import com.ivy.ui.typography.ivyInter
import java.util.Locale

// -----------------------------------------------------------------------------------------
// Cupertino sheets replacing the legacy AddRateModal / AmountModal.
// -----------------------------------------------------------------------------------------

@Composable
internal fun AddRateSheet(
    visible: Boolean,
    baseCurrency: String,
    dismiss: () -> Unit,
    onAdd: (RateUi) -> Unit,
) {
    var toCurrency by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }

    val parsedRate = rateText.replace(",", ".").toDoubleOrNull()
    val canAdd = toCurrency.isNotBlank() && parsedRate != null && parsedRate > 0.0

    CupertinoSheetModal(
        visible = visible,
        title = stringResource(R.string.add_manual_exchange_rate),
        onDismiss = dismiss,
    ) {
        Text(
            text = "1 $baseCurrency =",
            style = ivyInter(13.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(10.dp))

        CupertinoInputContainer {
            Box(
                Modifier.weight(1f)
            ) {
                if (toCurrency.isEmpty()) {
                    Text(
                        text = "Currency (e.g. EUR)",
                        style = ivyInter(16.sp),
                        color = UI.colors.gray
                    )
                }

                BasicTextField(
                    value = toCurrency,
                    onValueChange = { toCurrency = it.uppercase(Locale.US).take(5) },
                    singleLine = true,
                    textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                    cursorBrush = SolidColor(UI.colors.pureInverse),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        CupertinoInputContainer {
            BasicTextField(
                value = rateText,
                onValueChange = { rateText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                singleLine = true,
                textStyle = ivyInter(16.sp).copy(color = UI.colors.pureInverse),
                cursorBrush = SolidColor(UI.colors.pureInverse),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        CupertinoFilledButton(
            label = stringResource(R.string.add),
            enabled = canAdd,
            onClick = {
                val rate = parsedRate ?: return@CupertinoFilledButton
                onAdd(
                    RateUi(
                        from = baseCurrency,
                        to = toCurrency.trim(),
                        rate = rate
                    )
                )
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun EditRateSheet(
    rate: RateUi,
    dismiss: () -> Unit,
    onDone: (Double) -> Unit,
) {
    var rateText by remember(rate) {
        mutableStateOf(rate.rate.toString())
    }

    val parsedRate = rateText.replace(",", ".").toDoubleOrNull()

    CupertinoSheetModal(
        visible = true,
        title = "${rate.from} → ${rate.to}",
        onDismiss = dismiss,
    ) {
        Text(
            text = "1 ${rate.from} =",
            style = ivyInter(13.sp),
            color = UI.colors.gray
        )

        Spacer(Modifier.height(10.dp))

        CupertinoInputContainer {
            BasicTextField(
                value = rateText,
                onValueChange = { rateText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                singleLine = true,
                textStyle = ivyInter(18.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    .copy(color = UI.colors.pureInverse),
                cursorBrush = SolidColor(UI.colors.pureInverse),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        CupertinoFilledButton(
            label = stringResource(R.string.save),
            enabled = parsedRate != null && parsedRate > 0.0,
            onClick = {
                val newRate = parsedRate ?: return@CupertinoFilledButton
                onDone(newRate)
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}
