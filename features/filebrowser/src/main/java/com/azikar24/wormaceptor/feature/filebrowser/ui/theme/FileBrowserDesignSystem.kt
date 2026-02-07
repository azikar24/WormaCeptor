package com.azikar24.wormaceptor.feature.filebrowser.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Feature-specific design tokens for the File Browser feature.
 *
 * For common design tokens (Spacing, CornerRadius, BorderWidth, Alpha, etc.),
 * use [com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem].
 */
object FileBrowserDesignSystem {

    /**
     * File type colors and icons.
     */
    object FileTypes {
        val folderColor = Color(0xFFFFA726) // Orange
        val imageColor = Color(0xFF42A5F5) // Blue
        val textColor = Color(0xFF66BB6A) // Green
        val databaseColor = Color(0xFF9C27B0) // Purple
        val defaultColor = Color(0xFF78909C) // Blue Grey

        fun getIcon(fileName: String, isDirectory: Boolean): ImageVector {
            if (isDirectory) return Icons.Default.Folder

            val extension = fileName.substringAfterLast('.', "").lowercase()
            return when (extension) {
                "png", "jpg", "jpeg", "gif", "webp", "bmp" -> Icons.Default.Image
                "txt", "log", "json", "xml", "html", "css", "js", "kt", "java", "md" -> Icons.Default.Description
                "db", "sqlite", "db3" -> Icons.Default.Storage
                else -> Icons.Default.InsertDriveFile
            }
        }

        fun getColor(fileName: String, isDirectory: Boolean): Color {
            if (isDirectory) return folderColor

            val extension = fileName.substringAfterLast('.', "").lowercase()
            return when (extension) {
                "png", "jpg", "jpeg", "gif", "webp", "bmp" -> imageColor
                "txt", "log", "json", "xml", "html", "css", "js", "kt", "java", "md" -> textColor
                "db", "sqlite", "db3" -> databaseColor
                else -> defaultColor
            }
        }
    }
}
