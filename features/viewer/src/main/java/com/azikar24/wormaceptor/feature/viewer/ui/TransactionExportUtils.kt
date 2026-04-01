package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.Context
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewModel

/**
 * Saves PDF data to the device's Downloads directory.
 *
 * @return Message describing the result of the operation
 */
internal fun savePdfToDownloads(
    context: Context,
    pdfData: ByteArray,
): String {
    return try {
        val fileName = "wormaceptor_${System.currentTimeMillis()}.pdf"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ use MediaStore
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues,
            )

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pdfData)
                }

                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                "PDF saved to Downloads"
            } else {
                "Failed to save PDF"
            }
        } else {
            // Legacy approach for older Android versions
            @Suppress("DEPRECATION")
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS,
            )
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { it.write(pdfData) }
            "PDF saved to Downloads"
        }
    } catch (e: Exception) {
        "Failed to save PDF: ${e.message}"
    }
}

internal fun generateTextSummary(
    transaction: NetworkTransaction,
    requestBody: String? = null,
    responseBody: String? = null,
): String = buildString {
    appendLine("--- WormaCeptor Transaction ---")
    appendLine("URL: ${transaction.request.url}")
    appendLine("Method: ${transaction.request.method}")
    appendLine("Status: ${transaction.status.name}")
    transaction.response?.let { res ->
        append("Code: ${res.code}")
        if (res.message.isNotBlank()) append(" ${res.message}")
        appendLine()
        res.protocol?.let { appendLine("Protocol: $it") }
        res.tlsVersion?.let { appendLine("TLS: $it") }
        res.error?.let { appendLine("Error: $it") }
    } ?: appendLine("Code: -")
    appendLine("Duration: ${com.azikar24.wormaceptor.core.ui.util.formatDuration(transaction.durationMs)}")

    appendLine("\n[Request Headers]")
    appendLine(ViewerViewModel.formatHeaders(transaction.request.headers))

    if (!requestBody.isNullOrBlank()) {
        appendLine("\n[Request Body]")
        appendLine(requestBody)
    }

    transaction.response?.let { res ->
        appendLine("\n[Response Headers]")
        appendLine(ViewerViewModel.formatHeaders(res.headers))

        if (!responseBody.isNullOrBlank()) {
            appendLine("\n[Response Body]")
            appendLine(responseBody)
        }
    }
}
