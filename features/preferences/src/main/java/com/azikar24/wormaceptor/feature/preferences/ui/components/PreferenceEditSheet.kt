package com.azikar24.wormaceptor.feature.preferences.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import com.azikar24.wormaceptor.feature.preferences.R
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditFormState.Companion.TYPE_BOOLEAN
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditFormState.Companion.TYPE_FLOAT
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditFormState.Companion.TYPE_INT
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditFormState.Companion.TYPE_LONG
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditFormState.Companion.TYPE_STRING
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditFormState.Companion.TYPE_STRING_SET
import kotlinx.collections.immutable.ImmutableList

/** Bottom sheet for creating or editing a preference item. */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ModifierMissing")
@Composable
fun PreferenceEditSheet(
    item: PreferenceItem?,
    onDismiss: () -> Unit,
    onSave: (String, PreferenceValue) -> Unit,
    onDelete: ((String) -> Unit)? = null,
) {
    val formState = rememberPreferenceEditFormState(item)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (formState.isCreating) focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = WormaCeptorTokens.Shapes.sheet,
    ) {
        PreferenceEditSheetContent(
            formState = formState,
            focusRequester = focusRequester,
            onDismiss = onDismiss,
            onSave = onSave,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun PreferenceEditSheetContent(
    formState: PreferenceEditFormState,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit,
    onSave: (String, PreferenceValue) -> Unit,
    onDelete: ((String) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = WormaCeptorTokens.Spacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        Text(
            text = if (formState.isCreating) {
                stringResource(R.string.preferences_dialog_create_title)
            } else {
                stringResource(R.string.preferences_dialog_edit_title)
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        EditSheetKeyInput(formState = formState, focusRequester = focusRequester)
        EditSheetTypeSelector(formState = formState)
        EditSheetValueInput(formState = formState)
        EditSheetActions(
            formState = formState,
            onDismiss = onDismiss,
            onSave = onSave,
            onDelete = onDelete,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxxl))
    }
}

@Composable
private fun EditSheetKeyInput(
    formState: PreferenceEditFormState,
    focusRequester: FocusRequester,
) {
    WormaCeptorTextField(
        value = formState.key,
        onValueChange = { formState.key = it },
        label = { Text(stringResource(R.string.preferences_label_key)) },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (formState.isCreating) Modifier.focusRequester(focusRequester) else Modifier,
            ),
        singleLine = true,
        enabled = formState.isCreating,
        monospace = true,
        isError = formState.isKeyError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSheetTypeSelector(formState: PreferenceEditFormState) {
    ExposedDropdownMenuBox(
        expanded = formState.typeDropdownExpanded,
        onExpandedChange = { if (formState.isCreating) formState.typeDropdownExpanded = it },
    ) {
        OutlinedTextField(
            value = formState.selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.preferences_label_type)) },
            trailingIcon = {
                if (formState.isCreating) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = formState.typeDropdownExpanded)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            enabled = formState.isCreating,
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.sm),
        )
        if (formState.isCreating) {
            ExposedDropdownMenu(
                expanded = formState.typeDropdownExpanded,
                onDismissRequest = { formState.typeDropdownExpanded = false },
            ) {
                val typeColors = WormaCeptorTokens.Colors.Preferences.typeScheme()
                PreferenceEditFormState.AVAILABLE_TYPES.forEach { type ->
                    val typeColor = typeColors.forTypeName(type)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = typeColor.copy(
                                        alpha = WormaCeptorTokens.Alpha.MEDIUM,
                                    ),
                                    shape = RoundedCornerShape(
                                        WormaCeptorTokens.Radius.xs,
                                    ),
                                ) {
                                    Text(
                                        text = type,
                                        modifier = Modifier.padding(
                                            horizontal = WormaCeptorTokens.Spacing.sm,
                                            vertical = WormaCeptorTokens.Spacing.xs,
                                        ),
                                        color = typeColor,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        },
                        onClick = { formState.selectType(type) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EditSheetValueInput(formState: PreferenceEditFormState) {
    when (formState.selectedType) {
        TYPE_STRING -> StringValueInput(
            value = formState.stringValue,
            onValueChange = { formState.stringValue = it },
        )
        TYPE_INT -> NumericValueInput(
            value = formState.intValue,
            onValueChange = { formState.intValue = it },
            isError = formState.isIntError,
            errorText = stringResource(R.string.preferences_validation_invalid_integer),
            keyboardType = KeyboardType.Number,
        )
        TYPE_LONG -> NumericValueInput(
            value = formState.longValue,
            onValueChange = { formState.longValue = it },
            isError = formState.isLongError,
            errorText = stringResource(R.string.preferences_validation_invalid_long),
            keyboardType = KeyboardType.Number,
        )
        TYPE_FLOAT -> NumericValueInput(
            value = formState.floatValue,
            onValueChange = { formState.floatValue = it },
            isError = formState.isFloatError,
            errorText = stringResource(R.string.preferences_validation_invalid_float),
            keyboardType = KeyboardType.Decimal,
        )
        TYPE_BOOLEAN -> BooleanValueInput(
            value = formState.booleanValue,
            onValueChange = { formState.booleanValue = it },
        )
        TYPE_STRING_SET -> StringSetValueInput(formState = formState)
    }
}

@Composable
private fun EditSheetActions(
    formState: PreferenceEditFormState,
    onDismiss: () -> Unit,
    onSave: (String, PreferenceValue) -> Unit,
    onDelete: ((String) -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!formState.isCreating && onDelete != null) {
            WormaCeptorButton(
                text = stringResource(R.string.preferences_dialog_delete_confirm),
                onClick = { onDelete(formState.key) },
                variant = ButtonVariant.Text,
                contentColor = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        WormaCeptorButton(
            text = stringResource(R.string.preferences_dialog_cancel),
            onClick = onDismiss,
            variant = ButtonVariant.Text,
        )
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
        WormaCeptorButton(
            text = if (formState.isCreating) {
                stringResource(R.string.preferences_button_create)
            } else {
                stringResource(R.string.preferences_button_save)
            },
            onClick = { onSave(formState.key, formState.toPreferenceValue()) },
            variant = ButtonVariant.Primary,
            enabled = formState.canSave,
        )
    }
}

@Composable
private fun StringValueInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    WormaCeptorTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.preferences_label_value)) },
        modifier = Modifier.fillMaxWidth(),
        monospace = true,
        minLines = 2,
        maxLines = 5,
    )
}

@Composable
private fun NumericValueInput(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorText: String,
    keyboardType: KeyboardType,
) {
    WormaCeptorTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.preferences_label_value)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        supportingText = { if (isError) Text(errorText) },
        monospace = true,
        singleLine = true,
    )
}

@Composable
private fun BooleanValueInput(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.preferences_label_value),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (value) "true" else "false",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                ),
                color = if (value) {
                    WormaCeptorTokens.Colors.Preferences.typeScheme().boolean
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
            Switch(
                checked = value,
                onCheckedChange = { newValue ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onValueChange(newValue)
                },
            )
        }
    }
}

@Composable
private fun StringSetValueInput(formState: PreferenceEditFormState) {
    Column {
        Text(
            text = stringResource(R.string.preferences_label_values),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        if (formState.stringSetValues.isNotEmpty()) {
            StringSetChips(
                values = formState.stringSetValues,
                onRemoveItem = formState::removeStringSetItem,
            )
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
        }

        StringSetAddRow(
            newItem = formState.newStringSetItem,
            onNewItemChange = { formState.newStringSetItem = it },
            onAddItem = formState::addStringSetItem,
        )
    }
}

@Composable
private fun StringSetChips(
    values: ImmutableList<String>,
    onRemoveItem: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs)) {
        values.forEachIndexed { index, value ->
            InputChip(
                selected = false,
                onClick = {},
                label = { Text(text = value, fontFamily = FontFamily.Monospace) },
                trailingIcon = {
                    IconButton(onClick = { onRemoveItem(index) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.preferences_remove),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun StringSetAddRow(
    newItem: String,
    onNewItemChange: (String) -> Unit,
    onAddItem: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WormaCeptorTextField(
            value = newItem,
            onValueChange = onNewItemChange,
            placeholder = { Text(stringResource(R.string.preferences_add_item_placeholder)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            monospace = true,
        )

        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

        IconButton(onClick = onAddItem, enabled = newItem.isNotBlank()) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.preferences_add),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreferenceEditSheetCreatePreview() {
    WormaCeptorTheme {
        Surface {
            PreferenceEditSheetContent(
                formState = PreferenceEditFormState(item = null),
                focusRequester = remember { FocusRequester() },
                onDismiss = {},
                onSave = { _, _ -> },
                onDelete = null,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreferenceEditSheetEditPreview() {
    WormaCeptorTheme {
        Surface {
            PreferenceEditSheetContent(
                formState = PreferenceEditFormState(
                    item = PreferenceItem(
                        key = "dark_mode",
                        value = PreferenceValue.BooleanValue(true),
                    ),
                ),
                focusRequester = remember { FocusRequester() },
                onDismiss = {},
                onSave = { _, _ -> },
                onDelete = {},
            )
        }
    }
}
