/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.annotations.ComponentPreviews
import com.azikar24.wormaceptor.internal.data.HttpHeader
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSSl
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.example.wormaceptor.ui.drawables.MyIconPack
import java.util.*


@Composable
fun NetworkListItem(
    data: NetworkTransaction,
    searchKey: String? = null,
    onClick: (NetworkTransaction) -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(20.dp)
            .clickable {
                onClick(data)
            }
    ) {
        StatusCode(data, searchKey)
        ItemBody(data, searchKey)
    }
}

@Composable
private fun StatusCode(data: NetworkTransaction, searchKey: String?) {
    val responseCode = data.responseCode
    val status = data.getStatus()
    val transactionColor = ColorUtil.getTransactionColor(data, true)
    Text(
        text = when (status) {
            NetworkTransaction.Status.Complete -> {
                FormatUtils.getHighlightedText(
                    text = responseCode.toString(),
                    searchKey = searchKey
                )
            }

            NetworkTransaction.Status.Failed -> {
                AnnotatedString("!!!")
            }

            else -> {
                AnnotatedString("")
            }
        },
        color = transactionColor,
        modifier = Modifier.width(30.dp)
    )
}

@Composable
private fun ItemBody(networkTransaction: NetworkTransaction, searchKey: String?) {
    val status = networkTransaction.getStatus()
    val transactionColor = ColorUtil.getTransactionColor(networkTransaction, true)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        MethodAndPath(
            method = networkTransaction.method,
            path = networkTransaction.path,
            transactionColor = transactionColor,
            searchKey = searchKey
        )
        Host(
            host = networkTransaction.host,
            ssl = networkTransaction.isSsl(),
            searchKey = searchKey
        )
        Stats(
            requestStartTimeString = networkTransaction.getRequestStartTimeString(),
            tookMs = networkTransaction.tookMs,
            totalSizeString = networkTransaction.getTotalSizeString(),
            status = status
        )
    }
}

@Composable
fun MethodAndPath(method: String?, path: String?, transactionColor: Color, searchKey: String?) {
    Text(
        text = FormatUtils.getHighlightedText(text = "[${method}] ${path}", searchKey = searchKey),
        color = transactionColor,
    )
}

@Composable
fun Host(host: String?, ssl: Boolean, searchKey: String?) {
    Row {
        Text(
            text = FormatUtils.getHighlightedText(text = "$host", searchKey = searchKey),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(end = 8.dp)
        )
        if (ssl) {
            Icon(
                imageVector = MyIconPack.IcSSl, contentDescription = "",
                modifier = Modifier
                    .padding(4.dp)
                    .size(14.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
        }

    }
}


@Composable
fun Stats(
    requestStartTimeString: String?,
    tookMs: Long?,
    totalSizeString: String,
    status: NetworkTransaction.Status
) {
    Row {
        Text(
            text = requestStartTimeString.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (status == NetworkTransaction.Status.Complete) stringResource(
                id = R.string.duration_ms,
                tookMs ?: 0L
            ) else "",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (status == NetworkTransaction.Status.Complete) totalSizeString else "",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

    }
}


@ComponentPreviews
@Composable
fun PreviewNetworkListItem() {
    WormaCeptorMainTheme() {
        val data = NetworkTransaction(
            id = 1,
            requestDate = Date(),
            responseDate = Date(),
            tookMs = 100L,
            protocol = "protocol",
            method = "method",
            url = "url",
            host = "host",
            path = "path",
            scheme = "https",
            requestContentLength = 100L,
            requestContentType = "",
            requestHeaders = listOf(HttpHeader("name", "value")),
            requestBody = "requestBody",
            requestBodyIsPlainText = true,
            responseCode = 200,
            responseMessage = "",
            error = "",
            responseContentLength = 100L,
            responseContentType = "",
            responseHeaders = listOf(HttpHeader("name", "value")),
            responseBody = "",
            responseBodyIsPlainText = true
        )
        NetworkListItem(data = data, onClick = { })
    }

}