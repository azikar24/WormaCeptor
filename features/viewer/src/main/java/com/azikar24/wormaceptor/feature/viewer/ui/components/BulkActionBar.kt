package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.R

/**
 * Action bar that appears when items are selected in multi-select mode.
 * Provides bulk operations like share, export, and delete.
 *
 * Note: This composable renders its content unconditionally.
 * The parent is responsible for controlling visibility (e.g., via Crossfade).
 */
@Composable
fun BulkActionBar(
    selectedCount: Int,
    totalCount: Int,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.defaultMinSize(minHeight = 64.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.xs,
                )
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left side: Cancel button and selection count
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.viewer_bulk_cancel_selection),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))

                Text(
                    text = stringResource(R.string.viewer_bulk_selected_count, selectedCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Right side: Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Select all / Deselect all button
                if (selectedCount < totalCount) {
                    IconButton(onClick = onSelectAll) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = stringResource(R.string.viewer_bulk_select_all),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    IconButton(onClick = onDeselectAll) {
                        Icon(
                            imageVector = Icons.Outlined.Deselect,
                            contentDescription = stringResource(R.string.viewer_bulk_deselect_all),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.viewer_bulk_share_selected),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }

                IconButton(onClick = onExport) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(R.string.viewer_bulk_export_selected),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.viewer_bulk_delete_selected),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
