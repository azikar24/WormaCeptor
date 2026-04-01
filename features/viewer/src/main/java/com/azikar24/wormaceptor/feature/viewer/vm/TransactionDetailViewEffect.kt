package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.ExportFormat
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

/** One-time side effects emitted by [TransactionDetailViewModel] and consumed by the UI. */
internal sealed class TransactionDetailViewEffect {

    data class CopyToClipboard(val label: String, val content: String) : TransactionDetailViewEffect()

    data class ShareAsFile(
        val content: String,
        val fileName: String,
        val mimeType: String,
        val title: String,
    ) : TransactionDetailViewEffect()

    data class ExportTransactions(
        val transactions: List<NetworkTransaction>,
        val format: ExportFormat,
    ) : TransactionDetailViewEffect()

    data class ShareImageBytes(val bytes: ByteArray, val format: String) : TransactionDetailViewEffect() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ShareImageBytes) return false
            return bytes.contentEquals(other.bytes) && format == other.format
        }

        override fun hashCode(): Int = bytes.contentHashCode() * 31 + format.hashCode()
    }

    data class SaveImageToGallery(val bytes: ByteArray, val format: String) : TransactionDetailViewEffect() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SaveImageToGallery) return false
            return bytes.contentEquals(other.bytes) && format == other.format
        }

        override fun hashCode(): Int = bytes.contentHashCode() * 31 + format.hashCode()
    }

    data class SavePdfToDownloads(val bytes: ByteArray) : TransactionDetailViewEffect() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SavePdfToDownloads) return false
            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = bytes.contentHashCode()
    }

    data class ShowSnackbar(val message: String) : TransactionDetailViewEffect()
}
