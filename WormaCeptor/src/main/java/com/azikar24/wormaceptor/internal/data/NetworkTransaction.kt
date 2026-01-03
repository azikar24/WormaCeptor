/*
 * Copyright AziKar24 4/3/2023.
 */

package com.azikar24.wormaceptor.internal.data

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
            else -> "$responseCode $responseMessage"
        }
    }

    fun getNotificationText(): String {
        return when (getStatus()) {
            Status.Failed -> " ! ! !  $path"
            Status.Requested -> " . . .  $path"
            else -> "$responseCode $path"
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
        } else if (contentType?.lowercase(Locale.getDefault())
                ?.contains("form-urlencoded") == true
        ) {
            FormatUtils.formatFormEncoded(body)
        } else {
            buildAnnotatedString { }
        }
    }

    private fun formatBytes(bytes: Long): String {
        return FormatUtils.formatByteCount(bytes, true)
    }

    fun setUrlHostPathSchemeFromUrl(url: String) {
        this.url = url
        val uri = url.toUri()
        uri.host?.let { this.host = it }
        uri.path?.let { this.path = it + if (uri.query != null) "?${uri.query}" else "" }
        uri.scheme?.let { this.scheme = it }
    }
}
