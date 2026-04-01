package com.azikar24.wormaceptor.feature.mockrules.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSectionHeader
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import com.azikar24.wormaceptor.feature.mockrules.R

@Composable
internal fun RequestMatchingSection(
    urlPattern: String,
    matchType: UrlMatchType,
    method: String,
    methodDropdownExpanded: Boolean,
    onUrlPatternChange: (String) -> Unit,
    onMatchTypeChange: (UrlMatchType) -> Unit,
    onMethodChange: (String) -> Unit,
    onMethodDropdownExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)) {
        WormaCeptorSectionHeader(
            title = stringResource(R.string.mock_editor_section_request_matching),
            icon = Icons.Outlined.Link,
        )

        OutlinedTextField(
            value = urlPattern,
            onValueChange = onUrlPatternChange,
            label = { Text(stringResource(R.string.mock_editor_url_pattern)) },
            placeholder = { Text(stringResource(R.string.mock_editor_url_pattern_placeholder)) },
            singleLine = true,
            supportingText = {
                Text(
                    stringResource(
                        when (matchType) {
                            UrlMatchType.EXACT -> R.string.mock_editor_match_exact_hint
                            UrlMatchType.PREFIX -> R.string.mock_editor_match_prefix_hint
                            UrlMatchType.REGEX -> R.string.mock_editor_match_regex_hint
                        },
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.mock_editor_match_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            UrlMatchType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = matchType == type,
                    onClick = { onMatchTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, UrlMatchType.entries.size),
                ) {
                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }

        MethodDropdown(
            selectedMethod = method,
            expanded = methodDropdownExpanded,
            onExpandedChange = onMethodDropdownExpandedChange,
            onMethodSelected = onMethodChange,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RequestMatchingSectionPreview() {
    WormaCeptorTheme {
        RequestMatchingSection(
            urlPattern = "https://api.example.com/login*",
            matchType = UrlMatchType.PREFIX,
            method = "POST",
            methodDropdownExpanded = false,
            onUrlPatternChange = {},
            onMatchTypeChange = {},
            onMethodChange = {},
            onMethodDropdownExpandedChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RequestMatchingSectionEmptyPreview() {
    WormaCeptorTheme {
        RequestMatchingSection(
            urlPattern = "",
            matchType = UrlMatchType.EXACT,
            method = "",
            methodDropdownExpanded = false,
            onUrlPatternChange = {},
            onMatchTypeChange = {},
            onMethodChange = {},
            onMethodDropdownExpandedChange = {},
        )
    }
}
