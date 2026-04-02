package com.azikar24.wormaceptor.feature.webviewmonitor.vm

import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/** Immutable snapshot of the WebView Monitor screen state. */
data class WebViewMonitorViewState(
    val requests: ImmutableList<WebViewRequest> = persistentListOf(),
    val filteredRequests: ImmutableList<WebViewRequest> = persistentListOf(),
    val stats: WebViewRequestStats = WebViewRequestStats.empty(),
    val searchQuery: String = "",
    val resourceTypeFilter: ImmutableSet<WebViewResourceType> = persistentSetOf(),
    val selectedRequest: WebViewRequest? = null,
)
