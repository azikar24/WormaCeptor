package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

/** User actions dispatched from the transaction detail UI. */
internal sealed class TransactionDetailViewEvent {

    // -- Lifecycle --

    sealed class Lifecycle : TransactionDetailViewEvent() {
        data class TransactionLoaded(val transaction: NetworkTransaction) : Lifecycle()
    }

    // -- Search --

    sealed class Search : TransactionDetailViewEvent() {
        data class VisibilityChanged(val visible: Boolean) : Search()
        data class QueryChanged(val query: String) : Search()
        data object NavigateToNext : Search()
        data object NavigateToPrevious : Search()
        data class ActiveTabChanged(val tabIndex: Int) : Search()
    }

    // -- Menu --

    sealed class Menu : TransactionDetailViewEvent() {
        data class VisibilityChanged(val visible: Boolean) : Menu()
        data object CopyAsText : Menu()
        data object CopyAsCurl : Menu()
        data object ShareAsJson : Menu()
        data object ShareAsHar : Menu()
    }

    // -- Request section --

    sealed class Request : TransactionDetailViewEvent() {
        data object TogglePrettyMode : Request()
        data object ToggleHeadersExpanded : Request()
        data object ToggleBodyExpanded : Request()
        data object CopyBody : Request()
        data object CopyHeaders : Request()
        data object CopyAllContent : Request()
    }

    // -- Response section --

    sealed class Response : TransactionDetailViewEvent() {
        data object TogglePrettyMode : Response()
        data object ToggleHeadersExpanded : Response()
        data object ToggleBodyExpanded : Response()
        data object CopyBody : Response()
        data object CopyHeaders : Response()
        data object CopyAllContent : Response()
        data class ShowImageViewer(val show: Boolean) : Response()
        data class ShowPdfViewer(val show: Boolean) : Response()
        data object DownloadImage : Response()
        data object ShareImage : Response()
        data object DownloadPdf : Response()
    }

    // -- SnackBar --

    data class ShowMessage(val message: String) : TransactionDetailViewEvent()
}
