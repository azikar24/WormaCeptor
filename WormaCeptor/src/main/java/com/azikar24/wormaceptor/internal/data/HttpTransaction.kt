/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*


@Parcelize
class HttpTransaction(private val builder: Builder) : Parcelable {
    //region START Variables
    var id: Long = builder.id
    var requestDate = builder.requestDate
    var responseDate = builder.responseDate
    var tookMs = builder.tookMs
    var protocol = builder.protocol
    var method = builder.method
    var url = builder.url
    var host = builder.host
    var path = builder.path
    var scheme = builder.scheme
    var requestContentLength = builder.requestContentLength
    var requestContentType = builder.requestContentType
    var requestHeaders = builder.requestHeaders
    var requestBody = builder.requestBody
    var requestBodyIsPlainText = builder.requestBodyIsPlainText
    var responseCode = builder.responseCode
    var responseMessage = builder.responseMessage
    var error = builder.error
    var responseContentLength = builder.responseContentLength
    var responseContentType = builder.responseContentType
    var responseHeaders = builder.responseHeaders
    var responseBody = builder.responseBody
    var responseBodyIsPlainText = builder.responseBodyIsPlainText
    //endregion END Variables

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as HttpTransaction
        if (id != that.id) return false
        if (requestBodyIsPlainText != that.requestBodyIsPlainText) return false
        if (responseBodyIsPlainText != that.responseBodyIsPlainText) return false
        if (requestDate != that.requestDate) return false
        if (responseDate != that.responseDate) return false
        if (tookMs != that.tookMs) return false
        if (protocol != that.protocol) return false
        if (method != that.method) return false
        if (url != that.url) return false
        if (host != that.host) return false
        if (path != that.path) return false
        if (scheme != that.scheme) return false
        if (requestContentLength != that.requestContentLength) return false
        if (requestContentType != that.requestContentType) return false
        if (requestHeaders != that.requestHeaders) return false
        if (requestBody != that.requestBody) return false
        if (responseCode != that.responseCode) return false
        if (responseMessage != that.responseMessage) return false
        if (error != that.error) return false
        if (responseContentLength != that.responseContentLength) return false
        if (responseContentType != that.responseContentType) return false
        if (responseHeaders != that.responseHeaders) return false
        return if (responseBody != null) responseBody == that.responseBody else that.responseBody == null
    }

    override fun hashCode(): Int {
        var result = (id xor (id ushr 32)).toInt()
        result = 31 * result + if (requestDate != null) requestDate.hashCode() else 0
        result = 31 * result + if (responseDate != null) responseDate.hashCode() else 0
        result = 31 * result + if (tookMs != null) tookMs.hashCode() else 0
        result = 31 * result + if (protocol != null) protocol.hashCode() else 0
        result = 31 * result + if (method != null) method.hashCode() else 0
        result = 31 * result + if (url != null) url.hashCode() else 0
        result = 31 * result + if (host != null) host.hashCode() else 0
        result = 31 * result + if (path != null) path.hashCode() else 0
        result = 31 * result + if (scheme != null) scheme.hashCode() else 0
        result = 31 * result + if (requestContentLength != null) requestContentLength.hashCode() else 0
        result = 31 * result + if (requestContentType != null) requestContentType.hashCode() else 0
        result = 31 * result + if (requestHeaders != null) requestHeaders.hashCode() else 0
        result = 31 * result + if (requestBody != null) requestBody.hashCode() else 0
        result = 31 * result + if (requestBodyIsPlainText) 1 else 0
        result = 31 * result + if (responseCode != null) responseCode.hashCode() else 0
        result = 31 * result + if (responseMessage != null) responseMessage.hashCode() else 0
        result = 31 * result + if (error != null) error.hashCode() else 0
        result = 31 * result + if (responseContentLength != null) responseContentLength.hashCode() else 0
        result = 31 * result + if (responseContentType != null) responseContentType.hashCode() else 0
        result = 31 * result + if (responseHeaders != null) responseHeaders.hashCode() else 0
        result = 31 * result + if (responseBody != null) responseBody.hashCode() else 0
        result = 31 * result + if (responseBodyIsPlainText) 1 else 0
        return result
    }

    override fun toString(): String {
        return "HttpTransaction(builder=$builder, id=$id, requestDate=$requestDate, responseDate=$responseDate, tookMs=$tookMs, protocol=$protocol, method=$method, url=$url, host=$host, path=$path, scheme=$scheme, requestContentLength=$requestContentLength, requestContentType=$requestContentType, requestHeaders=$requestHeaders, requestBody=$requestBody, requestBodyIsPlainText=$requestBodyIsPlainText, responseCode=$responseCode, responseMessage=$responseMessage, error=$error, responseContentLength=$responseContentLength, responseContentType=$responseContentType, responseHeaders=$responseHeaders, responseBody=$responseBody, responseBodyIsPlainText=$responseBodyIsPlainText)"
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
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

        fun build(): HttpTransaction {
            return HttpTransaction(this)
        }
    }
}