package com.azikar24.wormaceptorapp.screens.securestorage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Composable
internal fun AddSecureEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (key: String, value: String) -> Unit,
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Encrypted Entry") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                WormaCeptorTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key") },
                    placeholder = { Text("my_secret_key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                WormaCeptorTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    placeholder = { Text("secret_value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "This value will be encrypted using AES-256-GCM",
                    style = MaterialTheme.typography.labelSmall,
                    color = WormaCeptorTokens.semantic().textSecondary,
                )
            }
        },
        confirmButton = {
            WormaCeptorButton(
                text = "Add",
                onClick = { onAdd(key, value) },
                enabled = key.isNotBlank() && value.isNotBlank(),
            )
        },
        dismissButton = {
            WormaCeptorButton(
                text = "Cancel",
                onClick = onDismiss,
                variant = ButtonVariant.Text,
            )
        },
    )
}
