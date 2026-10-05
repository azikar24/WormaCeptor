package com.azikar24.wormaceptor.feature.viewer.vm

import android.graphics.Bitmap
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

internal data class PdfViewerViewState(
    /** Rendered pages by index; only a bounded window is kept, missing pages are requested on demand. */
    val pages: ImmutableMap<Int, Bitmap> = persistentMapOf(),
    /** Low-res thumbnails by index, bounded separately from [pages]. */
    val thumbnails: ImmutableMap<Int, Bitmap> = persistentMapOf(),
    /** Pages whose render threw; shown as an error placeholder and never re-requested. */
    val failedPages: ImmutableSet<Int> = persistentSetOf(),
    /** Same as [failedPages] for thumbnails, tracked separately since either can fail alone. */
    val failedThumbnails: ImmutableSet<Int> = persistentSetOf(),
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
