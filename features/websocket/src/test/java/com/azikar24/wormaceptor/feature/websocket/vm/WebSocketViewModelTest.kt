package com.azikar24.wormaceptor.feature.websocket.vm

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.domain.entities.WebSocketState
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val connectionsFlow = MutableStateFlow<List<WebSocketConnection>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<WebSocketMessage>>(emptyList())

    private val engine = mockk<WebSocketMonitorEngine>(relaxed = true) {
        every { connections } returns connectionsFlow
        every { messages } returns messagesFlow
    }

    private lateinit var viewModel: WebSocketViewModel

    private fun makeConnection(
        id: Long = 1L,
        url: String = "wss://example.com/socket",
        state: WebSocketState = WebSocketState.OPEN,
        openedAt: Long? = 1000L,
    ) = WebSocketConnection(
        id = id,
        url = url,
        state = state,
        openedAt = openedAt,
    )

    private fun makeMessage(
        id: Long = 1L,
        connectionId: Long = 1L,
        direction: WebSocketMessageDirection = WebSocketMessageDirection.RECEIVED,
        payload: String = "Hello",
    ) = WebSocketMessage(
        id = id,
        connectionId = connectionId,
        type = WebSocketMessageType.TEXT,
        direction = direction,
        payload = payload,
        timestamp = System.currentTimeMillis(),
        size = payload.toByteArray().size.toLong(),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WebSocketViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `connection search query is empty`() {
            viewModel.uiState.value.connectionSearchQuery shouldBe ""
        }

        @Test
        fun `message search query is empty`() {
            viewModel.uiState.value.messageSearchQuery shouldBe ""
        }

        @Test
        fun `direction filter is null`() {
            viewModel.uiState.value.directionFilter shouldBe null
        }

        @Test
        fun `expanded message id is null`() {
            viewModel.uiState.value.expandedMessageId shouldBe null
        }
    }

    @Nested
    inner class `connections` {

        @Test
        fun `emits connections sorted by openedAt descending`() = runTest {
            connectionsFlow.value = listOf(
                makeConnection(id = 1, openedAt = 1000L),
                makeConnection(id = 2, openedAt = 2000L),
                makeConnection(id = 3, openedAt = 1500L),
            )
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitUntil { it.connections.size == 3 }
                state.connections.map { it.id } shouldBe listOf(2L, 3L, 1L)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters connections by search query matching url`() = runTest {
            viewModel.uiState.test {
                connectionsFlow.value = listOf(
                    makeConnection(id = 1, url = "wss://api.example.com/chat"),
                    makeConnection(id = 2, url = "wss://other.service.com/feed"),
                )
                viewModel.sendEvent(WebSocketViewEvent.ConnectionSearchQueryChanged("example"))

                val state = awaitUntil { it.connections.size == 1 }
                state.connections.first().id shouldBe 1L
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `blank search shows all connections`() = runTest {
            connectionsFlow.value = listOf(
                makeConnection(id = 1),
                makeConnection(id = 2),
            )
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitUntil { it.connections.size == 2 }.connections shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `selectConnection` {

        @Test
        fun `sets selected connection and resets message filters`() = runTest {
            viewModel.sendEvent(WebSocketViewEvent.MessageSearchQueryChanged("something"))
            viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(42))

            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))

            viewModel.uiState.value.messageSearchQuery shouldBe ""
            viewModel.uiState.value.directionFilter shouldBe null
            viewModel.uiState.value.expandedMessageId shouldBe null
        }

        @Test
        fun `selectedConnection emits the matching connection`() = runTest {
            connectionsFlow.value = listOf(
                makeConnection(id = 1, url = "wss://first.com"),
                makeConnection(id = 2, url = "wss://second.com"),
            )

            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(2L))
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitUntil { it.selectedConnection != null }
                state.selectedConnection?.id shouldBe 2L
                state.selectedConnection?.url shouldBe "wss://second.com"
            }
        }
    }

    @Nested
    inner class `clearConnectionSelection` {

        @Test
        fun `clears all selection and message state`() = runTest {
            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
            viewModel.sendEvent(WebSocketViewEvent.MessageSearchQueryChanged("query"))
            viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(5))

            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelectionCleared)

            viewModel.uiState.value.messageSearchQuery shouldBe ""
            viewModel.uiState.value.directionFilter shouldBe null
            viewModel.uiState.value.expandedMessageId shouldBe null
        }

        @Test
        fun `selectedConnection becomes null`() = runTest {
            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelectionCleared)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitUntil { it.selectedConnection == null }.selectedConnection shouldBe null
            }
        }
    }

    @Nested
    inner class `messages` {

        @Test
        fun `emits empty when no connection selected`() = runTest {
            messagesFlow.value = listOf(makeMessage())
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitUntil { it.messages.isEmpty() }.messages.shouldBeEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters messages by selected connection`() = runTest {
            viewModel.uiState.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L),
                    makeMessage(id = 2, connectionId = 2L),
                    makeMessage(id = 3, connectionId = 1L),
                )
                viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))

                awaitUntil { it.messages.size == 2 }.messages shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters messages by search query in payload`() = runTest {
            viewModel.uiState.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "Hello World"),
                    makeMessage(id = 2, connectionId = 1L, payload = "Goodbye"),
                )
                viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
                viewModel.sendEvent(WebSocketViewEvent.MessageSearchQueryChanged("Hello"))

                val state = awaitUntil { it.messages.size == 1 && it.messages.first().payload == "Hello World" }
                state.messages shouldHaveSize 1
                state.messages.first().payload shouldBe "Hello World"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters messages by direction`() = runTest {
            viewModel.uiState.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, direction = WebSocketMessageDirection.SENT),
                    makeMessage(id = 2, connectionId = 1L, direction = WebSocketMessageDirection.RECEIVED),
                )
                viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
                viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))

                val state = awaitUntil {
                    it.messages.size == 1 && it.messages.first().direction == WebSocketMessageDirection.SENT
                }
                state.messages shouldHaveSize 1
                state.messages.first().direction shouldBe WebSocketMessageDirection.SENT
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `combines all filters`() = runTest {
            viewModel.uiState.test {
                messagesFlow.value = listOf(
                    makeMessage(
                        id = 1,
                        connectionId = 1L,
                        direction = WebSocketMessageDirection.SENT,
                        payload = "ping",
                    ),
                    makeMessage(
                        id = 2,
                        connectionId = 1L,
                        direction = WebSocketMessageDirection.RECEIVED,
                        payload = "pong",
                    ),
                    makeMessage(
                        id = 3,
                        connectionId = 1L,
                        direction = WebSocketMessageDirection.SENT,
                        payload = "data",
                    ),
                    makeMessage(
                        id = 4,
                        connectionId = 2L,
                        direction = WebSocketMessageDirection.SENT,
                        payload = "ping",
                    ),
                )
                viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
                viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))
                viewModel.sendEvent(WebSocketViewEvent.MessageSearchQueryChanged("ping"))

                val state = awaitUntil { it.messages.size == 1 && it.messages.first().id == 1L }
                state.messages shouldHaveSize 1
                state.messages.first().id shouldBe 1L
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `search is case insensitive`() = runTest {
            viewModel.uiState.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "Hello World"),
                    makeMessage(id = 2, connectionId = 1L, payload = "Goodbye"),
                )
                viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
                viewModel.sendEvent(WebSocketViewEvent.MessageSearchQueryChanged("hello"))

                val state = awaitUntil { it.messages.size == 1 && it.messages.first().payload == "Hello World" }
                state.messages shouldHaveSize 1
                state.messages.first().payload shouldBe "Hello World"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `reacts to new messages from engine`() = runTest {
            viewModel.uiState.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "first"),
                )
                viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
                awaitUntil { it.messages.size == 1 }

                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "first"),
                    makeMessage(id = 2, connectionId = 1L, payload = "second"),
                )
                awaitUntil { it.messages.size == 2 }.messages shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `toggleDirectionFilter` {

        @Test
        fun `sets direction when currently null`() {
            viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))

            viewModel.uiState.value.directionFilter shouldBe WebSocketMessageDirection.SENT
        }

        @Test
        fun `clears direction when toggling same value`() {
            viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))
            viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))

            viewModel.uiState.value.directionFilter shouldBe null
        }

        @Test
        fun `switches to different direction`() {
            viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.SENT))
            viewModel.sendEvent(WebSocketViewEvent.DirectionFilterToggled(WebSocketMessageDirection.RECEIVED))

            viewModel.uiState.value.directionFilter shouldBe WebSocketMessageDirection.RECEIVED
        }
    }

    @Nested
    inner class `toggleMessageExpanded` {

        @Test
        fun `expands when currently null`() {
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(5L))

            viewModel.uiState.value.expandedMessageId shouldBe 5L
        }

        @Test
        fun `collapses when toggling same id`() {
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(5L))
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(5L))

            viewModel.uiState.value.expandedMessageId shouldBe null
        }

        @Test
        fun `switches to different id`() {
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(5L))
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(10L))

            viewModel.uiState.value.expandedMessageId shouldBe 10L
        }
    }

    @Nested
    inner class `totalMessageCount` {

        @Test
        fun `counts messages for selected connection`() = runTest {
            messagesFlow.value = listOf(
                makeMessage(id = 1, connectionId = 1L),
                makeMessage(id = 2, connectionId = 1L),
                makeMessage(id = 3, connectionId = 2L),
            )
            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitUntil { it.totalMessageCount == 2 }.totalMessageCount shouldBe 2
            }
        }

        @Test
        fun `is 0 when no connection selected`() = runTest {
            viewModel.uiState.test {
                awaitItem().totalMessageCount shouldBe 0
            }
        }
    }

    @Nested
    inner class `directionCounts` {

        @Test
        fun `counts messages by direction for selected connection`() = runTest {
            messagesFlow.value = listOf(
                makeMessage(id = 1, connectionId = 1L, direction = WebSocketMessageDirection.SENT),
                makeMessage(id = 2, connectionId = 1L, direction = WebSocketMessageDirection.SENT),
                makeMessage(id = 3, connectionId = 1L, direction = WebSocketMessageDirection.RECEIVED),
            )
            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitUntil { it.directionCounts.isNotEmpty() }
                state.directionCounts shouldContainKey WebSocketMessageDirection.SENT
                state.directionCounts[WebSocketMessageDirection.SENT] shouldBe 2
                state.directionCounts[WebSocketMessageDirection.RECEIVED] shouldBe 1
            }
        }
    }

    @Nested
    inner class `totalConnectionCount` {

        @Test
        fun `reflects connection list size`() = runTest {
            connectionsFlow.value = listOf(makeConnection(id = 1), makeConnection(id = 2))
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitUntil { it.totalConnectionCount == 2 }.totalConnectionCount shouldBe 2
            }
        }
    }

    @Nested
    inner class `clearAll` {

        @Test
        fun `delegates to engine and resets selection`() {
            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))
            viewModel.sendEvent(WebSocketViewEvent.MessageExpandToggled(5))

            viewModel.sendEvent(WebSocketViewEvent.ClearAll)

            verify { engine.clear() }
            viewModel.uiState.value.expandedMessageId shouldBe null
        }
    }

    @Nested
    inner class `clearCurrentConnectionMessages` {

        @Test
        fun `delegates to engine with connection id`() {
            viewModel.sendEvent(WebSocketViewEvent.ConnectionSelected(1L))

            viewModel.sendEvent(WebSocketViewEvent.ClearCurrentConnectionMessages)

            verify { engine.clearMessagesForConnection(1L) }
            viewModel.uiState.value.expandedMessageId shouldBe null
        }

        @Test
        fun `does nothing when no connection selected`() {
            viewModel.sendEvent(WebSocketViewEvent.ClearCurrentConnectionMessages)

            verify(exactly = 0) { engine.clearMessagesForConnection(any()) }
        }
    }

    @Nested
    inner class `getMessageCountForConnection` {

        @Test
        fun `delegates to engine`() {
            every { engine.getMessageCountForConnection(1L) } returns 7

            viewModel.getMessageCountForConnection(1L) shouldBe 7
        }
    }

    /**
     * Helper that keeps consuming items from the turbine until [predicate] returns true.
     * The debounce + flowOn(Dispatchers.Default) pipeline may emit intermediate states
     * before the final filtered result arrives.
     */
    private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
