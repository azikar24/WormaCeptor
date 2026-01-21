/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a network request intercepted from a WebView.
 *
 * @property id Unique identifier for this request
 * @property url The URL of the request
 * @property method HTTP method (GET, POST, etc.)
 * @property headers Request headers as key-value pairs
 * @property timestamp When the request was made
 * @property webViewId Identifier for the WebView instance that made the request
 * @property resourceType The type of resource being requested
 * @property isForMainFrame Whether this request is for the main frame
 * @property hasGesture Whether the request was triggered by a user gesture
 * @property isRedirect Whether this request is a redirect
 * @property statusCode HTTP status code of the response (null if not yet received)
 * @property responseHeaders Response headers as key-value pairs
 * @property errorMessage Error message if the request failed
 * @property mimeType MIME type of the response
 * @property encoding Character encoding of the response
 * @property contentLength Content length of the response in bytes
 * @property duration Time taken for the request in milliseconds
 */
data class WebViewRequest(
    val id: String,
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val timestamp: Long,
    val webViewId: String,
    val resourceType: WebViewResourceType,
    val isForMainFrame: Boolean = false,
    val hasGesture: Boolean = false,
    val isRedirect: Boolean = false,
    val statusCode: Int? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val mimeType: String? = null,
    val encoding: String? = null,
    val contentLength: Long? = null,
    val duration: Long? = null,
) {
    /**
     * Whether the request completed successfully (status code 2xx).
     */
    val isSuccess: Boolean
        get() = statusCode in 200..299

    /**
     * Whether the request failed.
     */
    val isFailed: Boolean
        get() = errorMessage != null || (statusCode != null && statusCode !in 200..399)

    /**
     * Whether the request is still pending.
     */
    val isPending: Boolean
        get() = statusCode == null && errorMessage == null

    /**
     * Extracts the host from the URL.
     */
    val host: String
        get() = try {
            java.net.URL(url).host
        } catch (e: Exception) {
            url
        }

    /**
     * Extracts the path from the URL.
     */
    val path: String
        get() = try {
            java.net.URL(url).path.ifEmpty { "/" }
        } catch (e: Exception) {
            "/"
        }

    companion object {
        /**
         * Creates a new WebViewRequest with a generated ID.
         */
        fun create(
            url: String,
            method: String = "GET",
            headers: Map<String, String> = emptyMap(),
            webViewId: String,
            resourceType: WebViewResourceType,
            isForMainFrame: Boolean = false,
            hasGesture: Boolean = false,
            isRedirect: Boolean = false,
        ): WebViewRequest {
            return WebViewRequest(
                id = java.util.UUID.randomUUID().toString(),
                url = url,
                method = method,
                headers = headers,
                timestamp = System.currentTimeMillis(),
                webViewId = webViewId,
                resourceType = resourceType,
                isForMainFrame = isForMainFrame,
                hasGesture = hasGesture,
                isRedirect = isRedirect,
            )
        }
    }
}

/**
 * Type of resource being requested by the WebView.
 */
enum class WebViewResourceType(
    val displayName: String,
    val description: String,
) {
    DOCUMENT("Document", "HTML documents and main frames"),
    SCRIPT("Script", "JavaScript files"),
    STYLESHEET("Stylesheet", "CSS files"),
    IMAGE("Image", "Image files (PNG, JPG, GIF, etc.)"),
    FONT("Font", "Web fonts (WOFF, TTF, etc.)"),
    XHR("XHR", "XMLHttpRequest / Fetch API calls"),
    MEDIA("Media", "Audio and video files"),
    WEBSOCKET("WebSocket", "WebSocket connections"),
    MANIFEST("Manifest", "Web app manifest files"),
    OBJECT("Object", "Plugin content"),
    IFRAME("IFrame", "Inline frame content"),
    OTHER("Other", "Other resource types"),
    UNKNOWN("Unknown", "Unknown resource type");

    companion object {
        /**
         * Infers the resource type from a URL and MIME type.
         */
        fun fromUrl(url: String, mimeType: String? = null): WebViewResourceType {
            val lowercaseUrl = url.lowercase()
            val lowercaseMime = mimeType?.lowercase()

            return when {
                // Check MIME type first if available
                lowercaseMime?.contains("text/html") == true -> DOCUMENT
                lowercaseMime?.contains("javascript") == true -> SCRIPT
                lowercaseMime?.contains("text/css") == true -> STYLESHEET
                lowercaseMime?.startsWith("image/") == true -> IMAGE
                lowercaseMime?.contains("font") == true -> FONT
                lowercaseMime?.startsWith("audio/") == true -> MEDIA
                lowercaseMime?.startsWith("video/") == true -> MEDIA
                lowercaseMime?.contains("json") == true -> XHR
                lowercaseMime?.contains("xml") == true -> XHR

                // Check URL extension
                lowercaseUrl.endsWith(".html") || lowercaseUrl.endsWith(".htm") -> DOCUMENT
                lowercaseUrl.endsWith(".js") || lowercaseUrl.contains(".js?") -> SCRIPT
                lowercaseUrl.endsWith(".css") || lowercaseUrl.contains(".css?") -> STYLESHEET
                lowercaseUrl.matches(Regex(".*\\.(png|jpg|jpeg|gif|webp|svg|ico|bmp)(\\?.*)?$")) -> IMAGE
                lowercaseUrl.matches(Regex(".*\\.(woff|woff2|ttf|otf|eot)(\\?.*)?$")) -> FONT
                lowercaseUrl.matches(Regex(".*\\.(mp3|wav|ogg|mp4|webm|avi|mov)(\\?.*)?$")) -> MEDIA
                lowercaseUrl.contains("manifest.json") || lowercaseUrl.endsWith(".webmanifest") -> MANIFEST
                lowercaseUrl.startsWith("ws://") || lowercaseUrl.startsWith("wss://") -> WEBSOCKET

                // API endpoints commonly return JSON
                lowercaseUrl.contains("/api/") -> XHR
                lowercaseUrl.endsWith(".json") -> XHR

                else -> UNKNOWN
            }
        }
    }
}

/**
 * Statistics about WebView requests.
 *
 * @property totalRequests Total number of requests captured
 * @property successfulRequests Number of successful requests (2xx status)
 * @property failedRequests Number of failed requests
 * @property pendingRequests Number of pending requests
 * @property byResourceType Count of requests by resource type
 * @property averageDuration Average request duration in milliseconds
 * @property totalDataTransferred Total bytes transferred
 */
data class WebViewRequestStats(
    val totalRequests: Int,
    val successfulRequests: Int,
    val failedRequests: Int,
    val pendingRequests: Int,
    val byResourceType: Map<WebViewResourceType, Int>,
    val averageDuration: Long,
    val totalDataTransferred: Long,
) {
    companion object {
        fun empty() = WebViewRequestStats(
            totalRequests = 0,
            successfulRequests = 0,
            failedRequests = 0,
            pendingRequests = 0,
            byResourceType = emptyMap(),
            averageDuration = 0L,
            totalDataTransferred = 0L,
        )

        fun from(requests: List<WebViewRequest>): WebViewRequestStats {
            if (requests.isEmpty()) return empty()

            val successful = requests.count { it.isSuccess }
            val failed = requests.count { it.isFailed }
            val pending = requests.count { it.isPending }

            val byType = requests.groupingBy { it.resourceType }.eachCount()

            val durations = requests.mapNotNull { it.duration }
            val avgDuration = if (durations.isNotEmpty()) durations.average().toLong() else 0L

            val totalBytes = requests.mapNotNull { it.contentLength }.sum()

            return WebViewRequestStats(
                totalRequests = requests.size,
                successfulRequests = successful,
                failedRequests = failed,
                pendingRequests = pending,
                byResourceType = byType,
                averageDuration = avgDuration,
                totalDataTransferred = totalBytes,
            )
        }
    }
}
