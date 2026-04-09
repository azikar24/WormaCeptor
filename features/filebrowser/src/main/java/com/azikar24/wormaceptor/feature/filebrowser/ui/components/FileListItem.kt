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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDateOnly
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.feature.filebrowser.R
import com.azikar24.wormaceptor.feature.filebrowser.ui.util.resolveFileAppearance

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: FileEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .padding(
                horizontal = WormaCeptorTokens.Spacing.lg,
                vertical = WormaCeptorTokens.Spacing.md,
            ),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val ext = file.name.substringAfterLast('.', "").lowercase()
        val appearance = resolveFileAppearance(
            ext = ext,
            isDirectory = file.isDirectory,
            scheme = WormaCeptorTokens.Colors.FileBrowser.fileTypeScheme(),
        )

        Icon(
            imageVector = appearance.icon,
            contentDescription = null,
            tint = appearance.tint,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xxs),
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                if (!file.isDirectory) {
                    Text(
                        text = formatBytes(file.sizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "·",
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

        if (!file.isReadable) {
            Text(
                text = stringResource(R.string.filebrowser_locked),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun FileListItemPreview() {
    WormaCeptorTheme {
        Column {
            FileListItem(
                file = FileEntry(
                    name = "Documents",
                    path = "/storage/Documents",
                    isDirectory = true,
                    sizeBytes = 0,
                    lastModified = System.currentTimeMillis(),
                    permissions = "rwx",
                ),
                onClick = {},
                onLongClick = {},
            )
            FileListItem(
                file = FileEntry(
                    name = "report.json",
                    path = "/storage/report.json",
                    isDirectory = false,
                    sizeBytes = 4096,
                    lastModified = System.currentTimeMillis(),
                    permissions = "rw-",
                ),
                onClick = {},
                onLongClick = {},
            )
            FileListItem(
                file = FileEntry(
                    name = "secret.dat",
                    path = "/storage/secret.dat",
                    isDirectory = false,
                    sizeBytes = 1024,
                    lastModified = System.currentTimeMillis(),
                    permissions = "---",
                    isReadable = false,
                ),
                onClick = {},
                onLongClick = {},
            )
        }
    }
}
