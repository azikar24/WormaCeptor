package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Action bar that appears when items are selected in multi-select mode.
 * Provides bulk operations like share, export, and delete.
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
    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = WormaCeptorDesignSystem.Elevation.sm,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
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
                            contentDescription = "Cancel selection",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }

                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))

                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                                contentDescription = "Select all",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    } else {
                        IconButton(onClick = onDeselectAll) {
                            Icon(
                                imageVector = Icons.Outlined.Deselect,
                                contentDescription = "Deselect all",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    IconButton(onClick = onExport) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete selected",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}
