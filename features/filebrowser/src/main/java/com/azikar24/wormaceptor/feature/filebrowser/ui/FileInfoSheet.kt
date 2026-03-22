package com.azikar24.wormaceptor.feature.filebrowser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.FileInfo
import com.azikar24.wormaceptor.feature.filebrowser.R

/**
 * Bottom sheet showing detailed file information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileInfoSheet(
    fileInfo: FileInfo,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Title
            Text(
                text = stringResource(R.string.filebrowser_file_information),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // File name
            InfoRow(stringResource(R.string.filebrowser_label_name), fileInfo.name)

            // File path with copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = fileInfo.path,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(
                    onClick = {
                        val message = copyToClipboard(context, "File Path", fileInfo.path)
                        onShowMessage(message)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.filebrowser_copy_path),
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // File size
            InfoRow(stringResource(R.string.filebrowser_label_size), formatBytes(fileInfo.sizeBytes))

            // Last modified
            InfoRow(stringResource(R.string.filebrowser_label_modified), formatTimestampFull(fileInfo.lastModified))

            // MIME type
            val typeLabel = stringResource(R.string.filebrowser_label_type)
            fileInfo.mimeType?.let {
                InfoRow(typeLabel, it)
            }

            // Extension
            val extensionLabel = stringResource(R.string.filebrowser_label_extension)
            fileInfo.extension?.let {
                InfoRow(extensionLabel, it)
            }

            // Permissions
            val permissionRead = stringResource(R.string.filebrowser_permission_read)
            val permissionWrite = stringResource(R.string.filebrowser_permission_write)
            val permissionNone = stringResource(R.string.filebrowser_permission_none)
            val permissions = buildString {
                append(if (fileInfo.isReadable) permissionRead else "")
                if (fileInfo.isWritable) {
                    if (isNotEmpty()) append(", ")
                    append(permissionWrite)
                }
                if (isEmpty()) append(permissionNone)
            }
            InfoRow(stringResource(R.string.filebrowser_label_permissions), permissions)

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
            WormaCeptorDivider()
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.filebrowser_close))
                }

                if (fileInfo.isWritable) {
                    Button(
                        onClick = {
                            onDelete(fileInfo.path)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                        Text(stringResource(R.string.filebrowser_delete))
                    }
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
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
            .padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
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
