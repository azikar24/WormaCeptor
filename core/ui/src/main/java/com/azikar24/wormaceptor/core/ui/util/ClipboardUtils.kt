package com.azikar24.wormaceptor.core.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Maximum size in bytes for clipboard copy operations.
 * Content larger than this should use shareAsFile instead.
 */
const val MAX_CLIPBOARD_SIZE = 100_000 // 100KB

/**
 * Copies text to the system clipboard.
 *
 * @param context Android context for clipboard service access
 * @param label Label for the clipboard data (shown in some Android versions)
 * @param text The text to copy
 * @return Confirmation message to display to the user
 */
fun copyToClipboard(
    context: Context,
    label: String,
    text: String,
): String {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    return "$label copied to clipboard"
}

/**
 * Checks if content is too large for clipboard and returns appropriate action.
 *
 * @param text The text to check
 * @return true if content is too large for clipboard
 */
fun isContentTooLargeForClipboard(text: String): Boolean {
    return text.length > MAX_CLIPBOARD_SIZE
}

/**
 * Result of a clipboard copy operation with size check.
 */
sealed class ClipboardResult {
    /**
     * The content was successfully copied to the clipboard.
     *
     * @property message Confirmation message to display to the user.
     */
    data class Success(val message: String) : ClipboardResult()

    /**
     * The content exceeds the maximum clipboard size.
     *
     * @property message Warning message describing the size limit.
     */
    data class TooLarge(val message: String) : ClipboardResult()
}

/**
 * Copies text to clipboard if small enough, otherwise returns a warning.
 *
 * @param context Android context
 * @param label Label for the clipboard data
 * @param text The text to copy
 * @return ClipboardResult indicating success or too-large warning
 */
fun copyToClipboardWithSizeCheck(
    context: Context,
    label: String,
    text: String,
): ClipboardResult {
    return if (isContentTooLargeForClipboard(text)) {
        ClipboardResult.TooLarge(
            "Content too large (${formatBytes(text.length.toLong())}). Use 'Share as File' instead.",
        )
    } else {
        val message = copyToClipboard(context, label, text)
        ClipboardResult.Success(message)
    }
}
