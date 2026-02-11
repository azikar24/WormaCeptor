package com.azikar24.wormaceptor.feature.webviewmonitor.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.R
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorFeature
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorViewModel

/**
 * Main composable for the WebView Monitor feature with internal engine.
 */
@Composable
fun WebViewMonitor(modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine = remember { WebViewMonitorFeature.createEngine() }
    WebViewMonitor(
        engine = engine,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
    )
}

/**
 * Main composable for the WebView Monitor feature with external engine.
 */
@Composable
fun WebViewMonitor(engine: WebViewMonitorEngine, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val factory = remember(engine) { WebViewMonitorFeature.createViewModelFactory(engine) }
    val viewModel: WebViewMonitorViewModel = viewModel(factory = factory)
    val navController = rememberNavController()

    val isEnabled by viewModel.isEnabled.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val resourceTypeFilter by viewModel.resourceTypeFilter.collectAsState()
    val selectedRequest by viewModel.selectedRequest.collectAsState()
    val showFilters by viewModel.showFilters.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "list",
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
            ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
            ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
            ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
            ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page))
        },
    ) {
        composable("list") {
            WebViewMonitorListScreen(
                isEnabled = isEnabled,
                requests = viewModel.getFilteredRequests(),
                totalCount = requests.size,
                stats = stats,
                searchQuery = searchQuery,
                resourceTypeFilter = resourceTypeFilter,
                showFilters = showFilters,
                onToggleEnabled = viewModel::toggleEnabled,
                onSearchQueryChanged = viewModel::setSearchQuery,
                onToggleResourceTypeFilter = viewModel::toggleResourceTypeFilter,
                onClearFilters = viewModel::clearFilters,
                onClearRequests = viewModel::clearRequests,
                onToggleFilters = viewModel::toggleFilters,
                onRequestClick = { request ->
                    viewModel.selectRequest(request)
                    navController.navigate("detail")
                },
                onNavigateBack = onNavigateBack,
            )
        }

        composable("detail") {
            selectedRequest?.let { request ->
                WebViewRequestDetailScreen(
                    request = request,
                    onNavigateBack = {
                        viewModel.clearSelection()
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WebViewMonitorListScreen(
    isEnabled: Boolean,
    requests: List<WebViewRequest>,
    totalCount: Int,
    stats: WebViewRequestStats,
    searchQuery: String,
    resourceTypeFilter: Set<WebViewResourceType>,
    showFilters: Boolean,
    onToggleEnabled: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleResourceTypeFilter: (WebViewResourceType) -> Unit,
    onClearFilters: () -> Unit,
    onClearRequests: () -> Unit,
    onToggleFilters: () -> Unit,
    onRequestClick: (WebViewRequest) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(R.string.webviewmonitor_title),
                            tint = Color(0xFF2196F3),
                        )
                        Text(stringResource(R.string.webviewmonitor_title), fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(R.string.webviewmonitor_action_back),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFilters) {
                        Icon(
                            Icons.Default.FilterList,
                            stringResource(R.string.webviewmonitor_action_filters),
                            tint = if (showFilters || resourceTypeFilter.isNotEmpty()) {
                                Color(0xFF2196F3)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    IconButton(onClick = onClearRequests) {
                        Icon(
                            Icons.Default.Delete,
                            stringResource(R.string.webviewmonitor_action_clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                .padding(top = WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Enable toggle card
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Language,
                                stringResource(R.string.webviewmonitor_monitoring_title),
                                tint = if (isEnabled) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column {
                            Text(
                                stringResource(R.string.webviewmonitor_monitoring_title),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (isEnabled) {
                                    stringResource(
                                        R.string.webviewmonitor_status_capturing,
                                    )
                                } else {
                                    stringResource(R.string.webviewmonitor_status_disabled)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Switch(checked = isEnabled, onCheckedChange = { onToggleEnabled() })
                }
            }

            // Stats card
            if (stats.totalRequests > 0) {
                Card(
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.Spacing.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorDesignSystem.Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatItem(
                            value = stats.totalRequests.toString(),
                            label = stringResource(R.string.webviewmonitor_stats_total),
                            color = Color(0xFF2196F3),
                        )
                        StatItem(
                            value = stats.successfulRequests.toString(),
                            label = stringResource(R.string.webviewmonitor_stats_success),
                            color = Color(0xFF4CAF50),
                        )
                        StatItem(
                            value = stats.failedRequests.toString(),
                            label = stringResource(R.string.webviewmonitor_stats_failed),
                            color = Color(0xFFF44336),
                        )
                        StatItem(
                            value = stats.pendingRequests.toString(),
                            label = stringResource(R.string.webviewmonitor_stats_pending),
                            color = Color(0xFFFF9800),
                        )
                    }
                }
            }

            // Search bar
            WormaCeptorSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChanged,
                placeholder = stringResource(R.string.webviewmonitor_search_placeholder),
            )

            // Filter chips
            AnimatedVisibility(visible = showFilters) {
                Card(
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.Spacing.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorDesignSystem.Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.webviewmonitor_filter_by_type),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                            if (resourceTypeFilter.isNotEmpty()) {
                                Text(
                                    stringResource(R.string.webviewmonitor_action_clear_filters),
                                    color = Color(0xFF2196F3),
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { onClearFilters() },
                                )
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        ) {
                            WebViewResourceType.entries.filter { it != WebViewResourceType.UNKNOWN }.forEach { type ->
                                FilterChip(
                                    selected = resourceTypeFilter.contains(type),
                                    onClick = { onToggleResourceTypeFilter(type) },
                                    label = { Text(type.displayName, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            getResourceTypeIcon(type),
                                            null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Request count
            Text(
                text = if (requests.size == totalCount) {
                    stringResource(R.string.webviewmonitor_request_count, totalCount)
                } else {
                    stringResource(R.string.webviewmonitor_request_count_filtered, requests.size, totalCount)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Request list
            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Icon(
                            Icons.Default.Language,
                            stringResource(R.string.webviewmonitor_title),
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.emptyState),
                            tint = MaterialTheme.colorScheme.outlineVariant,
                        )
                        Text(
                            if (isEnabled) {
                                stringResource(
                                    R.string.webviewmonitor_empty_state_enabled,
                                )
                            } else {
                                stringResource(R.string.webviewmonitor_empty_state_disabled)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    items(requests, key = { it.id }) { request ->
                        WebViewRequestItem(
                            request = request,
                            onClick = { onRequestClick(request) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WebViewRequestItem(request: WebViewRequest, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(request).copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getResourceTypeIcon(request.resourceType),
                    contentDescription = null,
                    tint = getStatusColor(request),
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Method badge
                    Text(
                        text = request.method,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                getMethodColor(request.method),
                                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                            )
                            .padding(
                                horizontal = WormaCeptorDesignSystem.CornerRadius.sm,
                                vertical = WormaCeptorDesignSystem.Spacing.xxs,
                            ),
                    )

                    // Resource type
                    Text(
                        text = request.resourceType.displayName,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Status
                    when {
                        request.isPending -> {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                stringResource(R.string.webviewmonitor_status_pending),
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        request.isSuccess -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    stringResource(R.string.webviewmonitor_status_success),
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "${request.statusCode}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        request.isFailed -> {
                            val errorText = request.statusCode?.toString()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    stringResource(R.string.webviewmonitor_status_failed),
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = errorText ?: stringResource(R.string.webviewmonitor_status_error),
                                    fontSize = 10.sp,
                                    color = Color(0xFFF44336),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                // URL
                Text(
                    text = request.path,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Host
                Text(
                    text = request.host,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Timestamp
                    Text(
                        text = formatTimestamp(request.timestamp),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.heavy,
                        ),
                    )

                    // Duration
                    request.duration?.let { duration ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = WormaCeptorDesignSystem.Alpha.heavy,
                                ),
                                modifier = Modifier.size(10.dp),
                            )
                            Text(
                                text = formatDuration(duration),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = WormaCeptorDesignSystem.Alpha.heavy,
                                ),
                            )
                        }
                    }

                    // Content length
                    request.contentLength?.let { length ->
                        Text(
                            text = formatBytes(length),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = WormaCeptorDesignSystem.Alpha.heavy,
                            ),
                        )
                    }
                }
            }
        }
    }
}

internal fun getResourceTypeIcon(type: WebViewResourceType): ImageVector {
    return when (type) {
        WebViewResourceType.DOCUMENT -> Icons.Default.Description
        WebViewResourceType.SCRIPT -> Icons.Default.Code
        WebViewResourceType.STYLESHEET -> Icons.Default.Style
        WebViewResourceType.IMAGE -> Icons.Default.Image
        WebViewResourceType.FONT -> Icons.Default.FontDownload
        WebViewResourceType.XHR -> Icons.Default.Sync
        WebViewResourceType.MEDIA -> Icons.Default.Movie
        WebViewResourceType.WEBSOCKET -> Icons.Default.Sync
        WebViewResourceType.MANIFEST -> Icons.Default.Description
        WebViewResourceType.OBJECT -> Icons.Default.Web
        WebViewResourceType.IFRAME -> Icons.Default.Web
        WebViewResourceType.OTHER -> Icons.Default.MoreHoriz
        WebViewResourceType.UNKNOWN -> Icons.Default.MoreHoriz
    }
}

internal fun getStatusColor(request: WebViewRequest): Color {
    return when {
        request.isPending -> Color(0xFFFF9800)
        request.isSuccess -> Color(0xFF4CAF50)
        else -> Color(0xFFF44336)
    }
}

internal fun getMethodColor(method: String): Color {
    return when (method.uppercase()) {
        "GET" -> Color(0xFF4CAF50)
        "POST" -> Color(0xFF2196F3)
        "PUT" -> Color(0xFFFF9800)
        "PATCH" -> Color(0xFF9C27B0)
        "DELETE" -> Color(0xFFF44336)
        "HEAD" -> Color(0xFF607D8B)
        "OPTIONS" -> Color(0xFF795548)
        else -> Color(0xFF757575)
    }
}
