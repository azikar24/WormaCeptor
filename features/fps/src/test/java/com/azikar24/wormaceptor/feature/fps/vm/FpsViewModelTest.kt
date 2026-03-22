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
            viewModel.currentFpsInfo.test {
                awaitItem() shouldBe FpsInfo.EMPTY
            }
        }

        @Test
        fun `emits updated fps info`() = runTest {
            val updated = FpsInfo.EMPTY.copy(currentFps = 60f, averageFps = 58f)

            viewModel.currentFpsInfo.test {
                awaitItem()
                currentFpsFlow.value = updated
                awaitItem() shouldBe updated
            }
        }
    }

    @Nested
    inner class `fpsHistory` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.fpsHistory.test {
                awaitItem().shouldBeEmpty()
            }
        }

        @Test
        fun `converts engine list to ImmutableList`() = runTest {
            val entries = listOf(
                FpsInfo.EMPTY.copy(currentFps = 30f),
                FpsInfo.EMPTY.copy(currentFps = 60f),
            )
            viewModel.fpsHistory.test {
                awaitItem().shouldBeEmpty()
                fpsHistoryFlow.value = entries
                awaitItem() shouldHaveSize 2
            }
        }
    }

    @Nested
    inner class `isMonitoring` {

        @Test
        fun `reflects engine state when not running`() = runTest {
            isRunningFlow.value = false
            viewModel.isMonitoring.test {
                awaitItem() shouldBe false
            }
        }

        @Test
        fun `reflects engine state when running`() = runTest {
            isRunningFlow.value = true
            viewModel.isMonitoring.test {
                awaitItem() shouldBe true
            }
        }
    }

    @Nested
    inner class `toggleMonitoring` {

        @Test
        fun `starts monitoring when not running`() {
            isRunningFlow.value = false

            viewModel.toggleMonitoring()

            verify { engine.start() }
        }

        @Test
        fun `stops monitoring when running`() {
            isRunningFlow.value = true

            viewModel.toggleMonitoring()

            verify { engine.stop() }
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
        fun `resetStats delegates to engine`() {
            viewModel.resetStats()
            verify { engine.reset() }
        }
    }
}
