package com.azikar24.wormaceptor.feature.viewer.vm

import java.io.File

internal sealed class PdfViewerViewEvent {
    class LoadPdf(val pdfData: ByteArray, val initialPage: Int, val cacheDir: File) : PdfViewerViewEvent()
    data class RequestPage(val page: Int) : PdfViewerViewEvent()
    data class RequestThumbnail(val page: Int) : PdfViewerViewEvent()

    /** The page left composition; drop its render if still queued or running. */
    data class CancelPage(val page: Int) : PdfViewerViewEvent()

    /** The thumbnail left composition; drop its render if still queued or running. */
    data class CancelThumbnail(val page: Int) : PdfViewerViewEvent()
    data class PageChanged(val page: Int) : PdfViewerViewEvent()
    data object ToggleControls : PdfViewerViewEvent()
    data object ToggleThumbnails : PdfViewerViewEvent()
    data object ShowPageJumpDialog : PdfViewerViewEvent()
    data object DismissPageJumpDialog : PdfViewerViewEvent()
    data class JumpToPage(val page: Int) : PdfViewerViewEvent()
    data object GoToFirstPage : PdfViewerViewEvent()
    data object GoToLastPage : PdfViewerViewEvent()
    data object GoToPreviousPage : PdfViewerViewEvent()
    data object GoToNextPage : PdfViewerViewEvent()
    data object Dismiss : PdfViewerViewEvent()
    data object Download : PdfViewerViewEvent()
    data object Share : PdfViewerViewEvent()
    data object ControlsTimedOut : PdfViewerViewEvent()

    /** The viewer left composition: close the document and drop every bitmap. */
    data object Release : PdfViewerViewEvent()
}
