package com.azikar24.wormaceptor.feature.websocket.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorStatusDot
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.domain.entities.WebSocketState
import com.azikar24.wormaceptor.feature.websocket.R
import com.azikar24.wormaceptor.feature.websocket.ui.components.DirectionFilterChips
import com.azikar24.wormaceptor.feature.websocket.ui.components.MessageList
import com.azikar24.wormaceptor.feature.websocket.ui.components.StatsBar
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewEvent
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebSocketDetailScreen(
    state: WebSocketViewState,
    onEvent: (WebSocketViewEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ws = WormaCeptorTokens.Colors.WebSocket
    val haptic = LocalHapticFeedback.current
    val connection = state.selectedConnection

    if (state.showClearMessagesConfirmation) {
        WormaCeptorAlertDialog(
            title = stringResource(R.string.websocket_clear_messages_title),
            message = stringResource(R.string.websocket_clear_messages_message),
            confirmLabel = stringResource(R.string.websocket_clear_messages),
            onConfirm = { onEvent(WebSocketViewEvent.ClearMessagesConfirmed) },
            dismissLabel = stringResource(R.string.websocket_cancel),
            onDismiss = { onEvent(WebSocketViewEvent.ClearMessagesDismissed) },
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
                        Column {
                            Text(
                                text = stringResource(R.string.websocket_messages_title),
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
                                contentDescription = stringResource(R.string.websocket_back),
                            )
                        }
                    },
                    actions = {
                        if (connection != null) {
                            val stateColor = when (connection.state) {
                                WebSocketState.CONNECTING -> ws.connecting
                                WebSocketState.OPEN -> ws.open
                                WebSocketState.CLOSING -> ws.closing
                                WebSocketState.CLOSED -> ws.closed
                            }
                            WormaCeptorStatusDot(
                                color = stateColor,
                                size = WormaCeptorTokens.Spacing.md,
                                modifier = Modifier.semantics {
                                    contentDescription = connection.state.name
                                },
                            )
                            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEvent(WebSocketViewEvent.ClearMessagesRequested)
                            },
                            enabled = state.messages.isNotEmpty(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.websocket_clear_messages),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                WormaCeptorSearchBar(
                    query = state.messageSearchQuery,
                    onQueryChange = { onEvent(WebSocketViewEvent.MessageSearchQueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WormaCeptorTokens.Spacing.lg,
                            vertical = WormaCeptorTokens.Spacing.sm,
                        ),
                    placeholder = stringResource(R.string.websocket_search_messages_placeholder),
                )

                DirectionFilterChips(
                    selectedDirection = state.directionFilter,
                    directionCounts = state.directionCounts,
                    onDirectionToggle = { onEvent(WebSocketViewEvent.DirectionFilterToggled(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = WormaCeptorTokens.Spacing.sm),
                )

                StatsBar(
                    label = stringResource(R.string.websocket_messages_title),
                    totalCount = state.totalMessageCount,
                    filteredCount = state.messages.size,
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
        Box(modifier = Modifier.imePadding()) {
            if (state.messages.isEmpty()) {
                WormaCeptorEmptyState(
                    title = stringResource(
                        if (state.messageSearchQuery.isNotBlank() || state.directionFilter != null) {
                            R.string.websocket_empty_no_matching_messages
                        } else {
                            R.string.websocket_empty_no_messages
                        },
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    subtitle = stringResource(
                        if (state.messageSearchQuery.isNotBlank() || state.directionFilter != null) {
                            R.string.websocket_empty_filter_hint
                        } else {
                            R.string.websocket_empty_messages_hint
                        },
                    ),
                )
            } else {
                MessageList(
                    messages = state.messages,
                    expandedMessageId = state.expandedMessageId,
                    onMessageClick = { onEvent(WebSocketViewEvent.MessageExpandToggled(it)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WebSocketDetailScreenPreview() {
    WormaCeptorTheme {
        WebSocketDetailScreen(
            state = WebSocketViewState(
                selectedConnection = WebSocketConnection(
                    id = 1L,
                    url = "wss://echo.websocket.org",
                    state = WebSocketState.OPEN,
                    openedAt = System.currentTimeMillis() - 60_000L,
                ),
                messages = persistentListOf(
                    WebSocketMessage(
                        id = 1L,
                        connectionId = 1L,
                        type = WebSocketMessageType.TEXT,
                        direction = WebSocketMessageDirection.SENT,
                        payload = "{\"type\":\"ping\",\"timestamp\":1234567890}",
                        timestamp = System.currentTimeMillis() - 5_000L,
                        size = 42L,
                    ),
                    WebSocketMessage(
                        id = 2L,
                        connectionId = 1L,
                        type = WebSocketMessageType.TEXT,
                        direction = WebSocketMessageDirection.RECEIVED,
                        payload = "{\"type\":\"pong\",\"timestamp\":1234567891}",
                        timestamp = System.currentTimeMillis() - 4_000L,
                        size = 43L,
                    ),
                ),
                totalMessageCount = 2,
                directionCounts = mapOf(
                    WebSocketMessageDirection.SENT to 1,
                    WebSocketMessageDirection.RECEIVED to 1,
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Clear Messages Confirmation")
@Composable
private fun WebSocketDetailScreenClearConfirmPreview() {
    WormaCeptorTheme {
        WebSocketDetailScreen(
            state = WebSocketViewState(
                selectedConnection = WebSocketConnection(
                    id = 1L,
                    url = "wss://echo.websocket.org",
                    state = WebSocketState.OPEN,
                    openedAt = System.currentTimeMillis() - 60_000L,
                ),
                messages = persistentListOf(
                    WebSocketMessage(
                        id = 1L,
                        connectionId = 1L,
                        type = WebSocketMessageType.TEXT,
                        direction = WebSocketMessageDirection.SENT,
                        payload = "{\"type\":\"ping\"}",
                        timestamp = System.currentTimeMillis() - 5_000L,
                        size = 16L,
                    ),
                ),
                totalMessageCount = 1,
                showClearMessagesConfirmation = true,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
private fun WebSocketDetailScreenEmptyPreview() {
    WormaCeptorTheme {
        WebSocketDetailScreen(
            state = WebSocketViewState(
                selectedConnection = WebSocketConnection(
                    id = 1L,
                    url = "wss://echo.websocket.org",
                    state = WebSocketState.OPEN,
                    openedAt = System.currentTimeMillis() - 60_000L,
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
