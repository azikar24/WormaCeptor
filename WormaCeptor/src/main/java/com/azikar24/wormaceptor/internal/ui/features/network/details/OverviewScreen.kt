/*
 * Copyright AziKar24 28/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.formatted

@Composable
fun OverviewScreen(networkTransaction: NetworkTransaction?) {

    val firstSection: List<Pair<String, String?>> = listOf(
        Pair(stringResource(id = R.string.url), networkTransaction?.url.toString()),
        Pair(stringResource(id = R.string.method), networkTransaction?.method.toString()),
        Pair(stringResource(id = R.string.protocol), networkTransaction?.protocol.toString()),
        Pair(stringResource(id = R.string.status), networkTransaction?.getStatus().toString()),
        Pair(stringResource(id = R.string.response), networkTransaction?.responseCode.toString()),
        Pair(stringResource(id = R.string.ssl), networkTransaction?.isSsl().toString())
    )

    val secondSection: List<Pair<String, String?>> = listOf(
        Pair(stringResource(id = R.string.request_time), networkTransaction?.requestDate?.formatted()),
        Pair(stringResource(id = R.string.response_time), networkTransaction?.responseDate?.formatted()),
        Pair(stringResource(id = R.string.duration), stringResource(id = R.string.duration_ms, networkTransaction?.tookMs ?:0)),
    )

    val thirdSection: List<Pair<String, String?>> = listOf(
        Pair(stringResource(id = R.string.request_size), networkTransaction?.getRequestSizeString().toString()),
        Pair(stringResource(id = R.string.response_size), networkTransaction?.getResponseSizeString().toString()),
        Pair(stringResource(id = R.string.total_size), networkTransaction?.getTotalSizeString().toString()),
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingPaddingValues(20.dp)
    ) {
        items(firstSection) {
            TextRow(it.first, it.second)
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
        items(secondSection){
            TextRow(it.first, it.second)
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
        items(thirdSection){
            TextRow(it.first, it.second)
        }
    }
}

private fun PaddingPaddingValues(all: androidx.compose.ui.unit.Dp) = PaddingValues(all)

@Preview(showBackground = true)
@Composable
fun TextRow(title: String = "title", description: String? = "description") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title, 
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description ?: "null", 
            modifier = Modifier.weight(0.6f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}