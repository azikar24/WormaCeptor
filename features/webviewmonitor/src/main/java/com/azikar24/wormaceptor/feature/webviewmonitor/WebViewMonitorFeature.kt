package com.azikar24.wormaceptor.feature.webviewmonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Entry point for the WebView Network Monitoring feature.
 * Provides factory methods and composable entry point.
 */
object WebViewMonitorFeature {

    /**
     * Creates a WebViewMonitorEngine instance.
     *
     * @param maxRequests Maximum number of requests to store
     * @return A new WebViewMonitorEngine instance
     */
    fun createEngine(maxRequests: Int = WebViewMonitorEngine.DEFAULT_MAX_REQUESTS): WebViewMonitorEngine {
        return WebViewMonitorEngine(maxRequests)
    }

    /**
     * Creates a WebViewMonitorViewModel factory for use with viewModel().
     *
     * @param engine The WebViewMonitorEngine instance to use
     * @return A ViewModelProvider.Factory for creating WebViewMonitorViewModel
     */
    fun createViewModelFactory(engine: WebViewMonitorEngine): WebViewMonitorViewModelFactory {
        return WebViewMonitorViewModelFactory(engine)
    }

    /**
     * Creates a ViewModelProvider.Factory with a new engine instance.
     * Use this when you don't need to share the engine with other components.
     *
     * @param maxRequests Maximum number of requests to store
     * @return A ViewModelProvider.Factory for creating WebViewMonitorViewModel
     */
    fun createViewModelFactory(
        maxRequests: Int = WebViewMonitorEngine.DEFAULT_MAX_REQUESTS,
    ): ViewModelProvider.Factory {
        return WebViewMonitorViewModelFactory(createEngine(maxRequests))
    }
}

/**
 * ViewModel for the WebView Monitor feature.
 */
class WebViewMonitorViewModel(private val engine: WebViewMonitorEngine) : ViewModel() {

    /** Timeout constants for StateFlow subscriptions. */
    companion object {
        private const val SUBSCRIPTION_TIMEOUT_MS = 5000L
    }

    /** Whether WebView network monitoring is currently active. */
    val isEnabled: StateFlow<Boolean> = engine.isEnabled

    /** All captured WebView network requests. */
    val requests: StateFlow<List<WebViewRequest>> = engine.requests

    /** Aggregated statistics for captured WebView requests. */
    val stats: StateFlow<WebViewRequestStats> = engine.stats

    /** Set of resource types currently included in the filter. */
    val resourceTypeFilter: StateFlow<Set<WebViewResourceType>> = engine.resourceTypeFilter

    private val _searchQuery = MutableStateFlow("")

    /** Current search query for filtering captured requests. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRequest = MutableStateFlow<WebViewRequest?>(null)

    /** Currently selected request for the detail view. */
    val selectedRequest: StateFlow<WebViewRequest?> = _selectedRequest.asStateFlow()

    private val _showFilters = MutableStateFlow(false)

    /** Whether the filter panel is visible. */
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    /** Reactively filtered requests based on search query and resource type filter. */
    val filteredRequests: StateFlow<List<WebViewRequest>> = combine(
        requests,
        _searchQuery,
        resourceTypeFilter,
    ) { allRequests, query, typeFilter ->
        val lowerQuery = query.lowercase()
        allRequests.filter { request ->
            val matchesSearch = lowerQuery.isEmpty() ||
                request.url.lowercase().contains(lowerQuery) ||
                request.method.lowercase().contains(lowerQuery) ||
                request.host.lowercase().contains(lowerQuery)

            val matchesType = typeFilter.isEmpty() || typeFilter.contains(request.resourceType)

            matchesSearch && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = emptyList(),
    )

    /** Toggles the WebView network monitoring on or off. */
    fun toggleEnabled() {
        engine.toggle()
    }

    /** Updates the search query used to filter captured requests. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Adds or removes a resource type from the active filter set. */
    fun toggleResourceTypeFilter(type: WebViewResourceType) {
        engine.toggleResourceTypeFilter(type)
    }

    /** Resets all active search and resource type filters. */
    fun clearFilters() {
        _searchQuery.value = ""
        engine.clearFilters()
    }

    /** Deletes all captured WebView network requests. */
    fun clearRequests() {
        engine.clearRequests()
    }

    /** Selects a request to display its details. */
    fun selectRequest(request: WebViewRequest) {
        _selectedRequest.value = request
    }

    /** Deselects the currently viewed request. */
    fun clearSelection() {
        _selectedRequest.value = null
    }

    /** Toggles the visibility of the filter panel. */
    fun toggleFilters() {
        _showFilters.update { !it }
    }

    /**
     * Creates a WebViewClient that monitors network requests.
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
}

/**
 * Factory for creating WebViewMonitorViewModel instances.
 */
class WebViewMonitorViewModelFactory(
    private val engine: WebViewMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebViewMonitorViewModel::class.java)) {
            return WebViewMonitorViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
