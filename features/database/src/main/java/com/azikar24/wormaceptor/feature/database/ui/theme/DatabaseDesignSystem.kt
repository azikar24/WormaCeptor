/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.database.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design system for the Database Browser feature.
 */
object DatabaseDesignSystem {

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
        val tableCell = RoundedCornerShape(CornerRadius.xs)
    }

    /**
     * Colors for different data types.
     */
    object DataTypeColors {
        val integer = Color(0xFF4FC3F7) // Light Blue
        val real = Color(0xFF81C784) // Light Green
        val text = Color(0xFFFFB74D) // Orange
        val blob = Color(0xFFBA68C8) // Purple
        val nullValue = Color(0xFF90A4AE) // Blue Grey
        val primaryKey = Color(0xFFFFD54F) // Amber

        fun forType(type: String): Color {
            return when (type.uppercase()) {
                "INTEGER", "INT", "BIGINT", "SMALLINT", "TINYINT" -> integer
                "REAL", "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC" -> real
                "TEXT", "VARCHAR", "CHAR", "CLOB" -> text
                "BLOB", "BINARY", "VARBINARY" -> blob
                else -> text
            }
        }
    }

    /**
     * Syntax highlighting colors for SQL.
     */
    object SqlSyntaxColors {
        val keyword = Color(0xFF569CD6) // Blue
        val string = Color(0xFFCE9178) // Orange
        val number = Color(0xFFB5CEA8) // Green
        val function = Color(0xFFDCDCAA) // Yellow
        val operator = Color(0xFFD4D4D4) // Light Grey
        val comment = Color(0xFF6A9955) // Green
        val table = Color(0xFF4EC9B0) // Cyan
    }
}
