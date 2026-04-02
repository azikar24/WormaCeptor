package com.azikar24.wormaceptor.feature.webviewmonitor.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the WebView Monitor feature.
 *
 * Observes [WebViewMonitorEngine] flows and consolidates them into
 * a single [WebViewMonitorViewState] exposed via [uiState].
 */
class WebViewMonitorViewModel(
    private val engine: WebViewMonitorEngine,
) : BaseViewModel<WebViewMonitorViewState, WebViewMonitorViewEffect, WebViewMonitorViewEvent>(
    initialState = WebViewMonitorViewState(),
) {

    init {
        observeEngine()
    }

    override fun handleEvent(event: WebViewMonitorViewEvent) {
        when (event) {
            is WebViewMonitorViewEvent.SetSearchQuery -> {
                updateState { copy(searchQuery = event.query) }
                refilter()
            }

            is WebViewMonitorViewEvent.ToggleResourceTypeFilter -> {
                engine.toggleResourceTypeFilter(event.type)
            }

            is WebViewMonitorViewEvent.ClearFilters -> {
                updateState { copy(searchQuery = "") }
                engine.clearFilters()
            }

            is WebViewMonitorViewEvent.ClearRequests -> {
                engine.clearRequests()
            }

            is WebViewMonitorViewEvent.SelectRequest -> {
                updateState { copy(selectedRequest = event.request) }
                emitEffect(WebViewMonitorViewEffect.NavigateToDetail)
            }

            is WebViewMonitorViewEvent.ClearSelection -> {
                updateState { copy(selectedRequest = null) }
            }

            is WebViewMonitorViewEvent.EnsureEnabled -> {
                if (!engine.isEnabled.value) {
                    engine.toggle()
                }
            }
        }
    }

    /**
     * Creates a WebViewClient that monitors network requests.
     *
     * This method is intentionally kept as a public function (not an event)
     * because it returns a value and is called by the host Activity/Fragment,
     * not the Composable UI layer.
     *
     * @param webViewId Unique identifier for the WebView being monitored
     * @param delegate Optional delegate WebViewClient to forward calls to
     * @return A WebViewClient that intercepts and logs requests
     */
    fun createMonitoringClient(
        webViewId: String,
        delegate: android.webkit.WebViewClient? = null,
    ): android.webkit.WebViewClient {
        return engine.createMonitoringClient(webViewId, delegate)
    }

    private fun observeEngine() {
        combine(
            engine.requests,
            engine.stats,
            engine.resourceTypeFilter,
        ) { requests, stats, typeFilter ->
            val searchQuery = uiState.value.searchQuery
            val filtered = filterRequests(requests, searchQuery, typeFilter)

            updateState {
                copy(
                    requests = requests.toImmutableList(),
                    filteredRequests = filtered.toImmutableList(),
                    stats = stats,
                    resourceTypeFilter = typeFilter.toImmutableSet(),
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun refilter() {
        val currentState = uiState.value
        val allRequests = currentState.requests
        val typeFilter = currentState.resourceTypeFilter
        val query = currentState.searchQuery

        val filtered = filterRequests(allRequests, query, typeFilter)
        updateState { copy(filteredRequests = filtered.toImmutableList()) }
    }

    private fun filterRequests(
        allRequests: List<WebViewRequest>,
        query: String,
        typeFilter: Set<WebViewResourceType>,
    ): List<WebViewRequest> {
        val lowerQuery = query.lowercase()
        return allRequests.filter { request ->
            val matchesSearch = lowerQuery.isEmpty() ||
                request.url.lowercase().contains(lowerQuery) ||
                request.method.lowercase().contains(lowerQuery) ||
                request.host.lowercase().contains(lowerQuery)

            val matchesType = typeFilter.isEmpty() || typeFilter.contains(request.resourceType)

            matchesSearch && matchesType
        }
    }
}
