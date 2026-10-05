package com.azikar24.wormaceptor.feature.viewer.vm

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/** An open PDF. Seam over [PdfRenderer], which is final and unusable in JVM tests. */
internal interface PdfDocument {
    val pageCount: Int

    /**
     * Renders [index] at [scale], shrunk further so the bitmap is at most [maxWidthPx] wide.
     * Returns null once the document is closed. Throws [IllegalStateException] or
     * [IllegalArgumentException] for a corrupt page or a zero-size page box.
     */
    suspend fun render(
        index: Int,
        scale: Float,
        maxWidthPx: Int,
    ): Bitmap?

    /** Never blocks; an in-flight render finishes first and the document closes after it. */
    fun close()
}

internal fun interface PdfDocumentOpener {
    /** Throws [SecurityException] for password-protected files, [java.io.IOException] for unreadable ones. */
    fun open(file: File): PdfDocument
}

internal object AndroidPdfDocumentOpener : PdfDocumentOpener {
    override fun open(file: File): PdfDocument {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        var opened = false
        try {
            val document = AndroidPdfDocument(fd, PdfRenderer(fd))
            opened = true
            return document
        } finally {
            if (!opened) fd.close()
        }
    }
}

/**
 * The renderer allows one open page at a time and is not thread-safe, so every access holds
 * the mutex. [close] never blocks: if a render holds the lock, that render closes the renderer
 * after releasing it.
 */
private class AndroidPdfDocument(
    private val fd: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
) : PdfDocument {
    override val pageCount: Int = renderer.pageCount
    private val mutex = Mutex()

    @Volatile
    private var closeRequested = false
    private var closed = false

    override suspend fun render(
        index: Int,
        scale: Float,
        maxWidthPx: Int,
    ): Bitmap? {
        try {
            return mutex.withLock {
                if (closeRequested) null else renderPage(index, scale, maxWidthPx)
            }
        } finally {
            // Also runs when cancelled while waiting for the lock or when the render throws.
            if (closeRequested) close()
        }
    }

    override fun close() {
        closeRequested = true
        if (!mutex.tryLock()) return
        try {
            if (!closed) {
                closed = true
                renderer.close()
                fd.close()
            }
        } finally {
            mutex.unlock()
        }
    }

    private fun renderPage(
        index: Int,
        scale: Float,
        maxWidthPx: Int,
    ): Bitmap {
        val page = renderer.openPage(index)
        try {
            val effectiveScale = minOf(scale, maxWidthPx.toFloat() / page.width)
            val bitmap = createBitmap((page.width * effectiveScale).toInt(), (page.height * effectiveScale).toInt())
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return bitmap
        } finally {
            page.close()
        }
    }
}
