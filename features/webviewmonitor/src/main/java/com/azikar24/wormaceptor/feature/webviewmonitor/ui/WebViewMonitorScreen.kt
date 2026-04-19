package com.azikar24.wormaceptor.feature.webviewmonitor.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.components.WebViewMonitorListScreen
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewState
import kotlinx.collections.immutable.persistentListOf

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun WebViewMonitorListScreenPreview() {
    val fixedTimestamp = 1_700_000_000_000L

    WormaCeptorTheme {
        WebViewMonitorListScreen(
            state = WebViewMonitorViewState(
                requests = persistentListOf(
                    WebViewRequest(
                        id = "1",
                        url = "https://api.example.com/v2/users",
                        method = "GET",
                        headers = mapOf("Accept" to "application/json"),
                        timestamp = fixedTimestamp - 3_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                        statusCode = 200,
                        mimeType = "application/json",
                        contentLength = 1_024L,
                        duration = 150L,
                    ),
                    WebViewRequest(
                        id = "2",
                        url = "https://cdn.example.com/style.css",
                        method = "GET",
                        headers = emptyMap(),
                        timestamp = fixedTimestamp - 5_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.STYLESHEET,
                        statusCode = 200,
                        contentLength = 4_096L,
                        duration = 80L,
                    ),
                    WebViewRequest(
                        id = "3",
                        url = "https://api.example.com/v2/analytics",
                        method = "POST",
                        headers = emptyMap(),
                        timestamp = fixedTimestamp - 1_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                    ),
                ),
                filteredRequests = persistentListOf(
                    WebViewRequest(
                        id = "1",
                        url = "https://api.example.com/v2/users",
                        method = "GET",
                        headers = mapOf("Accept" to "application/json"),
                        timestamp = fixedTimestamp - 3_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                        statusCode = 200,
                        mimeType = "application/json",
                        contentLength = 1_024L,
                        duration = 150L,
                    ),
                    WebViewRequest(
                        id = "3",
                        url = "https://api.example.com/v2/analytics",
                        method = "POST",
                        headers = emptyMap(),
                        timestamp = fixedTimestamp - 1_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                    ),
                ),
                stats = WebViewRequestStats(
                    totalRequests = 3,
                    successfulRequests = 2,
                    failedRequests = 0,
                    pendingRequests = 1,
                    byResourceType = mapOf(
                        WebViewResourceType.XHR to 2,
                        WebViewResourceType.STYLESHEET to 1,
                    ),
                    averageDuration = 115L,
                    totalDataTransferred = 5_120L,
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
