/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.feature.filebrowser.ui.theme.FileBrowserDesignSystem
import java.io.File

/**
 * Screen for viewing file content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(filePath: String, content: FileContent, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val fileName = File(filePath).name

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (content) {
                is FileContent.Text -> {
                    TextFileContent(content)
                }
                is FileContent.Binary -> {
                    BinaryFileContent(content)
                }
                is FileContent.Image -> {
                    ImageFileContent(content)
                }
                is FileContent.TooLarge -> {
                    ErrorContent(
                        "File too large: ${formatBytes(content.sizeBytes)}\nMax size: ${formatBytes(content.maxSize)}",
                    )
                }
                is FileContent.Error -> {
                    ErrorContent(content.message)
                }
            }
        }
    }
}

@Composable
private fun TextFileContent(content: FileContent.Text) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(FileBrowserDesignSystem.Spacing.lg),
    ) {
        Text(
            text = content.content,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BinaryFileContent(content: FileContent.Binary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(FileBrowserDesignSystem.Spacing.lg),
    ) {
        Text(
            text = "Binary file (${formatBytes(content.displaySize.toLong())})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = FileBrowserDesignSystem.Spacing.md),
        )

        Text(
            text = "Hex Preview:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = FileBrowserDesignSystem.Spacing.sm),
        )

        Text(
            text = content.previewHex,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ImageFileContent(content: FileContent.Image) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FileBrowserDesignSystem.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${content.width} x ${content.height}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = FileBrowserDesignSystem.Spacing.md),
        )

        val bitmap = BitmapFactory.decodeByteArray(content.bytes, 0, content.bytes.size)
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Image preview",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
