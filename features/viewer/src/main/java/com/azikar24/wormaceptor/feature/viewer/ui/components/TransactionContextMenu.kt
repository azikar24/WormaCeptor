package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R

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
            text = { Text(stringResource(R.string.viewer_context_menu_copy_url)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.viewer_context_menu_copy_url),
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                onCopyUrl()
                onDismiss()
            },
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.viewer_context_menu_share)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.viewer_context_menu_share_transaction),
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                onShare()
                onDismiss()
            },
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.viewer_context_menu_copy_as_curl)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = stringResource(R.string.viewer_context_menu_copy_as_curl_description),
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                onCopyAsCurl()
                onDismiss()
            },
        )

        WormaCeptorDivider()

        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.viewer_context_menu_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.viewer_context_menu_delete_transaction),
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
