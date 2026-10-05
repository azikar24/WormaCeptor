package com.azikar24.wormaceptor.feature.filebrowser.ui

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

internal sealed class PdfState {
    class Ready(
        val renderer: PdfRenderer,
        private val fileDescriptor: ParcelFileDescriptor,
    ) : PdfState() {
        // PdfRenderer allows one open page at a time and throws if closed while a page is open,
        // so rendering and closing are serialized on this lock.
        private val mutex = Mutex()
        private var isClosed = false

        /** Runs [block] under the lock, or returns null if the renderer was already closed. */
        suspend fun <T> withRenderer(block: (PdfRenderer) -> T): T? = mutex.withLock {
            if (isClosed) null else block(renderer)
        }

        /** Closes the renderer and descriptor after any in-flight render; safe to call twice. */
        suspend fun close() {
            mutex.withLock {
                if (isClosed) return
                isClosed = true
                try {
                    renderer.close()
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "PdfRenderer close failed", e)
                } finally {
                    try {
                        fileDescriptor.close()
                    } catch (e: IOException) {
                        Log.w(TAG, "PDF descriptor close failed", e)
                    }
                }
            }
        }
    }

    data class Error(val message: String) : PdfState()

    private companion object {
        const val TAG = "PdfState"
    }
}
