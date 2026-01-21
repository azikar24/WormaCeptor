/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Design system for the File Browser feature.
 */
object FileBrowserDesignSystem {

    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
    }

    object CornerRadius {
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
    }

    object Shapes {
        val card = RoundedCornerShape(CornerRadius.md)
        val chip = RoundedCornerShape(CornerRadius.xs)
    }

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
