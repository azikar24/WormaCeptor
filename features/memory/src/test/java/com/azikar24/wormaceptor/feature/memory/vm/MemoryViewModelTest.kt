package com.azikar24.wormaceptor.feature.memory.vm

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
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
class MemoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val currentMemoryFlow = MutableStateFlow(MemoryInfo.empty())
    private val memoryHistoryFlow = MutableStateFlow<List<MemoryInfo>>(emptyList())
    private val isMonitoringFlow = MutableStateFlow(false)

    private val engine = mockk<MemoryMonitorEngine>(relaxed = true) {
        every { currentMemory } returns currentMemoryFlow
        every { memoryHistory } returns memoryHistoryFlow
        every { isMonitoring } returns isMonitoringFlow
    }

    private lateinit var viewModel: MemoryViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MemoryViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `currentMemory` {

        @Test
        fun `initial value is empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().currentMemory shouldBe MemoryInfo.empty()
            }
        }

        @Test
        fun `emits updated memory info`() = runTest {
            val updated = MemoryInfo.empty().copy(usedMemory = 1024, heapUsagePercent = 50f)

            viewModel.uiState.test {
                awaitItem()
                currentMemoryFlow.value = updated
                awaitItem().currentMemory shouldBe updated
            }
        }
    }

    @Nested
    inner class `memoryHistory` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.uiState.test {
                awaitItem().memoryHistory.shouldBeEmpty()
            }
        }

        @Test
        fun `converts engine list to ImmutableList`() = runTest {
            val entries = listOf(
                MemoryInfo.empty().copy(usedMemory = 512),
                MemoryInfo.empty().copy(usedMemory = 1024),
            )
            viewModel.uiState.test {
                awaitItem().memoryHistory.shouldBeEmpty()
                memoryHistoryFlow.value = entries
                awaitItem().memoryHistory shouldHaveSize 2
            }
        }
    }

    @Nested
    inner class `isMonitoring` {

        @Test
        fun `reflects engine state when not monitoring`() = runTest {
            isMonitoringFlow.value = false
            viewModel.uiState.test {
                awaitItem().isMonitoring shouldBe false
            }
        }

        @Test
        fun `reflects engine state when monitoring`() = runTest {
            isMonitoringFlow.value = true
            viewModel.uiState.test {
                awaitItem().isMonitoring shouldBe true
            }
        }
    }

    @Nested
    inner class `isHeapWarning` {

        @Test
        fun `false when below threshold`() = runTest {
            currentMemoryFlow.value = MemoryInfo.empty().copy(heapUsagePercent = 50f)

            viewModel.uiState.test {
                awaitItem().isHeapWarning shouldBe false
            }
        }

        @Test
        fun `true when at or above threshold`() = runTest {
            currentMemoryFlow.value = MemoryInfo.empty().copy(
                heapUsagePercent = MemoryMonitorEngine.HEAP_WARNING_THRESHOLD,
            )

            viewModel.uiState.test {
                awaitItem().isHeapWarning shouldBe true
            }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `StartMonitoring delegates to engine`() {
            viewModel.sendEvent(MemoryViewEvent.StartMonitoring)
            verify { engine.start() }
        }

        @Test
        fun `StopMonitoring delegates to engine`() {
            viewModel.sendEvent(MemoryViewEvent.StopMonitoring)
            verify { engine.stop() }
        }

        @Test
        fun `ForceGc delegates to engine`() {
            viewModel.sendEvent(MemoryViewEvent.ForceGc)
            verify { engine.forceGc() }
        }

        @Test
        fun `ClearHistory delegates to engine`() {
            viewModel.sendEvent(MemoryViewEvent.ClearHistory)
            verify { engine.clearHistory() }
        }
    }
}
