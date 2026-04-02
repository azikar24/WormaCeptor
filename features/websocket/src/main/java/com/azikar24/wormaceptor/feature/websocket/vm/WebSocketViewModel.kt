package com.azikar24.wormaceptor.feature.websocket.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val ConnectionSearchDebounceMs = 150L
private const val MessageSearchDebounceMs = 150L

/**
 * ViewModel for the WebSocket Monitoring feature, using MVI via BaseViewModel.
 *
 * Provides filtered and searchable access to WebSocket connections and messages,
 * along with controls for direction filtering and search.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class WebSocketViewModel(
    private val engine: WebSocketMonitorEngine,
) : BaseViewModel<WebSocketViewState, WebSocketViewEffect, WebSocketViewEvent>(WebSocketViewState()) {

    // Internal flows used for debounced filtering pipelines
    private val _connectionSearchQuery = MutableStateFlow("")
    private val _selectedConnectionId = MutableStateFlow<Long?>(null)
    private val _messageSearchQuery = MutableStateFlow("")
    private val _directionFilter = MutableStateFlow<WebSocketMessageDirection?>(null)

    // Raw flows from engine
    private val rawConnections = engine.connections
    private val rawMessages = engine.messages

    init {
        observeFilteredConnections()
        observeTotalConnectionCount()
        observeSelectedConnection()
        observeFilteredMessages()
        observeTotalMessageCount()
        observeDirectionCounts()
    }

    override fun handleEvent(event: WebSocketViewEvent) {
        when (event) {
            is WebSocketViewEvent.ConnectionSearchQueryChanged -> onConnectionSearchQueryChanged(event.query)
            is WebSocketViewEvent.ConnectionSelected -> selectConnection(event.connectionId)
            is WebSocketViewEvent.ConnectionSelectionCleared -> clearConnectionSelection()
            is WebSocketViewEvent.MessageSearchQueryChanged -> onMessageSearchQueryChanged(event.query)
            is WebSocketViewEvent.DirectionFilterToggled -> toggleDirectionFilter(event.direction)
            is WebSocketViewEvent.MessageExpandToggled -> toggleMessageExpanded(event.messageId)
            is WebSocketViewEvent.ClearAll -> clearAll()
            is WebSocketViewEvent.ClearCurrentConnectionMessages -> clearCurrentConnectionMessages()
        }
    }

    /**
     * Message count per connection for list display.
     */
    fun getMessageCountForConnection(connectionId: Long): Int {
        return engine.getMessageCountForConnection(connectionId)
    }

    private fun onConnectionSearchQueryChanged(query: String) {
        _connectionSearchQuery.value = query
        updateState { copy(connectionSearchQuery = query) }
    }

    private fun selectConnection(connectionId: Long) {
        _selectedConnectionId.value = connectionId
        _messageSearchQuery.value = ""
        _directionFilter.value = null
        updateState {
            copy(
                messageSearchQuery = "",
                directionFilter = null,
                expandedMessageId = null,
            )
        }
    }

    private fun clearConnectionSelection() {
        _selectedConnectionId.value = null
        _messageSearchQuery.value = ""
        _directionFilter.value = null
        updateState {
            copy(
                selectedConnection = null,
                messageSearchQuery = "",
                directionFilter = null,
                expandedMessageId = null,
            )
        }
    }

    private fun onMessageSearchQueryChanged(query: String) {
        _messageSearchQuery.value = query
        updateState { copy(messageSearchQuery = query) }
    }

    private fun toggleDirectionFilter(direction: WebSocketMessageDirection) {
        val newDirection = if (_directionFilter.value == direction) null else direction
        _directionFilter.value = newDirection
        updateState { copy(directionFilter = newDirection) }
    }

    private fun toggleMessageExpanded(messageId: Long) {
        updateState {
            copy(
                expandedMessageId = if (expandedMessageId == messageId) null else messageId,
            )
        }
    }

    private fun clearAll() {
        engine.clear()
        _selectedConnectionId.value = null
        updateState {
            copy(
                selectedConnection = null,
                expandedMessageId = null,
            )
        }
    }

    private fun clearCurrentConnectionMessages() {
        val connectionId = _selectedConnectionId.value ?: return
        engine.clearMessagesForConnection(connectionId)
        updateState { copy(expandedMessageId = null) }
    }

    // --- Reactive observation pipelines ---

    private fun observeFilteredConnections() {
        combine(
            rawConnections,
            _connectionSearchQuery.debounce(ConnectionSearchDebounceMs),
        ) { connections, query ->
            connections.filter { conn ->
                query.isBlank() || conn.url.contains(query, ignoreCase = true)
            }.sortedByDescending { it.openedAt ?: it.id }
                .toImmutableList()
        }.flowOn(Dispatchers.Default)
            .onEach { filtered ->
                updateState { copy(connections = filtered) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTotalConnectionCount() {
        rawConnections
            .map { it.size }
            .onEach { count ->
                updateState { copy(totalConnectionCount = count) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSelectedConnection() {
        _selectedConnectionId
            .flatMapLatest { connectionId ->
                if (connectionId == null) {
                    flowOf<WebSocketConnection?>(null)
                } else {
                    rawConnections.map { connections ->
                        connections.find { it.id == connectionId }
                    }
                }
            }
            .onEach { connection ->
                updateState { copy(selectedConnection = connection) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFilteredMessages() {
        combine(
            rawMessages,
            _selectedConnectionId,
            _messageSearchQuery.debounce(MessageSearchDebounceMs),
            _directionFilter,
        ) { messages, connectionId, query, direction ->
            if (connectionId == null) {
                emptyList()
            } else {
                messages.filter { msg ->
                    val matchesConnection = msg.connectionId == connectionId
                    val matchesQuery = query.isBlank() ||
                        msg.payload.contains(query, ignoreCase = true)
                    val matchesDirection = direction == null ||
                        msg.direction == direction
                    matchesConnection && matchesQuery && matchesDirection
                }
            }
        }.map { it.toImmutableList() }
            .flowOn(Dispatchers.Default)
            .onEach { filtered ->
                updateState { copy(messages = filtered) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTotalMessageCount() {
        combine(
            rawMessages,
            _selectedConnectionId,
        ) { messages, connectionId ->
            if (connectionId == null) 0 else messages.count { it.connectionId == connectionId }
        }.onEach { count ->
            updateState { copy(totalMessageCount = count) }
        }.launchIn(viewModelScope)
    }

    private fun observeDirectionCounts() {
        combine(
            rawMessages,
            _selectedConnectionId,
        ) { messages, connectionId ->
            if (connectionId == null) {
                emptyMap()
            } else {
                messages.filter { it.connectionId == connectionId }
                    .groupingBy { it.direction }
                    .eachCount()
            }
        }.onEach { counts ->
            updateState { copy(directionCounts = counts) }
        }.launchIn(viewModelScope)
    }
}
