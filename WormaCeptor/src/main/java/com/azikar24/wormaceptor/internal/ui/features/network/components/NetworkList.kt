/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.azikar24.wormaceptor.annotations.ComponentPreviews
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.HttpHeader
import kotlinx.coroutines.flow.flowOf
import java.util.*


@Composable
fun NetworkList(data: LazyPagingItems<NetworkTransaction>, searchKey: String? = null, onClick: (NetworkTransaction) -> Unit) {
    LazyColumn() {
        items(data.itemSnapshotList.items) { item ->
            Column {
                NetworkListItem(data = item, searchKey = searchKey, onClick = onClick)
                Divider(color = MaterialTheme.colors.onSurface.copy(0.2f), thickness = 1.dp)
            }
        }
        item { Box(Modifier.height(120.dp)) }
    }
}


@ComponentPreviews
@Composable
private fun PreviewNetworkList() {
    val data = mutableListOf(
        NetworkTransaction(
            id = 1, requestDate = Date(), responseDate = Date(), tookMs = 100L, protocol = "protocol", method = "method", url = "url", host = "host", path = "path", scheme = "https", requestContentLength = 100L, requestContentType = "", requestHeaders = listOf(HttpHeader("name", "value")), requestBody = "requestBody", requestBodyIsPlainText = true, responseCode = 200, responseMessage = "", error = "", responseContentLength = 100L, responseContentType = "", responseHeaders = listOf(HttpHeader("name", "value")), responseBody = "", responseBodyIsPlainText = true
        )
    )

    NetworkList(data = flowOf(PagingData.from(data)).collectAsLazyPagingItems()) {

    }
}