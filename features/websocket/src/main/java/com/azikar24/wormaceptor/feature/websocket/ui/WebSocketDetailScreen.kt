/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.websocket.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.feature.websocket.ui.theme.webSocketColors
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying messages for a specific WebSocket connection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebSocketDetailScreen(
    connection: WebSocketConnection?,
    messages: ImmutableList<WebSocketMessage>,
    searchQuery: String,
    directionFilter: WebSocketMessageDirection?,
    totalMessageCount: Int,
    directionCounts: Map<WebSocketMessageDirection, Int>,
    expandedMessageId: Long?,
    onSearchQueryChanged: (String) -> Unit,
    onDirectionFilterToggle: (WebSocketMessageDirection) -> Unit,
    onMessageClick: (Long) -> Unit,
    onClearMessages: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = webSocketColors()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Messages",
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (connection != null) {
                                Text(
                                    text = connection.url,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        // Connection state indicator
                        if (connection != null) {
                            val stateColor = colors.forState(connection.state)
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(stateColor),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Clear messages
                        IconButton(
                            onClick = onClearMessages,
                            enabled = messages.isNotEmpty(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear messages",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )

                // Direction filter chips
                DirectionFilterChips(
                    selectedDirection = directionFilter,
                    directionCounts = directionCounts,
                    onDirectionToggle = onDirectionFilterToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )

                // Stats bar
                StatsBar(
                    totalCount = totalMessageCount,
                    filteredCount = messages.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        },
    ) { paddingValues ->
        if (messages.isEmpty()) {
            EmptyMessagesState(
                hasFilters = searchQuery.isNotBlank() || directionFilter != null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        } else {
            MessageList(
                messages = messages,
                expandedMessageId = expandedMessageId,
                onMessageClick = onMessageClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search messages...",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
    )
}

@Composable
private fun DirectionFilterChips(
    selectedDirection: WebSocketMessageDirection?,
    directionCounts: Map<WebSocketMessageDirection, Int>,
    onDirectionToggle: (WebSocketMessageDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = webSocketColors()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WebSocketMessageDirection.entries.forEach { direction ->
            val isSelected = selectedDirection == direction
            val count = directionCounts[direction] ?: 0
            val directionColor = colors.forDirection(direction)
            val icon = when (direction) {
                WebSocketMessageDirection.SENT -> Icons.AutoMirrored.Filled.CallMade
                WebSocketMessageDirection.RECEIVED -> Icons.AutoMirrored.Filled.CallReceived
            }

            FilterChip(
                selected = isSelected,
                onClick = { onDirectionToggle(direction) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = direction.name,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        if (count > 0) {
                            Text(
                                text = if (count > 999) "999+" else count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                },
                            )
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = directionColor.copy(alpha = 0.15f),
                    selectedLabelColor = directionColor,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = directionColor.copy(alpha = 0.3f),
                    selectedBorderColor = directionColor.copy(alpha = 0.5f),
                    enabled = true,
                    selected = isSelected,
                ),
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
            text = "Messages",
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
private fun MessageList(
    messages: ImmutableList<WebSocketMessage>,
    expandedMessageId: Long?,
    onMessageClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(
            items = messages,
            key = { it.id },
        ) { message ->
            MessageItem(
                message = message,
                isExpanded = message.id == expandedMessageId,
                onClick = { onMessageClick(message.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MessageItem(
    message: WebSocketMessage,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = webSocketColors()
    val directionColor = colors.forDirection(message.direction)
    val backgroundColor = colors.backgroundForDirection(message.direction)
    val typeColor = colors.forMessageType(message.type)

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val formattedTime = remember(message.timestamp) {
        timeFormat.format(Date(message.timestamp))
    }

    val directionIcon = when (message.direction) {
        WebSocketMessageDirection.SENT -> Icons.AutoMirrored.Filled.CallMade
        WebSocketMessageDirection.RECEIVED -> Icons.AutoMirrored.Filled.CallReceived
    }

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .animateContentSize(),
        color = backgroundColor.copy(alpha = 0.3f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Direction icon
                Icon(
                    imageVector = directionIcon,
                    contentDescription = message.direction.name,
                    tint = directionColor,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Type badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = typeColor.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = message.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Size
                Text(
                    text = formatSize(message.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Timestamp
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Expand indicator
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Payload preview or full content
            Text(
                text = if (isExpanded) message.payload else message.payloadPreview(150),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                lineHeight = 18.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptyMessagesState(hasFilters: Boolean, modifier: Modifier = Modifier) {
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
                Row {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (hasFilters) "No matching messages" else "No messages yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasFilters) {
                "Try adjusting your filters"
            } else {
                "Messages will appear as they are sent or received"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

/**
 * Formats a size in bytes to a human-readable string.
 */
private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
