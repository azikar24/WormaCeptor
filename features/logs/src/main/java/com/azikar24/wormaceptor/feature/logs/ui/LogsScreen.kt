/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.logs.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import com.azikar24.wormaceptor.feature.logs.R
import com.azikar24.wormaceptor.feature.logs.ui.theme.logLevelColors
import com.azikar24.wormaceptor.feature.logs.vm.LogsViewModel
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen for viewing console logs.
 *
 * Features:
 * - Search bar for filtering by tag or message
 * - Log level filter chips
 * - Auto-scroll toggle
 * - Play/pause capture controls
 * - Clear logs button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(viewModel: LogsViewModel, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    val logs by viewModel.logs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLevels by viewModel.selectedLevels.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val levelCounts by viewModel.levelCounts.collectAsState()

    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logs.size, autoScroll) {
        if (autoScroll && logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    // Detect if user has scrolled away from bottom
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 2
        }
    }

    // Disable auto-scroll when user scrolls up
    LaunchedEffect(isAtBottom) {
        if (!isAtBottom && autoScroll) {
            viewModel.setAutoScroll(false)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Console Logs",
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        onBack?.let { back ->
                            IconButton(onClick = back) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.logs_back),
                                )
                            }
                        }
                    },
                    actions = {
                        // Capture toggle
                        IconButton(onClick = {
                            if (isCapturing) viewModel.stopCapture() else viewModel.startCapture()
                        }) {
                            Icon(
                                imageVector = if (isCapturing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isCapturing) "Pause capture" else "Start capture",
                                tint = if (isCapturing) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }

                        // Clear logs
                        IconButton(onClick = { viewModel.clearLogs() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.logs_clear),
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
                    onQueryChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.lg,
                            vertical = WormaCeptorDesignSystem.Spacing.sm,
                        ),
                    placeholder = stringResource(R.string.logs_search_placeholder),
                )

                // Level filter chips
                LevelFilterChips(
                    selectedLevels = selectedLevels,
                    levelCounts = levelCounts,
                    onLevelToggle = viewModel::toggleLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
                )

                // Stats bar
                StatsBar(
                    totalCount = totalCount,
                    filteredCount = logs.size,
                    isCapturing = isCapturing,
                    pid = viewModel.currentPid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.lg,
                            vertical = WormaCeptorDesignSystem.Spacing.xs,
                        ),
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isAtBottom && logs.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.setAutoScroll(true)
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.logs_scroll_to_bottom),
                    )
                }
            }
        },
    ) { paddingValues ->
        if (logs.isEmpty()) {
            EmptyLogsState(
                isCapturing = isCapturing,
                onStartCapture = { viewModel.startCapture() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        } else {
            LogList(
                logs = logs,
                listState = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun LevelFilterChips(
    selectedLevels: Set<LogLevel>,
    levelCounts: Map<LogLevel, Int>,
    onLevelToggle: (LogLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = logLevelColors()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        LogLevel.entries.forEach { level ->
            val isSelected = level in selectedLevels
            val count = levelCounts[level] ?: 0
            val levelColor = colors.forLevel(level)

            FilterChip(
                selected = isSelected,
                onClick = { onLevelToggle(level) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                    ) {
                        Text(
                            text = level.tag,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
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
                    selectedContainerColor = levelColor.copy(alpha = 0.15f),
                    selectedLabelColor = levelColor,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = levelColor.copy(alpha = 0.3f),
                    selectedBorderColor = levelColor.copy(alpha = 0.5f),
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}

@Composable
private fun StatsBar(
    totalCount: Int,
    filteredCount: Int,
    isCapturing: Boolean,
    pid: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Capture indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorDesignSystem.Spacing.sm)
                        .clip(CircleShape)
                        .background(
                            if (isCapturing) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        ),
                )
                Text(
                    text = if (isCapturing) "Capturing" else "Paused",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // PID
            Text(
                text = "PID: $pid",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Counts
        Text(
            text = if (filteredCount != totalCount) {
                "$filteredCount / $totalCount entries"
            } else {
                "$totalCount entries"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LogList(
    logs: ImmutableList<LogEntry>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.sm),
    ) {
        items(
            items = logs,
            key = { it.id },
        ) { entry ->
            LogEntryItem(
                entry = entry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LogEntryItem(entry: LogEntry, modifier: Modifier = Modifier) {
    val colors = logLevelColors()
    val levelColor = colors.forLevel(entry.level)
    val backgroundColor = colors.backgroundForLevel(entry.level)

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val formattedTime = remember(entry.timestamp) {
        timeFormat.format(Date(entry.timestamp))
    }

    Surface(
        modifier = modifier,
        color = backgroundColor.copy(alpha = 0.3f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.md,
                    vertical = WormaCeptorDesignSystem.CornerRadius.sm,
                ),
            verticalAlignment = Alignment.Top,
        ) {
            // Level badge
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                color = levelColor.copy(alpha = 0.15f),
                modifier = Modifier.padding(top = WormaCeptorDesignSystem.Spacing.xxs),
            ) {
                Text(
                    text = entry.level.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.CornerRadius.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                // Tag and timestamp row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.tag,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))

                // Message
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyLogsState(isCapturing: Boolean, onStartCapture: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.VerticalAlignBottom,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

        Text(
            text = if (isCapturing) "Waiting for logs..." else "No logs captured",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = if (isCapturing) {
                "Logs will appear here as they are generated"
            } else {
                "Tap play to start capturing logs"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )

        if (!isCapturing) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            Surface(
                onClick = onStartCapture,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = WormaCeptorDesignSystem.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Start Capture",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
