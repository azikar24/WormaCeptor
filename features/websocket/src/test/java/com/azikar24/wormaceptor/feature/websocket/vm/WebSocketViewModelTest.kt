package com.azikar24.wormaceptor.feature.websocket.vm

import app.cash.turbine.ReceiveTurbine
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

    /**
     * Helper that keeps consuming items from the turbine until [predicate] returns true.
     * The debounce + flowOn(Dispatchers.Default) pipeline may emit intermediate states
     * before the final filtered result arrives.
     */
    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `connection search query is empty`() {
            viewModel.connectionSearchQuery.value shouldBe ""
        }

        @Test
        fun `message search query is empty`() {
            viewModel.messageSearchQuery.value shouldBe ""
        }

        @Test
        fun `direction filter is null`() {
            viewModel.directionFilter.value shouldBe null
        }

        @Test
        fun `expanded message id is null`() {
            viewModel.expandedMessageId.value shouldBe null
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

            viewModel.connections.test {
                val conns = awaitUntil { it.size == 3 }
                conns.map { it.id } shouldBe listOf(2L, 3L, 1L)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters connections by search query matching url`() = runTest {
            viewModel.connections.test {
                connectionsFlow.value = listOf(
                    makeConnection(id = 1, url = "wss://api.example.com/chat"),
                    makeConnection(id = 2, url = "wss://other.service.com/feed"),
                )
                viewModel.onConnectionSearchQueryChanged("example")

                val conns = awaitUntil { it.size == 1 }
                conns.first().id shouldBe 1L
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

            viewModel.connections.test {
                awaitUntil { it.size == 2 } shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `selectConnection` {

        @Test
        fun `sets selected connection and resets message filters`() = runTest {
            viewModel.onMessageSearchQueryChanged("something")
            viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)
            viewModel.toggleMessageExpanded(42)

            viewModel.selectConnection(1L)

            viewModel.messageSearchQuery.value shouldBe ""
            viewModel.directionFilter.value shouldBe null
            viewModel.expandedMessageId.value shouldBe null
        }

        @Test
        fun `selectedConnection emits the matching connection`() = runTest {
            connectionsFlow.value = listOf(
                makeConnection(id = 1, url = "wss://first.com"),
                makeConnection(id = 2, url = "wss://second.com"),
            )

            viewModel.selectConnection(2L)
            advanceUntilIdle()

            viewModel.selectedConnection.test {
                val conn = awaitItem()
                conn?.id shouldBe 2L
                conn?.url shouldBe "wss://second.com"
            }
        }
    }

    @Nested
    inner class `clearConnectionSelection` {

        @Test
        fun `clears all selection and message state`() = runTest {
            viewModel.selectConnection(1L)
            viewModel.onMessageSearchQueryChanged("query")
            viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)
            viewModel.toggleMessageExpanded(5)

            viewModel.clearConnectionSelection()

            viewModel.messageSearchQuery.value shouldBe ""
            viewModel.directionFilter.value shouldBe null
            viewModel.expandedMessageId.value shouldBe null
        }

        @Test
        fun `selectedConnection becomes null`() = runTest {
            viewModel.selectConnection(1L)
            viewModel.clearConnectionSelection()
            advanceUntilIdle()

            viewModel.selectedConnection.test {
                awaitItem() shouldBe null
            }
        }
    }

    @Nested
    inner class `messages` {

        @Test
        fun `emits empty when no connection selected`() = runTest {
            messagesFlow.value = listOf(makeMessage())
            advanceUntilIdle()

            viewModel.messages.test {
                awaitUntil { it.isEmpty() }.shouldBeEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters messages by selected connection`() = runTest {
            viewModel.messages.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L),
                    makeMessage(id = 2, connectionId = 2L),
                    makeMessage(id = 3, connectionId = 1L),
                )
                viewModel.selectConnection(1L)

                awaitUntil { it.size == 2 } shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters messages by search query in payload`() = runTest {
            viewModel.messages.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "Hello World"),
                    makeMessage(id = 2, connectionId = 1L, payload = "Goodbye"),
                )
                viewModel.selectConnection(1L)
                viewModel.onMessageSearchQueryChanged("Hello")

                val msgs = awaitUntil { it.size == 1 && it.first().payload == "Hello World" }
                msgs shouldHaveSize 1
                msgs.first().payload shouldBe "Hello World"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters messages by direction`() = runTest {
            viewModel.messages.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, direction = WebSocketMessageDirection.SENT),
                    makeMessage(id = 2, connectionId = 1L, direction = WebSocketMessageDirection.RECEIVED),
                )
                viewModel.selectConnection(1L)
                viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)

                val msgs = awaitUntil { it.size == 1 && it.first().direction == WebSocketMessageDirection.SENT }
                msgs shouldHaveSize 1
                msgs.first().direction shouldBe WebSocketMessageDirection.SENT
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `combines all filters`() = runTest {
            viewModel.messages.test {
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
                viewModel.selectConnection(1L)
                viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)
                viewModel.onMessageSearchQueryChanged("ping")

                val msgs = awaitUntil { it.size == 1 && it.first().id == 1L }
                msgs shouldHaveSize 1
                msgs.first().id shouldBe 1L
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `search is case insensitive`() = runTest {
            viewModel.messages.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "Hello World"),
                    makeMessage(id = 2, connectionId = 1L, payload = "Goodbye"),
                )
                viewModel.selectConnection(1L)
                viewModel.onMessageSearchQueryChanged("hello")

                val msgs = awaitUntil { it.size == 1 && it.first().payload == "Hello World" }
                msgs shouldHaveSize 1
                msgs.first().payload shouldBe "Hello World"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `reacts to new messages from engine`() = runTest {
            viewModel.messages.test {
                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "first"),
                )
                viewModel.selectConnection(1L)
                awaitUntil { it.size == 1 }

                messagesFlow.value = listOf(
                    makeMessage(id = 1, connectionId = 1L, payload = "first"),
                    makeMessage(id = 2, connectionId = 1L, payload = "second"),
                )
                awaitUntil { it.size == 2 } shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `toggleDirectionFilter` {

        @Test
        fun `sets direction when currently null`() {
            viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)

            viewModel.directionFilter.value shouldBe WebSocketMessageDirection.SENT
        }

        @Test
        fun `clears direction when toggling same value`() {
            viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)
            viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)

            viewModel.directionFilter.value shouldBe null
        }

        @Test
        fun `switches to different direction`() {
            viewModel.toggleDirectionFilter(WebSocketMessageDirection.SENT)
            viewModel.toggleDirectionFilter(WebSocketMessageDirection.RECEIVED)

            viewModel.directionFilter.value shouldBe WebSocketMessageDirection.RECEIVED
        }
    }

    @Nested
    inner class `toggleMessageExpanded` {

        @Test
        fun `expands when currently null`() {
            viewModel.toggleMessageExpanded(5L)

            viewModel.expandedMessageId.value shouldBe 5L
        }

        @Test
        fun `collapses when toggling same id`() {
            viewModel.toggleMessageExpanded(5L)
            viewModel.toggleMessageExpanded(5L)

            viewModel.expandedMessageId.value shouldBe null
        }

        @Test
        fun `switches to different id`() {
            viewModel.toggleMessageExpanded(5L)
            viewModel.toggleMessageExpanded(10L)

            viewModel.expandedMessageId.value shouldBe 10L
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
            viewModel.selectConnection(1L)
            advanceUntilIdle()

            viewModel.totalMessageCount.test {
                awaitItem() shouldBe 2
            }
        }

        @Test
        fun `is 0 when no connection selected`() = runTest {
            viewModel.totalMessageCount.test {
                awaitItem() shouldBe 0
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
            viewModel.selectConnection(1L)
            advanceUntilIdle()

            viewModel.directionCounts.test {
                val counts = awaitItem()
                counts shouldContainKey WebSocketMessageDirection.SENT
                counts[WebSocketMessageDirection.SENT] shouldBe 2
                counts[WebSocketMessageDirection.RECEIVED] shouldBe 1
            }
        }
    }

    @Nested
    inner class `totalConnectionCount` {

        @Test
        fun `reflects connection list size`() = runTest {
            connectionsFlow.value = listOf(makeConnection(id = 1), makeConnection(id = 2))

            viewModel.totalConnectionCount.test {
                awaitItem() shouldBe 2
            }
        }
    }

    @Nested
    inner class `clearAll` {

        @Test
        fun `delegates to engine and resets selection`() {
            viewModel.selectConnection(1L)
            viewModel.toggleMessageExpanded(5)

            viewModel.clearAll()

            verify { engine.clear() }
            viewModel.expandedMessageId.value shouldBe null
        }
    }

    @Nested
    inner class `clearCurrentConnectionMessages` {

        @Test
        fun `delegates to engine with connection id`() {
            viewModel.selectConnection(1L)

            viewModel.clearCurrentConnectionMessages()

            verify { engine.clearMessagesForConnection(1L) }
            viewModel.expandedMessageId.value shouldBe null
        }

        @Test
        fun `does nothing when no connection selected`() {
            viewModel.clearCurrentConnectionMessages()

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
}
