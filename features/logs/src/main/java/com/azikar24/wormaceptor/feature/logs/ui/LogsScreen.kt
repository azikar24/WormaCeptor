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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
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
import kotlinx.collections.immutable.ImmutableList

/** Main screen for viewing console logs with filtering, search, and auto-scroll. */
@Composable
fun LogsScreen(
    viewModel: LogsViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    BaseScreen(viewModel) { state, onEvent ->
        LogsScreenContent(
            state = state,
            onEvent = onEvent,
            modifier = modifier,
            onBack = onBack,
        )
    }
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
            Column {
                LogsAppBar(
                    isCapturing = state.isCapturing,
                    onToggleCapture = {
                        if (state.isCapturing) {
                            onEvent(LogsViewEvent.CaptureStopped)
                        } else {
                            onEvent(LogsViewEvent.CaptureStarted)
                        }
                    },
                    onClearLogs = { onEvent(LogsViewEvent.LogsCleared) },
                    onBack = onBack,
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
        },
        floatingActionButton = {
            ScrollToBottomFab(
                isAtBottom = isAtBottom,
                hasLogs = state.logs.isNotEmpty(),
                onClick = { onEvent(LogsViewEvent.AutoScrollSet(true)) },
            )
        },
    ) { paddingValues ->
        LogsBody(
            logs = state.logs,
            isLoading = state.isLogsLoading,
            isCapturing = state.isCapturing,
            onStartCapture = { onEvent(LogsViewEvent.CaptureStarted) },
            listState = listState,
            paddingValues = paddingValues,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogsAppBar(
    isCapturing: Boolean,
    onToggleCapture: () -> Unit,
    onClearLogs: () -> Unit,
    onBack: (() -> Unit)?,
) {
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
                isActive = isCapturing,
                onToggle = onToggleCapture,
                activeContentDescription = stringResource(R.string.logs_pause_capture),
                inactiveContentDescription = stringResource(R.string.logs_start_capture),
            )

            IconButton(onClick = onClearLogs) {
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
}

@Composable
private fun ScrollToBottomFab(
    isAtBottom: Boolean,
    hasLogs: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = !isAtBottom && hasLogs,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
    ) {
        WormaCeptorFAB(
            onClick = onClick,
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(R.string.logs_scroll_to_bottom),
        )
    }
}

@Composable
private fun LogsBody(
    logs: ImmutableList<LogEntry>,
    isLoading: Boolean,
    isCapturing: Boolean,
    onStartCapture: () -> Unit,
    listState: LazyListState,
    paddingValues: PaddingValues,
) {
    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        WormaCeptorLoadableContent(
            isLoading = isLoading,
            isEmpty = logs.isEmpty(),
            loading = {
                WormaCeptorListSkeleton(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                )
            },
            empty = {
                LogsEmptyState(
                    isCapturing = isCapturing,
                    onStartCapture = onStartCapture,
                    paddingValues = paddingValues,
                )
            },
            content = {
                LogList(
                    logs = logs,
                    listState = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun LogsEmptyState(
    isCapturing: Boolean,
    onStartCapture: () -> Unit,
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
        onAction = if (!isCapturing) onStartCapture else null,
    )
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun LogsScreenContentPreview(@PreviewParameter(LogsStatePreviewProvider::class) state: LogsViewState) {
    WormaCeptorTheme {
        LogsScreenContent(
            state = state,
            onEvent = {},
            onBack = {},
        )
    }
}

@Suppress("MagicNumber")
private class LogsStatePreviewProvider : PreviewParameterProvider<LogsViewState> {
    override val values: Sequence<LogsViewState> = sequenceOf(
        LogsViewState(
            isLogsLoading = false,
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
        LogsViewState(isLogsLoading = false, isCapturing = true, currentPid = 12_345),
        LogsViewState(isLogsLoading = false, isCapturing = false, currentPid = 12_345),
    )
}
