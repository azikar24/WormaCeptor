package com.azikar24.wormaceptor.feature.filebrowser.ui

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.sync.Mutex

internal sealed class PdfState {
    class Ready(
        val renderer: PdfRenderer,
        val fileDescriptor: ParcelFileDescriptor,
        val mutex: Mutex = Mutex(),
    ) : PdfState()

    data class Error(val message: String) : PdfState()
}
