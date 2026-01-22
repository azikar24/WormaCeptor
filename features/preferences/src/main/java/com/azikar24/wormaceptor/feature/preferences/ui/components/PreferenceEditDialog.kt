/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.preferences.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import com.azikar24.wormaceptor.feature.preferences.ui.theme.PreferencesDesignSystem

/**
 * Dialog for creating or editing a preference item.
 * Provides type-aware input fields based on the selected preference type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceEditDialog(
    item: PreferenceItem?,
    onDismiss: () -> Unit,
    onSave: (String, PreferenceValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCreating = item == null
    val focusRequester = remember { FocusRequester() }

    var key by remember(item) { mutableStateOf(item?.key ?: "") }
    var selectedType by remember(item) {
        mutableStateOf(item?.value?.typeName ?: "String")
    }

    // Type-specific value states
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

    val availableTypes = listOf("String", "Int", "Long", "Float", "Boolean", "StringSet")

    // Validation
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isCreating) "Create Preference" else "Edit Preference",
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PreferencesDesignSystem.Spacing.md),
            ) {
                // Key input
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isCreating) {
                                Modifier.focusRequester(focusRequester)
                            } else {
                                Modifier
                            },
                        ),
                    singleLine = true,
                    enabled = isCreating,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    isError = key.isBlank() && key.isNotEmpty(),
                    shape = RoundedCornerShape(PreferencesDesignSystem.CornerRadius.sm),
                )

                // Type selector
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { if (isCreating) typeDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            if (isCreating) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        enabled = isCreating,
                        shape = RoundedCornerShape(PreferencesDesignSystem.CornerRadius.sm),
                    )
                    if (isCreating) {
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false },
                        ) {
                            availableTypes.forEach { type ->
                                val typeColor = PreferencesDesignSystem.TypeColors.forTypeName(type)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = typeColor.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp),
                                            ) {
                                                Text(
                                                    text = type,
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp,
                                                    ),
                                                    color = typeColor,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedType = type
                                        typeDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Type-specific value input
                when (selectedType) {
                    "String" -> {
                        OutlinedTextField(
                            value = stringValue,
                            onValueChange = { stringValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            shape = RoundedCornerShape(PreferencesDesignSystem.CornerRadius.sm),
                            minLines = 2,
                            maxLines = 5,
                        )
                    }

                    "Int" -> {
                        OutlinedTextField(
                            value = intValue,
                            onValueChange = { intValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = intValue.isNotBlank() && intValue.toIntOrNull() == null,
                            supportingText = {
                                if (intValue.isNotBlank() && intValue.toIntOrNull() == null) {
                                    Text("Must be a valid integer")
                                }
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            shape = RoundedCornerShape(PreferencesDesignSystem.CornerRadius.sm),
                            singleLine = true,
                        )
                    }

                    "Long" -> {
                        OutlinedTextField(
                            value = longValue,
                            onValueChange = { longValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = longValue.isNotBlank() && longValue.toLongOrNull() == null,
                            supportingText = {
                                if (longValue.isNotBlank() && longValue.toLongOrNull() == null) {
                                    Text("Must be a valid long integer")
                                }
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            shape = RoundedCornerShape(PreferencesDesignSystem.CornerRadius.sm),
                            singleLine = true,
                        )
                    }

                    "Float" -> {
                        OutlinedTextField(
                            value = floatValue,
                            onValueChange = { floatValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = floatValue.isNotBlank() && floatValue.toFloatOrNull() == null,
                            supportingText = {
                                if (floatValue.isNotBlank() && floatValue.toFloatOrNull() == null) {
                                    Text("Must be a valid decimal number")
                                }
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            shape = RoundedCornerShape(PreferencesDesignSystem.CornerRadius.sm),
                            singleLine = true,
                        )
                    }

                    "Boolean" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Value",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (booleanValue) "true" else "false",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = if (booleanValue) {
                                        PreferencesDesignSystem.TypeColors.boolean
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = booleanValue,
                                    onCheckedChange = { booleanValue = it },
                                )
                            }
                        }
                    }

                    "StringSet" -> {
                        Column {
                            Text(
                                text = "Values",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Display existing items as chips
                            if (stringSetValues.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    stringSetValues.forEachIndexed { index, value ->
                                        InputChip(
                                            selected = false,
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = value,
                                                    fontFamily = FontFamily.Monospace,
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(
                                                    onClick = { stringSetValues.removeAt(index) },
                                                    modifier = Modifier
                                                        .padding(0.dp)
                                                        .then(Modifier),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove",
                                                        modifier = Modifier.padding(0.dp),
                                                    )
                                                }
                                            },
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Add new item input
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    value = newStringSetItem,
                                    onValueChange = { newStringSetItem = it },
                                    placeholder = { Text("Add item...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                    ),
                                    shape = RoundedCornerShape(PreferencesDesignSystem.CornerRadius.sm),
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        if (newStringSetItem.isNotBlank()) {
                                            stringSetValues.add(newStringSetItem)
                                            newStringSetItem = ""
                                        }
                                    },
                                    enabled = newStringSetItem.isNotBlank(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = when (selectedType) {
                        "String" -> PreferenceValue.StringValue(stringValue)
                        "Int" -> PreferenceValue.IntValue(intValue.toIntOrNull() ?: 0)
                        "Long" -> PreferenceValue.LongValue(longValue.toLongOrNull() ?: 0L)
                        "Float" -> PreferenceValue.FloatValue(floatValue.toFloatOrNull() ?: 0f)
                        "Boolean" -> PreferenceValue.BooleanValue(booleanValue)
                        "StringSet" -> PreferenceValue.StringSetValue(stringSetValues.toSet())
                        else -> PreferenceValue.StringValue(stringValue)
                    }
                    onSave(key, value)
                },
                enabled = canSave,
            ) {
                Text(if (isCreating) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier,
    )
}
