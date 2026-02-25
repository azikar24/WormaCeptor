package com.azikar24.wormaceptor.core.engine

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.azikar24.wormaceptor.domain.contracts.WebViewMonitorRepository
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Engine that monitors and captures network requests made from WebViews.
 *
 * Features:
 * - Intercepts all WebView network requests via custom WebViewClient
 * - Stores captured requests in a reactive StateFlow
 * - Supports filtering by WebView instance, URL pattern, or resource type
 * - Tracks request/response timing and status
 * - Provides statistics about captured requests
 * - Optional persistence via [configure] with a [WebViewMonitorRepository]
 *
 * Usage:
 * 1. Create an instance of WebViewMonitorEngine
 * 2. Get a monitoring WebViewClient via createMonitoringClient()
 * 3. Set the client on your WebView
 *
 * ```kotlin
 * val engine = WebViewMonitorEngine()
 * engine.enable()
 *
 * webView.webViewClient = engine.createMonitoringClient(
 *     webViewId = "main-webview",
 *     delegate = yourExistingWebViewClient, // optional
 * )
 * ```
 *
 * @param maxRequests Maximum number of requests to store (default 1000)
 */
class WebViewMonitorEngine(
    private val maxRequests: Int = DEFAULT_MAX_REQUESTS,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Whether monitoring is enabled (default: true)
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    // All captured requests
    private val _requests = MutableStateFlow<List<WebViewRequest>>(emptyList())
    val requests: StateFlow<List<WebViewRequest>> = _requests.asStateFlow()

    // Statistics about captured requests
    private val _stats = MutableStateFlow(WebViewRequestStats.empty())
    val stats: StateFlow<WebViewRequestStats> = _stats.asStateFlow()

    // Track pending requests for timing
    private val pendingRequests = ConcurrentHashMap<String, Long>()

    // Persistence repository (null = in-memory fallback)
    private var repository: WebViewMonitorRepository? = null

    // URL pattern filter
    private val _urlFilter = MutableStateFlow<String?>(null)
    val urlFilter: StateFlow<String?> = _urlFilter.asStateFlow()

    // Resource type filter
    private val _resourceTypeFilter = MutableStateFlow<Set<WebViewResourceType>>(emptySet())
    val resourceTypeFilter: StateFlow<Set<WebViewResourceType>> = _resourceTypeFilter.asStateFlow()

    // WebView ID filter
    private val _webViewIdFilter = MutableStateFlow<String?>(null)
    val webViewIdFilter: StateFlow<String?> = _webViewIdFilter.asStateFlow()

    /**
     * Configures the engine with a persistence repository.
     *
     * When configured, all request data is delegated to the repository and the
     * [requests] StateFlow is driven by [WebViewMonitorRepository.observeRequests].
     * Without a repository the engine falls back to in-memory storage.
     *
     * @param repository Repository for persisting WebView requests
     */
    fun configure(repository: WebViewMonitorRepository) {
        this.repository = repository
        scope.launch {
            repository.observeRequests().collect { repoRequests ->
                _requests.value = repoRequests
                _stats.value = WebViewRequestStats.from(repoRequests)
            }
        }
    }

    /**
     * Enables WebView network monitoring.
     */
    fun enable() {
        _isEnabled.value = true
    }

    /**
     * Disables WebView network monitoring.
     */
    fun disable() {
        _isEnabled.value = false
    }

    /**
     * Toggles the monitoring state.
     */
    fun toggle() {
        _isEnabled.update { !it }
    }

    /**
     * Sets a URL pattern filter. Only requests matching this pattern will be captured.
     *
     * @param pattern Regex pattern to match URLs, or null to capture all
     */
    fun setUrlFilter(pattern: String?) {
        _urlFilter.value = pattern
    }

    /**
     * Sets resource type filters. Only requests of these types will be captured.
     *
     * @param types Set of resource types to capture, or empty set for all
     */
    fun setResourceTypeFilter(types: Set<WebViewResourceType>) {
        _resourceTypeFilter.value = types
    }

    /**
     * Toggles a resource type in the filter.
     */
    fun toggleResourceTypeFilter(type: WebViewResourceType) {
        _resourceTypeFilter.update { current ->
            if (current.contains(type)) {
                current - type
            } else {
                current + type
            }
        }
    }

    /**
     * Sets a WebView ID filter. Only requests from this WebView will be captured.
     *
     * @param webViewId ID of the WebView to filter by, or null for all
     */
    fun setWebViewIdFilter(webViewId: String?) {
        _webViewIdFilter.value = webViewId
    }

    /**
     * Clears all filters.
     */
    fun clearFilters() {
        _urlFilter.value = null
        _resourceTypeFilter.value = emptySet()
        _webViewIdFilter.value = null
    }

    /**
     * Clears all captured requests.
     */
    fun clearRequests() {
        pendingRequests.clear()
        val repo = repository
        if (repo != null) {
            scope.launch { repo.clearRequests() }
        } else {
            _requests.value = emptyList()
            updateStats()
        }
    }

    /**
     * Clears requests for a specific WebView.
     *
     * @param webViewId ID of the WebView to clear requests for
     */
    fun clearRequestsForWebView(webViewId: String) {
        val repo = repository
        if (repo != null) {
            scope.launch { repo.clearRequestsForWebView(webViewId) }
        } else {
            _requests.update { current ->
                current.filter { it.webViewId != webViewId }
            }
            updateStats()
        }
    }

    /**
     * Gets requests filtered by current filter settings.
     */
    fun getFilteredRequests(): List<WebViewRequest> {
        val urlPattern = _urlFilter.value
        val typeFilter = _resourceTypeFilter.value
        val webViewId = _webViewIdFilter.value

        return _requests.value.filter { request ->
            val matchesUrl = urlPattern == null || request.url.contains(urlPattern, ignoreCase = true)
            val matchesType = typeFilter.isEmpty() || typeFilter.contains(request.resourceType)
            val matchesWebView = webViewId == null || request.webViewId == webViewId
            matchesUrl && matchesType && matchesWebView
        }
    }

    /**
     * Gets all unique WebView IDs that have made requests.
     */
    fun getWebViewIds(): Set<String> {
        return _requests.value.map { it.webViewId }.toSet()
    }

    /**
     * Creates a monitoring WebViewClient that wraps an optional delegate.
     *
     * @param webViewId Unique identifier for this WebView instance
     * @param delegate Optional existing WebViewClient to delegate calls to
     * @return A WebViewClient that monitors requests and delegates to the provided client
     */
    fun createMonitoringClient(
        webViewId: String,
        delegate: WebViewClient? = null,
    ): WebViewClient {
        return MonitoringWebViewClient(webViewId, delegate)
    }

    /**
     * Manually adds a request (useful for custom interception scenarios).
     */
    fun addRequest(request: WebViewRequest) {
        if (!_isEnabled.value) return
        if (!shouldCaptureRequest(request)) return

        pendingRequests[request.id] = request.timestamp

        val repo = repository
        if (repo != null) {
            scope.launch {
                repo.saveRequest(request)
                repo.deleteOldest(maxRequests)
            }
        } else {
            _requests.update { current ->
                val updated = listOf(request) + current
                if (updated.size > maxRequests) {
                    updated.dropLast(updated.size - maxRequests)
                } else {
                    updated
                }
            }
            updateStats()
        }
    }

    /**
     * Updates a request with response information.
     */
    fun updateRequest(
        requestId: String,
        statusCode: Int? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        mimeType: String? = null,
        encoding: String? = null,
        contentLength: Long? = null,
        errorMessage: String? = null,
    ) {
        val startTime = pendingRequests.remove(requestId)
        val duration = if (startTime != null) System.currentTimeMillis() - startTime else null

        val repo = repository
        if (repo != null) {
            scope.launch {
                val existing = _requests.value.find { it.id == requestId } ?: return@launch
                val updated = existing.copy(
                    statusCode = statusCode,
                    responseHeaders = responseHeaders,
                    mimeType = mimeType,
                    encoding = encoding,
                    contentLength = contentLength,
                    duration = duration,
                    errorMessage = errorMessage,
                )
                repo.updateRequest(updated)
            }
        } else {
            _requests.update { current ->
                current.map { request ->
                    if (request.id == requestId) {
                        request.copy(
                            statusCode = statusCode,
                            responseHeaders = responseHeaders,
                            mimeType = mimeType,
                            encoding = encoding,
                            contentLength = contentLength,
                            duration = duration,
                            errorMessage = errorMessage,
                        )
                    } else {
                        request
                    }
                }
            }
            updateStats()
        }
    }

    private fun shouldCaptureRequest(request: WebViewRequest): Boolean {
        val urlPattern = _urlFilter.value
        val typeFilter = _resourceTypeFilter.value
        val webViewId = _webViewIdFilter.value

        val matchesUrl = urlPattern == null || request.url.contains(urlPattern, ignoreCase = true)
        val matchesType = typeFilter.isEmpty() || typeFilter.contains(request.resourceType)
        val matchesWebView = webViewId == null || request.webViewId == webViewId

        return matchesUrl && matchesType && matchesWebView
    }

    private fun updateStats() {
        _stats.value = WebViewRequestStats.from(_requests.value)
    }

    /**
     * Internal WebViewClient that monitors requests.
     */
    private inner class MonitoringWebViewClient(
        private val webViewId: String,
        private val delegate: WebViewClient?,
    ) : WebViewClient() {

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?,
        ): WebResourceResponse? {
            if (_isEnabled.value && request != null) {
                val url = request.url.toString()
                val method = request.method ?: "GET"
                val headers = request.requestHeaders ?: emptyMap()
                val resourceType = inferResourceType(request)

                val webViewRequest = WebViewRequest.create(
                    url = url,
                    method = method,
                    headers = headers,
                    webViewId = webViewId,
                    resourceType = resourceType,
                    isForMainFrame = request.isForMainFrame,
                    hasGesture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        request.isRedirect
                    } else {
                        false
                    },
                    isRedirect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        request.isRedirect
                    } else {
                        false
                    },
                )

                addRequest(webViewRequest)
            }

            return delegate?.shouldInterceptRequest(view, request)
        }

        @Suppress("DEPRECATION")
        override fun shouldInterceptRequest(
            view: WebView?,
            url: String?,
        ): WebResourceResponse? {
            if (_isEnabled.value && url != null) {
                val resourceType = WebViewResourceType.fromUrl(url)

                val webViewRequest = WebViewRequest.create(
                    url = url,
                    method = "GET",
                    headers = emptyMap(),
                    webViewId = webViewId,
                    resourceType = resourceType,
                    isForMainFrame = false,
                    hasGesture = false,
                    isRedirect = false,
                )

                addRequest(webViewRequest)
            }

            @Suppress("DEPRECATION")
            return delegate?.shouldInterceptRequest(view, url)
        }

        override fun onPageStarted(
            view: WebView?,
            url: String?,
            favicon: Bitmap?,
        ) {
            delegate?.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(
            view: WebView?,
            url: String?,
        ) {
            delegate?.onPageFinished(view, url)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?,
        ) {
            if (_isEnabled.value && request != null && error != null) {
                val url = request.url.toString()
                val errorDescription = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    error.description?.toString() ?: "Unknown error"
                } else {
                    "Error"
                }

                // Find and update the matching request
                _requests.value.find { it.url == url && it.webViewId == webViewId && it.isPending }?.let { req ->
                    updateRequest(
                        requestId = req.id,
                        errorMessage = errorDescription,
                    )
                }
            }

            delegate?.onReceivedError(view, request, error)
        }

        @Suppress("DEPRECATION")
        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?,
        ) {
            if (_isEnabled.value && failingUrl != null) {
                _requests.value.find { it.url == failingUrl && it.webViewId == webViewId && it.isPending }?.let { req ->
                    updateRequest(
                        requestId = req.id,
                        errorMessage = description ?: "Error code: $errorCode",
                    )
                }
            }

            @Suppress("DEPRECATION")
            delegate?.onReceivedError(view, errorCode, description, failingUrl)
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?,
        ) {
            if (_isEnabled.value && request != null && errorResponse != null) {
                val url = request.url.toString()

                _requests.value.find { it.url == url && it.webViewId == webViewId && it.isPending }?.let { req ->
                    updateRequest(
                        requestId = req.id,
                        statusCode = errorResponse.statusCode,
                        mimeType = errorResponse.mimeType,
                        encoding = errorResponse.encoding,
                        responseHeaders = errorResponse.responseHeaders ?: emptyMap(),
                    )
                }
            }

            delegate?.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?,
        ) {
            if (_isEnabled.value && error != null) {
                val url = error.url

                _requests.value.find { it.url == url && it.webViewId == webViewId && it.isPending }?.let { req ->
                    updateRequest(
                        requestId = req.id,
                        errorMessage = "SSL Error: ${getSslErrorMessage(error)}",
                    )
                }
            }

            delegate?.onReceivedSslError(view, handler, error)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
        ): Boolean {
            return delegate?.shouldOverrideUrlLoading(view, request) ?: false
        }

        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            url: String?,
        ): Boolean {
            @Suppress("DEPRECATION")
            return delegate?.shouldOverrideUrlLoading(view, url) ?: false
        }

        private fun inferResourceType(request: WebResourceRequest): WebViewResourceType {
            val url = request.url.toString()
            val acceptHeader = request.requestHeaders?.get("Accept") ?: request.requestHeaders?.get("accept")

            // Check Accept header for hints
            return when {
                acceptHeader?.contains("text/html") == true -> WebViewResourceType.DOCUMENT
                acceptHeader?.contains("application/javascript") == true -> WebViewResourceType.SCRIPT
                acceptHeader?.contains("text/css") == true -> WebViewResourceType.STYLESHEET
                acceptHeader?.contains("image/") == true -> WebViewResourceType.IMAGE
                acceptHeader?.contains("font/") == true -> WebViewResourceType.FONT
                acceptHeader?.contains("application/json") == true -> WebViewResourceType.XHR
                request.isForMainFrame -> WebViewResourceType.DOCUMENT
                else -> WebViewResourceType.fromUrl(url)
            }
        }

        private fun getSslErrorMessage(error: SslError): String {
            return when (error.primaryError) {
                SslError.SSL_NOTYETVALID -> "Certificate not yet valid"
                SslError.SSL_EXPIRED -> "Certificate expired"
                SslError.SSL_IDMISMATCH -> "Certificate hostname mismatch"
                SslError.SSL_UNTRUSTED -> "Certificate not trusted"
                SslError.SSL_DATE_INVALID -> "Certificate date invalid"
                SslError.SSL_INVALID -> "Certificate invalid"
                else -> "Unknown SSL error"
            }
        }
    }

    companion object {
        /** Default maximum number of requests to store */
        const val DEFAULT_MAX_REQUESTS = 1000
    }
}
