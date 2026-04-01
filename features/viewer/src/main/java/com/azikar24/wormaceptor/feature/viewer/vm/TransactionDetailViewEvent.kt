package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

/** User actions dispatched from the transaction detail UI. */
internal sealed class TransactionDetailViewEvent {

    // -- Lifecycle --
    data class TransactionLoaded(val transaction: NetworkTransaction) : TransactionDetailViewEvent()

    // -- Search --
    data class SearchVisibilityChanged(val visible: Boolean) : TransactionDetailViewEvent()
    data class SearchQueryChanged(val query: String) : TransactionDetailViewEvent()
    data object NavigateToNextMatch : TransactionDetailViewEvent()
    data object NavigateToPreviousMatch : TransactionDetailViewEvent()
    data class ActiveTabChanged(val tabIndex: Int) : TransactionDetailViewEvent()

    // -- Menu --
    data class MenuVisibilityChanged(val visible: Boolean) : TransactionDetailViewEvent()
    data object CopyAsText : TransactionDetailViewEvent()
    data object CopyAsCurl : TransactionDetailViewEvent()
    data object ShareAsJson : TransactionDetailViewEvent()
    data object ShareAsHar : TransactionDetailViewEvent()

    // -- Request section --
    data object ToggleRequestPrettyMode : TransactionDetailViewEvent()
    data object ToggleRequestHeadersExpanded : TransactionDetailViewEvent()
    data object ToggleRequestBodyExpanded : TransactionDetailViewEvent()
    data object CopyRequestBody : TransactionDetailViewEvent()
    data object CopyRequestHeaders : TransactionDetailViewEvent()

    // -- Response section --
    data object ToggleResponsePrettyMode : TransactionDetailViewEvent()
    data object ToggleResponseHeadersExpanded : TransactionDetailViewEvent()
    data object ToggleResponseBodyExpanded : TransactionDetailViewEvent()
    data object CopyResponseBody : TransactionDetailViewEvent()
    data object CopyResponseHeaders : TransactionDetailViewEvent()
    data class ShowImageViewer(val show: Boolean) : TransactionDetailViewEvent()
    data class ShowPdfViewer(val show: Boolean) : TransactionDetailViewEvent()
    data object DownloadImage : TransactionDetailViewEvent()
    data object ShareImage : TransactionDetailViewEvent()
    data object DownloadPdf : TransactionDetailViewEvent()

    // -- Snackbar --
    data class ShowMessage(val message: String) : TransactionDetailViewEvent()
}
