package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.feature.viewer.ui.components.QuickFilter
import java.util.UUID

/** All user-initiated actions dispatched from the viewer UI into [ViewerViewModel]. */
sealed class ViewerViewEvent {
    /**
     * User typed or cleared search text.
     *
     * @property query The current text in the search field.
     */
    data class SearchQueryChanged(val query: String) : ViewerViewEvent()

    /**
     * User changed the selected HTTP method filters.
     *
     * @property methods The updated set of HTTP method strings (e.g. "GET", "POST").
     */
    data class MethodFiltersChanged(val methods: Set<String>) : ViewerViewEvent()

    /**
     * User changed the selected status-code range filters.
     *
     * @property ranges The updated set of HTTP status-code ranges (e.g. 200..299).
     */
    data class StatusFiltersChanged(val ranges: Set<IntRange>) : ViewerViewEvent()

    /** User cleared all method and status filters. */
    data object ClearFilters : ViewerViewEvent()

    /**
     * User selected a different tab (Transactions / Crashes / Tools).
     *
     * @property index Zero-based index of the newly selected tab.
     */
    data class TabSelected(val index: Int) : ViewerViewEvent()

    /**
     * User toggled a quick filter chip.
     *
     * @property filter The [QuickFilter] that was toggled.
     */
    data class QuickFilterToggled(val filter: QuickFilter) : ViewerViewEvent()

    /** User cleared all quick filters at once. */
    data object QuickFiltersCleared : ViewerViewEvent()

    /**
     * User toggled selection of a single transaction.
     *
     * @property id Unique identifier of the transaction whose selection was toggled.
     */
    data class SelectionToggled(val id: UUID) : ViewerViewEvent()

    /** User tapped "Select All" in multi-select mode. */
    data object SelectAllClicked : ViewerViewEvent()

    /** User exited multi-select mode. */
    data object SelectionCleared : ViewerViewEvent()

    /** User confirmed deletion of the selected transactions. */
    data object DeleteSelectedClicked : ViewerViewEvent()

    /**
     * User deleted a single transaction via context menu.
     *
     * @property id Unique identifier of the transaction to delete.
     */
    data class DeleteTransaction(val id: UUID) : ViewerViewEvent()

    /** User confirmed clearing all transactions. */
    data object ClearAllTransactions : ViewerViewEvent()

    /** User confirmed clearing all crashes. */
    data object ClearAllCrashes : ViewerViewEvent()

    /** User triggered pull-to-refresh on the transactions list. */
    data object RefreshTransactions : ViewerViewEvent()

    /** User triggered pull-to-refresh on the crashes list. */
    data object RefreshCrashes : ViewerViewEvent()

    /**
     * Display a snackbar message to the user.
     *
     * @property message The text to show in the snackbar.
     */
    data class ShowMessage(val message: String) : ViewerViewEvent()
}
