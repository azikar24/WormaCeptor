package com.azikar24.wormaceptor.feature.websocket.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.components.status.WormaCeptorStatusDot
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketState
import com.azikar24.wormaceptor.feature.websocket.R
import com.azikar24.wormaceptor.feature.websocket.ui.components.StatsBar
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewEvent
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebSocketListScreen(
    state: WebSocketViewState,
    onEvent: (WebSocketViewEvent) -> Unit,
    getMessageCount: (Long) -> Int,
    onConnectionClick: (WebSocketConnection) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    if (state.showClearAllConfirmation) {
        WormaCeptorAlertDialog(
            title = stringResource(R.string.websocket_clear_all_title),
            message = stringResource(R.string.websocket_clear_all_message),
            confirmLabel = stringResource(R.string.websocket_clear_all),
            onConfirm = { onEvent(WebSocketViewEvent.ClearAllConfirmed) },
            dismissLabel = stringResource(R.string.websocket_cancel),
            onDismiss = { onEvent(WebSocketViewEvent.ClearAllDismissed) },
            icon = Icons.Default.Delete,
            destructive = true,
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
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
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEvent(WebSocketViewEvent.ClearAllRequested)
                            },
                            enabled = state.connections.isNotEmpty(),
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

                WormaCeptorSearchBar(
                    query = state.connectionSearchQuery,
                    onQueryChange = { onEvent(WebSocketViewEvent.ConnectionSearchQueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WormaCeptorTokens.Spacing.lg,
                            vertical = WormaCeptorTokens.Spacing.sm,
                        ),
                    placeholder = stringResource(R.string.websocket_search_url_placeholder),
                )

                StatsBar(
                    label = stringResource(R.string.websocket_connections_label),
                    totalCount = state.totalConnectionCount,
                    filteredCount = state.connections.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WormaCeptorTokens.Spacing.lg,
                            vertical = WormaCeptorTokens.Spacing.xs,
                        ),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
        ) {
            WormaCeptorLoadableContent(
                isLoading = state.isConnectionsLoading,
                isEmpty = state.connections.isEmpty(),
                loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
                empty = {
                    WormaCeptorEmptyState(
                        title = stringResource(
                            if (state.connectionSearchQuery.isNotBlank()) {
                                R.string.websocket_empty_no_matching_connections
                            } else {
                                R.string.websocket_empty_no_connections
                            },
                        ),
                        modifier = Modifier.fillMaxSize(),
                        subtitle = stringResource(
                            if (state.connectionSearchQuery.isNotBlank()) {
                                R.string.websocket_empty_search_hint
                            } else {
                                R.string.websocket_empty_connections_hint
                            },
                        ),
                        icon = Icons.Default.Sync,
                    )
                },
                content = {
                    ConnectionList(
                        connections = state.connections,
                        onConnectionClick = onConnectionClick,
                        getMessageCount = getMessageCount,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
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
        contentPadding = PaddingValues(
            top = WormaCeptorTokens.Spacing.sm,
            bottom = WormaCeptorTokens.Spacing.sm +
                WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding(),
        ),
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
    val ws = WormaCeptorTokens.Colors.WebSocket
    val stateColor = when (connection.state) {
        WebSocketState.CONNECTING -> ws.connecting
        WebSocketState.OPEN -> ws.open
        WebSocketState.CLOSING -> ws.closing
        WebSocketState.CLOSED -> ws.closed
    }
    val backgroundColor = when (connection.state) {
        WebSocketState.CONNECTING -> ws.connecting.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
        WebSocketState.OPEN -> ws.open.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
        WebSocketState.CLOSING -> ws.closing.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
        WebSocketState.CLOSED -> ws.closed.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
    }

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
            alpha = WormaCeptorTokens.Alpha.MODERATE,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.lg,
                    vertical = WormaCeptorTokens.Spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WormaCeptorStatusDot(
                color = stateColor,
                size = WormaCeptorTokens.Spacing.md,
            )

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.url,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                        color = stateColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                    ) {
                        Text(
                            text = connection.state.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = stateColor,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.xs,
                                vertical = WormaCeptorTokens.Spacing.xxs,
                            ),
                        )
                    }

                    Text(
                        text = stringResource(R.string.websocket_message_count, messageCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = duration,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.HEAVY,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WebSocketListScreenPreview() {
    WormaCeptorTheme {
        WebSocketListScreen(
            state = WebSocketViewState(
                connections = persistentListOf(
                    WebSocketConnection(
                        id = 1L,
                        url = "wss://echo.websocket.org",
                        state = WebSocketState.OPEN,
                        openedAt = System.currentTimeMillis() - 60_000L,
                    ),
                    WebSocketConnection(
                        id = 2L,
                        url = "wss://api.example.com/ws",
                        state = WebSocketState.CLOSED,
                        openedAt = System.currentTimeMillis() - 120_000L,
                        closedAt = System.currentTimeMillis() - 30_000L,
                        closeCode = 1000,
                        closeReason = "Normal closure",
                    ),
                ),
                totalConnectionCount = 2,
            ),
            onEvent = {},
            onConnectionClick = {},
            getMessageCount = { 5 },
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Clear All Confirmation")
@Composable
private fun WebSocketListScreenClearConfirmPreview() {
    WormaCeptorTheme {
        WebSocketListScreen(
            state = WebSocketViewState(
                connections = persistentListOf(
                    WebSocketConnection(
                        id = 1L,
                        url = "wss://echo.websocket.org",
                        state = WebSocketState.OPEN,
                        openedAt = System.currentTimeMillis() - 60_000L,
                    ),
                ),
                totalConnectionCount = 1,
                showClearAllConfirmation = true,
            ),
            onEvent = {},
            onConnectionClick = {},
            getMessageCount = { 3 },
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
private fun WebSocketListScreenEmptyPreview() {
    WormaCeptorTheme {
        WebSocketListScreen(
            state = WebSocketViewState(),
            onEvent = {},
            onConnectionClick = {},
            getMessageCount = { 0 },
            onBack = {},
        )
    }
}
