package com.azikar24.wormaceptor.feature.filebrowser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorBottomSheet
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.FileInfo
import com.azikar24.wormaceptor.feature.filebrowser.R
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewEvent

@Composable
fun FileInfoSheet(
    fileInfo: FileInfo,
    onEvent: (FileBrowserViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorBottomSheet(
        onDismissRequest = { onEvent(FileBrowserViewEvent.HideFileInfo) },
        modifier = modifier,
    ) {
        FileInfoSheetContent(
            fileInfo = fileInfo,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun FileInfoSheetContent(
    fileInfo: FileInfo,
    onEvent: (FileBrowserViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.filebrowser_file_information),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        InfoRow(stringResource(R.string.filebrowser_label_name), fileInfo.name)

        FilePathRow(
            path = fileInfo.path,
            onCopyPath = { onEvent(FileBrowserViewEvent.CopyFilePath(fileInfo.path)) },
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        InfoRow(stringResource(R.string.filebrowser_label_size), formatBytes(fileInfo.sizeBytes))

        InfoRow(stringResource(R.string.filebrowser_label_modified), formatTimestampFull(fileInfo.lastModified))

        fileInfo.mimeType?.let {
            InfoRow(stringResource(R.string.filebrowser_label_type), it)
        }

        fileInfo.extension?.let {
            InfoRow(stringResource(R.string.filebrowser_label_extension), it)
        }

        InfoRow(
            stringResource(R.string.filebrowser_label_permissions),
            formatPermissions(
                isReadable = fileInfo.isReadable,
                isWritable = fileInfo.isWritable,
                readLabel = stringResource(R.string.filebrowser_permission_read),
                writeLabel = stringResource(R.string.filebrowser_permission_write),
                noneLabel = stringResource(R.string.filebrowser_permission_none),
            ),
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))
        WormaCeptorDivider()
        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        ActionButtons(
            isWritable = fileInfo.isWritable,
            onDismiss = { onEvent(FileBrowserViewEvent.HideFileInfo) },
            onDelete = {
                onEvent(FileBrowserViewEvent.DeleteFile(fileInfo.path))
                onEvent(FileBrowserViewEvent.HideFileInfo)
            },
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))
    }
}

@Composable
private fun FilePathRow(
    path: String,
    onCopyPath: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.filebrowser_label_path),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = path,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onCopyPath) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.filebrowser_copy_path),
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isWritable: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        WormaCeptorButton(
            text = stringResource(R.string.filebrowser_close),
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Text,
        )

        if (isWritable) {
            WormaCeptorButton(
                text = stringResource(R.string.filebrowser_delete),
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Destructive,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorTokens.Spacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatPermissions(
    isReadable: Boolean,
    isWritable: Boolean,
    readLabel: String,
    writeLabel: String,
    noneLabel: String,
): String = buildString {
    if (isReadable) append(readLabel)
    if (isWritable) {
        if (isNotEmpty()) append(", ")
        append(writeLabel)
    }
    if (isEmpty()) append(noneLabel)
}

@Preview(showBackground = true)
@Composable
private fun FileInfoSheetContentPreview() {
    WormaCeptorTheme {
        FileInfoSheetContent(
            fileInfo = FileInfo(
                name = "example.json",
                path = "/data/data/com.example/files/example.json",
                sizeBytes = 4096,
                lastModified = System.currentTimeMillis(),
                mimeType = "application/json",
                isReadable = true,
                isWritable = true,
                extension = "json",
                parentPath = "/data/data/com.example/files",
            ),
            onEvent = {},
        )
    }
}
