package com.azikar24.wormaceptor.feature.mockrules.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.mock.RequestMatcher
import com.azikar24.wormaceptor.feature.mockrules.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MethodDropdown(
    selectedMethod: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMethodSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anyLabel = stringResource(R.string.mock_editor_method_any)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedMethod.ifBlank { anyLabel },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.mock_editor_http_method)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text(anyLabel) },
                onClick = {
                    onMethodSelected("")
                    onExpandedChange(false)
                },
            )
            RequestMatcher.COMMON_METHODS.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method) },
                    onClick = {
                        onMethodSelected(method)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MethodDropdownCollapsedPreview() {
    WormaCeptorTheme {
        MethodDropdown(
            selectedMethod = "GET",
            expanded = false,
            onExpandedChange = {},
            onMethodSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MethodDropdownExpandedPreview() {
    WormaCeptorTheme {
        MethodDropdown(
            selectedMethod = "",
            expanded = true,
            onExpandedChange = {},
            onMethodSelected = {},
        )
    }
}
