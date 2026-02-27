package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room entity representing an intercepted WebView network request.
 *
 * @property id Unique identifier for the WebView request.
 * @property url Full URL that was requested.
 * @property method HTTP method used for the request.
 * @property headersJson JSON-serialised map of request headers.
 * @property timestamp Epoch millis when the request was intercepted.
 * @property webViewId Identifier of the WebView instance that originated the request.
 * @property resourceType Stringified [WebViewResourceType] of the requested resource.
 * @property isForMainFrame Whether this request targets the main frame.
 * @property hasGesture Whether the request was initiated by a user gesture.
 * @property isRedirect Whether this request is a server-side redirect.
 * @property statusCode HTTP response status code, or null if no response received.
 * @property responseHeadersJson JSON-serialised map of response headers.
 * @property errorMessage Error description if the request failed, or null on success.
 * @property mimeType MIME type of the response content, or null if unknown.
 * @property encoding Character encoding of the response content, or null if unknown.
 * @property contentLength Size of the response body in bytes, or null if unknown.
 * @property duration Request duration in milliseconds, or null if not measured.
 */
@Entity(tableName = "webview_requests")
data class WebViewRequestEntity(
    @PrimaryKey val id: String,
    val url: String,
    val method: String,
    val headersJson: String,
    val timestamp: Long,
    val webViewId: String,
    val resourceType: String,
    val isForMainFrame: Boolean,
    val hasGesture: Boolean,
    val isRedirect: Boolean,
    val statusCode: Int?,
    val responseHeadersJson: String,
    val errorMessage: String?,
    val mimeType: String?,
    val encoding: String?,
    val contentLength: Long?,
    val duration: Long?,
) {
    /** Converts this entity to a domain [WebViewRequest] model. */
    fun toDomain(): WebViewRequest {
        val headers = try {
            json.decodeFromString<Map<String, String>>(headersJson)
        } catch (_: Exception) {
            emptyMap()
        }
        val respHeaders = try {
            json.decodeFromString<Map<String, String>>(responseHeadersJson)
        } catch (_: Exception) {
            emptyMap()
        }
        val type = try {
            WebViewResourceType.valueOf(resourceType)
        } catch (_: Exception) {
            WebViewResourceType.UNKNOWN
        }
        return WebViewRequest(
            id = id,
            url = url,
            method = method,
            headers = headers,
            timestamp = timestamp,
            webViewId = webViewId,
            resourceType = type,
            isForMainFrame = isForMainFrame,
            hasGesture = hasGesture,
            isRedirect = isRedirect,
            statusCode = statusCode,
            responseHeaders = respHeaders,
            errorMessage = errorMessage,
            mimeType = mimeType,
            encoding = encoding,
            contentLength = contentLength,
            duration = duration,
        )
    }

    /** JSON serialization and domain-entity conversion factory. */
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Creates a [WebViewRequestEntity] from a domain [WebViewRequest] model. */
        fun fromDomain(request: WebViewRequest): WebViewRequestEntity {
            return WebViewRequestEntity(
                id = request.id,
                url = request.url,
                method = request.method,
                headersJson = json.encodeToString(request.headers),
                timestamp = request.timestamp,
                webViewId = request.webViewId,
                resourceType = request.resourceType.name,
                isForMainFrame = request.isForMainFrame,
                hasGesture = request.hasGesture,
                isRedirect = request.isRedirect,
                statusCode = request.statusCode,
                responseHeadersJson = json.encodeToString(request.responseHeaders),
                errorMessage = request.errorMessage,
                mimeType = request.mimeType,
                encoding = request.encoding,
                contentLength = request.contentLength,
                duration = request.duration,
            )
        }
    }
}
