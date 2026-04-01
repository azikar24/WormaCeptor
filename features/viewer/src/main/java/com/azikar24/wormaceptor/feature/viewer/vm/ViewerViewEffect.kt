package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.ExportFormat
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

/** One-time side-effects emitted by [ViewerViewModel] and consumed by the UI. */
sealed class ViewerViewEffect {
    /** Show a transient snackbar with the given [message]. */
    data class ShowSnackBar(val message: String) : ViewerViewEffect()

    /** Share plain text via the system share sheet. */
    data class ShareText(val text: String, val title: String) : ViewerViewEffect()

    /** Copy text to the system clipboard and show a confirmation snackbar. */
    data class CopyToClipboard(val label: String, val content: String) : ViewerViewEffect()

    /** Export the given transactions via the share sheet in the specified [format]. */
    data class ExportTransactions(
        val transactions: List<NetworkTransaction>,
        val format: ExportFormat,
    ) : ViewerViewEffect()

    /** Export the given crashes via the share sheet. */
    data class ExportCrashes(val crashes: List<Crash>) : ViewerViewEffect()
}
