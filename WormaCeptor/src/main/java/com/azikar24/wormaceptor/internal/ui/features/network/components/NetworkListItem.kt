/*
 * Copyright AziKar24 2024.
 */

package com.azikar24.wormaceptor.internal.ui.features.network.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.annotations.ComponentPreviews
import com.azikar24.wormaceptor.internal.data.HttpHeader
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.example.wormaceptor.ui.drawables.MyIconPack
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSSl
import java.util.*

@Composable
fun NetworkListItem(
    data: NetworkTransaction,
    searchKey: String? = null,
    onClick: (NetworkTransaction) -> Unit
) {
    val transactionColors = ColorUtil.getTransactionColors(data)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(data) }
            .background(MaterialTheme.colorScheme.surface)
            .height(IntrinsicSize.Min)
    ) {
        // Left Side Status Strip
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(transactionColors.text)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Method Badge
                Surface(
                    color = transactionColors.container,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = data.method ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = transactionColors.onContainer,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Status Code
                Text(
                    text = when (data.getStatus()) {
                        NetworkTransaction.Status.Complete -> data.responseCode.toString()
                        NetworkTransaction.Status.Failed -> "FAILED"
                        NetworkTransaction.Status.Requested -> "..."
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = transactionColors.text,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Time and SSL in header row to save space
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = data.getRequestStartTimeString() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (data.isSsl()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = MyIconPack.IcSSl,
                            contentDescription = "SSL",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Path - Now on its own line to support length
            Text(
                text = FormatUtils.getHighlightedText(data.path, searchKey),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Host
            Text(
                text = FormatUtils.getHighlightedText(data.host, searchKey),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Footer Metadata
            if (data.getStatus() == NetworkTransaction.Status.Complete) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.duration_ms, data.tookMs ?: 0L),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = data.getTotalSizeString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@ComponentPreviews
@Composable
fun PreviewNetworkListItem() {
    WormaCeptorMainTheme {
        val data = NetworkTransaction(
            id = 1,
            requestDate = Date(),
            responseDate = Date(),
            tookMs = 100L,
            protocol = "protocol",
            method = "GET",
            url = "https://example.com/api/v1/users",
            host = "example.com",
            path = "/api/v1/users",
            scheme = "https",
            requestContentLength = 100L,
            requestContentType = "",
            requestHeaders = listOf(HttpHeader("name", "value")),
            requestBody = "requestBody",
            requestBodyIsPlainText = true,
            responseCode = 200,
            responseMessage = "OK",
            error = null,
            responseContentLength = 1024L,
            responseContentType = "application/json",
            responseHeaders = listOf(HttpHeader("Content-Type", "application/json")),
            responseBody = "{}",
            responseBodyIsPlainText = true
        )
        NetworkListItem(data = data, onClick = { })
    }
}
