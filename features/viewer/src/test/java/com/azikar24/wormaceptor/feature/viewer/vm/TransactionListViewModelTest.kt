package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.paging.PagingData
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val queryEngine = mockk<QueryEngine>(relaxed = true) {
        every { observeTransactions() } returns flowOf(emptyList())
        every { observeTransactionsPaged(any(), any(), any()) } returns flowOf(PagingData.empty())
        coEvery { getTransactionCount(any()) } returns 0
    }

    private lateinit var viewModel: TransactionListViewModel

    private val summary = TransactionSummary(
        id = UUID.randomUUID(),
        method = "GET",
        host = "10.0.2.2",
        path = "/api/users",
        code = 200,
        tookMs = 42,
        hasRequestBody = false,
        hasResponseBody = true,
        status = TransactionStatus.COMPLETED,
        timestamp = 1_700_000_000_000L,
        url = "http://10.0.2.2:8080/api/users?page=2",
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionListViewModel(queryEngine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `copy URL keeps scheme, port and query`() = runTest {
        viewModel.effects.test {
            viewModel.sendEvent(TransactionListViewEvent.CopyTransactionUrl(summary))

            val effect = awaitItem()
            effect.shouldBeInstanceOf<TransactionListViewEffect.CopyToClipboard>()
            effect.content shouldBe "http://10.0.2.2:8080/api/users?page=2"
        }
    }

    @Test
    fun `share transaction uses full URL`() = runTest {
        viewModel.effects.test {
            viewModel.sendEvent(TransactionListViewEvent.ShareTransaction(summary))

            val effect = awaitItem()
            effect.shouldBeInstanceOf<TransactionListViewEffect.ShareText>()
            effect.text shouldContain "GET http://10.0.2.2:8080/api/users?page=2"
        }
    }
}
