package com.azikar24.wormaceptor.feature.mockrules.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSectionHeader
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.mockrules.R

@Composable
internal fun BasicInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)) {
        WormaCeptorSectionHeader(
            title = stringResource(R.string.mock_editor_section_basic_info),
            icon = Icons.Outlined.Info,
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.mock_editor_rule_name)) },
            placeholder = { Text(stringResource(R.string.mock_editor_rule_name_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BasicInfoSectionPreview() {
    WormaCeptorTheme {
        BasicInfoSection(
            name = "Login Error Mock",
            onNameChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BasicInfoSectionEmptyPreview() {
    WormaCeptorTheme {
        BasicInfoSection(
            name = "",
            onNameChange = {},
        )
    }
}
