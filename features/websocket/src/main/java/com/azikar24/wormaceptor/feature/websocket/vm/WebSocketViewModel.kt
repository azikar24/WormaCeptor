/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.websocket.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the WebSocket Monitoring feature.
 *
 * Provides filtered and searchable access to WebSocket connections and messages,
 * along with controls for direction filtering and search.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class WebSocketViewModel(
    private val engine: WebSocketMonitorEngine,
) : ViewModel() {

    // Connection list search query
    private val _connectionSearchQuery = MutableStateFlow("")
    val connectionSearchQuery: StateFlow<String> = _connectionSearchQuery

    // Currently selected connection ID
    private val _selectedConnectionId = MutableStateFlow<Long?>(null)
    val selectedConnectionId: StateFlow<Long?> = _selectedConnectionId

    // Message search query (for detail screen)
    private val _messageSearchQuery = MutableStateFlow("")
    val messageSearchQuery: StateFlow<String> = _messageSearchQuery

    // Direction filter (null = all directions)
    private val _directionFilter = MutableStateFlow<WebSocketMessageDirection?>(null)
    val directionFilter: StateFlow<WebSocketMessageDirection?> = _directionFilter

    // Expanded message ID (for showing full payload)
    private val _expandedMessageId = MutableStateFlow<Long?>(null)
    val expandedMessageId: StateFlow<Long?> = _expandedMessageId

    // Raw connections from engine
    private val rawConnections: StateFlow<List<WebSocketConnection>> = engine.connections

    // Raw messages from engine
    private val rawMessages: StateFlow<List<WebSocketMessage>> = engine.messages

    /**
     * Filtered connections based on search query.
     */
    val connections: StateFlow<ImmutableList<WebSocketConnection>> = combine(
        rawConnections,
        _connectionSearchQuery.debounce(150),
    ) { connections, query ->
        connections.filter { conn ->
            query.isBlank() || conn.url.contains(query, ignoreCase = true)
        }.sortedByDescending { it.openedAt ?: it.id }
            .toImmutableList()
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    /**
     * Gets the currently selected connection.
     */
    val selectedConnection: StateFlow<WebSocketConnection?> = _selectedConnectionId
        .flatMapLatest { connectionId ->
            if (connectionId == null) {
                flowOf(null)
            } else {
                rawConnections.map { connections ->
                    connections.find { it.id == connectionId }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Filtered messages for the selected connection.
     */
    val messages: StateFlow<ImmutableList<WebSocketMessage>> = combine(
        rawMessages,
        _selectedConnectionId,
        _messageSearchQuery.debounce(150),
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    /**
     * Total message count for the selected connection (unfiltered).
     */
    val totalMessageCount: StateFlow<Int> = combine(
        rawMessages,
        _selectedConnectionId,
    ) { messages, connectionId ->
        if (connectionId == null) {
            0
        } else {
            messages.count { it.connectionId == connectionId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Message counts by direction for the selected connection.
     */
    val directionCounts: StateFlow<Map<WebSocketMessageDirection, Int>> = combine(
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Total connection count.
     */
    val totalConnectionCount: StateFlow<Int> = rawConnections
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Message count per connection for list display.
     */
    fun getMessageCountForConnection(connectionId: Long): Int {
        return engine.getMessageCountForConnection(connectionId)
    }

    /**
     * Updates the connection search query.
     */
    fun onConnectionSearchQueryChanged(query: String) {
        _connectionSearchQuery.value = query
    }

    /**
     * Selects a connection to view its messages.
     */
    fun selectConnection(connectionId: Long) {
        _selectedConnectionId.value = connectionId
        _messageSearchQuery.value = ""
        _directionFilter.value = null
        _expandedMessageId.value = null
    }

    /**
     * Clears the connection selection.
     */
    fun clearConnectionSelection() {
        _selectedConnectionId.value = null
        _messageSearchQuery.value = ""
        _directionFilter.value = null
        _expandedMessageId.value = null
    }

    /**
     * Updates the message search query.
     */
    fun onMessageSearchQueryChanged(query: String) {
        _messageSearchQuery.value = query
    }

    /**
     * Sets the direction filter.
     */
    fun setDirectionFilter(direction: WebSocketMessageDirection?) {
        _directionFilter.value = direction
    }

    /**
     * Toggles the direction filter.
     */
    fun toggleDirectionFilter(direction: WebSocketMessageDirection) {
        _directionFilter.value = if (_directionFilter.value == direction) {
            null
        } else {
            direction
        }
    }

    /**
     * Toggles the expanded state of a message.
     */
    fun toggleMessageExpanded(messageId: Long) {
        _expandedMessageId.value = if (_expandedMessageId.value == messageId) {
            null
        } else {
            messageId
        }
    }

    /**
     * Checks if a message is expanded.
     */
    fun isMessageExpanded(messageId: Long): Boolean {
        return _expandedMessageId.value == messageId
    }

    /**
     * Clears all message and direction filters.
     */
    fun clearFilters() {
        _messageSearchQuery.value = ""
        _directionFilter.value = null
    }

    /**
     * Clears all connections and messages.
     */
    fun clearAll() {
        engine.clear()
        _selectedConnectionId.value = null
        _expandedMessageId.value = null
    }

    /**
     * Clears messages for the currently selected connection.
     */
    fun clearCurrentConnectionMessages() {
        val connectionId = _selectedConnectionId.value ?: return
        engine.clearMessagesForConnection(connectionId)
        _expandedMessageId.value = null
    }
}
