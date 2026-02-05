/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.feature.filebrowser.R
import com.azikar24.wormaceptor.feature.filebrowser.ui.theme.FileBrowserDesignSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * List item displaying a file or directory entry.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(file: FileEntry, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.md,
            ),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // File icon
        Icon(
            imageVector = FileBrowserDesignSystem.FileTypes.getIcon(file.name, file.isDirectory),
            contentDescription = null,
            tint = FileBrowserDesignSystem.FileTypes.getColor(file.name, file.isDirectory),
            modifier = Modifier.size(24.dp),
        )

        // File info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
        ) {
            // File name
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // File details
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                if (!file.isDirectory) {
                    Text(
                        text = formatFileSize(file.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = formatDate(file.lastModified),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Permissions indicator
        if (!file.isReadable) {
            Text(
                text = stringResource(R.string.filebrowser_locked),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatDate(millis: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(millis))
}
