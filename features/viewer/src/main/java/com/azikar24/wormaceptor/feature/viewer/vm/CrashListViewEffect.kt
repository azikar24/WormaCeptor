package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.Crash

/** One-time side-effects emitted by the crash list ViewModel and consumed by the UI. */
sealed class CrashListViewEffect {
    /** Show a transient snackbar with the given [message]. */
    data class ShowSnackBar(val message: String) : CrashListViewEffect()

    /** Export the given crashes via the share sheet. */
    data class ExportCrashes(val crashes: List<Crash>) : CrashListViewEffect()
}
