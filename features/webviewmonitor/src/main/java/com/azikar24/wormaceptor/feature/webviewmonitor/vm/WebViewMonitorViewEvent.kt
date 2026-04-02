package com.azikar24.wormaceptor.feature.webviewmonitor.vm

import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType

/** User actions dispatched from the WebView Monitor UI. */
sealed class WebViewMonitorViewEvent {
    data class SetSearchQuery(val query: String) : WebViewMonitorViewEvent()
    data class ToggleResourceTypeFilter(val type: WebViewResourceType) : WebViewMonitorViewEvent()
    data object ClearFilters : WebViewMonitorViewEvent()
    data object ClearRequests : WebViewMonitorViewEvent()
    data class SelectRequest(val request: WebViewRequest) : WebViewMonitorViewEvent()
    data object ClearSelection : WebViewMonitorViewEvent()
    data object EnsureEnabled : WebViewMonitorViewEvent()
}
