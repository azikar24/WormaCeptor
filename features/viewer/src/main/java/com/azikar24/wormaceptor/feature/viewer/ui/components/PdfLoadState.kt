package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.graphics.Bitmap
import com.azikar24.wormaceptor.domain.entities.PdfMetadata

sealed class PdfLoadState {
    data object Loading : PdfLoadState()
    data class Success(val thumbnail: Bitmap, val metadata: PdfMetadata) : PdfLoadState()
    data class Error(val message: String) : PdfLoadState()
    data class PasswordProtected(val metadata: PdfMetadata) : PdfLoadState()
}
