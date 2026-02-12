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
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import com.azikar24.wormaceptor.feature.preferences.R
import com.azikar24.wormaceptor.feature.preferences.ui.theme.PreferencesDesignSystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** Bottom sheet for creating or editing a preference item. */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "CyclomaticComplexMethod", "ModifierMissing")
@Composable
fun PreferenceEditSheet(item: PreferenceItem?, onDismiss: () -> Unit, onSave: (String, PreferenceValue) -> Unit) {
    val isCreating = item == null
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var key by remember(item) { mutableStateOf(item?.key ?: "") }
    var selectedType by remember(item) {
        mutableStateOf(item?.value?.typeName ?: "String")
    }

    var stringValue by remember(item) {
        mutableStateOf((item?.value as? PreferenceValue.StringValue)?.value ?: "")
    }
    var intValue by remember(item) {
        mutableStateOf((item?.value as? PreferenceValue.IntValue)?.value?.toString() ?: "")
    }
    var longValue by remember(item) {
        mutableStateOf((item?.value as? PreferenceValue.LongValue)?.value?.toString() ?: "")
    }
    var floatValue by remember(item) {
        mutableStateOf((item?.value as? PreferenceValue.FloatValue)?.value?.toString() ?: "")
    }
    var booleanValue by remember(item) {
        mutableStateOf((item?.value as? PreferenceValue.BooleanValue)?.value ?: false)
    }
    val stringSetValues = remember(item) {
        mutableStateListOf<String>().also {
            (item?.value as? PreferenceValue.StringSetValue)?.value?.let { set ->
                it.addAll(set)
            }
        }
    }
    var newStringSetItem by remember { mutableStateOf("") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val availableTypes = listOf("String", "Int", "Long", "Float", "Boolean", "StringSet").toImmutableList()

    val isKeyValid = key.isNotBlank()
    val isValueValid = when (selectedType) {
        "String" -> true
        "Int" -> intValue.toIntOrNull() != null || intValue.isBlank()
        "Long" -> longValue.toLongOrNull() != null || longValue.isBlank()
        "Float" -> floatValue.toFloatOrNull() != null || floatValue.isBlank()
        "Boolean" -> true
        "StringSet" -> true
        else -> true
    }
    val canSave = isKeyValid && isValueValid

    LaunchedEffect(Unit) {
        if (isCreating) {
            focusRequester.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = WormaCeptorDesignSystem.Shapes.sheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = if (isCreating) {
                    stringResource(R.string.preferences_dialog_create_title)
                } else {
                    stringResource(R.string.preferences_dialog_edit_title)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            EditSheetKeyInput(
                key = key,
                onKeyChange = { key = it },
                isCreating = isCreating,
                focusRequester = focusRequester,
            )

            EditSheetTypeSelector(
                selectedType = selectedType,
                isCreating = isCreating,
                expanded = typeDropdownExpanded,
                onExpandedChange = { if (isCreating) typeDropdownExpanded = it },
                availableTypes = availableTypes,
                onTypeSelected = {
                    selectedType = it
                    typeDropdownExpanded = false
                },
                onDismissDropdown = { typeDropdownExpanded = false },
            )

            EditSheetValueInput(
                selectedType = selectedType,
                stringValue = stringValue,
                onStringValueChange = { stringValue = it },
                intValue = intValue,
                onIntValueChange = { intValue = it },
                longValue = longValue,
                onLongValueChange = { longValue = it },
                floatValue = floatValue,
                onFloatValueChange = { floatValue = it },
                booleanValue = booleanValue,
                onBooleanValueChange = { booleanValue = it },
                stringSetValues = stringSetValues.toImmutableList(),
                newStringSetItem = newStringSetItem,
                onNewStringSetItemChange = { newStringSetItem = it },
                onAddStringSetItem = {
                    if (newStringSetItem.isNotBlank()) {
                        stringSetValues.add(newStringSetItem)
                        newStringSetItem = ""
                    }
                },
                onRemoveStringSetItem = { stringSetValues.removeAt(it) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.preferences_dialog_cancel))
                }
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Button(
                    onClick = {
                        val value = buildPreferenceValue(
                            selectedType = selectedType,
                            stringValue = stringValue,
                            intValue = intValue,
                            longValue = longValue,
                            floatValue = floatValue,
                            booleanValue = booleanValue,
                            stringSetValues = stringSetValues.toImmutableList(),
                        )
                        onSave(key, value)
                    },
                    enabled = canSave,
                ) {
                    Text(
                        if (isCreating) {
                            stringResource(R.string.preferences_button_create)
                        } else {
                            stringResource(R.string.preferences_button_save)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxxl))
        }
    }
}

@Composable
private fun EditSheetKeyInput(
    key: String,
    onKeyChange: (String) -> Unit,
    isCreating: Boolean,
    focusRequester: FocusRequester,
) {
    OutlinedTextField(
        value = key,
        onValueChange = onKeyChange,
        label = { Text(stringResource(R.string.preferences_label_key)) },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCreating) Modifier.focusRequester(focusRequester) else Modifier,
            ),
        singleLine = true,
        enabled = isCreating,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
        ),
        isError = key.isBlank() && key.isNotEmpty(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
private fun EditSheetTypeSelector(
    selectedType: String,
    isCreating: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    availableTypes: ImmutableList<String>,
    onTypeSelected: (String) -> Unit,
    onDismissDropdown: () -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.preferences_label_type)) },
            trailingIcon = {
                if (isCreating) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            enabled = isCreating,
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
        )
        if (isCreating) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissDropdown,
            ) {
                availableTypes.forEach { type ->
                    val typeColor = PreferencesDesignSystem.TypeColors.forTypeName(type)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = typeColor.copy(
                                        alpha = WormaCeptorDesignSystem.Alpha.medium,
                                    ),
                                    shape = RoundedCornerShape(
                                        WormaCeptorDesignSystem.CornerRadius.xs,
                                    ),
                                ) {
                                    Text(
                                        text = type,
                                        modifier = Modifier.padding(
                                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                            vertical = WormaCeptorDesignSystem.Spacing.xs,
                                        ),
                                        color = typeColor,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        },
                        onClick = { onTypeSelected(type) },
                    )
                }
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun EditSheetValueInput(
    selectedType: String,
    stringValue: String,
    onStringValueChange: (String) -> Unit,
    intValue: String,
    onIntValueChange: (String) -> Unit,
    longValue: String,
    onLongValueChange: (String) -> Unit,
    floatValue: String,
    onFloatValueChange: (String) -> Unit,
    booleanValue: Boolean,
    onBooleanValueChange: (Boolean) -> Unit,
    stringSetValues: ImmutableList<String>,
    newStringSetItem: String,
    onNewStringSetItemChange: (String) -> Unit,
    onAddStringSetItem: () -> Unit,
    onRemoveStringSetItem: (Int) -> Unit,
) {
    when (selectedType) {
        "String" -> StringValueInput(stringValue, onStringValueChange)
        "Int" -> NumericValueInput(
            value = intValue,
            onValueChange = onIntValueChange,
            isError = intValue.isNotBlank() && intValue.toIntOrNull() == null,
            errorText = stringResource(R.string.preferences_validation_invalid_integer),
            keyboardType = KeyboardType.Number,
        )
        "Long" -> NumericValueInput(
            value = longValue,
            onValueChange = onLongValueChange,
            isError = longValue.isNotBlank() && longValue.toLongOrNull() == null,
            errorText = stringResource(R.string.preferences_validation_invalid_long),
            keyboardType = KeyboardType.Number,
        )
        "Float" -> NumericValueInput(
            value = floatValue,
            onValueChange = onFloatValueChange,
            isError = floatValue.isNotBlank() && floatValue.toFloatOrNull() == null,
            errorText = stringResource(R.string.preferences_validation_invalid_float),
            keyboardType = KeyboardType.Decimal,
        )
        "Boolean" -> BooleanValueInput(booleanValue, onBooleanValueChange)
        "StringSet" -> StringSetValueInput(
            values = stringSetValues,
            newItem = newStringSetItem,
            onNewItemChange = onNewStringSetItemChange,
            onAddItem = onAddStringSetItem,
            onRemoveItem = onRemoveStringSetItem,
        )
    }
}

@Composable
private fun StringValueInput(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.preferences_label_value)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.preferences_label_value)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        supportingText = { if (isError) Text(errorText) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
        singleLine = true,
    )
}

@Composable
private fun BooleanValueInput(value: Boolean, onValueChange: (Boolean) -> Unit) {
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
                    PreferencesDesignSystem.TypeColors.boolean
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Switch(checked = value, onCheckedChange = onValueChange)
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun StringSetValueInput(
    values: ImmutableList<String>,
    newItem: String,
    onNewItemChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.preferences_label_values),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        if (values.isNotEmpty()) {
            StringSetChips(values = values, onRemoveItem = onRemoveItem)
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
        }

        StringSetAddRow(newItem = newItem, onNewItemChange = onNewItemChange, onAddItem = onAddItem)
    }
}

@Composable
private fun StringSetChips(values: ImmutableList<String>, onRemoveItem: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)) {
        values.forEachIndexed { index, value ->
            InputChip(
                selected = false,
                onClick = {},
                label = { Text(text = value, fontFamily = FontFamily.Monospace) },
                trailingIcon = {
                    IconButton(
                        onClick = { onRemoveItem(index) },
                        modifier = Modifier.padding(0.dp).then(Modifier),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.preferences_remove),
                            modifier = Modifier.padding(0.dp),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun StringSetAddRow(newItem: String, onNewItemChange: (String) -> Unit, onAddItem: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = newItem,
            onValueChange = onNewItemChange,
            placeholder = { Text(stringResource(R.string.preferences_add_item_placeholder)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
        )

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

        IconButton(onClick = onAddItem, enabled = newItem.isNotBlank()) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.preferences_add),
            )
        }
    }
}

@Suppress("LongParameterList")
private fun buildPreferenceValue(
    selectedType: String,
    stringValue: String,
    intValue: String,
    longValue: String,
    floatValue: String,
    booleanValue: Boolean,
    stringSetValues: ImmutableList<String>,
): PreferenceValue = when (selectedType) {
    "String" -> PreferenceValue.StringValue(stringValue)
    "Int" -> PreferenceValue.IntValue(intValue.toIntOrNull() ?: 0)
    "Long" -> PreferenceValue.LongValue(longValue.toLongOrNull() ?: 0L)
    "Float" -> PreferenceValue.FloatValue(floatValue.toFloatOrNull() ?: 0f)
    "Boolean" -> PreferenceValue.BooleanValue(booleanValue)
    "StringSet" -> PreferenceValue.StringSetValue(stringSetValues.toSet())
    else -> PreferenceValue.StringValue(stringValue)
}
