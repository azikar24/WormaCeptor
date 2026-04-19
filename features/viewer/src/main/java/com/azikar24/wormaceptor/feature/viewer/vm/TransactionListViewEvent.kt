package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.components.QuickFilter
import java.util.UUID

/** All user-initiated actions dispatched from the transaction list UI. */
sealed class TransactionListViewEvent {
    /**
     * User typed or cleared search text.
     *
     * @property query The current text in the search field.
     */
    data class SearchQueryChanged(val query: String) : TransactionListViewEvent()

    /**
     * User changed the selected HTTP method filters.
     *
     * @property methods The updated set of HTTP method strings (e.g. "GET", "POST").
     */
    data class MethodFiltersChanged(val methods: Set<String>) : TransactionListViewEvent()

    /**
     * User changed the selected status-code range filters.
     *
     * @property ranges The updated set of HTTP status-code ranges (e.g. 200..299).
     */
    data class StatusFiltersChanged(val ranges: Set<IntRange>) : TransactionListViewEvent()

    /** User cleared all method and status filters. */
    data object ClearFilters : TransactionListViewEvent()

    /**
     * User toggled a quick filter chip.
     *
     * @property filter The [QuickFilter] that was toggled.
     */
    data class QuickFilterToggled(val filter: QuickFilter) : TransactionListViewEvent()

    /** User cleared all quick filters at once. */
    data object QuickFiltersCleared : TransactionListViewEvent()

    /**
     * User toggled selection of a single transaction.
     *
     * @property id Unique identifier of the transaction whose selection was toggled.
     */
    data class SelectionToggled(val id: UUID) : TransactionListViewEvent()

    /** User tapped "Select All" in multi-select mode. */
    data object SelectAllClicked : TransactionListViewEvent()

    /** User exited multi-select mode. */
    data object SelectionCleared : TransactionListViewEvent()

    /** User confirmed deletion of the selected transactions. */
    data object DeleteSelectedClicked : TransactionListViewEvent()

    /**
     * User deleted a single transaction via context menu.
     *
     * @property id Unique identifier of the transaction to delete.
     */
    data class DeleteTransaction(val id: UUID) : TransactionListViewEvent()

    /** User confirmed clearing all transactions. */
    data object ClearAllTransactions : TransactionListViewEvent()

    /** User triggered pull-to-refresh on the transactions list. */
    data object RefreshTransactions : TransactionListViewEvent()

    /**
     * Display a snackbar message to the user.
     *
     * @property message The text to show in the snackbar.
     */
    data class ShowMessage(val message: String) : TransactionListViewEvent()

    /**
     * Filter bottom sheet visibility changed.
     *
     * @property visible Whether the sheet should be visible.
     */
    data class FilterSheetVisibilityChanged(val visible: Boolean) : TransactionListViewEvent()

    /**
     * Clear-transactions confirmation dialog visibility changed.
     *
     * @property visible Whether the dialog should be visible.
     */
    data class ClearTransactionsDialogVisibilityChanged(val visible: Boolean) : TransactionListViewEvent()

    /**
     * Delete-selected confirmation dialog visibility changed.
     *
     * @property visible Whether the dialog should be visible.
     */
    data class DeleteSelectedDialogVisibilityChanged(val visible: Boolean) : TransactionListViewEvent()

    // ── Export events ──────────────────────────────────────────────────

    /** User requested exporting all transactions as JSON. */
    data object ExportAllTransactions : TransactionListViewEvent()

    /** User requested exporting all transactions as HAR. */
    data object ExportAllTransactionsAsHar : TransactionListViewEvent()

    /** User requested exporting the selected transactions as JSON. */
    data object ExportSelectedTransactions : TransactionListViewEvent()

    /** User requested exporting the selected transactions as HAR. */
    data object ExportSelectedTransactionsAsHar : TransactionListViewEvent()

    // ── Share events ───────────────────────────────────────────────────

    /** User requested sharing the selected transactions as text. */
    data object ShareSelectedTransactions : TransactionListViewEvent()

    /**
     * User requested sharing a single transaction as HAR.
     *
     * @property id The transaction to export and share.
     */
    data class ShareTransactionAsHar(val id: UUID) : TransactionListViewEvent()

    /**
     * User requested sharing a single transaction summary as text.
     *
     * @property summary The transaction summary to share.
     */
    data class ShareTransaction(val summary: TransactionSummary) : TransactionListViewEvent()

    // ── Clipboard events ───────────────────────────────────────────────

    /**
     * User requested copying a transaction URL to clipboard.
     *
     * @property summary The transaction whose URL to copy.
     */
    data class CopyTransactionUrl(val summary: TransactionSummary) : TransactionListViewEvent()

    /**
     * User requested copying a transaction as a cURL command.
     *
     * @property id The transaction to generate cURL for.
     */
    data class CopyTransactionAsCurl(val id: UUID) : TransactionListViewEvent()

    // ── Filter bottom sheet draft events ──────────────────────────────

    /** Events for the filter bottom sheet draft state. */
    sealed class Filter : TransactionListViewEvent() {
        /** User opened the filter sheet — initializes draft from committed state. */
        data object SheetOpened : Filter()

        /** User changed the draft search query text in the filter sheet. */
        data class DraftQueryChanged(val query: String) : Filter()

        /** User toggled a draft HTTP method filter in the filter sheet. */
        data class DraftMethodToggled(val method: String) : Filter()

        /** User toggled a draft status range filter in the filter sheet. */
        data class DraftStatusRangeToggled(val range: IntRange) : Filter()

        /** User cleared all draft filters in the filter sheet. */
        data object DraftCleared : Filter()

        /** User applied the draft filters — commits draft to actual state and closes sheet. */
        data object Applied : Filter()
    }
}
