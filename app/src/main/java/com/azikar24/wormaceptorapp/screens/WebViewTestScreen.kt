/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorFeature
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorViewModel
import org.koin.compose.koinInject

/**
 * Test screen for the WebView Monitor feature.
 * Displays a WebView with an inline request list showing captured requests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewTestScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    // Use Koin singleton engine so requests appear in WebView Monitor tool
    val engine: WebViewMonitorEngine = koinInject()
    val factory = remember(engine) { WebViewMonitorFeature.createViewModelFactory(engine) }
    val viewModel: WebViewMonitorViewModel = viewModel(factory = factory)

    val isEnabled by viewModel.isEnabled.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val stats by viewModel.stats.collectAsState()

    // Enable/disable engine with lifecycle
    DisposableEffect(Unit) {
        if (!isEnabled) {
            viewModel.toggleEnabled()
        }
        onDispose {
            // Engine cleanup is handled internally
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text(
                                text = "WebView Test",
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (requests.isNotEmpty()) {
                                Text(
                                    text = "${requests.size} request${if (requests.size != 1) "s" else ""} captured",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearRequests() },
                        enabled = requests.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.Clear, "Clear requests")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        WebViewTestContent(
            viewModel = viewModel,
            requests = requests,
            stats = stats,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewTestContent(
    viewModel: WebViewMonitorViewModel,
    requests: List<WebViewRequest>,
    stats: WebViewRequestStats,
    modifier: Modifier = Modifier,
) {
    var currentUrl by remember { mutableStateOf("https://httpbin.org/html") }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = modifier.padding(WormaCeptorDesignSystem.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        // URL buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            Button(
                onClick = {
                    currentUrl = "https://httpbin.org/html"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("HTML")
            }
            Button(
                onClick = {
                    currentUrl = "https://httpbin.org/json"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("JSON")
            }
            Button(
                onClick = {
                    currentUrl = "https://httpbin.org/image/png"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Image")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            Button(
                onClick = {
                    currentUrl = "https://example.com"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Example")
            }
            IconButton(
                onClick = { webView?.reload() },
            ) {
                Icon(Icons.Default.Refresh, "Reload")
            }
        }

        // WebView
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = viewModel.createMonitoringClient(
                        webViewId = "test_webview",
                        delegate = WebViewClient(),
                    )
                    loadUrl(currentUrl)
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md)),
        )

        // Stats card
        if (requests.isNotEmpty()) {
            RequestStatsCard(stats = stats)
        }

        // Request list
        Text(
            text = "Captured Requests",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No requests captured yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                items(requests, key = { it.id }) { request ->
                    CapturedRequestItem(
                        request = request,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestStatsCard(stats: WebViewRequestStats, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                label = "Total",
                value = stats.totalRequests.toString(),
                color = MaterialTheme.colorScheme.primary,
            )
            StatItem(
                label = "Success",
                value = stats.successfulRequests.toString(),
                color = Color(0xFF4CAF50),
            )
            StatItem(
                label = "Failed",
                value = stats.failedRequests.toString(),
                color = Color(0xFFF44336),
            )
            StatItem(
                label = "Pending",
                value = stats.pendingRequests.toString(),
                color = Color(0xFFFF9800),
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CapturedRequestItem(request: WebViewRequest, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Status indicator
                StatusIndicator(request = request)

                // Method badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = getMethodColor(request.method).copy(alpha = 0.15f),
                ) {
                    Text(
                        text = request.method,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = getMethodColor(request.method),
                    )
                }

                // URL
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.path,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = request.host,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Duration/Status
                Column(horizontalAlignment = Alignment.End) {
                    request.statusCode?.let { code ->
                        Text(
                            text = code.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = getStatusColor(code),
                        )
                    }
                    request.duration?.let { duration ->
                        Text(
                            text = "${duration}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Expanded details
            if (isExpanded) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                        )
                        .padding(WormaCeptorDesignSystem.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    DetailRow("URL", request.url)
                    DetailRow("Type", request.resourceType.displayName)
                    request.mimeType?.let { DetailRow("MIME", it) }
                    request.contentLength?.let { DetailRow("Size", formatBytes(it)) }
                    request.errorMessage?.let {
                        DetailRow("Error", it, isError = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(request: WebViewRequest, modifier: Modifier = Modifier) {
    val (icon, color) = when {
        request.isFailed -> Icons.Default.Error to Color(0xFFF44336)
        request.isSuccess -> Icons.Default.Check to Color(0xFF4CAF50)
        request.isPending -> Icons.Default.MoreTime to Color(0xFFFF9800)
        else -> Icons.Default.MoreTime to Color(0xFFFF9800)
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .background(color.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, isError: Boolean = false, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = if (isError) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun getMethodColor(method: String): Color {
    return when (method.uppercase()) {
        "GET" -> Color(0xFF4CAF50)
        "POST" -> Color(0xFF2196F3)
        "PUT" -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFF44336)
        "PATCH" -> Color(0xFF9C27B0)
        else -> Color(0xFF757575)
    }
}

private fun getStatusColor(code: Int): Color {
    return when {
        code in 200..299 -> Color(0xFF4CAF50)
        code in 300..399 -> Color(0xFF2196F3)
        code in 400..499 -> Color(0xFFFF9800)
        code >= 500 -> Color(0xFFF44336)
        else -> Color(0xFF757575)
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
