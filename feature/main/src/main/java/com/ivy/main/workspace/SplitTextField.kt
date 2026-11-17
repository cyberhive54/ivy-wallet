package com.ivy.main.workspace

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter

/** Shared text field with a placeholder — uses the same decorationBox pattern
 * that is proven working in the fork's existing fields. */
@Composable
fun SplitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = ivyInter(15.sp).copy(color = UI.colors.pureInverse),
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        modifier = modifier,
        decorationBox = { inner ->
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(text = hint, style = textStyle.copy(color = UI.colors.gray))
                }
                inner()
            }
        }
    )
}
