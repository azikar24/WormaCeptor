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
            viewModel.searchQuery.value shouldBe ""
        }

        @Test
        fun `minimum level is VERBOSE`() = runTest {
            viewModel.minimumLevel.value shouldBe LogLevel.VERBOSE
        }

        @Test
        fun `all levels are selected`() = runTest {
            viewModel.selectedLevels.value shouldBe LogLevel.entries.toSet()
        }

        @Test
        fun `auto scroll is enabled`() = runTest {
            viewModel.autoScroll.value shouldBe true
        }

        @Test
        fun `current pid is from engine`() {
            viewModel.currentPid shouldBe 1234
        }
    }

    @Nested
    inner class `setMinimumLevel` {

        @Test
        fun `updates minimum level`() = runTest {
            viewModel.setMinimumLevel(LogLevel.WARN)

            viewModel.minimumLevel.value shouldBe LogLevel.WARN
        }

        @Test
        fun `cascades to selected levels including WARN and above`() = runTest {
            viewModel.setMinimumLevel(LogLevel.WARN)

            viewModel.selectedLevels.value shouldBe setOf(LogLevel.WARN, LogLevel.ERROR, LogLevel.ASSERT)
        }

        @Test
        fun `setting VERBOSE selects all levels`() = runTest {
            viewModel.setMinimumLevel(LogLevel.ERROR)
            viewModel.setMinimumLevel(LogLevel.VERBOSE)

            viewModel.selectedLevels.value shouldBe LogLevel.entries.toSet()
        }
    }

    @Nested
    inner class `toggleLevel` {

        @Test
        fun `removes a selected level`() = runTest {
            viewModel.toggleLevel(LogLevel.DEBUG)

            viewModel.selectedLevels.value shouldBe LogLevel.entries.toSet() - LogLevel.DEBUG
        }

        @Test
        fun `adds back a deselected level`() = runTest {
            viewModel.toggleLevel(LogLevel.DEBUG)
            viewModel.toggleLevel(LogLevel.DEBUG)

            viewModel.selectedLevels.value shouldBe LogLevel.entries.toSet()
        }
    }

    @Nested
    inner class `selectAllLevels` {

        @Test
        fun `restores all levels after partial deselection`() = runTest {
            viewModel.toggleLevel(LogLevel.DEBUG)
            viewModel.toggleLevel(LogLevel.INFO)

            viewModel.selectAllLevels()

            viewModel.selectedLevels.value shouldBe LogLevel.entries.toSet()
        }
    }

    @Nested
    inner class `clearLevelSelection` {

        @Test
        fun `removes all selected levels`() = runTest {
            viewModel.clearLevelSelection()

            viewModel.selectedLevels.value.shouldBeEmpty()
        }
    }

    @Nested
    inner class `toggleAutoScroll` {

        @Test
        fun `toggles from true to false`() = runTest {
            viewModel.toggleAutoScroll()

            viewModel.autoScroll.value shouldBe false
        }

        @Test
        fun `toggles from false to true`() = runTest {
            viewModel.toggleAutoScroll()
            viewModel.toggleAutoScroll()

            viewModel.autoScroll.value shouldBe true
        }
    }

    @Nested
    inner class `setAutoScroll` {

        @Test
        fun `sets auto scroll to specified value`() = runTest {
            viewModel.setAutoScroll(false)
            viewModel.autoScroll.value shouldBe false

            viewModel.setAutoScroll(true)
            viewModel.autoScroll.value shouldBe true
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

            viewModel.logs.test {
                skipItems(1) // initial empty

                logsFlow.value = listOf(debugLog, errorLog)
                viewModel.setMinimumLevel(LogLevel.ERROR)

                val items = awaitUntil { it.size == 1 && it.first().level == LogLevel.ERROR }
                items shouldHaveSize 1
                items.first().level shouldBe LogLevel.ERROR
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by search query matching tag`() = runTest {
            val matchingLog = makeLogEntry(id = 1, tag = "NetworkTag", message = "irrelevant")
            val nonMatchingLog = makeLogEntry(id = 2, tag = "OtherTag", message = "irrelevant")

            viewModel.logs.test {
                skipItems(1)

                logsFlow.value = listOf(matchingLog, nonMatchingLog)
                viewModel.onSearchQueryChanged("Network")

                val items = awaitUntil { it.size == 1 && it.first().tag == "NetworkTag" }
                items shouldHaveSize 1
                items.first().tag shouldBe "NetworkTag"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by search query matching message`() = runTest {
            val matchingLog = makeLogEntry(id = 1, tag = "Tag", message = "Connection established")
            val nonMatchingLog = makeLogEntry(id = 2, tag = "Tag", message = "Something else")

            viewModel.logs.test {
                skipItems(1)

                logsFlow.value = listOf(matchingLog, nonMatchingLog)
                viewModel.onSearchQueryChanged("Connection")

                val items = awaitUntil { it.size == 1 && it.first().message == "Connection established" }
                items shouldHaveSize 1
                items.first().message shouldBe "Connection established"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `combines search and level filter`() = runTest {
            val debugMatch = makeLogEntry(id = 1, level = LogLevel.DEBUG, tag = "NetworkTag")
            val errorMatch = makeLogEntry(id = 2, level = LogLevel.ERROR, tag = "NetworkTag")
            val errorNoMatch = makeLogEntry(id = 3, level = LogLevel.ERROR, tag = "OtherTag")

            viewModel.logs.test {
                skipItems(1)

                logsFlow.value = listOf(debugMatch, errorMatch, errorNoMatch)
                viewModel.setMinimumLevel(LogLevel.ERROR)
                viewModel.onSearchQueryChanged("Network")

                val items = awaitUntil { it.size == 1 && it.first().id == 2L }
                items shouldHaveSize 1
                items.first().id shouldBe 2L
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `totalCount` {

        @Test
        fun `reflects raw logs size`() = runTest {
            logsFlow.value = listOf(makeLogEntry(id = 1), makeLogEntry(id = 2), makeLogEntry(id = 3))

            viewModel.totalCount.test {
                awaitItem() shouldBe 3
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

            viewModel.levelCounts.test {
                val counts = awaitItem()
                counts[LogLevel.DEBUG] shouldBe 2
                counts[LogLevel.ERROR] shouldBe 1
            }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `startCapture delegates to engine`() {
            viewModel.startCapture()
            verify { engine.start() }
        }

        @Test
        fun `stopCapture delegates to engine`() {
            viewModel.stopCapture()
            verify { engine.stop() }
        }

        @Test
        fun `clearLogs delegates to engine`() {
            viewModel.clearLogs()
            verify { engine.clear() }
        }
    }

    @Nested
    inner class `clearFilters` {

        @Test
        fun `resets search query and level selection`() = runTest {
            viewModel.onSearchQueryChanged("something")
            viewModel.setMinimumLevel(LogLevel.ERROR)

            viewModel.clearFilters()

            viewModel.searchQuery.value shouldBe ""
            viewModel.selectedLevels.value shouldBe LogLevel.entries.toSet()
        }
    }
}
