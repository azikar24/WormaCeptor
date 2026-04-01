package com.azikar24.wormaceptor.feature.cpu.vm

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.domain.entities.CpuInfo
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
class CpuViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val currentCpuFlow = MutableStateFlow(CpuInfo.empty())
    private val cpuHistoryFlow = MutableStateFlow<List<CpuInfo>>(emptyList())
    private val isMonitoringFlow = MutableStateFlow(false)

    private val engine = mockk<CpuMonitorEngine>(relaxed = true) {
        every { currentCpu } returns currentCpuFlow
        every { cpuHistory } returns cpuHistoryFlow
        every { isMonitoring } returns isMonitoringFlow
    }

    private lateinit var viewModel: CpuViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CpuViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `currentCpu` {

        @Test
        fun `initial value is empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().currentCpu shouldBe CpuInfo.empty()
            }
        }

        @Test
        fun `emits updated cpu info`() = runTest {
            val updated = CpuInfo.empty().copy(overallUsagePercent = 45f)

            viewModel.uiState.test {
                awaitItem()
                currentCpuFlow.value = updated
                awaitItem().currentCpu shouldBe updated
            }
        }
    }

    @Nested
    inner class `cpuHistory` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.uiState.test {
                awaitItem().cpuHistory.shouldBeEmpty()
            }
        }

        @Test
        fun `converts engine list to ImmutableList`() = runTest {
            val entries = listOf(
                CpuInfo.empty().copy(overallUsagePercent = 10f),
                CpuInfo.empty().copy(overallUsagePercent = 20f),
            )
            viewModel.uiState.test {
                awaitItem().cpuHistory.shouldBeEmpty()
                cpuHistoryFlow.value = entries
                awaitItem().cpuHistory shouldHaveSize 2
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
    inner class `isCpuWarning` {

        @Test
        fun `false when below threshold`() = runTest {
            currentCpuFlow.value = CpuInfo.empty().copy(overallUsagePercent = 50f)

            viewModel.uiState.test {
                awaitItem().isCpuWarning shouldBe false
            }
        }

        @Test
        fun `true when at or above threshold`() = runTest {
            currentCpuFlow.value = CpuInfo.empty().copy(
                overallUsagePercent = CpuMonitorEngine.CPU_WARNING_THRESHOLD,
            )

            viewModel.uiState.test {
                awaitItem().isCpuWarning shouldBe true
            }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `startMonitoring delegates to engine`() {
            viewModel.sendEvent(CpuEvent.StartMonitoring)
            verify { engine.start() }
        }

        @Test
        fun `stopMonitoring delegates to engine`() {
            viewModel.sendEvent(CpuEvent.StopMonitoring)
            verify { engine.stop() }
        }

        @Test
        fun `clearHistory delegates to engine`() {
            viewModel.sendEvent(CpuEvent.ClearHistory)
            verify { engine.clearHistory() }
        }
    }
}
