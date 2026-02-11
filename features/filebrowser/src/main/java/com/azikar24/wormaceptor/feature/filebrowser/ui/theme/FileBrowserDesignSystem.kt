package com.azikar24.wormaceptor.feature.filebrowser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
     * Syntax highlighting colors for JSON and XML content in the file viewer.
     * Provides light and dark variants for theme awareness.
     */
    @Immutable
    data class SyntaxColors(
        val jsonKey: Color,
        val jsonString: Color,
        val jsonNumber: Color,
        val jsonBoolNull: Color,
        val jsonBracket: Color,
        val xmlTag: Color,
        val xmlAttrName: Color,
        val xmlAttrValue: Color,
        val xmlContent: Color,
        val xmlComment: Color,
    )

    val LightSyntaxColors = SyntaxColors(
        jsonKey = Color(0xFF9C27B0),
        jsonString = Color(0xFF2E7D32),
        jsonNumber = Color(0xFF1565C0),
        jsonBoolNull = Color(0xFFE64A19),
        jsonBracket = Color(0xFF616161),
        xmlTag = Color(0xFF1565C0),
        xmlAttrName = Color(0xFF9C27B0),
        xmlAttrValue = Color(0xFF2E7D32),
        xmlContent = Color(0xFF212121),
        xmlComment = Color(0xFF757575),
    )

    val DarkSyntaxColors = SyntaxColors(
        jsonKey = Color(0xFFCE93D8),
        jsonString = Color(0xFF81C784),
        jsonNumber = Color(0xFF64B5F6),
        jsonBoolNull = Color(0xFFFF8A65),
        jsonBracket = Color(0xFFBDBDBD),
        xmlTag = Color(0xFF64B5F6),
        xmlAttrName = Color(0xFFCE93D8),
        xmlAttrValue = Color(0xFF81C784),
        xmlContent = Color(0xFFE0E0E0),
        xmlComment = Color(0xFF9E9E9E),
    )

    @Composable
    fun syntaxColors(darkTheme: Boolean = isSystemInDarkTheme()): SyntaxColors =
        if (darkTheme) DarkSyntaxColors else LightSyntaxColors

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
