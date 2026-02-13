package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

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
