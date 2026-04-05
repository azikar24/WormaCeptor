package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewEvent
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewState

@Composable
internal fun WebViewMonitorListScreen(
    state: WebViewMonitorViewState,
    onEvent: (WebViewMonitorViewEvent) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            ListTopBar(
                searchActive = searchActive,
                searchQuery = state.searchQuery,
                onSearchToggle = {
                    searchActive = !searchActive
                    if (!searchActive) onEvent(WebViewMonitorViewEvent.SetSearchQuery(""))
                },
                onSearchQueryChanged = { onEvent(WebViewMonitorViewEvent.SetSearchQuery(it)) },
                onClearRequests = { onEvent(WebViewMonitorViewEvent.ClearRequests) },
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        ListContent(
            requests = state.filteredRequests,
            totalCount = state.requests.size,
            stats = state.stats,
            resourceTypeFilter = state.resourceTypeFilter,
            onToggleResourceTypeFilter = { onEvent(WebViewMonitorViewEvent.ToggleResourceTypeFilter(it)) },
            onClearFilters = { onEvent(WebViewMonitorViewEvent.ClearFilters) },
            onRequestClick = { request -> onEvent(WebViewMonitorViewEvent.SelectRequest(request)) },
            modifier = Modifier.padding(padding).imePadding(),
        )
    }
}
