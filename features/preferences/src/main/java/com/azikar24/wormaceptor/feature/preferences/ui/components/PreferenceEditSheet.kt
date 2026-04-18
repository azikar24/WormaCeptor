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
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.preferences.R
import com.azikar24.wormaceptor.feature.preferences.vm.PreferenceEditorState
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewEvent
import kotlinx.collections.immutable.ImmutableList

/** Bottom sheet for creating or editing a preference item. */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ModifierMissing")
@Composable
fun PreferenceEditSheet(
    editor: PreferenceEditorState,
    onEvent: (PreferencesViewEvent) -> Unit,
    showDelete: Boolean,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (editor.isCreating) focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = { onEvent(PreferencesViewEvent.Detail.EditSheetDismissed) },
        sheetState = sheetState,
        shape = WormaCeptorTokens.Shapes.sheet,
    ) {
        PreferenceEditSheetContent(
            editor = editor,
            onEvent = onEvent,
            showDelete = showDelete,
            focusRequester = focusRequester,
        )
    }
}

@Composable
private fun PreferenceEditSheetContent(
    editor: PreferenceEditorState,
    onEvent: (PreferencesViewEvent) -> Unit,
    showDelete: Boolean,
    focusRequester: FocusRequester,
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
            text = if (editor.isCreating) {
                stringResource(R.string.preferences_dialog_create_title)
            } else {
                stringResource(R.string.preferences_dialog_edit_title)
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        EditSheetKeyInput(editor = editor, onEvent = onEvent, focusRequester = focusRequester)
        EditSheetTypeSelector(editor = editor, onEvent = onEvent)
        EditSheetValueInput(editor = editor, onEvent = onEvent)
        EditSheetActions(
            editor = editor,
            onEvent = onEvent,
            showDelete = showDelete,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxxl))
    }
}

@Composable
private fun EditSheetKeyInput(
    editor: PreferenceEditorState,
    onEvent: (PreferencesViewEvent) -> Unit,
    focusRequester: FocusRequester,
) {
    WormaCeptorTextField(
        value = editor.key,
        onValueChange = { onEvent(PreferencesViewEvent.Editor.KeyChanged(it)) },
        label = { Text(stringResource(R.string.preferences_label_key)) },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (editor.isCreating) Modifier.focusRequester(focusRequester) else Modifier,
            ),
        singleLine = true,
        enabled = editor.isCreating,
        monospace = true,
        isError = editor.isKeyError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSheetTypeSelector(
    editor: PreferenceEditorState,
    onEvent: (PreferencesViewEvent) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = editor.typeDropdownExpanded,
        onExpandedChange = {
            if (editor.isCreating) {
                onEvent(PreferencesViewEvent.Editor.TypeDropdownExpandedChanged(it))
            }
        },
    ) {
        OutlinedTextField(
            value = editor.selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.preferences_label_type)) },
            trailingIcon = {
                if (editor.isCreating) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = editor.typeDropdownExpanded)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            enabled = editor.isCreating,
            shape = WormaCeptorTokens.Shapes.button,
        )
        if (editor.isCreating) {
            ExposedDropdownMenu(
                expanded = editor.typeDropdownExpanded,
                onDismissRequest = {
                    onEvent(PreferencesViewEvent.Editor.TypeDropdownExpandedChanged(false))
                },
            ) {
                val typeColors = WormaCeptorTokens.Colors.Preferences.typeScheme()
                PreferenceEditorState.AVAILABLE_TYPES.forEach { type ->
                    val typeColor = typeColors.forTypeName(type)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = typeColor.copy(
                                        alpha = WormaCeptorTokens.Alpha.MEDIUM,
                                    ),
                                    shape = WormaCeptorTokens.Shapes.chip,
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
                        onClick = { onEvent(PreferencesViewEvent.Editor.TypeSelected(type)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EditSheetValueInput(
    editor: PreferenceEditorState,
    onEvent: (PreferencesViewEvent) -> Unit,
) {
    when (editor.selectedType) {
        PreferenceEditorState.TYPE_STRING -> StringValueInput(
            value = editor.stringValue,
            onValueChange = { onEvent(PreferencesViewEvent.Editor.StringValueChanged(it)) },
        )
        PreferenceEditorState.TYPE_INT -> NumericValueInput(
            value = editor.intValue,
            onValueChange = { onEvent(PreferencesViewEvent.Editor.IntValueChanged(it)) },
            isError = editor.isIntError,
            errorText = stringResource(R.string.preferences_validation_invalid_integer),
            keyboardType = KeyboardType.Number,
        )
        PreferenceEditorState.TYPE_LONG -> NumericValueInput(
            value = editor.longValue,
            onValueChange = { onEvent(PreferencesViewEvent.Editor.LongValueChanged(it)) },
            isError = editor.isLongError,
            errorText = stringResource(R.string.preferences_validation_invalid_long),
            keyboardType = KeyboardType.Number,
        )
        PreferenceEditorState.TYPE_FLOAT -> NumericValueInput(
            value = editor.floatValue,
            onValueChange = { onEvent(PreferencesViewEvent.Editor.FloatValueChanged(it)) },
            isError = editor.isFloatError,
            errorText = stringResource(R.string.preferences_validation_invalid_float),
            keyboardType = KeyboardType.Decimal,
        )
        PreferenceEditorState.TYPE_BOOLEAN -> BooleanValueInput(
            value = editor.booleanValue,
            onValueChange = { onEvent(PreferencesViewEvent.Editor.BooleanValueChanged(it)) },
        )
        PreferenceEditorState.TYPE_STRING_SET -> StringSetValueInput(editor = editor, onEvent = onEvent)
    }
}

@Composable
private fun EditSheetActions(
    editor: PreferenceEditorState,
    onEvent: (PreferencesViewEvent) -> Unit,
    showDelete: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!editor.isCreating && showDelete) {
            WormaCeptorButton(
                text = stringResource(R.string.preferences_dialog_delete_confirm),
                onClick = { onEvent(PreferencesViewEvent.Editor.DeleteRequested) },
                variant = ButtonVariant.Text,
                contentColor = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        WormaCeptorButton(
            text = stringResource(R.string.preferences_dialog_cancel),
            onClick = { onEvent(PreferencesViewEvent.Detail.EditSheetDismissed) },
            variant = ButtonVariant.Text,
        )
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
        WormaCeptorButton(
            text = if (editor.isCreating) {
                stringResource(R.string.preferences_button_create)
            } else {
                stringResource(R.string.preferences_button_save)
            },
            onClick = { onEvent(PreferencesViewEvent.Editor.SaveRequested) },
            variant = ButtonVariant.Primary,
            enabled = editor.canSave,
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
private fun StringSetValueInput(
    editor: PreferenceEditorState,
    onEvent: (PreferencesViewEvent) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.preferences_label_values),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        if (editor.stringSetValues.isNotEmpty()) {
            StringSetChips(
                values = editor.stringSetValues,
                onRemoveItem = { index ->
                    onEvent(PreferencesViewEvent.Editor.RemoveStringSetItem(index))
                },
            )
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
        }

        StringSetAddRow(
            newItem = editor.newStringSetItem,
            onNewItemChange = { onEvent(PreferencesViewEvent.Editor.NewStringSetItemChanged(it)) },
            onAddItem = { onEvent(PreferencesViewEvent.Editor.AddStringSetItem) },
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
                editor = PreferenceEditorState(),
                onEvent = {},
                showDelete = false,
                focusRequester = remember { FocusRequester() },
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
                editor = PreferenceEditorState(
                    isEditing = true,
                    key = "dark_mode",
                    selectedType = PreferenceEditorState.TYPE_BOOLEAN,
                    booleanValue = true,
                ),
                onEvent = {},
                showDelete = true,
                focusRequester = remember { FocusRequester() },
            )
        }
    }
}
