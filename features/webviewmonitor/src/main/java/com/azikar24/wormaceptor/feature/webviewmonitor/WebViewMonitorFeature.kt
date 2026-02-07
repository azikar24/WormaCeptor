package com.azikar24.wormaceptor.feature.webviewmonitor

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Entry point for the WebView Network Monitoring feature.
 * Provides factory methods and composable entry point.
 */
object WebViewMonitorFeature {

    /**
     * Creates a WebViewMonitorEngine instance.
     *
     * @param maxRequests Maximum number of requests to store
     * @return A new WebViewMonitorEngine instance
     */
    fun createEngine(maxRequests: Int = WebViewMonitorEngine.DEFAULT_MAX_REQUESTS): WebViewMonitorEngine {
        return WebViewMonitorEngine(maxRequests)
    }

    /**
     * Creates a WebViewMonitorViewModel factory for use with viewModel().
     *
     * @param engine The WebViewMonitorEngine instance to use
     * @return A ViewModelProvider.Factory for creating WebViewMonitorViewModel
     */
    fun createViewModelFactory(engine: WebViewMonitorEngine): WebViewMonitorViewModelFactory {
        return WebViewMonitorViewModelFactory(engine)
    }

    /**
     * Creates a ViewModelProvider.Factory with a new engine instance.
     * Use this when you don't need to share the engine with other components.
     *
     * @param maxRequests Maximum number of requests to store
     * @return A ViewModelProvider.Factory for creating WebViewMonitorViewModel
     */
    fun createViewModelFactory(
        maxRequests: Int = WebViewMonitorEngine.DEFAULT_MAX_REQUESTS,
    ): ViewModelProvider.Factory {
        return WebViewMonitorViewModelFactory(createEngine(maxRequests))
    }
}

/**
 * ViewModel for the WebView Monitor feature.
 */
class WebViewMonitorViewModel(private val engine: WebViewMonitorEngine) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = engine.isEnabled
    val requests: StateFlow<List<WebViewRequest>> = engine.requests
    val stats: StateFlow<WebViewRequestStats> = engine.stats
    val resourceTypeFilter: StateFlow<Set<WebViewResourceType>> = engine.resourceTypeFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRequest = MutableStateFlow<WebViewRequest?>(null)
    val selectedRequest: StateFlow<WebViewRequest?> = _selectedRequest.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    /**
     * Gets filtered requests based on search query and resource type filters.
     */
    fun getFilteredRequests(): List<WebViewRequest> {
        val query = _searchQuery.value.lowercase()
        val typeFilter = resourceTypeFilter.value

        return requests.value.filter { request ->
            val matchesSearch = query.isEmpty() ||
                request.url.lowercase().contains(query) ||
                request.method.lowercase().contains(query) ||
                request.host.lowercase().contains(query)

            val matchesType = typeFilter.isEmpty() || typeFilter.contains(request.resourceType)

            matchesSearch && matchesType
        }
    }

    fun toggleEnabled() {
        engine.toggle()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleResourceTypeFilter(type: WebViewResourceType) {
        engine.toggleResourceTypeFilter(type)
    }

    fun clearFilters() {
        _searchQuery.value = ""
        engine.clearFilters()
    }

    fun clearRequests() {
        engine.clearRequests()
    }

    fun selectRequest(request: WebViewRequest) {
        _selectedRequest.value = request
    }

    fun clearSelection() {
        _selectedRequest.value = null
    }

    fun toggleFilters() {
        _showFilters.update { !it }
    }

    /**
     * Creates a WebViewClient that monitors network requests.
     *
     * @param webViewId Unique identifier for the WebView being monitored
     * @param delegate Optional delegate WebViewClient to forward calls to
     * @return A WebViewClient that intercepts and logs requests
     */
    fun createMonitoringClient(
        webViewId: String,
        delegate: android.webkit.WebViewClient? = null,
    ): android.webkit.WebViewClient {
        return engine.createMonitoringClient(webViewId, delegate)
    }
}

/**
 * Factory for creating WebViewMonitorViewModel instances.
 */
class WebViewMonitorViewModelFactory(
    private val engine: WebViewMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebViewMonitorViewModel::class.java)) {
            return WebViewMonitorViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

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
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
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
                            contentDescription = null,
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
                .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
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
                                null,
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
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text(stringResource(R.string.webviewmonitor_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                Icons.Default.Clear,
                                stringResource(R.string.webviewmonitor_action_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.Spacing.lg),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
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
                            null,
                            modifier = Modifier.size(64.dp),
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
                    .background(getStatusColor(request).copy(alpha = 0.1f)),
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
                                null,
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
                                    null,
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
                                    null,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(10.dp),
                            )
                            Text(
                                text = formatDuration(duration),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }

                    // Content length
                    request.contentLength?.let { length ->
                        Text(
                            text = formatBytes(length),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebViewRequestDetailScreen(request: WebViewRequest, onNavigateBack: () -> Unit) {
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
                                .background(getStatusColor(request).copy(alpha = 0.1f)),
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
                    formatFullTimestamp(request.timestamp),
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
private fun DetailCard(title: String, content: @Composable () -> Unit) {
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
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
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

private fun getResourceTypeIcon(type: WebViewResourceType): ImageVector {
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

private fun getStatusColor(request: WebViewRequest): Color {
    return when {
        request.isPending -> Color(0xFFFF9800)
        request.isSuccess -> Color(0xFF4CAF50)
        else -> Color(0xFFF44336)
    }
}

private fun getMethodColor(method: String): Color {
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

private fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun formatFullTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun formatDuration(ms: Long): String {
    return when {
        ms < 1000 -> "${ms}ms"
        ms < 60000 -> String.format(Locale.US, "%.2fs", ms / 1000.0)
        else -> String.format(Locale.US, "%.1fm", ms / 60000.0)
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
