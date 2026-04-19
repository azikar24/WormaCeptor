package com.azikar24.wormaceptor.feature.fps.vm

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.domain.entities.FpsInfo
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
class FpsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val currentFpsFlow = MutableStateFlow(FpsInfo.EMPTY)
    private val fpsHistoryFlow = MutableStateFlow<List<FpsInfo>>(emptyList())
    private val isRunningFlow = MutableStateFlow(false)

    private val engine = mockk<FpsMonitorEngine>(relaxed = true) {
        every { currentFpsInfo } returns currentFpsFlow
        every { fpsHistory } returns fpsHistoryFlow
        every { isRunning } returns isRunningFlow
    }

    private lateinit var viewModel: FpsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FpsViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `currentFpsInfo` {

        @Test
        fun `initial value is EMPTY`() = runTest {
            viewModel.uiState.test {
                awaitItem().currentFpsInfo shouldBe FpsInfo.EMPTY
            }
        }

        @Test
        fun `emits updated fps info`() = runTest {
            val updated = FpsInfo.EMPTY.copy(currentFps = 60f, averageFps = 58f)

            viewModel.uiState.test {
                awaitItem()
                currentFpsFlow.value = updated
                awaitItem().currentFpsInfo shouldBe updated
            }
        }
    }

    @Nested
    inner class `fpsHistory` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.uiState.test {
                awaitItem().fpsHistory.shouldBeEmpty()
            }
        }

        @Test
        fun `converts engine list to ImmutableList`() = runTest {
            val entries = listOf(
                FpsInfo.EMPTY.copy(currentFps = 30f),
                FpsInfo.EMPTY.copy(currentFps = 60f),
            )
            viewModel.uiState.test {
                awaitItem().fpsHistory.shouldBeEmpty()
                fpsHistoryFlow.value = entries
                awaitItem().fpsHistory shouldHaveSize 2
            }
        }
    }

    @Nested
    inner class `isMonitoring` {

        @Test
        fun `reflects engine state when not running`() = runTest {
            isRunningFlow.value = false
            viewModel.uiState.test {
                awaitItem().isMonitoring shouldBe false
            }
        }

        @Test
        fun `reflects engine state when running`() = runTest {
            isRunningFlow.value = true
            viewModel.uiState.test {
                awaitItem().isMonitoring shouldBe true
            }
        }
    }

    @Nested
    inner class `ToggleMonitoring` {

        @Test
        fun `starts monitoring when not running`() {
            isRunningFlow.value = false

            viewModel.sendEvent(FpsViewEvent.ToggleMonitoring)

            verify { engine.start() }
        }

        @Test
        fun `stops monitoring when running`() {
            isRunningFlow.value = true

            viewModel.sendEvent(FpsViewEvent.ToggleMonitoring)

            verify { engine.stop() }
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `StartMonitoring delegates to engine`() {
            viewModel.sendEvent(FpsViewEvent.StartMonitoring)
            verify { engine.start() }
        }

        @Test
        fun `StopMonitoring delegates to engine`() {
            viewModel.sendEvent(FpsViewEvent.StopMonitoring)
            verify { engine.stop() }
        }

        @Test
        fun `ResetStats delegates to engine`() {
            viewModel.sendEvent(FpsViewEvent.ResetStats)
            verify { engine.reset() }
        }
    }
}
