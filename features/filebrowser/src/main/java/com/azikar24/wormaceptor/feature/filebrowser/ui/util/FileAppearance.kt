package com.azikar24.wormaceptor.feature.filebrowser.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector

internal val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp")
internal val textExtensions = setOf("txt", "log", "json", "xml", "html", "css", "js", "kt", "java", "md")
internal val databaseExtensions = setOf("db", "sqlite", "db3")

internal fun resolveFileIcon(
    ext: String,
    isDirectory: Boolean,
): ImageVector = when {
    isDirectory -> Icons.Default.Folder
    ext in imageExtensions -> Icons.Default.Image
    ext in textExtensions -> Icons.Default.Description
    ext in databaseExtensions -> Icons.Default.Storage
    else -> Icons.AutoMirrored.Filled.InsertDriveFile
}
