/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal

import android.os.Parcelable
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.FormatUtils
import java.text.SimpleDateFormat
import java.util.*
import androidx.arch.core.util.Function
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.parcelize.Parcelize


@Parcelize
class NetworkTransactionUIHelper(val networkTransaction: NetworkTransaction) : Parcelable {
    enum class Status {
        Requested, Complete, Failed
    }

    var searchKey: String? = null


    fun getFormattedRequestBody(): AnnotatedString? {
        return formatBody(networkTransaction.requestBody, networkTransaction.requestContentType)
    }

    fun getFormattedResponseBody(): AnnotatedString? {
        return formatBody(networkTransaction.responseBody, networkTransaction.responseContentType)
    }

    fun getStatus(): Status {
        return if (networkTransaction.error != null) {
            Status.Failed
        } else if (networkTransaction.responseCode == null) {
            Status.Requested
        } else {
            Status.Complete
        }
    }

    fun getResponseSummaryText(): String? {
        return when (getStatus()) {
            Status.Failed -> networkTransaction.error
            Status.Requested -> null
            else -> networkTransaction.responseCode.toString() + " " + networkTransaction.responseMessage
        }
    }

    fun getNotificationText(): String {
        return when (getStatus()) {
            Status.Failed -> " ! ! !  " + networkTransaction.path
            Status.Requested -> " . . .  " + networkTransaction.path
            else -> java.lang.String.valueOf(networkTransaction.responseCode) + " " + networkTransaction.path
        }
    }

    fun isSsl(): Boolean {
        return networkTransaction.scheme?.lowercase(Locale.ROOT) == "https"
    }

    fun getRequestStartTimeString(): String? {
        val TIME_ONLY_FMT = SimpleDateFormat("HH:mm:ss", Locale.US)
        return networkTransaction.requestDate?.let {
            TIME_ONLY_FMT.format(it)
        }
    }

    fun getRequestSizeString(): String {
        return formatBytes(networkTransaction.requestContentLength ?: 0)
    }

    fun getResponseSizeString(): String {
        return formatBytes(networkTransaction.responseContentLength ?: 0)
    }


    fun getTotalSizeString(): String {
        val reqBytes: Long = networkTransaction.requestContentLength ?: 0L
        val resBytes: Long = networkTransaction.responseContentLength ?: 0L
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

    override fun toString(): String {
        return "NetworkTransactionUIHelper(networkTransaction=$networkTransaction, searchKey=$searchKey)"
    }


    companion object {
        val NETWORK_TRANSACTION_UI_HELPER_FUNCTION: Function<NetworkTransaction, NetworkTransactionUIHelper> = Function { networkTransaction ->
            NetworkTransactionUIHelper(networkTransaction)
        }
    }
}