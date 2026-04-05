package com.azikar24.wormaceptor.feature.viewer.vm

/** One-time side-effects emitted by the home ViewModel and consumed by the UI. */
sealed class HomeViewEffect {
    /** Show a transient snackbar with the given [message]. */
    data class ShowSnackBar(val message: String) : HomeViewEffect()
}
