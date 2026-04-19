package com.azikar24.wormaceptor.feature.viewer.vm

import android.graphics.Bitmap
import com.azikar24.wormaceptor.domain.entities.PdfMetadata

internal data class PdfViewerViewState(
    val pages: List<Bitmap> = emptyList(),
    val pageCount: Int = 0,
    val isLoading: Boolean = true,
    val error: PdfViewerError? = null,
    val metadata: PdfMetadata? = null,
    val showControls: Boolean = true,
    val showThumbnails: Boolean = false,
    val showPageJumpDialog: Boolean = false,
    val currentPage: Int = 0,
)

internal sealed class PdfViewerError {
    data object NoPages : PdfViewerError()
    data object PasswordProtected : PdfViewerError()
    data class LoadFailed(val message: String?) : PdfViewerError()
}
