package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Unified text field component for WormaCeptor.
 *
 * Wraps [OutlinedTextField] with consistent shape, colors, and styling.
 * Supports a monospace mode for structured data (keys, values, code).
 *
 * @param value Current text value
 * @param onValueChange Value change callback
 * @param modifier Modifier for the text field
 * @param label Optional label composable
 * @param placeholder Optional placeholder composable
 * @param leadingIcon Optional leading icon composable
 * @param trailingIcon Optional trailing icon composable
 * @param supportingText Optional helper/error text composable
 * @param isError Whether the field is in an error state
 * @param enabled Whether the field is editable
 * @param readOnly Whether the field is read-only
 * @param singleLine Whether to constrain to a single line
 * @param minLines Minimum visible lines for multi-line fields
 * @param maxLines Maximum visible lines for multi-line fields
 * @param monospace When true, uses monospace font family for structured data
 * @param keyboardOptions Keyboard configuration (type, IME action, etc.)
 * @param keyboardActions Keyboard action callbacks
 */
@Suppress("LongParameterList")
@Composable
fun WormaCeptorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    monospace: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val textStyle = if (monospace) {
        MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    } else {
        MaterialTheme.typography.bodyMedium
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        textStyle = textStyle,
        shape = WormaCeptorDesignSystem.Shapes.textField,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.BOLD,
            ),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
