package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionSummary

/** One-time side-effects emitted by the home ViewModel and consumed by the UI. */
sealed class HomeViewEffect {
    /** Show a transient snackbar with the given [message]. */
    data class ShowSnackBar(val message: String) : HomeViewEffect()

    /** Navigate to transaction detail screen. */
    data class NavigateToTransaction(val summary: TransactionSummary) : HomeViewEffect()

    /** Navigate to crash detail screen. */
    data class NavigateToCrash(val crash: Crash) : HomeViewEffect()

    /** Navigate to a tool screen. */
    data class NavigateToTool(val route: String) : HomeViewEffect()

    /** Navigate back (finish the activity). */
    data object NavigateBack : HomeViewEffect()
}
