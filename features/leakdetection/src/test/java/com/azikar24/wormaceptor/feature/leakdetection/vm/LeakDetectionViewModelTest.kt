package com.azikar24.wormaceptor.feature.leakdetection.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import com.azikar24.wormaceptor.domain.entities.LeakSummary
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
class LeakDetectionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val detectedLeaksFlow = MutableStateFlow<List<LeakInfo>>(emptyList())
    private val leakSummaryFlow = MutableStateFlow(LeakSummary.empty())
    private val isRunningFlow = MutableStateFlow(false)

    private val engine = mockk<LeakDetectionEngine>(relaxed = true) {
        every { detectedLeaks } returns detectedLeaksFlow
        every { leakSummary } returns leakSummaryFlow
        every { isRunning } returns isRunningFlow
    }

    private lateinit var viewModel: LeakDetectionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LeakDetectionViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeLeak(
        objectClass: String = "com.example.MyActivity",
        severity: LeakSeverity = LeakSeverity.HIGH,
        retainedSize: Long = 2_097_152L,
    ) = LeakInfo(
        timestamp = System.currentTimeMillis(),
        objectClass = objectClass,
        leakDescription = "Activity destroyed: $objectClass",
        retainedSize = retainedSize,
        referencePath = listOf("GC Root", "Static reference", "-> $objectClass"),
        severity = severity,
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
        fun `selectedSeverity is null`() = runTest {
            viewModel.uiState.value.selectedSeverity shouldBe null
        }

        @Test
        fun `selectedLeak is null`() = runTest {
            viewModel.uiState.value.selectedLeak shouldBe null
        }

        @Test
        fun `filteredLeaks is empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().filteredLeaks.shouldBeEmpty()
            }
        }
    }

    @Nested
    inner class `SelectSeverity event` {

        @Test
        fun `updates selected severity`() = runTest {
            viewModel.sendEvent(LeakDetectionViewEvent.SelectSeverity(LeakSeverity.CRITICAL))

            viewModel.uiState.value.selectedSeverity shouldBe LeakSeverity.CRITICAL
        }

        @Test
        fun `setting null clears the severity filter`() = runTest {
            viewModel.sendEvent(LeakDetectionViewEvent.SelectSeverity(LeakSeverity.HIGH))
            viewModel.sendEvent(LeakDetectionViewEvent.SelectSeverity(null))

            viewModel.uiState.value.selectedSeverity shouldBe null
        }

        @Test
        fun `filters leaks by severity`() = runTest {
            val highLeak = makeLeak(
                objectClass = "com.example.HighActivity",
                severity = LeakSeverity.HIGH,
            )
            val lowLeak = makeLeak(
                objectClass = "com.example.LowFragment",
                severity = LeakSeverity.LOW,
                retainedSize = 1024L,
            )
            detectedLeaksFlow.value = listOf(highLeak, lowLeak)
            viewModel.sendEvent(LeakDetectionViewEvent.SelectSeverity(LeakSeverity.HIGH))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredLeaks.size == 1 && it.filteredLeaks.first().severity == LeakSeverity.HIGH
                }
                state.filteredLeaks shouldHaveSize 1
                state.filteredLeaks.first().severity shouldBe LeakSeverity.HIGH
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `null severity shows all leaks`() = runTest {
            val highLeak = makeLeak(severity = LeakSeverity.HIGH)
            val lowLeak = makeLeak(
                objectClass = "com.example.LowFragment",
                severity = LeakSeverity.LOW,
                retainedSize = 1024L,
            )
            val criticalLeak = makeLeak(
                objectClass = "com.example.CriticalActivity",
                severity = LeakSeverity.CRITICAL,
            )
            detectedLeaksFlow.value = listOf(highLeak, lowLeak, criticalLeak)

            viewModel.uiState.test {
                val state = awaitUntil { it.filteredLeaks.size == 3 }
                state.filteredLeaks shouldHaveSize 3
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SelectLeak and DismissDetail events` {

        @Test
        fun `SelectLeak sets the selected leak`() = runTest {
            val leak = makeLeak()

            viewModel.sendEvent(LeakDetectionViewEvent.SelectLeak(leak))

            viewModel.uiState.value.selectedLeak shouldBe leak
        }

        @Test
        fun `DismissDetail clears the selected leak`() = runTest {
            viewModel.sendEvent(LeakDetectionViewEvent.SelectLeak(makeLeak()))
            viewModel.sendEvent(LeakDetectionViewEvent.DismissDetail)

            viewModel.uiState.value.selectedLeak shouldBe null
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `TriggerCheck delegates to engine`() {
            viewModel.sendEvent(LeakDetectionViewEvent.TriggerCheck)

            verify { engine.triggerCheck() }
        }

        @Test
        fun `ClearLeaks delegates to engine`() {
            viewModel.sendEvent(LeakDetectionViewEvent.ClearLeaks)

            verify { engine.clearLeaks() }
        }

        @Test
        fun `isRunning reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().isRunning shouldBe false
                isRunningFlow.value = true
                awaitUntil { it.isRunning }.isRunning shouldBe true
            }
        }

        @Test
        fun `summary reflects engine state`() = runTest {
            val updatedSummary = LeakSummary(
                totalLeaks = 3,
                criticalCount = 1,
                highCount = 1,
                mediumCount = 0,
                lowCount = 1,
                totalRetainedBytes = 5_000_000L,
            )

            viewModel.uiState.test {
                awaitItem().summary shouldBe LeakSummary.empty()
                leakSummaryFlow.value = updatedSummary
                awaitUntil { it.summary == updatedSummary }.summary shouldBe updatedSummary
            }
        }
    }
}
