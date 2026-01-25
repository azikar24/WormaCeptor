/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.websocket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.feature.websocket.ui.theme.webSocketColors
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
                            text = "WebSocket Monitor",
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        onBack?.let { back ->
                            IconButton(onClick = back) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
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
                                contentDescription = "Clear all",
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = "Search by URL...",
                )

                // Stats bar
                StatsBar(
                    totalCount = totalCount,
                    filteredCount = connections.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        },
    ) { paddingValues ->
        if (connections.isEmpty()) {
            EmptyConnectionsState(
                hasSearchQuery = searchQuery.isNotBlank(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
            text = "Connections",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = if (filteredCount != totalCount) {
                "$filteredCount / $totalCount"
            } else {
                "$totalCount total"
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
        contentPadding = PaddingValues(vertical = 8.dp),
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
        color = backgroundColor.copy(alpha = 0.3f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // State indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(stateColor),
            )

            Spacer(modifier = Modifier.width(12.dp))

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

                Spacer(modifier = Modifier.height(4.dp))

                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // State badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = stateColor.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = connection.state.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = stateColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    // Message count
                    Text(
                        text = "$messageCount msgs",
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

            Spacer(modifier = Modifier.width(8.dp))

            // Time
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun EmptyConnectionsState(hasSearchQuery: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (hasSearchQuery) "No matching connections" else "No WebSocket connections",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasSearchQuery) {
                "Try a different search query"
            } else {
                "WebSocket connections will appear here"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

/**
 * Formats a duration in milliseconds to a human-readable string.
 */
private fun formatDuration(durationMs: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
