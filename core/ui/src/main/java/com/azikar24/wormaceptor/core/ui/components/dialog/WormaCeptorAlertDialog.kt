package com.azikar24.wormaceptor.core.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

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

/**
 * Alert dialog overload that renders custom content (forms, inputs, multi-line text) in the body.
 *
 * @param title Dialog title text
 * @param confirmLabel Confirm button label
 * @param onConfirm Confirm callback
 * @param dismissLabel Dismiss button label
 * @param onDismiss Dismiss callback (also triggered on outside tap)
 * @param modifier Modifier for the dialog
 * @param icon Optional icon displayed above the title
 * @param confirmEnabled Whether the confirm button is enabled
 * @param confirmVariant Variant for the confirm button; defaults to [ButtonVariant.Text]
 * @param content Custom content rendered in the body (e.g. a form)
 */
@Suppress("LongParameterList")
@Composable
fun WormaCeptorAlertDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    confirmEnabled: Boolean = true,
    confirmVariant: ButtonVariant = ButtonVariant.Text,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = icon?.let {
            {
                androidx.compose.material3.Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
                content = content,
            )
        },
        confirmButton = {
            WormaCeptorButton(
                text = confirmLabel,
                onClick = onConfirm,
                variant = confirmVariant,
                enabled = confirmEnabled,
            )
        },
        dismissButton = {
            WormaCeptorButton(
                text = dismissLabel,
                onClick = onDismiss,
                variant = ButtonVariant.Text,
            )
        },
    )
}

@Preview(name = "AlertDialog - Light")
@Composable
private fun WormaCeptorAlertDialogPreview() {
    WormaCeptorTheme {
        WormaCeptorAlertDialog(
            title = "Clear transactions",
            message = "Are you sure you want to clear all recorded transactions?",
            confirmLabel = "Clear",
            onConfirm = {},
            dismissLabel = "Cancel",
            onDismiss = {},
        )
    }
}

@Preview(name = "AlertDialog Destructive - Light")
@Composable
private fun WormaCeptorAlertDialogDestructivePreview() {
    WormaCeptorTheme {
        WormaCeptorAlertDialog(
            title = "Delete all data",
            message = "This action cannot be undone. All saved data will be permanently removed.",
            confirmLabel = "Delete",
            onConfirm = {},
            dismissLabel = "Cancel",
            onDismiss = {},
            icon = Icons.Default.Delete,
            destructive = true,
        )
    }
}
