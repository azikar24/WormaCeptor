package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
fun BulkActionBar(
    selectedCount: Int,
    totalCount: Int,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onExportAsHar: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    WormaCeptorTopBar(
        title = stringResource(R.string.viewer_bulk_selected_count, selectedCount),
        modifier = modifier,
        navigationIcon = {
            WormaCeptorIconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.viewer_bulk_cancel_selection),
                )
            }
        },
        actions = {
            if (selectedCount < totalCount) {
                WormaCeptorIconButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = stringResource(R.string.viewer_bulk_select_all),
                    )
                }
            } else {
                WormaCeptorIconButton(onClick = onDeselectAll) {
                    Icon(
                        imageVector = Icons.Outlined.Deselect,
                        contentDescription = stringResource(R.string.viewer_bulk_deselect_all),
                    )
                }
            }

            WormaCeptorIconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.viewer_bulk_share_selected),
                )
            }

            WormaCeptorIconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.viewer_bulk_export_selected),
                )
            }

            WormaCeptorIconButton(onClick = onExportAsHar) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.viewer_bulk_export_selected_as_har),
                )
            }

            WormaCeptorIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.viewer_bulk_delete_selected),
                    tint = WormaCeptorTokens.semantic().error,
                )
            }
        },
    )
}
