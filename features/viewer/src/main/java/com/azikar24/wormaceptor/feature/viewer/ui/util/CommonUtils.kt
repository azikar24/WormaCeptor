/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import java.net.URI

// ============================================================================
// BYTE FORMATTING UTILITIES
// ============================================================================

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
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

// ============================================================================
// CLIPBOARD UTILITIES
// ============================================================================

/**
 * Copies text to the system clipboard and shows a toast confirmation.
 *
 * @param context Android context for clipboard service access
 * @param label Label for the clipboard data (shown in some Android versions)
 * @param text The text to copy
 */
fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

// ============================================================================
// SHARE UTILITIES
// ============================================================================

/**
 * Shares text content using the Android share sheet.
 *
 * @param context Android context for starting the share activity
 * @param text The text content to share
 * @param title Optional title for the share chooser dialog
 * @param subject Optional subject line (used by email apps)
 */
fun shareText(context: Context, text: String, title: String = "Share", subject: String? = null) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    }
    context.startActivity(Intent.createChooser(intent, title))
}

// ============================================================================
// URL PARSING UTILITIES
// ============================================================================

/**
 * Extracts the path from a URL string.
 *
 * @param url The full URL string
 * @return The path portion of the URL, or the original URL if parsing fails
 */
fun extractUrlPath(url: String): String {
    return try {
        val uri = URI(url)
        uri.path ?: url
    } catch (_: Exception) {
        url
    }
}

/**
 * Extracts the host from a URL string.
 *
 * @param url The full URL string
 * @return The host portion of the URL, or empty string if parsing fails
 */
fun extractUrlHost(url: String): String {
    return try {
        val uri = URI(url)
        uri.host ?: ""
    } catch (_: Exception) {
        ""
    }
}

/**
 * Builds a full URL from host and path components.
 *
 * @param host The host name
 * @param path The path
 * @param scheme The URL scheme (default: https)
 * @return The complete URL string
 */
fun buildFullUrl(host: String, path: String, scheme: String = "https"): String {
    return "$scheme://$host$path"
}

// ============================================================================
// STATUS COLOR UTILITIES
// ============================================================================

/**
 * Determines the appropriate status color based on transaction status and response code.
 *
 * Color scheme:
 * - Green (2xx): Successful responses
 * - Blue (3xx): Redirect responses
 * - Amber (4xx or null code): Client errors or pending
 * - Red (5xx or FAILED): Server errors or failed requests
 * - Grey: Active/pending requests
 *
 * @param status The transaction status
 * @param code The HTTP response code (nullable)
 * @return The appropriate Color for the status
 */
@Composable
fun getStatusColor(status: TransactionStatus, code: Int?): Color {
    return when (status) {
        TransactionStatus.COMPLETED -> when {
            code == null -> WormaCeptorColors.StatusAmber
            code in 200..299 -> WormaCeptorColors.StatusGreen
            code in 300..399 -> WormaCeptorColors.StatusBlue
            code in 400..499 -> WormaCeptorColors.StatusAmber
            code in 500..599 -> WormaCeptorColors.StatusRed
            else -> WormaCeptorColors.StatusGrey
        }
        TransactionStatus.FAILED -> WormaCeptorColors.StatusRed
        TransactionStatus.ACTIVE -> WormaCeptorColors.StatusGrey
    }
}

/**
 * Non-composable version of getStatusColor for use in non-Compose contexts.
 */
fun getStatusColorValue(status: TransactionStatus, code: Int?): Color {
    return when (status) {
        TransactionStatus.COMPLETED -> when {
            code == null -> WormaCeptorColors.StatusAmber
            code in 200..299 -> WormaCeptorColors.StatusGreen
            code in 300..399 -> WormaCeptorColors.StatusBlue
            code in 400..499 -> WormaCeptorColors.StatusAmber
            code in 500..599 -> WormaCeptorColors.StatusRed
            else -> WormaCeptorColors.StatusGrey
        }
        TransactionStatus.FAILED -> WormaCeptorColors.StatusRed
        TransactionStatus.ACTIVE -> WormaCeptorColors.StatusGrey
    }
}

/**
 * Determines the color for HTTP methods.
 *
 * @param method The HTTP method string
 * @return The appropriate Color for the method
 */
fun getMethodColor(method: String): Color = when (method.uppercase()) {
    "GET" -> WormaCeptorColors.StatusGreen
    "POST" -> WormaCeptorColors.StatusBlue
    "PUT" -> WormaCeptorColors.StatusAmber
    "DELETE" -> WormaCeptorColors.StatusRed
    "PATCH" -> Color(0xFF9C27B0)
    else -> WormaCeptorColors.StatusGrey
}
