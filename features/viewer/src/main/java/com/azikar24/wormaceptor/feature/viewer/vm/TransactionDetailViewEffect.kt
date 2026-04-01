package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.annotation.StringRes
import com.azikar24.wormaceptor.domain.entities.ExportFormat
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

internal sealed class TransactionDetailViewEffect {
    sealed class Clipboard : TransactionDetailViewEffect() {
        data class CopyText(@StringRes val labelResId: Int, val content: String) : Clipboard()
    }

    sealed class Share : TransactionDetailViewEffect() {
        data class AsFile(
            val content: String,
            val fileName: String,
            val mimeType: String,
            @StringRes val titleResId: Int,
        ) : Share()

        data class Image(val payload: BinaryPayload) : Share()
    }

    sealed class Save : TransactionDetailViewEffect() {
        data class ExportTransactions(
            val transactions: List<NetworkTransaction>,
            val format: ExportFormat,
        ) : Save()

        data class ImageToGallery(val payload: BinaryPayload) : Save()

        data class PdfToDownloads(val bytes: ByteArray) : Save() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is PdfToDownloads) return false
                return bytes.contentEquals(other.bytes)
            }

            override fun hashCode(): Int = bytes.contentHashCode()
        }
    }

    data class ShowSnackBar(val message: String) : TransactionDetailViewEffect()
}
