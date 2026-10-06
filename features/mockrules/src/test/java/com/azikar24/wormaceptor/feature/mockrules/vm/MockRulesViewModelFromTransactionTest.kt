package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class MockRulesViewModelFromTransactionTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository = mockk<MockRuleRepository>(relaxed = true) {
        every { getAll() } returns MutableStateFlow<List<MockRule>>(emptyList())
    }

    private val engine = mockk<MockEngine>(relaxed = true) {
        every { mockingEnabled } returns MutableStateFlow(true)
    }

    private val queryEngine = mockk<QueryEngine>()

    private lateinit var viewModel: MockRulesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MockRulesViewModel(repository, engine, queryEngine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads transaction and body into an unsaved draft`() = runTest {
        val tx = transaction()
        coEvery { queryEngine.getDetails(tx.id) } returns tx
        coEvery { queryEngine.getBodyBytes("res") } returns """{"ok":true}""".toByteArray()

        viewModel.sendEvent(load(tx.id.toString()))

        with(viewModel.uiState.value.editor) {
            isLoaded shouldBe true
            isEditing shouldBe false
            name shouldBe "GET /items"
            urlPattern shouldBe "https://api.example.com/items?page=2"
            matchType shouldBe UrlMatchType.EXACT
            statusCodeText shouldBe "404"
            responseBody shouldBe """{"ok":true}"""
        }
        coVerify(exactly = 0) { repository.insert(any()) }
        coVerify(exactly = 0) { repository.update(any()) }
    }

    @Test
    fun `missing transaction opens an empty editor and reports it`() = runTest {
        val id = UUID.randomUUID()
        coEvery { queryEngine.getDetails(id) } returns null

        viewModel.sendEvent(load(id.toString()))

        viewModel.uiState.value.editor shouldBe
            EditorState(isLoaded = true, notice = EditorNotice.TransactionNotFound)
    }

    @Test
    fun `malformed transaction id reports not found without any collector`() = runTest {
        viewModel.sendEvent(load("not-a-uuid"))

        viewModel.uiState.value.editor.notice shouldBe EditorNotice.TransactionNotFound
    }

    @Test
    fun `missing query engine reports not found`() = runTest {
        val vm = MockRulesViewModel(repository, engine, queryEngine = null)

        vm.sendEvent(load(UUID.randomUUID().toString()))

        vm.uiState.value.editor.notice shouldBe EditorNotice.TransactionNotFound
    }

    @Test
    fun `NoticeShown clears the notice`() = runTest {
        viewModel.sendEvent(load("not-a-uuid"))
        viewModel.sendEvent(MockRulesViewEvent.Editor.NoticeShown)

        viewModel.uiState.value.editor.notice shouldBe null
    }

    @Test
    fun `recreation with the same key keeps edits and does not refetch`() = runTest {
        val tx = transaction()
        coEvery { queryEngine.getDetails(tx.id) } returns tx
        coEvery { queryEngine.getBodyBytes("res") } returns null

        viewModel.sendEvent(load(tx.id.toString()))
        viewModel.sendEvent(MockRulesViewEvent.Editor.NameChanged("Edited"))
        viewModel.sendEvent(load(tx.id.toString()))

        viewModel.uiState.value.editor.name shouldBe "Edited"
        coVerify(exactly = 1) { queryEngine.getDetails(tx.id) }
    }

    @Test
    fun `binary response body raises ResponseBodyOmitted`() = runTest {
        val tx = transaction(contentType = "application/octet-stream")
        coEvery { queryEngine.getDetails(tx.id) } returns tx
        coEvery { queryEngine.getBodyBytes("res") } returns byteArrayOf(1, 2, 3)

        viewModel.sendEvent(load(tx.id.toString()))

        viewModel.uiState.value.editor.notice shouldBe EditorNotice.ResponseBodyOmitted
        viewModel.uiState.value.editor.responseBody shouldBe ""
    }

    @Test
    fun `saving the draft inserts a new exact-match rule`() = runTest {
        val tx = transaction()
        coEvery { queryEngine.getDetails(tx.id) } returns tx
        coEvery { queryEngine.getBodyBytes("res") } returns null
        coEvery { repository.getById(any()) } returns null

        viewModel.sendEvent(load(tx.id.toString()))
        viewModel.sendEvent(MockRulesViewEvent.Editor.SaveRule)

        coVerify {
            repository.insert(
                match {
                    it.matcher.urlPattern == tx.request.url &&
                        it.matcher.matchType == UrlMatchType.EXACT &&
                        it.matcher.method == "GET" &&
                        it.response.statusCode == 404 &&
                        it.enabled
                },
            )
        }
    }

    private fun load(transactionId: String) =
        MockRulesViewEvent.Editor.LoadFromTransaction(transactionId, loadKey = "visit-1")

    private fun transaction(contentType: String = "application/json") = NetworkTransaction(
        request = Request(
            url = "https://api.example.com/items?page=2",
            method = "GET",
            headers = emptyMap(),
            bodyRef = null,
        ),
        response = Response(
            code = 404,
            message = "Not Found",
            headers = mapOf("Content-Type" to listOf(contentType)),
            bodyRef = "res",
        ),
    )
}
