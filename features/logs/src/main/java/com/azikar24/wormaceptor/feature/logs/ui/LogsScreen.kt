package com.azikar24.wormaceptor.feature.logs.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import com.azikar24.wormaceptor.feature.logs.R
import com.azikar24.wormaceptor.feature.logs.ui.components.LevelFilterChips
import com.azikar24.wormaceptor.feature.logs.ui.components.LogList
import com.azikar24.wormaceptor.feature.logs.ui.components.StatsBar
import com.azikar24.wormaceptor.feature.logs.vm.LogsViewEvent
import com.azikar24.wormaceptor.feature.logs.vm.LogsViewModel
import com.azikar24.wormaceptor.feature.logs.vm.LogsViewState

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
@Composable
fun LogsScreen(
    viewModel: LogsViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    val onEvent: (LogsViewEvent) -> Unit = { viewModel.sendEvent(it) }

    LogsScreenContent(
        state = state,
        onEvent = onEvent,
        modifier = modifier,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogsScreenContent(
    state: LogsViewState,
    onEvent: (LogsViewEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val currentOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(state.logs.size, state.autoScroll) {
        if (state.autoScroll && state.logs.isNotEmpty()) {
            listState.animateScrollToItem(state.logs.size - 1)
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 2
        }
    }

    LaunchedEffect(isAtBottom) {
        if (!isAtBottom && state.autoScroll) {
            currentOnEvent(LogsViewEvent.AutoScrollSet(false))
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            LogsTopBar(
                state = state,
                onEvent = onEvent,
                onBack = onBack,
            )
        },
        floatingActionButton = {
            LogsScrollFab(
                isAtBottom = isAtBottom,
                hasLogs = state.logs.isNotEmpty(),
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        LogsBody(
            state = state,
            onEvent = onEvent,
            listState = listState,
            paddingValues = paddingValues,
        )
    }
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsTopBar(
    state: LogsViewState,
    onEvent: (LogsViewEvent) -> Unit,
    onBack: (() -> Unit)?,
) {
    val haptic = LocalHapticFeedback.current

    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.logs_console_title),
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
                WormaCeptorPlayPauseButton(
                    isActive = state.isCapturing,
                    onToggle = {
                        if (state.isCapturing) {
                            onEvent(LogsViewEvent.CaptureStopped)
                        } else {
                            onEvent(LogsViewEvent.CaptureStarted)
                        }
                    },
                    activeContentDescription = stringResource(R.string.logs_pause_capture),
                    inactiveContentDescription = stringResource(R.string.logs_start_capture),
                )

                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEvent(LogsViewEvent.LogsCleared)
                }) {
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

        WormaCeptorSearchBar(
            query = state.searchQuery,
            onQueryChange = { onEvent(LogsViewEvent.SearchQueryChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.lg,
                    vertical = WormaCeptorTokens.Spacing.sm,
                ),
            placeholder = stringResource(R.string.logs_search_placeholder),
        )

        LevelFilterChips(
            selectedLevels = state.selectedLevels,
            levelCounts = state.levelCounts,
            onLevelToggle = { onEvent(LogsViewEvent.LevelToggled(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WormaCeptorTokens.Spacing.sm),
        )

        StatsBar(
            totalCount = state.totalCount,
            filteredCount = state.logs.size,
            isCapturing = state.isCapturing,
            pid = state.currentPid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.lg,
                    vertical = WormaCeptorTokens.Spacing.xs,
                ),
        )
    }
}

@Composable
private fun LogsScrollFab(
    isAtBottom: Boolean,
    hasLogs: Boolean,
    onEvent: (LogsViewEvent) -> Unit,
) {
    AnimatedVisibility(
        visible = !isAtBottom && hasLogs,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
    ) {
        WormaCeptorFAB(
            onClick = { onEvent(LogsViewEvent.AutoScrollSet(true)) },
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(R.string.logs_scroll_to_bottom),
        )
    }
}

@Composable
private fun LogsBody(
    state: LogsViewState,
    onEvent: (LogsViewEvent) -> Unit,
    listState: LazyListState,
    paddingValues: PaddingValues,
) {
    Box(modifier = Modifier.imePadding()) {
        if (state.logs.isEmpty()) {
            LogsEmptyState(
                isCapturing = state.isCapturing,
                onEvent = onEvent,
                paddingValues = paddingValues,
            )
        } else {
            LogList(
                logs = state.logs,
                listState = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun LogsEmptyState(
    isCapturing: Boolean,
    onEvent: (LogsViewEvent) -> Unit,
    paddingValues: PaddingValues,
) {
    WormaCeptorEmptyState(
        title = if (isCapturing) {
            stringResource(R.string.logs_empty_capturing_title)
        } else {
            stringResource(R.string.logs_empty_paused_title)
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        subtitle = if (isCapturing) {
            stringResource(R.string.logs_empty_capturing_subtitle)
        } else {
            stringResource(R.string.logs_empty_paused_subtitle)
        },
        icon = Icons.Default.VerticalAlignBottom,
        actionLabel = if (!isCapturing) stringResource(R.string.logs_start_capture_action) else null,
        onAction = if (!isCapturing) {
            { onEvent(LogsViewEvent.CaptureStarted) }
        } else {
            null
        },
    )
}

@Suppress("HardcodedString")
@Preview(showBackground = true)
@Composable
private fun LogsScreenContentPreview() {
    WormaCeptorTheme {
        LogsScreenContent(
            state = LogsViewState(
                logs = kotlinx.collections.immutable.persistentListOf(
                    LogEntry(
                        id = 1L,
                        timestamp = System.currentTimeMillis() - 5_000,
                        level = LogLevel.DEBUG,
                        tag = "OkHttp",
                        pid = 12_345,
                        message = "Sending request https://api.example.com/users",
                    ),
                    LogEntry(
                        id = 2L,
                        timestamp = System.currentTimeMillis() - 3_000,
                        level = LogLevel.INFO,
                        tag = "MainActivity",
                        pid = 12_345,
                        message = "User authenticated successfully",
                    ),
                    LogEntry(
                        id = 3L,
                        timestamp = System.currentTimeMillis() - 1_000,
                        level = LogLevel.ERROR,
                        tag = "CrashReporter",
                        pid = 12_345,
                        message = "Failed to upload crash report: timeout",
                    ),
                ),
                searchQuery = "",
                selectedLevels = LogLevel.entries.toSet(),
                autoScroll = true,
                isCapturing = true,
                totalCount = 3,
                levelCounts = mapOf(
                    LogLevel.DEBUG to 1,
                    LogLevel.INFO to 1,
                    LogLevel.ERROR to 1,
                ),
                currentPid = 12_345,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
