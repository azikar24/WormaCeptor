package com.azikar24.wormaceptor.feature.logs.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    private val isCapturingFlow = MutableStateFlow(false)
    private val engine = mockk<LogCaptureEngine>(relaxed = true) {
        every { logs } returns logsFlow
        every { isCapturing } returns isCapturingFlow
        every { getCurrentPid() } returns 1234
    }

    private lateinit var viewModel: LogsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LogsViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeLogEntry(
        id: Long = 1L,
        level: LogLevel = LogLevel.DEBUG,
        tag: String = "TestTag",
        message: String = "Test message",
    ) = LogEntry(
        id = id,
        timestamp = System.currentTimeMillis(),
        level = level,
        tag = tag,
        pid = 1234,
        tid = 5678,
        message = message,
    )

    @Nested
    inner class `initial state` {

        @Test
        fun `search query is empty`() = runTest {
            viewModel.uiState.value.searchQuery shouldBe ""
        }

        @Test
        fun `minimum level is VERBOSE`() = runTest {
            viewModel.uiState.value.minimumLevel shouldBe LogLevel.VERBOSE
        }

        @Test
        fun `all levels are selected`() = runTest {
            viewModel.uiState.value.selectedLevels shouldBe LogLevel.entries.toSet()
        }

        @Test
        fun `auto scroll is enabled`() = runTest {
            viewModel.uiState.value.autoScroll shouldBe true
        }

        @Test
        fun `current pid is from engine`() {
            viewModel.uiState.value.currentPid shouldBe 1234
        }
    }

    @Nested
    inner class `setMinimumLevel` {

        @Test
        fun `updates minimum level`() = runTest {
            viewModel.sendEvent(LogsViewEvent.MinimumLevelSet(LogLevel.WARN))

            viewModel.uiState.value.minimumLevel shouldBe LogLevel.WARN
        }

        @Test
        fun `cascades to selected levels including WARN and above`() = runTest {
            viewModel.sendEvent(LogsViewEvent.MinimumLevelSet(LogLevel.WARN))

            viewModel.uiState.value.selectedLevels shouldBe setOf(LogLevel.WARN, LogLevel.ERROR, LogLevel.ASSERT)
        }

        @Test
        fun `setting VERBOSE selects all levels`() = runTest {
            viewModel.sendEvent(LogsViewEvent.MinimumLevelSet(LogLevel.ERROR))
            viewModel.sendEvent(LogsViewEvent.MinimumLevelSet(LogLevel.VERBOSE))

            viewModel.uiState.value.selectedLevels shouldBe LogLevel.entries.toSet()
        }
    }

    @Nested
    inner class `toggleLevel` {

        @Test
        fun `removes a selected level`() = runTest {
            viewModel.sendEvent(LogsViewEvent.LevelToggled(LogLevel.DEBUG))

            viewModel.uiState.value.selectedLevels shouldBe LogLevel.entries.toSet() - LogLevel.DEBUG
        }

        @Test
        fun `adds back a deselected level`() = runTest {
            viewModel.sendEvent(LogsViewEvent.LevelToggled(LogLevel.DEBUG))
            viewModel.sendEvent(LogsViewEvent.LevelToggled(LogLevel.DEBUG))

            viewModel.uiState.value.selectedLevels shouldBe LogLevel.entries.toSet()
        }
    }

    @Nested
    inner class `selectAllLevels` {

        @Test
        fun `restores all levels after partial deselection`() = runTest {
            viewModel.sendEvent(LogsViewEvent.LevelToggled(LogLevel.DEBUG))
            viewModel.sendEvent(LogsViewEvent.LevelToggled(LogLevel.INFO))

            viewModel.sendEvent(LogsViewEvent.AllLevelsSelected)

            viewModel.uiState.value.selectedLevels shouldBe LogLevel.entries.toSet()
        }
    }

    @Nested
    inner class `clearLevelSelection` {

        @Test
        fun `removes all selected levels`() = runTest {
            viewModel.sendEvent(LogsViewEvent.LevelSelectionCleared)

            viewModel.uiState.value.selectedLevels.shouldBeEmpty()
        }
    }

    @Nested
    inner class `toggleAutoScroll` {

        @Test
        fun `toggles from true to false`() = runTest {
            viewModel.sendEvent(LogsViewEvent.AutoScrollToggled)

            viewModel.uiState.value.autoScroll shouldBe false
        }

        @Test
        fun `toggles from false to true`() = runTest {
            viewModel.sendEvent(LogsViewEvent.AutoScrollToggled)
            viewModel.sendEvent(LogsViewEvent.AutoScrollToggled)

            viewModel.uiState.value.autoScroll shouldBe true
        }
    }

    @Nested
    inner class `setAutoScroll` {

        @Test
        fun `sets auto scroll to specified value`() = runTest {
            viewModel.sendEvent(LogsViewEvent.AutoScrollSet(false))
            viewModel.uiState.value.autoScroll shouldBe false

            viewModel.sendEvent(LogsViewEvent.AutoScrollSet(true))
            viewModel.uiState.value.autoScroll shouldBe true
        }
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
    inner class `logs filtering` {

        @Test
        fun `filters by selected levels`() = runTest {
            val debugLog = makeLogEntry(id = 1, level = LogLevel.DEBUG)
            val errorLog = makeLogEntry(id = 2, level = LogLevel.ERROR)

            viewModel.uiState.test {
                skipItems(1) // initial state

                logsFlow.value = listOf(debugLog, errorLog)
                viewModel.sendEvent(LogsViewEvent.MinimumLevelSet(LogLevel.ERROR))

                val state = awaitUntil { it.logs.size == 1 && it.logs.first().level == LogLevel.ERROR }
                state.logs shouldHaveSize 1
                state.logs.first().level shouldBe LogLevel.ERROR
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by search query matching tag`() = runTest {
            val matchingLog = makeLogEntry(id = 1, tag = "NetworkTag", message = "irrelevant")
            val nonMatchingLog = makeLogEntry(id = 2, tag = "OtherTag", message = "irrelevant")

            viewModel.uiState.test {
                skipItems(1)

                logsFlow.value = listOf(matchingLog, nonMatchingLog)
                viewModel.sendEvent(LogsViewEvent.SearchQueryChanged("Network"))

                val state = awaitUntil { it.logs.size == 1 && it.logs.first().tag == "NetworkTag" }
                state.logs shouldHaveSize 1
                state.logs.first().tag shouldBe "NetworkTag"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by search query matching message`() = runTest {
            val matchingLog = makeLogEntry(id = 1, tag = "Tag", message = "Connection established")
            val nonMatchingLog = makeLogEntry(id = 2, tag = "Tag", message = "Something else")

            viewModel.uiState.test {
                skipItems(1)

                logsFlow.value = listOf(matchingLog, nonMatchingLog)
                viewModel.sendEvent(LogsViewEvent.SearchQueryChanged("Connection"))

                val state = awaitUntil { it.logs.size == 1 && it.logs.first().message == "Connection established" }
                state.logs shouldHaveSize 1
                state.logs.first().message shouldBe "Connection established"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `combines search and level filter`() = runTest {
            val debugMatch = makeLogEntry(id = 1, level = LogLevel.DEBUG, tag = "NetworkTag")
            val errorMatch = makeLogEntry(id = 2, level = LogLevel.ERROR, tag = "NetworkTag")
            val errorNoMatch = makeLogEntry(id = 3, level = LogLevel.ERROR, tag = "OtherTag")

            viewModel.uiState.test {
                skipItems(1)

                logsFlow.value = listOf(debugMatch, errorMatch, errorNoMatch)
                viewModel.sendEvent(LogsViewEvent.MinimumLevelSet(LogLevel.ERROR))
                viewModel.sendEvent(LogsViewEvent.SearchQueryChanged("Network"))

                val state = awaitUntil { it.logs.size == 1 && it.logs.first().id == 2L }
                state.logs shouldHaveSize 1
                state.logs.first().id shouldBe 2L
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `totalCount` {

        @Test
        fun `reflects raw logs size`() = runTest {
            logsFlow.value = listOf(makeLogEntry(id = 1), makeLogEntry(id = 2), makeLogEntry(id = 3))

            viewModel.uiState.test {
                awaitUntil { it.totalCount == 3 }.totalCount shouldBe 3
            }
        }
    }

    @Nested
    inner class `levelCounts` {

        @Test
        fun `aggregates counts by level`() = runTest {
            logsFlow.value = listOf(
                makeLogEntry(id = 1, level = LogLevel.DEBUG),
                makeLogEntry(id = 2, level = LogLevel.DEBUG),
                makeLogEntry(id = 3, level = LogLevel.ERROR),
            )

            viewModel.uiState.test {
                val state = awaitUntil { it.levelCounts.isNotEmpty() }
                state.levelCounts[LogLevel.DEBUG] shouldBe 2
                state.levelCounts[LogLevel.ERROR] shouldBe 1
            }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `startCapture delegates to engine`() {
            viewModel.sendEvent(LogsViewEvent.CaptureStarted)
            verify { engine.start() }
        }

        @Test
        fun `stopCapture delegates to engine`() {
            viewModel.sendEvent(LogsViewEvent.CaptureStopped)
            verify { engine.stop() }
        }

        @Test
        fun `clearLogs delegates to engine`() {
            viewModel.sendEvent(LogsViewEvent.LogsCleared)
            verify { engine.clear() }
        }
    }

    @Nested
    inner class `clearFilters` {

        @Test
        fun `resets search query and level selection`() = runTest {
            viewModel.sendEvent(LogsViewEvent.SearchQueryChanged("something"))
            viewModel.sendEvent(LogsViewEvent.MinimumLevelSet(LogLevel.ERROR))

            viewModel.sendEvent(LogsViewEvent.FiltersCleared)

            viewModel.uiState.value.searchQuery shouldBe ""
            viewModel.uiState.value.selectedLevels shouldBe LogLevel.entries.toSet()
        }
    }
}
