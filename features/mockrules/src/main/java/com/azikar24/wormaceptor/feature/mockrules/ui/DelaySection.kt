package com.azikar24.wormaceptor.feature.mockrules.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSectionHeader
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.mockrules.R
import com.azikar24.wormaceptor.feature.mockrules.vm.DelayType

@Composable
internal fun DelaySection(
    delayType: DelayType,
    delayMs: String,
    delayMinMs: String,
    delayMaxMs: String,
    onDelayTypeChange: (DelayType) -> Unit,
    onDelayMsChange: (String) -> Unit,
    onDelayMinMsChange: (String) -> Unit,
    onDelayMaxMsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)) {
        WormaCeptorSectionHeader(
            title = stringResource(R.string.mock_editor_section_delay),
            icon = Icons.Outlined.Timer,
        )

        Text(
            text = stringResource(R.string.mock_editor_delay_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            DelayType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = delayType == type,
                    onClick = { onDelayTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, DelayType.entries.size),
                ) {
                    Text(type.displayName)
                }
            }
        }

        when (delayType) {
            DelayType.NONE -> { /* No delay fields */ }
            DelayType.FIXED -> {
                OutlinedTextField(
                    value = delayMs,
                    onValueChange = onDelayMsChange,
                    label = { Text(stringResource(R.string.mock_editor_delay_ms)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            DelayType.RANGE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    OutlinedTextField(
                        value = delayMinMs,
                        onValueChange = onDelayMinMsChange,
                        label = { Text(stringResource(R.string.mock_editor_delay_min_ms)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = delayMaxMs,
                        onValueChange = onDelayMaxMsChange,
                        label = { Text(stringResource(R.string.mock_editor_delay_max_ms)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DelaySectionNonePreview() {
    WormaCeptorTheme {
        DelaySection(
            delayType = DelayType.NONE,
            delayMs = "0",
            delayMinMs = "0",
            delayMaxMs = "1000",
            onDelayTypeChange = {},
            onDelayMsChange = {},
            onDelayMinMsChange = {},
            onDelayMaxMsChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DelaySectionFixedPreview() {
    WormaCeptorTheme {
        DelaySection(
            delayType = DelayType.FIXED,
            delayMs = "2000",
            delayMinMs = "0",
            delayMaxMs = "1000",
            onDelayTypeChange = {},
            onDelayMsChange = {},
            onDelayMinMsChange = {},
            onDelayMaxMsChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DelaySectionRangePreview() {
    WormaCeptorTheme {
        DelaySection(
            delayType = DelayType.RANGE,
            delayMs = "0",
            delayMinMs = "500",
            delayMaxMs = "3000",
            onDelayTypeChange = {},
            onDelayMsChange = {},
            onDelayMinMsChange = {},
            onDelayMaxMsChange = {},
        )
    }
}
