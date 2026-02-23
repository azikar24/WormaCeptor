package com.azikar24.wormaceptor.feature.webviewmonitor.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorExpandableCard
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMethodBadge
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.R
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorFeature
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorViewModel
import org.koin.compose.koinInject

/** Main composable for the WebView Monitor feature. */
@Composable
fun WebViewMonitor(modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine: WebViewMonitorEngine = koinInject()
    WebViewMonitor(
        engine = engine,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
    )
}

/** Main composable for the WebView Monitor feature with external engine. */
@Suppress("LongMethod")
@Composable
fun WebViewMonitor(engine: WebViewMonitorEngine, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val factory = remember(engine) { WebViewMonitorFeature.createViewModelFactory(engine) }
    val viewModel: WebViewMonitorViewModel = viewModel(factory = factory)
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        if (!viewModel.isEnabled.value) {
            viewModel.toggleEnabled()
        }
    }

    val requests by viewModel.requests.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val resourceTypeFilter by viewModel.resourceTypeFilter.collectAsState()
    val selectedRequest by viewModel.selectedRequest.collectAsState()

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
                requests = viewModel.getFilteredRequests(),
                totalCount = requests.size,
                stats = stats,
                searchQuery = searchQuery,
                resourceTypeFilter = resourceTypeFilter,
                onSearchQueryChanged = viewModel::setSearchQuery,
                onToggleResourceTypeFilter = viewModel::toggleResourceTypeFilter,
                onClearFilters = viewModel::clearFilters,
                onClearRequests = viewModel::clearRequests,
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

@Suppress("LongParameterList")
@Composable
private fun WebViewMonitorListScreen(
    requests: List<WebViewRequest>,
    totalCount: Int,
    stats: WebViewRequestStats,
    searchQuery: String,
    resourceTypeFilter: Set<WebViewResourceType>,
    onSearchQueryChanged: (String) -> Unit,
    onToggleResourceTypeFilter: (WebViewResourceType) -> Unit,
    onClearFilters: () -> Unit,
    onClearRequests: () -> Unit,
    onRequestClick: (WebViewRequest) -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ListTopBar(
                searchActive = searchActive,
                searchQuery = searchQuery,
                onSearchToggle = {
                    searchActive = !searchActive
                    if (!searchActive) onSearchQueryChanged("")
                },
                onSearchQueryChanged = onSearchQueryChanged,
                onClearRequests = onClearRequests,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        ListContent(
            requests = requests,
            totalCount = totalCount,
            stats = stats,
            resourceTypeFilter = resourceTypeFilter,
            onToggleResourceTypeFilter = onToggleResourceTypeFilter,
            onClearFilters = onClearFilters,
            onRequestClick = onRequestClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
private fun ListTopBar(
    searchActive: Boolean,
    searchQuery: String,
    onSearchToggle: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearRequests: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.webviewmonitor_title),
                    fontWeight = FontWeight.SemiBold,
                )
            },
            navigationIcon = {
                onNavigateBack?.let {
                    IconButton(onClick = it) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.webviewmonitor_action_back),
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(R.string.webviewmonitor_action_search),
                    )
                }
                IconButton(onClick = onClearRequests) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.webviewmonitor_action_clear),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        ExpandableSearchBar(
            visible = searchActive,
            query = searchQuery,
            onQueryChange = onSearchQueryChanged,
        )
    }
}

@Composable
private fun ExpandableSearchBar(visible: Boolean, query: String, onQueryChange: (String) -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
        exit = shrinkVertically(
            animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
    ) {
        WormaCeptorSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.lg,
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                ),
            placeholder = stringResource(R.string.webviewmonitor_search_placeholder),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun ListContent(
    requests: List<WebViewRequest>,
    totalCount: Int,
    stats: WebViewRequestStats,
    resourceTypeFilter: Set<WebViewResourceType>,
    onToggleResourceTypeFilter: (WebViewResourceType) -> Unit,
    onClearFilters: () -> Unit,
    onRequestClick: (WebViewRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (requests.isEmpty() && stats.totalRequests == 0) {
        WormaCeptorEmptyState(
            title = stringResource(R.string.webviewmonitor_empty_state_enabled),
            subtitle = stringResource(R.string.webviewmonitor_empty_subtitle),
            icon = Icons.Default.Language,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = WormaCeptorDesignSystem.Spacing.lg),
        ) {
            if (stats.totalRequests > 0) {
                item {
                    StatsRow(
                        stats = stats,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.lg,
                            vertical = WormaCeptorDesignSystem.Spacing.sm,
                        ),
                    )
                }
            }
            item {
                FilterSection(
                    resourceTypeFilter = resourceTypeFilter,
                    onToggleResourceTypeFilter = onToggleResourceTypeFilter,
                    onClearFilters = onClearFilters,
                    modifier = Modifier.padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
                )
            }
            item {
                CountText(
                    filteredCount = requests.size,
                    totalCount = totalCount,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.lg,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
                )
            }
            items(requests, key = { it.id }) { request ->
                WebViewRequestItem(
                    request = request,
                    onClick = { onRequestClick(request) },
                )
            }
        }
    }
}

@Composable
private fun StatsRow(stats: WebViewRequestStats, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        WormaCeptorSummaryCard(
            count = stats.totalRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_total),
            color = WormaCeptorColors.StatusBlue,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.successfulRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_success),
            color = WormaCeptorColors.StatusGreen,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.failedRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_failed),
            color = WormaCeptorColors.StatusRed,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.pendingRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_pending),
            color = WormaCeptorColors.StatusAmber,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    resourceTypeFilter: Set<WebViewResourceType>,
    onToggleResourceTypeFilter: (WebViewResourceType) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    WormaCeptorExpandableCard(
        isExpanded = expanded,
        onToggle = { expanded = !expanded },
        modifier = modifier,
        header = {
            Text(
                text = stringResource(R.string.webviewmonitor_filter_by_type),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            if (resourceTypeFilter.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.webviewmonitor_action_clear_filters),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onClearFilters() }
                        .padding(horizontal = WormaCeptorDesignSystem.Spacing.sm),
                )
            }
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            WebViewResourceType.entries
                .filter { it != WebViewResourceType.UNKNOWN }
                .forEach { type ->
                    FilterChip(
                        selected = resourceTypeFilter.contains(type),
                        onClick = { onToggleResourceTypeFilter(type) },
                        label = {
                            Text(type.displayName, style = MaterialTheme.typography.labelSmall)
                        },
                        leadingIcon = {
                            Icon(
                                getResourceTypeIcon(type),
                                contentDescription = null,
                                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                            )
                        },
                    )
                }
        }
    }
}

@Composable
private fun CountText(filteredCount: Int, totalCount: Int, modifier: Modifier = Modifier) {
    Text(
        text = if (filteredCount == totalCount) {
            stringResource(R.string.webviewmonitor_request_count, totalCount)
        } else {
            stringResource(R.string.webviewmonitor_request_count_filtered, filteredCount, totalCount)
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun WebViewRequestItem(request: WebViewRequest, onClick: () -> Unit) {
    val statusColor = getStatusColor(request)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            )
            .clip(WormaCeptorDesignSystem.Shapes.card)
            .border(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.medium,
                ),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .background(
                color = statusColor.asSubtleBackground(),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .clickable(onClick = onClick)
            .padding(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RequestItemContent(request, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))
        RequestStatusBadge(request, statusColor)
    }
}

@Composable
private fun RequestItemContent(request: WebViewRequest, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            WormaCeptorMethodBadge(request.method)
            Text(
                text = request.path,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            RequestHostChip(request.host)
            Text(
                text = request.resourceType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
        RequestMetaRow(request)
    }
}

@Composable
private fun RequestHostChip(host: String) {
    Text(
        text = host,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.prominent,
                ),
                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
    )
}

@Composable
private fun RequestStatusBadge(request: WebViewRequest, statusColor: Color) {
    Column(horizontalAlignment = Alignment.End) {
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
        request.duration?.let { duration ->
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
            Text(
                text = formatDuration(duration),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.heavy,
                ),
            )
        }
    }
}

@Composable
private fun RequestMetaRow(request: WebViewRequest) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatTimestamp(request.timestamp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.heavy,
            ),
        )
        request.contentLength?.let { length ->
            Text(
                text = formatBytes(length),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.heavy,
                ),
            )
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
        request.isPending -> WormaCeptorColors.StatusAmber
        request.isSuccess -> WormaCeptorColors.StatusGreen
        else -> WormaCeptorColors.StatusRed
    }
}

@Preview(showBackground = true)
@Composable
private fun WebViewMonitorListScreenPreview() {
    WormaCeptorTheme {
        WebViewMonitorListScreen(
            requests = listOf(
                WebViewRequest(
                    id = "1",
                    url = "https://api.example.com/v2/users",
                    method = "GET",
                    headers = mapOf("Accept" to "application/json"),
                    timestamp = System.currentTimeMillis() - 3_000L,
                    webViewId = "main",
                    resourceType = WebViewResourceType.XHR,
                    statusCode = 200,
                    mimeType = "application/json",
                    contentLength = 1_024L,
                    duration = 150L,
                ),
                WebViewRequest(
                    id = "2",
                    url = "https://cdn.example.com/style.css",
                    method = "GET",
                    headers = emptyMap(),
                    timestamp = System.currentTimeMillis() - 5_000L,
                    webViewId = "main",
                    resourceType = WebViewResourceType.STYLESHEET,
                    statusCode = 200,
                    contentLength = 4_096L,
                    duration = 80L,
                ),
                WebViewRequest(
                    id = "3",
                    url = "https://api.example.com/v2/analytics",
                    method = "POST",
                    headers = emptyMap(),
                    timestamp = System.currentTimeMillis() - 1_000L,
                    webViewId = "main",
                    resourceType = WebViewResourceType.XHR,
                ),
            ),
            totalCount = 3,
            stats = WebViewRequestStats(
                totalRequests = 3,
                successfulRequests = 2,
                failedRequests = 0,
                pendingRequests = 1,
                byResourceType = mapOf(
                    WebViewResourceType.XHR to 2,
                    WebViewResourceType.STYLESHEET to 1,
                ),
                averageDuration = 115L,
                totalDataTransferred = 5_120L,
            ),
            searchQuery = "",
            resourceTypeFilter = emptySet(),
            onSearchQueryChanged = {},
            onToggleResourceTypeFilter = {},
            onClearFilters = {},
            onClearRequests = {},
            onRequestClick = {},
            onNavigateBack = {},
        )
    }
}
