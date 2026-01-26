package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.TransactionSummary

/**
 * Data class representing a quick action in the context menu.
 */
data class QuickAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * Context menu for transaction items.
 * Appears on long-press and provides quick actions like copy, share, delete, etc.
 */
@Composable
fun TransactionContextMenu(
    transaction: TransactionSummary,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCopyUrl: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onCopyAsCurl: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier,
        offset = offset,
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        DropdownMenuItem(
            text = { Text("Copy URL") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy URL",
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                onCopyUrl()
                onDismiss()
            },
        )

        DropdownMenuItem(
            text = { Text("Share") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share transaction",
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                onShare()
                onDismiss()
            },
        )

        DropdownMenuItem(
            text = { Text("Copy as cURL") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Copy as cURL command",
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                onCopyAsCurl()
                onDismiss()
            },
        )

        HorizontalDivider()

        DropdownMenuItem(
            text = {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete transaction",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                onDelete()
                onDismiss()
            },
        )
    }
}
