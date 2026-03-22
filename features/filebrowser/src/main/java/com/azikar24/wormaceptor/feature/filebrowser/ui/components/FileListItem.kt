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
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDateOnly
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.feature.filebrowser.R
import com.azikar24.wormaceptor.feature.filebrowser.ui.theme.FileBrowserDesignSystem

/**
 * List item displaying a file or directory entry.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: FileEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        text = formatBytes(file.sizeBytes),
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
                    text = formatDateOnly(file.lastModified),
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
