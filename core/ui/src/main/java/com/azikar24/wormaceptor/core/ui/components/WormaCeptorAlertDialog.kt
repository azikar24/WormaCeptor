package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

/**
 * Unified alert dialog for WormaCeptor.
 *
 * Standardizes button ordering, styling, and destructive action coloring.
 * For dialogs with custom content (forms, inputs), use Material3 [AlertDialog] directly
 * with a `text` slot.
 *
 * @param title Dialog title text
 * @param message Dialog body text
 * @param confirmLabel Confirm button label
 * @param onConfirm Confirm callback
 * @param dismissLabel Dismiss button label
 * @param onDismiss Dismiss callback (also triggered on outside tap)
 * @param modifier Modifier for the dialog
 * @param icon Optional icon displayed above the title
 * @param destructive When true, the confirm button uses error coloring
 */
@Composable
fun WormaCeptorAlertDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = icon?.let {
            {
                androidx.compose.material3.Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (destructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (destructive) {
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                } else {
                    ButtonDefaults.textButtonColors()
                },
            ) {
                Text(
                    text = confirmLabel,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissLabel)
            }
        },
    )
}
