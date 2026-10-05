package com.azikar24.wormaceptor.feature.viewer.vm

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.MaxParseBodySize
import com.azikar24.wormaceptor.feature.viewer.ui.TruncatedDisplaySize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val queryEngine = mockk<QueryEngine>()
    private lateinit var viewModel: TransactionDetailViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionDetailViewModel(queryEngine, testDispatcher, testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `re-delivering the same transaction keeps tab, search and bodies without reloading`() = runTest {
        val tx = transaction(requestBlob = "req")
        coEvery { queryEngine.getBodyBytes("req") } returns "hello".toByteArray()

        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(tx))
        viewModel.sendEvent(TransactionDetailViewEvent.Search.ActiveTabChanged(1))
        viewModel.sendEvent(TransactionDetailViewEvent.Search.VisibilityChanged(true))
        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(tx.copy()))
        viewModel.sendEvent(TransactionDetailViewEvent.Search.ActiveTabChanged(1))

        val state = viewModel.uiState.value
        state.activeTabIndex shouldBe 1
        state.showSearch shouldBe true
        state.requestState.rawBody shouldBe "hello"
        coVerify(exactly = 1) { queryEngine.getBodyBytes("req") }
    }

    @Test
    fun `in-flight transaction loads the response body once it arrives`() = runTest {
        val inFlight = transaction(status = TransactionStatus.ACTIVE)
        coEvery { queryEngine.getBodyBytes("res") } returns "done".toByteArray()

        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(inFlight))
        val completed = inFlight.copy(
            status = TransactionStatus.COMPLETED,
            response = Response(200, "OK", emptyMap(), bodyRef = "res"),
        )
        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(completed))

        val state = viewModel.uiState.value
        state.transaction shouldBe completed
        state.responseState.rawBody shouldBe "done"
        state.responseState.isLoading shouldBe false
    }

    @Test
    fun `stale body load is dropped when paging to another transaction`() = runTest {
        val pendingA = CompletableDeferred<ByteArray?>()
        coEvery { queryEngine.getBodyBytes("a") } coAnswers { pendingA.await() }
        coEvery { queryEngine.getBodyBytes("b") } returns "B".toByteArray()

        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(transaction(requestBlob = "a")))
        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(transaction(requestBlob = "b")))
        pendingA.complete("A".toByteArray())

        viewModel.uiState.value.requestState.rawBody shouldBe "B"
    }

    @Test
    fun `stale body load is dropped when the next transaction has no body`() = runTest {
        val pendingA = CompletableDeferred<ByteArray?>()
        coEvery { queryEngine.getBodyBytes("a") } coAnswers { pendingA.await() }

        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(transaction(requestBlob = "a")))
        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(transaction()))
        pendingA.complete("A".toByteArray())

        viewModel.uiState.value.requestState.rawBody shouldBe null
        viewModel.uiState.value.requestState.isLoading shouldBe false
    }

    @Test
    fun `oversized raw body is capped for display but kept whole for copy`() = runTest {
        val body = "a".repeat(MaxParseBodySize + 1)
        coEvery { queryEngine.getBodyBytes("req") } returns body.toByteArray()

        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(transaction(requestBlob = "req")))

        val section = viewModel.uiState.value.requestState
        section.rawBody?.length shouldBe body.length
        section.displayRawBody?.length shouldBe TruncatedDisplaySize
        section.isRawBodyTruncated shouldBe true
    }

    @Test
    fun `copy url emits CopyText with the full request url`() = runTest {
        val url = "https://api.example.com/v1/search?q=a%20b&page=2#frag"
        val tx = transaction().copy(request = Request(url, "GET", emptyMap(), bodyRef = null))
        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(tx))

        viewModel.effects.test {
            viewModel.sendEvent(TransactionDetailViewEvent.Overview.CopyUrl)

            awaitItem() shouldBe TransactionDetailViewEffect.Clipboard.CopyText(R.string.viewer_clipboard_url, url)
        }
    }

    @Test
    fun `add to mock rules closes the menu and emits navigation with the transaction id`() = runTest {
        val tx = transaction()
        viewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(tx))
        viewModel.sendEvent(TransactionDetailViewEvent.Menu.VisibilityChanged(true))

        viewModel.effects.test {
            viewModel.sendEvent(TransactionDetailViewEvent.Menu.AddToMockRules)

            awaitItem() shouldBe TransactionDetailViewEffect.Navigate.AddToMockRules(tx.id)
        }
        viewModel.uiState.value.showMenu shouldBe false
    }

    private fun transaction(
        requestBlob: String? = null,
        status: TransactionStatus = TransactionStatus.COMPLETED,
    ) = NetworkTransaction(
        id = UUID.randomUUID(),
        status = status,
        request = Request("https://example.com", "POST", emptyMap(), bodyRef = requestBlob),
    )
}
