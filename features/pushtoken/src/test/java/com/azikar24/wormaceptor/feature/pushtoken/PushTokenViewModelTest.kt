package com.azikar24.wormaceptor.feature.pushtoken

import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo.PushProvider
import com.azikar24.wormaceptor.domain.entities.TokenHistory
import com.azikar24.wormaceptor.domain.entities.TokenHistory.TokenEvent
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewEvent
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewModel
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
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
class PushTokenViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val currentTokenFlow = MutableStateFlow<PushTokenInfo?>(null)
    private val tokenHistoryFlow = MutableStateFlow<List<TokenHistory>>(emptyList())
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)

    private val engine = mockk<PushTokenEngine>(relaxed = true) {
        every { currentToken } returns currentTokenFlow
        every { tokenHistory } returns tokenHistoryFlow
        every { isLoading } returns isLoadingFlow
        every { error } returns errorFlow
    }

    private lateinit var viewModel: PushTokenViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PushTokenViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeSampleToken(
        token: String = "fcm-token-abc123",
        provider: PushProvider = PushProvider.FCM,
    ) = PushTokenInfo(
        token = token,
        provider = provider,
        createdAt = 1_700_000_000_000L,
        lastRefreshed = 1_700_000_000_000L,
        isValid = true,
        associatedUserId = null,
        metadata = emptyMap(),
    )

    @Nested
    inner class `currentToken` {

        @Test
        fun `initial value is null`() = runTest {
            viewModel.uiState.value.currentToken.shouldBeNull()
        }

        @Test
        fun `emits updated token from engine`() = runTest {
            val token = makeSampleToken()

            currentTokenFlow.value = token

            viewModel.uiState.value.currentToken shouldBe token
        }
    }

    @Nested
    inner class `tokenHistory` {

        @Test
        fun `initial value is empty list`() = runTest {
            viewModel.uiState.value.tokenHistory.shouldBeEmpty()
        }

        @Test
        fun `emits history updates from engine`() = runTest {
            val history = listOf(
                TokenHistory(
                    token = "token-1",
                    timestamp = 1_700_000_000_000L,
                    event = TokenEvent.CREATED,
                ),
                TokenHistory(
                    token = "token-1",
                    timestamp = 1_700_001_000_000L,
                    event = TokenEvent.REFRESHED,
                ),
            )

            tokenHistoryFlow.value = history

            val items = viewModel.uiState.value.tokenHistory
            items shouldHaveSize 2
            items.first().event shouldBe TokenEvent.CREATED
        }
    }

    @Nested
    inner class `isLoading` {

        @Test
        fun `initial value is false`() = runTest {
            viewModel.uiState.value.isLoading shouldBe false
        }

        @Test
        fun `reflects engine loading state`() = runTest {
            isLoadingFlow.value = true

            viewModel.uiState.value.isLoading shouldBe true
        }
    }

    @Nested
    inner class `error` {

        @Test
        fun `initial value is null`() = runTest {
            viewModel.uiState.value.error.shouldBeNull()
        }

        @Test
        fun `reflects engine error state`() = runTest {
            errorFlow.value = "Token fetch failed"

            viewModel.uiState.value.error shouldBe "Token fetch failed"
        }
    }

    @Nested
    inner class `fetchToken` {

        @Test
        fun `delegates to engine fetchCurrentToken`() = runTest {
            viewModel.sendEvent(PushTokenViewEvent.FetchToken)

            verify { engine.fetchCurrentToken() }
        }
    }

    @Nested
    inner class `refreshToken` {

        @Test
        fun `delegates to engine requestNewToken`() = runTest {
            viewModel.sendEvent(PushTokenViewEvent.RefreshToken)

            verify { engine.requestNewToken() }
        }
    }

    @Nested
    inner class `deleteToken` {

        @Test
        fun `delegates to engine deleteToken`() = runTest {
            viewModel.sendEvent(PushTokenViewEvent.DeleteToken)

            verify { engine.deleteToken() }
        }
    }

    @Nested
    inner class `clearHistory` {

        @Test
        fun `delegates to engine clearHistory`() = runTest {
            viewModel.sendEvent(PushTokenViewEvent.ClearHistory)

            verify { engine.clearHistory() }
        }
    }

    @Nested
    inner class `clearError` {

        @Test
        fun `delegates to engine clearError`() = runTest {
            viewModel.sendEvent(PushTokenViewEvent.DismissError)

            verify { engine.clearError() }
        }
    }
}
