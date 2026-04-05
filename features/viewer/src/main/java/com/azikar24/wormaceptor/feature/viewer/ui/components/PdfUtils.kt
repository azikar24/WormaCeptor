package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.feature.viewer.R
import java.io.File

internal fun extractPdfTitle(data: ByteArray): String? {
    return try {
        val text = data.decodeToString(throwOnInvalidSequence = false)
        val titleRegex = """/Title\s*\(([^)]+)\)""".toRegex()
        titleRegex.find(text)?.groupValues?.getOrNull(1)
    } catch (_: Exception) {
        null
    }
}

private const val PdfHeaderSize = 20
private const val PdfVersionOffset = 5
private const val PDF_MAGIC_SIZE = 5
private const val PDF_MAGIC = "%PDF-"

internal fun extractPdfVersion(data: ByteArray): String? {
    return try {
        val header = data.take(PdfHeaderSize).toByteArray().decodeToString()
        if (header.startsWith("%PDF-")) {
            header.substring(PdfVersionOffset).takeWhile { it.isDigit() || it == '.' }
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

internal fun isPdfContent(
    contentType: String?,
    bodyBytes: ByteArray?,
): Boolean {
    if (contentType?.contains("pdf", ignoreCase = true) == true) {
        return true
    }
    if (bodyBytes != null && bodyBytes.size >= PDF_MAGIC_SIZE) {
        val header = bodyBytes.take(PDF_MAGIC_SIZE).toByteArray().decodeToString()
        return header == PDF_MAGIC
    }
    return false
}

@Suppress("TooGenericExceptionCaught")
internal fun sharePdf(
    context: Context,
    pdfData: ByteArray,
    existingFile: File?,
): String? {
    return try {
        val file = existingFile ?: run {
            val newFile = File(context.cacheDir, "WormaCeptor_${System.currentTimeMillis()}.pdf")
            newFile.outputStream().use { it.write(pdfData) }
            newFile
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.wormaceptor.fileprovider",
            file,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.viewer_pdf_share_chooser)),
        )
        null
    } catch (e: Exception) {
        context.getString(R.string.viewer_pdf_share_failed, e.message.orEmpty())
    }
}
