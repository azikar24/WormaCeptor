package com.azikar24.wormaceptor.feature.mockrules.vm

import android.util.Log
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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

@OptIn(ExperimentalCoroutinesApi::class)
class MockRulesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository = mockk<MockRuleRepository>(relaxed = true) {
        every { getAll() } returns MutableStateFlow<List<MockRule>>(emptyList())
        coEvery { getById(any()) } returns null
    }

    private val engine = mockk<MockEngine>(relaxed = true) {
        every { mockingEnabled } returns MutableStateFlow(true)
    }

    private lateinit var viewModel: MockRulesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
        viewModel = MockRulesViewModel(repository, engine, queryEngine = null)
        viewModel.sendEvent(MockRulesViewEvent.Editor.LoadRule(null))
        viewModel.sendEvent(MockRulesViewEvent.Editor.NameChanged("Login error"))
        viewModel.sendEvent(MockRulesViewEvent.Editor.UrlPatternChanged("https://api.example.com/login"))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `failed save resets isSaving and emits SaveFailed`() = runTest {
        coEvery { repository.insert(any()) } throws IllegalStateException("database closed")

        viewModel.effects.test {
            viewModel.sendEvent(MockRulesViewEvent.Editor.SaveRule)

            awaitItem() shouldBe MockRulesEffect.SaveFailed
            viewModel.uiState.value.editor.isSaving shouldBe false
            viewModel.uiState.value.editor.name shouldBe "Login error"
        }
    }

    @Test
    fun `save can be retried after a failure`() = runTest {
        coEvery { repository.insert(any()) } throws IllegalStateException("database closed") andThen Unit

        viewModel.effects.test {
            viewModel.sendEvent(MockRulesViewEvent.Editor.SaveRule)
            awaitItem() shouldBe MockRulesEffect.SaveFailed

            viewModel.sendEvent(MockRulesViewEvent.Editor.SaveRule)
            awaitItem() shouldBe MockRulesEffect.NavigateBack
        }
        coVerify(exactly = 2) { repository.insert(any()) }
    }
}
