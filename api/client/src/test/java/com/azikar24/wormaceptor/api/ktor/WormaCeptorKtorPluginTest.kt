package com.azikar24.wormaceptor.api.ktor

import android.util.Log
import com.azikar24.wormaceptor.api.ServiceProvider
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.entities.mock.MockDelay
import com.azikar24.wormaceptor.domain.entities.mock.MockResponse
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.RequestMatcher
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class WormaCeptorKtorPluginTest {

    private val provider = mockk<ServiceProvider>(relaxed = true)
    private val id = UUID.randomUUID()
    private val network = NetworkEngine()
    private val mockEngine = MockEngine(initialRulesTimeoutMs = 0)

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        mockkObject(WormaCeptorApi)
        every { WormaCeptorApi.capturingProvider } returns provider
        every { provider.startTransaction(any(), any(), any(), any(), any(), any<Long>()) } returns id
        mockkStatic(WormaCeptorKoin::class)
        every { WormaCeptorKoin.get(MockEngine::class.java) } returns mockEngine
    }

    @AfterEach
    fun tearDown() = unmockkAll()

    private fun client() = HttpClient(network) { install(WormaCeptorKtorPlugin) }

    private fun rule(delay: MockDelay = MockDelay.None) = MockRule(
        name = "users",
        matcher = RequestMatcher(urlPattern = "https://example.com/users"),
        response = MockResponse(
            statusCode = 418,
            statusMessage = "Mocked",
            headers = mapOf("X-Mock" to "yes"),
            body = """{"mocked":true}""",
        ),
        delay = delay,
    )

    @Test
    fun `serves a matching mock rule without calling the network and records it`() = runTest {
        mockEngine.setRules(listOf(rule()))

        val response = client().get("https://example.com/users")

        response.status shouldBe HttpStatusCode(418, "Mocked")
        response.headers["X-Mock"] shouldBe "yes"
        response.headers["Content-Type"] shouldBe "application/json"
        response.bodyAsText() shouldBe """{"mocked":true}"""
        network.calls.get() shouldBe 0
        verify {
            provider.completeTransaction(id, 418, "Mocked", any(), any(), 15, any(), any(), null, true, any())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `waits for the rule's delay before responding`() = runTest {
        mockEngine.setRules(listOf(rule(MockDelay.Fixed(MOCK_DELAY_MS))))

        client().get("https://example.com/users").bodyAsText()

        // The plugin's delay runs on runTest's virtual clock.
        currentTime shouldBeGreaterThanOrEqualTo MOCK_DELAY_MS
    }

    @Test
    fun `sends a request no rule matches to the network`() = runTest {
        mockEngine.setRules(listOf(rule()))

        val response = client().get("https://example.com/orders")

        response.bodyAsText() shouldBe "network"
        network.calls.get() shouldBe 1
    }

    private class NetworkEngine : HttpClientEngineBase("network") {
        val calls = AtomicInteger()

        override val config = HttpClientEngineConfig()

        @OptIn(InternalAPI::class)
        override suspend fun execute(data: HttpRequestData): HttpResponseData {
            calls.incrementAndGet()
            return HttpResponseData(
                HttpStatusCode.OK,
                GMTDate(),
                Headers.Empty,
                HttpProtocolVersion.HTTP_1_1,
                ByteReadChannel("network"),
                callContext(),
            )
        }
    }

    private companion object {
        const val MOCK_DELAY_MS = 200L
    }
}
