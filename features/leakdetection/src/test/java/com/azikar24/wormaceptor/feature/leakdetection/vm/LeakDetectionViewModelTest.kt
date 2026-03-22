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
            viewModel.selectedSeverity.value shouldBe null
        }

        @Test
        fun `selectedLeak is null`() = runTest {
            viewModel.selectedLeak.value shouldBe null
        }

        @Test
        fun `filteredLeaks is empty`() = runTest {
            viewModel.filteredLeaks.test {
                awaitItem().shouldBeEmpty()
            }
        }
    }

    @Nested
    inner class `setSelectedSeverity` {

        @Test
        fun `updates selected severity`() = runTest {
            viewModel.setSelectedSeverity(LeakSeverity.CRITICAL)

            viewModel.selectedSeverity.value shouldBe LeakSeverity.CRITICAL
        }

        @Test
        fun `setting null clears the severity filter`() = runTest {
            viewModel.setSelectedSeverity(LeakSeverity.HIGH)
            viewModel.setSelectedSeverity(null)

            viewModel.selectedSeverity.value shouldBe null
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
            viewModel.setSelectedSeverity(LeakSeverity.HIGH)

            viewModel.filteredLeaks.test {
                val items = awaitUntil {
                    it.size == 1 && it.first().severity == LeakSeverity.HIGH
                }
                items shouldHaveSize 1
                items.first().severity shouldBe LeakSeverity.HIGH
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

            viewModel.filteredLeaks.test {
                val items = awaitUntil { it.size == 3 }
                items shouldHaveSize 3
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `selectLeak and dismissDetail` {

        @Test
        fun `selectLeak sets the selected leak`() = runTest {
            val leak = makeLeak()

            viewModel.selectLeak(leak)

            viewModel.selectedLeak.value shouldBe leak
        }

        @Test
        fun `dismissDetail clears the selected leak`() = runTest {
            viewModel.selectLeak(makeLeak())
            viewModel.dismissDetail()

            viewModel.selectedLeak.value shouldBe null
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `triggerCheck delegates to engine`() {
            viewModel.triggerCheck()

            verify { engine.triggerCheck() }
        }

        @Test
        fun `clearLeaks delegates to engine`() {
            viewModel.clearLeaks()

            verify { engine.clearLeaks() }
        }

        @Test
        fun `isRunning reflects engine state`() = runTest {
            viewModel.isRunning.test {
                awaitItem() shouldBe false
                isRunningFlow.value = true
                awaitItem() shouldBe true
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

            viewModel.summary.test {
                awaitItem() shouldBe LeakSummary.empty()
                leakSummaryFlow.value = updatedSummary
                awaitItem() shouldBe updatedSummary
            }
        }
    }
}
