package com.azikar24.wormaceptor.feature.location.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.azikar24.wormaceptor.core.ui.components.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.CardStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.location.R

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

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        style = CardStyle.Outlined,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.location_set_custom_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

            // Latitude input
            WormaCeptorTextField(
                value = latitudeInput,
                onValueChange = onLatitudeChanged,
                label = { Text(stringResource(R.string.location_latitude)) },
                placeholder = { Text(stringResource(R.string.location_latitude_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = {
                    Text(stringResource(R.string.location_latitude_range))
                },
                isError = latitudeInput.isNotBlank() &&
                    latitudeInput.toDoubleOrNull()?.let { it !in -90.0..90.0 } == true,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            // Longitude input
            WormaCeptorTextField(
                value = longitudeInput,
                onValueChange = onLongitudeChanged,
                label = { Text(stringResource(R.string.location_longitude)) },
                placeholder = { Text(stringResource(R.string.location_longitude_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = {
                    Text(stringResource(R.string.location_longitude_range))
                },
                isError = longitudeInput.isNotBlank() &&
                    longitudeInput.toDoubleOrNull()?.let { it !in -180.0..180.0 } == true,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                    WormaCeptorTokens.Spacing.sm,
                ),
            ) {
                // Get current location button
                WormaCeptorButton(
                    text = stringResource(R.string.location_current),
                    onClick = onSetToCurrentLocation,
                    variant = ButtonVariant.Outlined,
                    enabled = !isLoading,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )

                // Save as preset button
                WormaCeptorButton(
                    text = stringResource(R.string.location_save),
                    onClick = onSaveAsPreset,
                    variant = ButtonVariant.Outlined,
                    enabled = isInputValid && !isLoading,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

            // Set mock location button
            val isMatchingCurrentMock = isMockEnabled &&
                latitudeInput.toDoubleOrNull() == currentMockLatitude &&
                longitudeInput.toDoubleOrNull() == currentMockLongitude
            WormaCeptorButton(
                text = stringResource(R.string.location_set_mock),
                onClick = {
                    keyboardController?.hide()
                    onSetMockLocation()
                },
                variant = ButtonVariant.Primary,
                enabled = isMockLocationAvailable && isInputValid && !isLoading && !isMatchingCurrentMock,
                containerColor = WormaCeptorTokens.Colors.Location.enabled,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun SavePresetDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.location_preset_dialog_title)) },
        text = {
            WormaCeptorTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text(stringResource(R.string.location_preset_name)) },
                placeholder = { Text(stringResource(R.string.location_preset_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            WormaCeptorButton(
                text = stringResource(R.string.location_save),
                onClick = { onSave(presetName) },
                variant = ButtonVariant.Primary,
                enabled = presetName.isNotBlank(),
            )
        },
        dismissButton = {
            WormaCeptorButton(
                text = stringResource(R.string.location_cancel),
                onClick = onDismiss,
                variant = ButtonVariant.Text,
            )
        },
    )
}
