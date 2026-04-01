package com.azikar24.wormaceptor.feature.mockrules.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material3.OutlinedTextField
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

@Composable
internal fun ResponseSection(
    statusCode: Int,
    statusMessage: String,
    contentType: String,
    responseBody: String,
    onStatusCodeChange: (String) -> Unit,
    onStatusMessageChange: (String) -> Unit,
    onContentTypeChange: (String) -> Unit,
    onResponseBodyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)) {
        WormaCeptorSectionHeader(
            title = stringResource(R.string.mock_editor_section_response),
            icon = Icons.AutoMirrored.Outlined.Reply,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            OutlinedTextField(
                value = statusCode.toString(),
                onValueChange = onStatusCodeChange,
                label = { Text(stringResource(R.string.mock_editor_status_code)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )

            OutlinedTextField(
                value = statusMessage,
                onValueChange = onStatusMessageChange,
                label = { Text(stringResource(R.string.mock_editor_status_message)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedTextField(
            value = contentType,
            onValueChange = onContentTypeChange,
            label = { Text(stringResource(R.string.mock_editor_content_type)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = responseBody,
            onValueChange = onResponseBodyChange,
            label = { Text(stringResource(R.string.mock_editor_response_body)) },
            placeholder = { Text(stringResource(R.string.mock_editor_response_body_placeholder)) },
            minLines = 4,
            maxLines = 12,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResponseSectionPreview() {
    WormaCeptorTheme {
        ResponseSection(
            statusCode = 500,
            statusMessage = "Internal Server Error",
            contentType = "application/json",
            responseBody = "{\"error\": \"Something went wrong\"}",
            onStatusCodeChange = {},
            onStatusMessageChange = {},
            onContentTypeChange = {},
            onResponseBodyChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResponseSectionDefaultPreview() {
    WormaCeptorTheme {
        ResponseSection(
            statusCode = 200,
            statusMessage = "OK",
            contentType = "application/json",
            responseBody = "",
            onStatusCodeChange = {},
            onStatusMessageChange = {},
            onContentTypeChange = {},
            onResponseBodyChange = {},
        )
    }
}
