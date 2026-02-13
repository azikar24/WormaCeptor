package com.azikar24.wormaceptor.feature.websocket.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorStatusDot
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.feature.websocket.R
import com.azikar24.wormaceptor.feature.websocket.ui.theme.webSocketColors
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying a list of WebSocket connections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebSocketListScreen(
    connections: ImmutableList<WebSocketConnection>,
    searchQuery: String,
    totalCount: Int,
    onSearchQueryChanged: (String) -> Unit,
    onConnectionClick: (WebSocketConnection) -> Unit,
    onClearAll: () -> Unit,
    getMessageCount: (Long) -> Int,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.websocket_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        onBack?.let { back ->
                            IconButton(onClick = back) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.websocket_back),
                                )
                            }
                        }
                    },
                    actions = {
                        // Clear all connections
                        IconButton(
                            onClick = onClearAll,
                            enabled = connections.isNotEmpty(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.websocket_clear_all),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                // Search bar
                WormaCeptorSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.lg,
                            vertical = WormaCeptorDesignSystem.Spacing.sm,
                        ),
                    placeholder = stringResource(R.string.websocket_search_url_placeholder),
                )

                // Stats bar
                StatsBar(
                    totalCount = totalCount,
                    filteredCount = connections.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.lg,
                            vertical = WormaCeptorDesignSystem.Spacing.xs,
                        ),
                )
            }
        },
    ) { paddingValues ->
        if (connections.isEmpty()) {
            WormaCeptorEmptyState(
                title = stringResource(
                    if (searchQuery.isNotBlank()) {
                        R.string.websocket_empty_no_matching_connections
                    } else {
                        R.string.websocket_empty_no_connections
                    },
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                subtitle = stringResource(
                    if (searchQuery.isNotBlank()) {
                        R.string.websocket_empty_search_hint
                    } else {
                        R.string.websocket_empty_connections_hint
                    },
                ),
                icon = Icons.Default.Sync,
            )
        } else {
            ConnectionList(
                connections = connections,
                onConnectionClick = onConnectionClick,
                getMessageCount = getMessageCount,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun StatsBar(totalCount: Int, filteredCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.websocket_connections_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = if (filteredCount != totalCount) {
                stringResource(R.string.websocket_stats_filtered, filteredCount, totalCount)
            } else {
                stringResource(R.string.websocket_stats_total, totalCount)
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConnectionList(
    connections: ImmutableList<WebSocketConnection>,
    onConnectionClick: (WebSocketConnection) -> Unit,
    getMessageCount: (Long) -> Int,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.sm),
    ) {
        items(
            items = connections,
            key = { it.id },
        ) { connection ->
            ConnectionItem(
                connection = connection,
                messageCount = getMessageCount(connection.id),
                onClick = { onConnectionClick(connection) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConnectionItem(
    connection: WebSocketConnection,
    messageCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = webSocketColors()
    val stateColor = colors.forState(connection.state)
    val backgroundColor = colors.backgroundForState(connection.state)

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val formattedTime = remember(connection.openedAt) {
        connection.openedAt?.let { timeFormat.format(Date(it)) } ?: "--:--:--"
    }

    val duration = remember(connection.duration) {
        connection.duration?.let { formatDuration(it) } ?: "--"
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = backgroundColor.copy(
            alpha = WormaCeptorDesignSystem.Alpha.medium + WormaCeptorDesignSystem.Alpha.subtle,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.lg,
                    vertical = WormaCeptorDesignSystem.Spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // State indicator dot
            WormaCeptorStatusDot(
                color = stateColor,
                size = WormaCeptorDesignSystem.Spacing.md,
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                // URL
                Text(
                    text = connection.url,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // State badge
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                        color = stateColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
                    ) {
                        Text(
                            text = connection.state.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = stateColor,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.sm - WormaCeptorDesignSystem.Spacing.xxs,
                                vertical = WormaCeptorDesignSystem.Spacing.xxs,
                            ),
                        )
                    }

                    // Message count
                    Text(
                        text = stringResource(R.string.websocket_message_count, messageCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Duration
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

            // Time
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.intense + WormaCeptorDesignSystem.Alpha.subtle,
                ),
            )
        }
    }
}
