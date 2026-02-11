package com.azikar24.wormaceptor.core.ui.util

import java.util.Locale

/**
 * Formats bytes into human-readable string.
 * Uses 1024-based units (KiB, MiB, etc. displayed as KB, MB for simplicity).
 *
 * @param bytes The number of bytes to format
 * @return Formatted string like "1.5 MB" or "256 B"
 */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

/**
 * Formats an Int byte count into human-readable string.
 * Convenience overload for APIs that use Int instead of Long.
 */
fun formatBytes(bytes: Int): String = formatBytes(bytes.toLong())
