package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
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
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionPagerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val queryEngine = mockk<QueryEngine>()

    private val first = transaction(status = TransactionStatus.ACTIVE)
    private val second = transaction(status = TransactionStatus.COMPLETED)
    private val firstFlow = MutableStateFlow<NetworkTransaction?>(first)
    private val secondFlow = MutableStateFlow<NetworkTransaction?>(second)
    private val ids = listOf(first.id, second.id)

    private lateinit var viewModel: TransactionPagerViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { queryEngine.observeDetails(first.id) } returns firstFlow
        every { queryEngine.observeDetails(second.id) } returns secondFlow
        viewModel = TransactionPagerViewModel(queryEngine, testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `in-flight transaction updates when its response arrives`() = runTest {
        viewModel.sendEvent(TransactionPagerEvent.Initialize(ids, initialIndex = 0))
        viewModel.uiState.value.transaction?.status shouldBe TransactionStatus.ACTIVE

        val completed = first.copy(
            status = TransactionStatus.COMPLETED,
            response = Response(200, "OK", emptyMap(), bodyRef = "res"),
        )
        firstFlow.value = completed

        viewModel.uiState.value.transaction shouldBe completed
        viewModel.uiState.value.isLoading shouldBe false
    }

    @Test
    fun `re-sent Initialize after recreation keeps the navigated index`() = runTest {
        viewModel.sendEvent(TransactionPagerEvent.Initialize(ids, initialIndex = 0))
        viewModel.sendEvent(TransactionPagerEvent.NavigateNext)

        viewModel.sendEvent(TransactionPagerEvent.Initialize(ids, initialIndex = 0))

        viewModel.uiState.value.currentIndex shouldBe 1
        viewModel.uiState.value.transaction shouldBe second
        verify(exactly = 1) { queryEngine.observeDetails(first.id) }
    }

    @Test
    fun `navigating away stops applying updates from the previous transaction`() = runTest {
        viewModel.sendEvent(TransactionPagerEvent.Initialize(ids, initialIndex = 0))
        viewModel.sendEvent(TransactionPagerEvent.NavigateNext)

        firstFlow.value = first.copy(status = TransactionStatus.FAILED)

        viewModel.uiState.value.transaction shouldBe second
    }

    private fun transaction(status: TransactionStatus) = NetworkTransaction(
        id = UUID.randomUUID(),
        status = status,
        request = Request("https://example.com", "GET", emptyMap(), bodyRef = null),
    )
}
