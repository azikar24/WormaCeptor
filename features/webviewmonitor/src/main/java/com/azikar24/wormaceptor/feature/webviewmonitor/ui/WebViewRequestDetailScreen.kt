package com.azikar24.wormaceptor.feature.webviewmonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.feature.webviewmonitor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebViewRequestDetailScreen(request: WebViewRequest, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            request.method,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                        Text(
                            request.host,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.webviewmonitor_action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Status card
            Card(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.Spacing.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WormaCeptorDesignSystem.Spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(getStatusColor(request).copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = when {
                                    request.isPending -> Icons.Default.HourglassEmpty
                                    request.isSuccess -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Error
                                },
                                contentDescription = null,
                                tint = getStatusColor(request),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column {
                            Text(
                                when {
                                    request.isPending -> stringResource(R.string.webviewmonitor_status_pending)
                                    request.isSuccess -> stringResource(R.string.webviewmonitor_status_success)
                                    else -> stringResource(R.string.webviewmonitor_status_failed)
                                },
                                fontWeight = FontWeight.SemiBold,
                            )
                            val statusText = request.statusCode?.let {
                                stringResource(
                                    R.string.webviewmonitor_detail_status_prefix,
                                    it,
                                )
                            }
                                ?: request.errorMessage
                                ?: stringResource(R.string.webviewmonitor_status_waiting)
                            Text(
                                statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    request.duration?.let { duration ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                formatDuration(duration),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3),
                            )
                            Text(
                                stringResource(R.string.webviewmonitor_detail_duration),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // URL card
            DetailCard(title = stringResource(R.string.webviewmonitor_detail_url)) {
                Text(
                    text = request.url,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Request info
            val yesString = stringResource(R.string.webviewmonitor_yes)
            val noString = stringResource(R.string.webviewmonitor_no)
            DetailCard(title = stringResource(R.string.webviewmonitor_detail_request_info)) {
                DetailRow(stringResource(R.string.webviewmonitor_label_method), request.method)
                DetailRow(stringResource(R.string.webviewmonitor_label_resource_type), request.resourceType.displayName)
                DetailRow(
                    stringResource(R.string.webviewmonitor_label_main_frame),
                    if (request.isForMainFrame) yesString else noString,
                )
                DetailRow(
                    stringResource(R.string.webviewmonitor_label_has_gesture),
                    if (request.hasGesture) yesString else noString,
                )
                DetailRow(
                    stringResource(R.string.webviewmonitor_label_is_redirect),
                    if (request.isRedirect) yesString else noString,
                )
                DetailRow(stringResource(R.string.webviewmonitor_label_webview_id), request.webViewId)
                DetailRow(
                    stringResource(R.string.webviewmonitor_label_timestamp),
                    formatTimestampFull(request.timestamp),
                )
            }

            // Request headers
            if (request.headers.isNotEmpty()) {
                DetailCard(
                    title = stringResource(R.string.webviewmonitor_detail_request_headers, request.headers.size),
                ) {
                    request.headers.forEach { (key, value) ->
                        DetailRow(key, value)
                    }
                }
            }

            // Response info
            if (request.statusCode != null || request.mimeType != null || request.contentLength != null) {
                DetailCard(title = stringResource(R.string.webviewmonitor_detail_response_info)) {
                    request.statusCode?.let {
                        DetailRow(
                            stringResource(R.string.webviewmonitor_label_status_code),
                            it.toString(),
                        )
                    }
                    request.mimeType?.let { DetailRow(stringResource(R.string.webviewmonitor_label_mime_type), it) }
                    request.encoding?.let { DetailRow(stringResource(R.string.webviewmonitor_label_encoding), it) }
                    request.contentLength?.let {
                        DetailRow(
                            stringResource(R.string.webviewmonitor_label_content_length),
                            formatBytes(it),
                        )
                    }
                    request.duration?.let {
                        DetailRow(
                            stringResource(R.string.webviewmonitor_detail_duration),
                            formatDuration(it),
                        )
                    }
                }
            }

            // Response headers
            if (request.responseHeaders.isNotEmpty()) {
                DetailCard(
                    title = stringResource(
                        R.string.webviewmonitor_detail_response_headers,
                        request.responseHeaders.size,
                    ),
                ) {
                    request.responseHeaders.forEach { (key, value) ->
                        DetailRow(key, value)
                    }
                }
            }

            // Error message
            request.errorMessage?.let { error ->
                DetailCard(title = stringResource(R.string.webviewmonitor_detail_error)) {
                    Text(
                        text = error,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Color(0xFFF44336),
                    )
                }
            }
        }
    }
}

@Composable
internal fun DetailCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(WormaCeptorDesignSystem.Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            content()
        }
    }
}

@Composable
internal fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
        WormaCeptorDivider(
            modifier = Modifier.padding(top = WormaCeptorDesignSystem.Spacing.sm),
        )
    }
}
