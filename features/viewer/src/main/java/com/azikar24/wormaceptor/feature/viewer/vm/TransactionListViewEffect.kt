package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.ExportFormat
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

/** One-time side-effects emitted by the transaction list ViewModel and consumed by the UI. */
sealed class TransactionListViewEffect {
    /** Show a transient snackbar with the given [message]. */
    data class ShowSnackBar(val message: String) : TransactionListViewEffect()

    /** Share plain text via the system share sheet. */
    data class ShareText(val text: String, val title: String) : TransactionListViewEffect()

    /** Copy text to the system clipboard and show a confirmation snackbar. */
    data class CopyToClipboard(val label: String, val content: String) : TransactionListViewEffect()

    /** Export the given transactions via the share sheet in the specified [format]. */
    data class ExportTransactions(
        val transactions: List<NetworkTransaction>,
        val format: ExportFormat,
    ) : TransactionListViewEffect()
}
