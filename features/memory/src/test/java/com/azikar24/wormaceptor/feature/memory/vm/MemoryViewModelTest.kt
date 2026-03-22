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
            viewModel.currentMemory.test {
                awaitItem() shouldBe MemoryInfo.empty()
            }
        }

        @Test
        fun `emits updated memory info`() = runTest {
            val updated = MemoryInfo.empty().copy(usedMemory = 1024, heapUsagePercent = 50f)

            viewModel.currentMemory.test {
                awaitItem()
                currentMemoryFlow.value = updated
                awaitItem() shouldBe updated
            }
        }
    }

    @Nested
    inner class `memoryHistory` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.memoryHistory.test {
                awaitItem().shouldBeEmpty()
            }
        }

        @Test
        fun `converts engine list to ImmutableList`() = runTest {
            val entries = listOf(
                MemoryInfo.empty().copy(usedMemory = 512),
                MemoryInfo.empty().copy(usedMemory = 1024),
            )
            viewModel.memoryHistory.test {
                awaitItem().shouldBeEmpty()
                memoryHistoryFlow.value = entries
                awaitItem() shouldHaveSize 2
            }
        }
    }

    @Nested
    inner class `isMonitoring` {

        @Test
        fun `reflects engine state when not monitoring`() = runTest {
            isMonitoringFlow.value = false
            viewModel.isMonitoring.test {
                awaitItem() shouldBe false
            }
        }

        @Test
        fun `reflects engine state when monitoring`() = runTest {
            isMonitoringFlow.value = true
            viewModel.isMonitoring.test {
                awaitItem() shouldBe true
            }
        }
    }

    @Nested
    inner class `isHeapWarning` {

        @Test
        fun `false when below threshold`() = runTest {
            currentMemoryFlow.value = MemoryInfo.empty().copy(heapUsagePercent = 50f)

            viewModel.isHeapWarning.test {
                awaitItem() shouldBe false
            }
        }

        @Test
        fun `true when at or above threshold`() = runTest {
            currentMemoryFlow.value = MemoryInfo.empty().copy(
                heapUsagePercent = MemoryMonitorEngine.HEAP_WARNING_THRESHOLD,
            )

            viewModel.isHeapWarning.test {
                awaitItem() shouldBe true
            }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `startMonitoring delegates to engine`() {
            viewModel.startMonitoring()
            verify { engine.start() }
        }

        @Test
        fun `stopMonitoring delegates to engine`() {
            viewModel.stopMonitoring()
            verify { engine.stop() }
        }

        @Test
        fun `forceGc delegates to engine`() {
            viewModel.forceGc()
            verify { engine.forceGc() }
        }

        @Test
        fun `clearHistory delegates to engine`() {
            viewModel.clearHistory()
            verify { engine.clearHistory() }
        }
    }
}
