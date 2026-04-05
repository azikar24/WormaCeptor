package com.azikar24.wormaceptor.feature.viewer.vm

import java.io.File

internal sealed class PdfViewerViewEffect {
    data object Dismiss : PdfViewerViewEffect()
    data object Download : PdfViewerViewEffect()
    class SharePdf(val pdfData: ByteArray, val tempFile: File?) : PdfViewerViewEffect()
    data class ShowSnackBar(val message: String) : PdfViewerViewEffect()
}
