package com.azikar24.wormaceptor.feature.pushsimulator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.pushsimulator.R

@Composable
internal fun SaveTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var templateName by remember { mutableStateOf("") }
    val isValid by remember(templateName) { derivedStateOf { templateName.isNotBlank() } }

    val saveLabel = stringResource(R.string.pushsimulator_dialog_save)
    val cancelLabel = stringResource(R.string.pushsimulator_dialog_cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pushsimulator_dialog_save_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                Text(
                    text = stringResource(R.string.pushsimulator_dialog_save_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WormaCeptorTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text(stringResource(R.string.pushsimulator_dialog_template_name)) },
                    placeholder = { Text(stringResource(R.string.pushsimulator_dialog_template_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            WormaCeptorButton(
                text = saveLabel,
                onClick = { onSave(templateName) },
                variant = ButtonVariant.Primary,
                enabled = isValid,
            )
        },
        dismissButton = {
            WormaCeptorButton(
                text = cancelLabel,
                onClick = onDismiss,
                variant = ButtonVariant.Text,
            )
        },
    )
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun SaveTemplateDialogPreview() {
    WormaCeptorTheme {
        SaveTemplateDialog(
            onDismiss = {},
            onSave = {},
        )
    }
}
