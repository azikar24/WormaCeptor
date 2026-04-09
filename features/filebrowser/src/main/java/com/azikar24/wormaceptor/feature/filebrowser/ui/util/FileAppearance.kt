package com.azikar24.wormaceptor.feature.filebrowser.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors

internal val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp")
internal val textExtensions = setOf("txt", "log", "json", "xml", "html", "css", "js", "kt", "java", "md")
internal val databaseExtensions = setOf("db", "sqlite", "db3")

internal data class FileAppearance(
    val icon: ImageVector,
    val tint: Color,
)

internal fun resolveFileAppearance(
    ext: String,
    isDirectory: Boolean,
    scheme: ToolColors.FileBrowser.FileTypeScheme,
): FileAppearance = when {
    isDirectory -> FileAppearance(Icons.Default.Folder, scheme.folder)
    ext in imageExtensions -> FileAppearance(Icons.Default.Image, scheme.image)
    ext in textExtensions -> FileAppearance(Icons.Default.Description, scheme.text)
    ext in databaseExtensions -> FileAppearance(Icons.Default.Storage, scheme.database)
    else -> FileAppearance(Icons.AutoMirrored.Filled.InsertDriveFile, scheme.other)
}
