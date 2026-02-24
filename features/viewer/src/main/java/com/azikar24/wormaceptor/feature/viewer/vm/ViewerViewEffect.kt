package com.azikar24.wormaceptor.feature.viewer.vm

/** One-time side-effects emitted by [ViewerViewModel] and consumed by the UI. */
sealed class ViewerViewEffect {
    /** Show a transient snackbar with the given [message]. */
    data class ShowSnackbar(val message: String) : ViewerViewEffect()
}
