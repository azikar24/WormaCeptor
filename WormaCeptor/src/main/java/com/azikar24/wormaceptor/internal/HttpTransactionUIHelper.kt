/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal

import android.os.Parcelable
import com.azikar24.wormaceptor.internal.data.HttpTransaction
import com.azikar24.wormaceptor.internal.support.FormatUtils
import java.text.SimpleDateFormat
import java.util.*
import androidx.arch.core.util.Function
import kotlinx.parcelize.Parcelize


@Parcelize
class HttpTransactionUIHelper(val httpTransaction: HttpTransaction) : Parcelable {
    enum class Status {
        Requested, Complete, Failed
    }

    var searchKey: String? = null


    fun getFormattedRequestBody(): CharSequence? {
        return formatBody(httpTransaction.requestBody, httpTransaction.requestContentType)
    }

    fun getFormattedResponseBody(): CharSequence? {
        return formatBody(httpTransaction.responseBody, httpTransaction.responseContentType)
    }

    fun getStatus(): Status {
        return if (httpTransaction.error != null) {
            Status.Failed
        } else if (httpTransaction.responseCode == null) {
            Status.Requested
        } else {
            Status.Complete
        }
    }

    fun getResponseSummaryText(): String? {
        return when (getStatus()) {
            Status.Failed -> httpTransaction.error
            Status.Requested -> null
            else -> httpTransaction.responseCode.toString() + " " + httpTransaction.responseMessage
        }
    }

    fun getNotificationText(): String {
        return when (getStatus()) {
            Status.Failed -> " ! ! !  " + httpTransaction.path
            Status.Requested -> " . . .  " + httpTransaction.path
            else -> java.lang.String.valueOf(httpTransaction.responseCode) + " " + httpTransaction.path
        }
    }

    fun isSsl(): Boolean {
        return httpTransaction.scheme?.lowercase(Locale.ROOT) == "https"
    }

    fun getRequestStartTimeString(): String? {
        val TIME_ONLY_FMT = SimpleDateFormat("HH:mm:ss", Locale.US)
        return httpTransaction.requestDate?.let {
            TIME_ONLY_FMT.format(it)
        }
    }

    fun getRequestSizeString(): String {
        return formatBytes(httpTransaction.requestContentLength ?: 0)
    }

    fun getResponseSizeString(): String {
        return formatBytes(httpTransaction.responseContentLength ?: 0)
    }


    fun getTotalSizeString(): String {
        val reqBytes: Long = httpTransaction.requestContentLength ?: 0L
        val resBytes: Long = httpTransaction.responseContentLength ?: 0L
        return formatBytes(reqBytes + resBytes)
    }


    private fun formatBody(body: String?, contentType: String?): CharSequence? {
        if (contentType != null && body != null) {
            if (contentType.lowercase(Locale.getDefault()).contains("json")) {
                return FormatUtils.formatJson(body)
            } else if (contentType.lowercase(Locale.getDefault()).contains("xml")) {
                return FormatUtils.formatXml(body)
            } else if (contentType.lowercase(Locale.getDefault()).contains("form-urlencoded")) {
                return FormatUtils.formatFormEncoded(body)
            }
        }
        return body
    }

    private fun formatBytes(bytes: Long): String {
        return FormatUtils.formatByteCount(bytes, true)
    }

    fun getResponseHeadersString(withMarkup: Boolean): CharSequence {
        return FormatUtils.formatHeaders(httpTransaction.responseHeaders, withMarkup)
    }

    fun getRequestHeadersString(withMarkup: Boolean): CharSequence {
        return FormatUtils.formatHeaders(httpTransaction.requestHeaders, withMarkup)
    }

    override fun toString(): String {
        return "HttpTransactionUIHelper(httpTransaction=$httpTransaction, searchKey=$searchKey)"
    }

    companion object {
        val HTTP_TRANSACTION_UI_HELPER_FUNCTION: Function<HttpTransaction, HttpTransactionUIHelper> = Function { httpTransaction ->
            HttpTransactionUIHelper(httpTransaction)
        }
    }
}