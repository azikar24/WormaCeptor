package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.feature.viewer.R

/**
 * Action bar that appears when items are selected in multi-select mode.
 * Provides bulk operations like share, export, and delete.
 *
 * Uses Material3 TopAppBar to guarantee identical height, padding,
 * and status bar inset handling as the normal app bar.
 *
 * Note: This composable renders its content unconditionally.
 * The parent is responsible for controlling visibility (e.g., via Crossfade).
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.viewer_bulk_selected_count, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.viewer_bulk_cancel_selection),
                )
            }
        },
        actions = {
            if (selectedCount < totalCount) {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = stringResource(R.string.viewer_bulk_select_all),
                    )
                }
            } else {
                IconButton(onClick = onDeselectAll) {
                    Icon(
                        imageVector = Icons.Outlined.Deselect,
                        contentDescription = stringResource(R.string.viewer_bulk_deselect_all),
                    )
                }
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.viewer_bulk_share_selected),
                )
            }

            IconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.viewer_bulk_export_selected),
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.viewer_bulk_delete_selected),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        modifier = modifier,
    )
}
