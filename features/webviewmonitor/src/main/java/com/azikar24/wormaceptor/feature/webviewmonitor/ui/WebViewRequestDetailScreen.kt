package com.azikar24.wormaceptor.feature.webviewmonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMethodBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebViewRequestDetailScreen(request: WebViewRequest, onNavigateBack: () -> Unit) {
    val statusColor = getStatusColor(request)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        WormaCeptorMethodBadge(request.method)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = request.path,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = request.host,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        DetailStatusBadge(request, statusColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.webviewmonitor_action_back),
                        )
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
            StatusCard(request)
            UrlCard(request)
            RequestInfoCard(request)
            if (request.headers.isNotEmpty()) {
                HeadersCard(
                    title = stringResource(R.string.webviewmonitor_detail_request_headers, request.headers.size),
                    headers = request.headers,
                    icon = Icons.Default.Code,
                    iconTint = WormaCeptorColors.StatusBlue,
                )
            }
            ResponseInfoCard(request)
            if (request.responseHeaders.isNotEmpty()) {
                HeadersCard(
                    title = stringResource(
                        R.string.webviewmonitor_detail_response_headers,
                        request.responseHeaders.size,
                    ),
                    headers = request.responseHeaders,
                    icon = Icons.Default.Code,
                    iconTint = WormaCeptorColors.StatusGreen,
                )
            }
            request.errorMessage?.let { ErrorCard(it) }
        }
    }
}

@Composable
private fun DetailStatusBadge(request: WebViewRequest, statusColor: Color) {
    val statusText = when {
        request.isPending -> "..."
        request.statusCode != null -> request.statusCode.toString()
        request.isFailed -> "ERR"
        else -> "?"
    }
    Text(
        text = statusText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = statusColor,
        modifier = Modifier
            .background(
                statusColor.asSubtleBackground(),
                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
    )
}

@Composable
private fun StatusCard(request: WebViewRequest) {
    val statusColor = getStatusColor(request)
    val statusIcon = when {
        request.isPending -> Icons.Default.HourglassEmpty
        request.isSuccess -> Icons.Default.CheckCircle
        else -> Icons.Default.Error
    }
    val statusTitle = when {
        request.isPending -> stringResource(R.string.webviewmonitor_status_pending)
        request.isSuccess -> stringResource(R.string.webviewmonitor_status_success)
        else -> stringResource(R.string.webviewmonitor_status_failed)
    }

    WormaCeptorInfoCard(
        title = statusTitle,
        icon = statusIcon,
        iconTint = statusColor,
    ) {
        val statusText = request.statusCode?.let {
            stringResource(R.string.webviewmonitor_detail_status_prefix, it)
        } ?: request.errorMessage ?: stringResource(R.string.webviewmonitor_status_waiting)

        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_status_code),
            value = statusText,
        )
        request.duration?.let { duration ->
            WormaCeptorDetailRow(
                label = stringResource(R.string.webviewmonitor_detail_duration),
                value = formatDuration(duration),
            )
        }
    }
}

@Composable
private fun UrlCard(request: WebViewRequest) {
    WormaCeptorInfoCard(
        title = stringResource(R.string.webviewmonitor_detail_url),
        icon = Icons.Default.Link,
        iconTint = MaterialTheme.colorScheme.primary,
    ) {
        SelectionContainer {
            Text(
                text = request.url,
                style = WormaCeptorDesignSystem.Typography.codeMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RequestInfoCard(request: WebViewRequest) {
    val yesString = stringResource(R.string.webviewmonitor_yes)
    val noString = stringResource(R.string.webviewmonitor_no)

    WormaCeptorInfoCard(
        title = stringResource(R.string.webviewmonitor_detail_request_info),
        icon = Icons.Default.Description,
        iconTint = WormaCeptorColors.StatusBlue,
    ) {
        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_method),
            value = request.method,
        )
        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_resource_type),
            value = request.resourceType.displayName,
        )
        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_main_frame),
            value = if (request.isForMainFrame) yesString else noString,
        )
        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_has_gesture),
            value = if (request.hasGesture) yesString else noString,
        )
        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_is_redirect),
            value = if (request.isRedirect) yesString else noString,
        )
        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_webview_id),
            value = request.webViewId,
        )
        WormaCeptorDetailRow(
            label = stringResource(R.string.webviewmonitor_label_timestamp),
            value = formatTimestampFull(request.timestamp),
        )
    }
}

@Composable
private fun HeadersCard(title: String, headers: Map<String, String>, icon: ImageVector, iconTint: Color) {
    WormaCeptorInfoCard(
        title = title,
        icon = icon,
        iconTint = iconTint,
    ) {
        headers.entries.forEachIndexed { index, (key, value) ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            }
            Column {
                Text(
                    text = key,
                    style = WormaCeptorDesignSystem.Typography.codeSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = iconTint,
                )
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                SelectionContainer {
                    Text(
                        text = value,
                        style = WormaCeptorDesignSystem.Typography.codeSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResponseInfoCard(request: WebViewRequest) {
    if (request.statusCode == null && request.mimeType == null && request.contentLength == null) return

    WormaCeptorInfoCard(
        title = stringResource(R.string.webviewmonitor_detail_response_info),
        icon = Icons.Default.CheckCircle,
        iconTint = WormaCeptorColors.StatusGreen,
    ) {
        request.statusCode?.let {
            WormaCeptorDetailRow(
                label = stringResource(R.string.webviewmonitor_label_status_code),
                value = it.toString(),
            )
        }
        request.mimeType?.let {
            WormaCeptorDetailRow(
                label = stringResource(R.string.webviewmonitor_label_mime_type),
                value = it,
            )
        }
        request.encoding?.let {
            WormaCeptorDetailRow(
                label = stringResource(R.string.webviewmonitor_label_encoding),
                value = it,
            )
        }
        request.contentLength?.let {
            WormaCeptorDetailRow(
                label = stringResource(R.string.webviewmonitor_label_content_length),
                value = formatBytes(it),
            )
        }
        request.duration?.let {
            WormaCeptorDetailRow(
                label = stringResource(R.string.webviewmonitor_detail_duration),
                value = formatDuration(it),
            )
        }
    }
}

@Composable
private fun ErrorCard(errorMessage: String) {
    WormaCeptorInfoCard(
        title = stringResource(R.string.webviewmonitor_detail_error),
        icon = Icons.Default.Error,
        iconTint = WormaCeptorColors.StatusRed,
    ) {
        Text(
            text = errorMessage,
            style = WormaCeptorDesignSystem.Typography.codeMedium,
            color = WormaCeptorColors.StatusRed,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WebViewRequestDetailScreenPreview() {
    WormaCeptorTheme {
        WebViewRequestDetailScreen(
            request = WebViewRequest(
                id = "req-1",
                url = "https://api.example.com/v2/users/profile",
                method = "GET",
                headers = mapOf(
                    "Authorization" to "Bearer eyJhbGci...",
                    "Accept" to "application/json",
                ),
                timestamp = System.currentTimeMillis() - 5_000L,
                webViewId = "main_webview",
                resourceType = WebViewResourceType.XHR,
                isForMainFrame = false,
                statusCode = 200,
                mimeType = "application/json",
                encoding = "UTF-8",
                contentLength = 2_048L,
                duration = 234L,
                responseHeaders = mapOf(
                    "Content-Type" to "application/json; charset=utf-8",
                    "Cache-Control" to "no-cache",
                ),
            ),
            onNavigateBack = {},
        )
    }
}
