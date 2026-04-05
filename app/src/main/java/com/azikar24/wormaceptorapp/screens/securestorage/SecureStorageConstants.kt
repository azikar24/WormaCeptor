package com.azikar24.wormaceptorapp.screens.securestorage

import androidx.compose.ui.graphics.Color

internal val SecureGreen = Color(0xFF4CAF50)

internal fun maskValue(value: String): String {
    return if (value.length <= 8) {
        "*".repeat(value.length)
    } else {
        value.take(4) + "*".repeat(minOf(8, value.length - 8)) + value.takeLast(4)
    }
}

internal fun getTypeColor(type: String): Color {
    return when (type) {
        "String" -> Color(0xFF2196F3)
        "Int" -> Color(0xFF4CAF50)
        "Long" -> Color(0xFF9C27B0)
        "Float" -> Color(0xFFFF9800)
        "Boolean" -> Color(0xFFE91E63)
        "StringSet" -> Color(0xFF00BCD4)
        else -> Color(0xFF757575)
    }
}
