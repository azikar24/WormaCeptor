package com.azikar24.wormaceptor.feature.viewer.vm

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import com.azikar24.wormaceptor.feature.viewer.ui.components.extractPdfTitle
import com.azikar24.wormaceptor.feature.viewer.ui.components.extractPdfVersion
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

internal class PdfViewerViewModel(
    private val documentOpener: PdfDocumentOpener = AndroidPdfDocumentOpener,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseViewModel<PdfViewerViewState, PdfViewerViewEffect, PdfViewerViewEvent, NoOpNavigator>(
    PdfViewerViewState(),
    NoOpNavigator,
) {
    private var pdfData: ByteArray? = null
    private var tempFile: File? = null
    private var controlsHideJob: Job? = null
    private var loadJob: Job? = null
    private var document: PdfDocument? = null

    // Main-thread only.
    private val pageCache = RenderCache(MAX_CACHED_PAGES, maxWidthPx = Int.MAX_VALUE)
    private val thumbnailCache = RenderCache(MAX_CACHED_THUMBNAILS, maxWidthPx = THUMBNAIL_WIDTH_PX)
    private val failedPages = mutableSetOf<Int>()

    override fun handleEvent(event: PdfViewerViewEvent) {
        when (event) {
            is PdfViewerViewEvent.LoadPdf -> loadPdf(event.pdfData, event.initialPage, event.cacheDir)
            is PdfViewerViewEvent.RequestPage -> requestRender(event.page, pageCache) { copy(pages = it) }
            is PdfViewerViewEvent.RequestThumbnail -> {
                requestRender(event.page, thumbnailCache) { copy(thumbnails = it) }
            }
            is PdfViewerViewEvent.PageChanged -> updateState { copy(currentPage = event.page) }
            is PdfViewerViewEvent.ToggleControls -> {
                val newValue = !uiState.value.showControls
                updateState { copy(showControls = newValue) }
                if (newValue) startControlsAutoHide()
            }
            is PdfViewerViewEvent.ToggleThumbnails -> updateState { copy(showThumbnails = !showThumbnails) }
            is PdfViewerViewEvent.ShowPageJumpDialog -> updateState { copy(showPageJumpDialog = true) }
            is PdfViewerViewEvent.DismissPageJumpDialog -> updateState { copy(showPageJumpDialog = false) }
            is PdfViewerViewEvent.JumpToPage -> {
                updateState { copy(showPageJumpDialog = false, currentPage = event.page) }
            }
            is PdfViewerViewEvent.GoToFirstPage -> updateState { copy(currentPage = 0) }
            is PdfViewerViewEvent.GoToLastPage -> updateState { copy(currentPage = pageCount - 1) }
            is PdfViewerViewEvent.GoToPreviousPage -> {
                updateState { copy(currentPage = (currentPage - 1).coerceAtLeast(0)) }
            }
            is PdfViewerViewEvent.GoToNextPage -> {
                updateState { copy(currentPage = (currentPage + 1).coerceAtMost(pageCount - 1)) }
            }
            is PdfViewerViewEvent.Dismiss -> emitEffect(PdfViewerViewEffect.Dismiss)
            is PdfViewerViewEvent.Download -> emitEffect(PdfViewerViewEffect.Download)
            is PdfViewerViewEvent.Share -> {
                val data = pdfData ?: return
                emitEffect(PdfViewerViewEffect.SharePdf(data, tempFile))
            }
            is PdfViewerViewEvent.ControlsTimedOut -> updateState { copy(showControls = false) }
            is PdfViewerViewEvent.Release -> release()
        }
    }

    private fun loadPdf(
        data: ByteArray,
        initialPage: Int,
        cacheDir: File,
    ) {
        if (pdfData === data) return
        // The VM outlives a single PDF (scoped to the detail entry), so drop the previous document.
        loadJob?.cancel()
        releaseDocument()
        updateState { PdfViewerViewState() }
        pdfData = data

        loadJob = viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            // Holds an opened document until it is published, so a superseded load still closes it.
            var unpublished: PdfOpenResult.Opened? = null
            try {
                val result = withContext(ioDispatcher) {
                    openPdf(data, cacheDir).also { if (it is PdfOpenResult.Opened) unpublished = it }
                }
                when (result) {
                    is PdfOpenResult.Opened -> {
                        unpublished = null
                        document = result.document
                        tempFile = result.file
                        val totalPages = result.document.pageCount
                        updateState {
                            copy(
                                pageCount = totalPages,
                                isLoading = false,
                                metadata = result.metadata,
                                currentPage = initialPage.coerceIn(0, totalPages - 1),
                            )
                        }
                    }
                    is PdfOpenResult.Failed -> updateState { copy(isLoading = false, error = result.error) }
                }
            } finally {
                unpublished?.let {
                    it.document.close()
                    it.file.delete()
                }
            }

            startControlsAutoHide()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun openPdf(
        data: ByteArray,
        cacheDir: File,
    ): PdfOpenResult {
        val file = File(cacheDir, "viewer_${System.currentTimeMillis()}.pdf")
        return try {
            FileOutputStream(file).use { it.write(data) }
            val title = extractPdfTitle(data)
            val version = extractPdfVersion(data).orEmpty()

            val opened = documentOpener.open(file)
            if (opened.pageCount == 0) {
                opened.close()
                file.delete()
                PdfOpenResult.Failed(PdfViewerError.NoPages)
            } else {
                val metadata = PdfMetadata(
                    pageCount = opened.pageCount,
                    title = title,
                    author = null,
                    creator = null,
                    creationDate = null,
                    fileSize = data.size.toLong(),
                    version = version,
                )
                PdfOpenResult.Opened(opened, file, metadata)
            }
        } catch (_: SecurityException) {
            file.delete()
            PdfOpenResult.Failed(PdfViewerError.PasswordProtected)
        } catch (e: Exception) {
            file.delete()
            PdfOpenResult.Failed(PdfViewerError.LoadFailed(e.message))
        }
    }

    private fun requestRender(
        index: Int,
        cache: RenderCache,
        publish: PdfViewerViewState.(ImmutableMap<Int, Bitmap>) -> PdfViewerViewState,
    ) {
        val doc = document ?: return
        if (index !in 0 until doc.pageCount || index in failedPages) return
        if (cache.bitmaps[index] != null || !cache.pending.add(index)) return

        viewModelScope.launch {
            val bitmap = try {
                withContext(ioDispatcher) { doc.render(index, RENDER_SCALE, cache.maxWidthPx) }
            } catch (e: IllegalStateException) {
                onRenderFailed(doc, index, e)
                return@launch
            } catch (e: IllegalArgumentException) {
                onRenderFailed(doc, index, e)
                return@launch
            } finally {
                // A replaced document's pending set was already cleared and may now track the new one.
                if (document === doc) cache.pending.remove(index)
            }
            if (document !== doc) {
                // Rendered for a document that has since been replaced; it was never shown.
                bitmap?.recycle()
                return@launch
            }
            if (bitmap == null) return@launch
            cache.put(index, bitmap)
            updateState { publish(cache.bitmaps.toImmutableMap()) }
        }
    }

    private fun onRenderFailed(
        doc: PdfDocument,
        index: Int,
        error: RuntimeException,
    ) {
        Log.w(TAG, "Failed to render PDF page $index", error)
        if (document !== doc) return
        failedPages.add(index)
        updateState { copy(failedPages = this@PdfViewerViewModel.failedPages.toImmutableSet()) }
    }

    private fun release() {
        loadJob?.cancel()
        controlsHideJob?.cancel()
        releaseDocument()
        pdfData = null
        updateState { PdfViewerViewState() }
    }

    private fun releaseDocument() {
        document?.close()
        document = null
        tempFile?.delete()
        tempFile = null
        pageCache.clear()
        thumbnailCache.clear()
        failedPages.clear()
    }

    private fun startControlsAutoHide() {
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(CONTROLS_AUTO_HIDE_MS)
            updateState { copy(showControls = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseDocument()
    }

    private sealed class PdfOpenResult {
        class Opened(val document: PdfDocument, val file: File, val metadata: PdfMetadata) : PdfOpenResult()
        class Failed(val error: PdfViewerError) : PdfOpenResult()
    }

    /** LRU bitmap cache; access order makes iteration start at the least recently requested page. */
    private class RenderCache(
        private val capacity: Int,
        val maxWidthPx: Int,
    ) {
        val bitmaps = LinkedHashMap<Int, Bitmap>(capacity, LRU_LOAD_FACTOR, true)
        val pending = mutableSetOf<Int>()

        // Evicted bitmaps are dropped, not recycled: a frame already recorded may still draw them,
        // and recycling would crash with "trying to use a recycled bitmap". The cap bounds live references.
        fun put(
            index: Int,
            bitmap: Bitmap,
        ) {
            bitmaps[index] = bitmap
            val iterator = bitmaps.entries.iterator()
            while (bitmaps.size > capacity && iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            }
        }

        /** Recycles everything; only call once no composition can draw these bitmaps. */
        fun clear() {
            bitmaps.values.forEach { it.recycle() }
            bitmaps.clear()
            pending.clear()
        }
    }

    companion object {
        private const val TAG = "PdfViewerViewModel"
        private const val MAX_CACHED_PAGES = 6
        private const val MAX_CACHED_THUMBNAILS = 30
        private const val THUMBNAIL_WIDTH_PX = 180
        private const val LRU_LOAD_FACTOR = 0.75f
        private const val RENDER_SCALE = 2f
        private const val CONTROLS_AUTO_HIDE_MS = 4000L
    }
}
