/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.net.Uri
import android.os.Parcelable
import java.util.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class NetworkTransaction(
    var id: Long,
    var requestDate: Date?,
    var responseDate: Date?,
    var tookMs: Long?,
    var protocol: String?,
    var method: String?,
    var url: String?,
    var host: String?,
    var path: String?,
    var scheme: String?,
    var requestContentLength: Long?,
    var requestContentType: String?,
    var requestHeaders: List<HttpHeader>?,
    var requestBody: String?,
    var requestBodyIsPlainText: Boolean,
    var responseCode: Int?,
    var responseMessage: String?,
    var error: String?,
    var responseContentLength: Long?,
    var responseContentType: String?,
    var responseHeaders: List<HttpHeader>?,
    var responseBody: String?,
    var responseBodyIsPlainText: Boolean,
) : Parcelable {
    fun toBuilder(): Builder {
        return Builder().also { builder ->
            builder.id = id
            builder.requestDate = requestDate
            builder.responseDate = responseDate
            builder.tookMs = tookMs
            builder.protocol = protocol
            builder.method = method
            builder.url = url
            builder.host = host
            builder.path = path
            builder.scheme = scheme
            builder.requestContentLength = requestContentLength
            builder.requestContentType = requestContentType
            builder.requestHeaders = requestHeaders
            builder.requestBody = requestBody
            builder.requestBodyIsPlainText = requestBodyIsPlainText
            builder.responseCode = responseCode
            builder.responseMessage = responseMessage
            builder.error = error
            builder.responseContentLength = responseContentLength
            builder.responseContentType = responseContentType
            builder.responseHeaders = responseHeaders
            builder.responseBody = responseBody
            builder.responseBodyIsPlainText = responseBodyIsPlainText
        }
    }

    @Parcelize
    class Builder : Parcelable {
        //region START Variables
        var id: Long = 0
        var requestDate: Date? = null
        var responseDate: Date? = null
        var tookMs: Long? = null
        var protocol: String? = null
        var method: String? = null
        var url: String? = null
        var host: String? = null
        var path: String? = null
        var scheme: String? = null
        var requestContentLength: Long? = null
        var requestContentType: String? = null
        var requestHeaders: List<HttpHeader>? = null
        var requestBody: String? = null
        var requestBodyIsPlainText = true
        var responseCode: Int? = null
        var responseMessage: String? = null
        var error: String? = null
        var responseContentLength: Long? = null
        var responseContentType: String? = null
        var responseHeaders: List<HttpHeader>? = null
        var responseBody: String? = null
        var responseBodyIsPlainText = true
        //endregion END Variables

        //region START Builder Setters
        fun setId(value: Long) = this.apply { id = value }
        fun setRequestDate(value: Date) = this.apply { requestDate = value }
        fun setResponseDate(value: Date) = this.apply { responseDate = value }
        fun setTookMs(value: Long) = this.apply { tookMs = value }
        fun setProtocol(value: String) = this.apply { protocol = value }
        fun setMethod(value: String) = this.apply { method = value }
        fun setUrl(value: String) = this.apply { url = value }
        fun setHost(value: String) = this.apply { host = value }
        fun setPath(value: String) = this.apply { path = value }
        fun setScheme(value: String) = this.apply { scheme = value }
        fun setRequestContentLength(value: Long) = this.apply { requestContentLength = value }
        fun setRequestContentType(value: String) = this.apply { requestContentType = value }
        fun setRequestHeaders(value: List<HttpHeader>) = this.apply { requestHeaders = value }
        fun setRequestBody(value: String) = this.apply { requestBody = value }
        fun setRequestBodyIsPlainText(value: Boolean) = this.apply { requestBodyIsPlainText = value }
        fun setResponseCode(value: Int) = this.apply { responseCode = value }
        fun setResponseMessage(value: String) = this.apply { responseMessage = value }
        fun setError(value: String) = this.apply { error = value }
        fun setResponseContentLength(value: Long) = this.apply { responseContentLength = value }
        fun setResponseContentType(value: String) = this.apply { responseContentType = value }
        fun setResponseHeaders(value: List<HttpHeader>) = this.apply { responseHeaders = value }
        fun setResponseBody(value: String) = this.apply { responseBody = value }
        fun setResponseBodyIsPlainText(value: Boolean) = this.apply { responseBodyIsPlainText = value }

        fun setUrlHostPathSchemeFromUrl(url: String) {
            setUrl(url)
            val uri = Uri.parse(url)
            uri.host?.let { setHost(it) }
            uri.path?.let { setPath(it + if (uri.query != null) "?${uri.query}" else "") }
            uri.scheme?.let { setScheme(it) }
        }
        //endregion END Builder Setters

        fun build(): NetworkTransaction {
            return NetworkTransaction(
                id,
                requestDate,
                responseDate,
                tookMs,
                protocol,
                method,
                url,
                host,
                path,
                scheme,
                requestContentLength,
                requestContentType,
                requestHeaders,
                requestBody,
                requestBodyIsPlainText,
                responseCode,
                responseMessage,
                error,
                responseContentLength,
                responseContentType,
                responseHeaders,
                responseBody,
                responseBodyIsPlainText,
            )
        }
    }
}