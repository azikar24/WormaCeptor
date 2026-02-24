package com.azikar24.wormaceptor.feature.location.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.location.R
import com.azikar24.wormaceptor.feature.location.ui.theme.LocationColors

@Composable
internal fun CoordinateInputCard(
    latitudeInput: String,
    longitudeInput: String,
    isInputValid: Boolean,
    isLoading: Boolean,
    isMockEnabled: Boolean,
    isMockLocationAvailable: Boolean,
    currentMockLatitude: Double?,
    currentMockLongitude: Double?,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onSetMockLocation: () -> Unit,
    onSetToCurrentLocation: () -> Unit,
    onSaveAsPreset: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.location_set_custom_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // Latitude input
            OutlinedTextField(
                value = latitudeInput,
                onValueChange = onLatitudeChanged,
                label = { Text(stringResource(R.string.location_latitude)) },
                placeholder = { Text(stringResource(R.string.location_latitude_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                supportingText = {
                    Text(stringResource(R.string.location_latitude_range))
                },
                isError = latitudeInput.isNotBlank() && latitudeInput.toDoubleOrNull()?.let { it !in -90.0..90.0 } == true,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Longitude input
            OutlinedTextField(
                value = longitudeInput,
                onValueChange = onLongitudeChanged,
                label = { Text(stringResource(R.string.location_longitude)) },
                placeholder = { Text(stringResource(R.string.location_longitude_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                supportingText = {
                    Text(stringResource(R.string.location_longitude_range))
                },
                isError = longitudeInput.isNotBlank() && longitudeInput.toDoubleOrNull()?.let { it !in -180.0..180.0 } == true,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                    WormaCeptorDesignSystem.Spacing.sm,
                ),
            ) {
                // Get current location button
                OutlinedButton(
                    onClick = onSetToCurrentLocation,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text(stringResource(R.string.location_current))
                }

                // Save as preset button
                OutlinedButton(
                    onClick = onSaveAsPreset,
                    enabled = isInputValid && !isLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text(stringResource(R.string.location_save))
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Set mock location button
            val isMatchingCurrentMock = isMockEnabled &&
                latitudeInput.toDoubleOrNull() == currentMockLatitude &&
                longitudeInput.toDoubleOrNull() == currentMockLongitude
            Button(
                onClick = {
                    keyboardController?.hide()
                    onSetMockLocation()
                },
                enabled = isMockLocationAvailable && isInputValid && !isLoading && !isMatchingCurrentMock,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocationColors.enabled,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text(stringResource(R.string.location_set_mock))
            }
        }
    }
}

@Composable
internal fun SavePresetDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.location_preset_dialog_title)) },
        text = {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text(stringResource(R.string.location_preset_name)) },
                placeholder = { Text(stringResource(R.string.location_preset_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(presetName) },
                enabled = presetName.isNotBlank(),
            ) {
                Text(stringResource(R.string.location_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.location_cancel))
            }
        },
    )
}
