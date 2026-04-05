package com.azikar24.wormaceptor.feature.viewer.vm

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import com.azikar24.wormaceptor.feature.viewer.ui.components.extractPdfTitle
import com.azikar24.wormaceptor.feature.viewer.ui.components.extractPdfVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

internal class PdfViewerViewModel : BaseViewModel<PdfViewerViewState, PdfViewerViewEffect, PdfViewerViewEvent>(
    PdfViewerViewState(),
) {
    private var pdfData: ByteArray? = null
    private var tempFile: File? = null
    private var controlsHideJob: Job? = null

    override fun handleEvent(event: PdfViewerViewEvent) {
        when (event) {
            is PdfViewerViewEvent.LoadPdf -> loadPdf(event.pdfData, event.initialPage, event.cacheDir)
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
        }
    }

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    private fun loadPdf(
        data: ByteArray,
        initialPage: Int,
        cacheDir: File,
    ) {
        if (pdfData != null) return
        pdfData = data

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            withContext(Dispatchers.IO) {
                try {
                    val file = File(cacheDir, "viewer_${System.currentTimeMillis()}.pdf")
                    FileOutputStream(file).use { it.write(data) }
                    tempFile = file

                    val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fd)

                    try {
                        val totalPages = renderer.pageCount

                        if (totalPages == 0) {
                            updateState { copy(isLoading = false, error = PdfViewerError.NoPages) }
                            return@withContext
                        }

                        val metadata = PdfMetadata(
                            pageCount = totalPages,
                            title = extractPdfTitle(data),
                            author = null,
                            creator = null,
                            creationDate = null,
                            fileSize = data.size.toLong(),
                            version = extractPdfVersion(data).orEmpty(),
                        )

                        val maxPages = minOf(totalPages, MAX_PRELOADED_PAGES)
                        val renderedPages = mutableListOf<Bitmap>()

                        for (i in 0 until maxPages) {
                            val page = renderer.openPage(i)
                            try {
                                val bitmap = createBitmap(
                                    (page.width * RENDER_SCALE).toInt(),
                                    (page.height * RENDER_SCALE).toInt(),
                                )
                                bitmap.eraseColor(android.graphics.Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                renderedPages.add(bitmap)
                            } finally {
                                page.close()
                            }
                        }

                        updateState {
                            copy(
                                pages = renderedPages,
                                pageCount = totalPages,
                                isLoading = false,
                                metadata = metadata,
                                currentPage = initialPage.coerceIn(0, totalPages - 1),
                            )
                        }
                    } finally {
                        renderer.close()
                        fd.close()
                    }
                } catch (_: SecurityException) {
                    updateState { copy(isLoading = false, error = PdfViewerError.PasswordProtected) }
                } catch (e: Exception) {
                    updateState { copy(isLoading = false, error = PdfViewerError.LoadFailed(e.message)) }
                }
            }

            startControlsAutoHide()
        }
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
        tempFile?.delete()
        uiState.value.pages.forEach { it.recycle() }
    }

    companion object {
        private const val MAX_PRELOADED_PAGES = 10
        private const val RENDER_SCALE = 2f
        private const val CONTROLS_AUTO_HIDE_MS = 4000L
    }
}
