package com.ivy.main.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.ui.typography.ivyInter

/** Shared text field with a placeholder.
 *
 * Focus is managed explicitly: the field requests focus on tap (a fallback for
 * hosts that intercept touch before Compose's own input handling) and can
 * auto-focus on mount via [autoFocus]. This keeps ADB/hardware input and real
 * touch behaving identically. */
@Composable
fun SplitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = ivyInter(15.sp).copy(color = UI.colors.pureInverse),
    autoFocus: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .focusRequester(focusRequester)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { runCatching { focusRequester.requestFocus() } },
            ),
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
