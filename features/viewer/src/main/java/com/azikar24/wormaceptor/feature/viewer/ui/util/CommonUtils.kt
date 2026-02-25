package com.azikar24.wormaceptor.feature.viewer.ui.util

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI

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
fun shareText(
    context: Context,
    text: String,
    title: String = "Share",
    subject: String? = null,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    }
    context.startActivity(Intent.createChooser(intent, title))
}

/**
 * Shares content as a file using the Android share sheet.
 * Creates a temporary file and shares it via FileProvider.
 * Runs file I/O on background thread to avoid blocking UI.
 *
 * @param context Android context
 * @param content The content to write to the file
 * @param fileName The name for the shared file (e.g., "response.json")
 * @param mimeType The MIME type of the content (e.g., "application/json", "text/plain")
 * @param title Optional title for the share chooser dialog
 * @param onMessage Callback for status messages (preparing, error)
 */
suspend fun shareAsFile(
    context: Context,
    content: String,
    fileName: String,
    mimeType: String = "text/plain",
    title: String = "Share File",
    onMessage: (String) -> Unit = {},
) {
    // Show immediate feedback
    withContext(Dispatchers.Main) {
        onMessage("Preparing file (${formatBytes(content.length.toLong())})...")
    }

    try {
        // Write file on IO thread
        val uri = withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "shared_bodies")
            cacheDir.mkdirs()
            val file = File(cacheDir, fileName)

            // Use buffered writer for large content
            file.bufferedWriter().use { writer ->
                writer.write(content)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.wormaceptor.fileprovider",
                file,
            )
        }

        // Launch share intent on main thread
        withContext(Dispatchers.Main) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            onMessage("Failed to share file: ${e.message}")
        }
    }
}

/**
 * Determines the appropriate file extension and MIME type based on content type.
 *
 * @param contentType The detected content type
 * @return Pair of (file extension, MIME type)
 */
fun getFileInfoForContentType(contentType: String?): Pair<String, String> {
    return when {
        contentType?.contains("json", ignoreCase = true) == true -> "json" to "application/json"
        contentType?.contains("xml", ignoreCase = true) == true -> "xml" to "application/xml"
        contentType?.contains("html", ignoreCase = true) == true -> "html" to "text/html"
        else -> "txt" to "text/plain"
    }
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
 * Builds a full URL from host and path components.
 *
 * @param host The host name
 * @param path The path
 * @param scheme The URL scheme (default: https)
 * @return The complete URL string
 */
fun buildFullUrl(
    host: String,
    path: String,
    scheme: String = "https",
): String {
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
fun getStatusColor(
    status: TransactionStatus,
    code: Int?,
): Color {
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
fun getMethodColor(method: String): Color = WormaCeptorColors.HttpMethod.forMethod(method)
