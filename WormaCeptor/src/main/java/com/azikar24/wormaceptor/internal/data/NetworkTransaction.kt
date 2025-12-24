/*
 * Copyright AziKar24 4/3/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.net.Uri
import android.os.Parcelable
import com.azikar24.wormaceptor.internal.support.FormatUtils
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.parcelize.Parcelize
import androidx.core.net.toUri


@Parcelize
data class NetworkTransaction(
    var id: Long = 0,
    var requestDate: Date? = null,
    var responseDate: Date? = null,
    var tookMs: Long? = null,
    var protocol: String? = null,
    var method: String? = null,
    var url: String? = null,
    var host: String? = null,
    var path: String? = null,
    var scheme: String? = null,
    var requestContentLength: Long? = null,
    var requestContentType: String? = null,
    var requestHeaders: List<HttpHeader>? = null,
    var requestBody: String? = null,
    var requestBodyIsPlainText: Boolean = true,
    var responseCode: Int? = null,
    var responseMessage: String? = null,
    var error: String? = null,
    var responseContentLength: Long? = null,
    var responseContentType: String? = null,
    var responseHeaders: List<HttpHeader>? = null,
    var responseBody: String? = null,
    var responseBodyIsPlainText: Boolean = true,
) : Parcelable {
    enum class Status {
        Requested, Complete, Failed
    }

    fun getFormattedRequestBody(): AnnotatedString? {
        return formatBody(requestBody, requestContentType)
    }

    fun getFormattedResponseBody(): AnnotatedString? {
        return formatBody(responseBody, responseContentType)
    }

    fun getStatus(): Status {
        return if (error != null) {
            Status.Failed
        } else if (responseCode == null) {
            Status.Requested
        } else {
            Status.Complete
        }
    }

    fun getResponseSummaryText(): String? {
        return when (getStatus()) {
            Status.Failed -> error
            Status.Requested -> null
            else -> responseCode.toString() + " " + responseMessage
        }
    }

    fun getNotificationText(): String {
        return when (getStatus()) {
            Status.Failed -> " ! ! !  " + path
            Status.Requested -> " . . .  " + path
            else -> java.lang.String.valueOf(responseCode) + " " + path
        }
    }

    fun isSsl(): Boolean {
        return scheme?.lowercase(Locale.ROOT) == "https"
    }

    fun getRequestStartTimeString(): String? {
        val TIME_ONLY_FMT = SimpleDateFormat("HH:mm:ss", Locale.US)
        return requestDate?.let {
            TIME_ONLY_FMT.format(it)
        }
    }

    fun getRequestSizeString(): String {
        return formatBytes(requestContentLength ?: 0)
    }

    fun getResponseSizeString(): String {
        return formatBytes(responseContentLength ?: 0)
    }


    fun getTotalSizeString(): String {
        val reqBytes: Long = requestContentLength ?: 0L
        val resBytes: Long = responseContentLength ?: 0L
        return formatBytes(reqBytes + resBytes)
    }


    private fun formatBody(body: String?, contentType: String?): AnnotatedString? {
        return if (contentType?.lowercase(Locale.getDefault())?.contains("json") == true) {
            FormatUtils.formatJson(body)
        } else if (contentType?.lowercase(Locale.getDefault())?.contains("xml") == true) {
            FormatUtils.formatXml(body)
        } else if (contentType?.lowercase(Locale.getDefault())?.contains("form-urlencoded") == true) {
            FormatUtils.formatFormEncoded(body)
        } else {
            buildAnnotatedString { }
        }
    }

    private fun formatBytes(bytes: Long): String {
        return FormatUtils.formatByteCount(bytes, true)
    }

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
            val uri = url.toUri()
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