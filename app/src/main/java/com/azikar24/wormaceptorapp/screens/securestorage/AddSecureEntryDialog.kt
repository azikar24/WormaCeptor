package com.azikar24.wormaceptorapp.screens.securestorage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Composable
internal fun AddSecureEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (key: String, value: String) -> Unit,
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    WormaCeptorAlertDialog(
        title = "Add Encrypted Entry",
        confirmLabel = "Add",
        onConfirm = { onAdd(key, value) },
        dismissLabel = "Cancel",
        onDismiss = onDismiss,
        confirmEnabled = key.isNotBlank() && value.isNotBlank(),
        confirmVariant = ButtonVariant.Primary,
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
}
