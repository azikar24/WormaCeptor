package com.azikar24.wormaceptor.feature.threadviolation.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.domain.entities.ViolationStats
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
class ThreadViolationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val violationsFlow = MutableStateFlow<List<ThreadViolation>>(emptyList())
    private val statsFlow = MutableStateFlow(ViolationStats.empty())
    private val isMonitoringFlow = MutableStateFlow(false)

    private val engine = mockk<ThreadViolationEngine>(relaxed = true) {
        every { violations } returns violationsFlow
        every { stats } returns statsFlow
        every { isMonitoring } returns isMonitoringFlow
    }

    private lateinit var viewModel: ThreadViolationViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ThreadViolationViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViolation(
        id: Long = 1L,
        violationType: ViolationType = ViolationType.DISK_READ,
        description: String = "Disk read on main thread",
    ) = ThreadViolation(
        id = id,
        timestamp = System.currentTimeMillis(),
        violationType = violationType,
        description = description,
        stackTrace = listOf("com.example.MyClass.doWork(MyClass.kt:42)"),
        durationMs = 50L,
        threadName = "main",
    )

    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `selectedType is null`() = runTest {
            viewModel.uiState.value.selectedType shouldBe null
        }

        @Test
        fun `selectedViolation is null`() = runTest {
            viewModel.uiState.value.selectedViolation shouldBe null
        }

        @Test
        fun `filteredViolations is empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().filteredViolations.shouldBeEmpty()
            }
        }
    }

    @Nested
    inner class `ToggleMonitoring event` {

        @Test
        fun `enables monitoring when currently disabled`() {
            isMonitoringFlow.value = false

            viewModel.sendEvent(ThreadViolationViewEvent.ToggleMonitoring)

            verify { engine.enable() }
        }

        @Test
        fun `disables monitoring when currently enabled`() {
            isMonitoringFlow.value = true

            viewModel.sendEvent(ThreadViolationViewEvent.ToggleMonitoring)

            verify { engine.disable() }
        }
    }

    @Nested
    inner class `SelectType event` {

        @Test
        fun `updates selected type`() = runTest {
            viewModel.sendEvent(ThreadViolationViewEvent.SelectType(ViolationType.NETWORK))

            viewModel.uiState.value.selectedType shouldBe ViolationType.NETWORK
        }

        @Test
        fun `setting null clears the type filter`() = runTest {
            viewModel.sendEvent(ThreadViolationViewEvent.SelectType(ViolationType.DISK_READ))
            viewModel.sendEvent(ThreadViolationViewEvent.SelectType(null))

            viewModel.uiState.value.selectedType shouldBe null
        }

        @Test
        fun `filters violations by type`() = runTest {
            val diskRead = makeViolation(id = 1, violationType = ViolationType.DISK_READ)
            val network = makeViolation(id = 2, violationType = ViolationType.NETWORK)
            violationsFlow.value = listOf(diskRead, network)
            viewModel.sendEvent(ThreadViolationViewEvent.SelectType(ViolationType.DISK_READ))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredViolations.size == 1 &&
                        it.filteredViolations.first().violationType == ViolationType.DISK_READ
                }
                state.filteredViolations shouldHaveSize 1
                state.filteredViolations.first().violationType shouldBe ViolationType.DISK_READ
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `null type shows all violations`() = runTest {
            val diskRead = makeViolation(id = 1, violationType = ViolationType.DISK_READ)
            val network = makeViolation(id = 2, violationType = ViolationType.NETWORK)
            val diskWrite = makeViolation(id = 3, violationType = ViolationType.DISK_WRITE)
            violationsFlow.value = listOf(diskRead, network, diskWrite)

            viewModel.uiState.test {
                val state = awaitUntil { it.filteredViolations.size == 3 }
                state.filteredViolations shouldHaveSize 3
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SelectViolation and DismissDetail events` {

        @Test
        fun `SelectViolation sets the selected violation`() = runTest {
            val violation = makeViolation()

            viewModel.sendEvent(ThreadViolationViewEvent.SelectViolation(violation))

            viewModel.uiState.value.selectedViolation shouldBe violation
        }

        @Test
        fun `DismissDetail clears the selected violation`() = runTest {
            viewModel.sendEvent(ThreadViolationViewEvent.SelectViolation(makeViolation()))
            viewModel.sendEvent(ThreadViolationViewEvent.DismissDetail)

            viewModel.uiState.value.selectedViolation shouldBe null
        }
    }

    @Nested
    inner class `ClearViolations event` {

        @Test
        fun `delegates to engine`() {
            viewModel.sendEvent(ThreadViolationViewEvent.ClearViolations)

            verify { engine.clearViolations() }
        }
    }

    @Nested
    inner class `engine state delegation` {

        @Test
        fun `isMonitoring reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().isMonitoring shouldBe false
                isMonitoringFlow.value = true
                awaitUntil { it.isMonitoring }.isMonitoring shouldBe true
            }
        }

        @Test
        fun `stats reflects engine state`() = runTest {
            val updatedStats = ViolationStats(
                totalViolations = 5,
                diskReadCount = 2,
                diskWriteCount = 1,
                networkCount = 1,
                slowCallCount = 1,
                customSlowCodeCount = 0,
            )

            viewModel.uiState.test {
                awaitItem().stats shouldBe ViolationStats.empty()
                statsFlow.value = updatedStats
                awaitUntil { it.stats == updatedStats }.stats shouldBe updatedStats
            }
        }
    }
}
