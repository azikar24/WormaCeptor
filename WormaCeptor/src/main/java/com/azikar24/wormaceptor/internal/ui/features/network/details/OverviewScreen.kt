/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.formatted

@Composable
fun OverviewScreen(networkTransactionUIHelper: NetworkTransactionUIHelper?) {

    val firstSection: List<Pair<String, String?>> = listOf(
        Pair(stringResource(id = R.string.url), networkTransactionUIHelper?.networkTransaction?.url.toString()),
        Pair(stringResource(id = R.string.method), networkTransactionUIHelper?.networkTransaction?.method.toString()),
        Pair(stringResource(id = R.string.protocol), networkTransactionUIHelper?.networkTransaction?.protocol.toString()),
        Pair(stringResource(id = R.string.status), networkTransactionUIHelper?.getStatus().toString()),
        Pair(stringResource(id = R.string.response), networkTransactionUIHelper?.networkTransaction?.responseCode.toString()),
        Pair(stringResource(id = R.string.ssl), networkTransactionUIHelper?.isSsl().toString())
    )

    val secondSection: List<Pair<String, String?>> = listOf(
        Pair(stringResource(id = R.string.request_time), networkTransactionUIHelper?.networkTransaction?.requestDate?.formatted()),
        Pair(stringResource(id = R.string.response_time), networkTransactionUIHelper?.networkTransaction?.responseDate?.formatted()),
        Pair(stringResource(id = R.string.duration), stringResource(id = R.string.duration_ms, networkTransactionUIHelper?.networkTransaction?.tookMs ?:0)),
    )

    val thirdSection: List<Pair<String, String?>> = listOf(
        Pair(stringResource(id = R.string.response_size), networkTransactionUIHelper?.getRequestSizeString().toString()),
        Pair(stringResource(id = R.string.response_size), networkTransactionUIHelper?.getResponseSizeString().toString()),
        Pair(stringResource(id = R.string.total_size), networkTransactionUIHelper?.getTotalSizeString().toString()),
    )
    LazyColumn(modifier = Modifier.padding(20.dp)) {
        items(firstSection) {
            TextRow(it.first, it.second)
        }
        item {
            Box(modifier = Modifier.height(20.dp))
        }
        items(secondSection){
            TextRow(it.first, it.second)
        }
        item {
            Box(modifier = Modifier.height(20.dp))
        }
        items(thirdSection){
            TextRow(it.first, it.second)
        }
    }
}

@Preview
@Composable
fun TextRow(title: String = "title", description: String? = "description") {
    println(title)
    Row() {
        Text(text = title, modifier = Modifier.weight(0.8f))
        Text(text = description ?: "null", modifier = Modifier.weight(1f))
    }
}