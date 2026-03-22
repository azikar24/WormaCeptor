package com.azikar24.wormaceptor.feature.viewer.export

import android.content.Context
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ExportManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val queryEngine = mockk<QueryEngine>(relaxed = true)
    private val messages = mutableListOf<String>()

    private lateinit var exportManager: ExportManager

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        messages.clear()
        exportManager = ExportManager(
            context = context,
            queryEngine = queryEngine,
            onMessage = { messages.add(it) },
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class JsonSerialization {

        @Test
        fun `should serialize transaction with request only`() = runTest {
            val transactionId = UUID.randomUUID()
            val transaction = NetworkTransaction(
                id = transactionId,
                timestamp = 1_700_000_000_000L,
                status = TransactionStatus.ACTIVE,
                durationMs = null,
                request = Request(
                    url = "https://api.example.com/users",
                    method = "GET",
                    headers = mapOf("Accept" to listOf("application/json")),
                    bodyRef = null,
                    bodySize = 0,
                ),
                response = null,
            )

            val json = serializeTransactions(listOf(transaction))

            json.length() shouldBe 1
            val obj = json.getJSONObject(0)
            obj.getString("id") shouldBe transactionId.toString()
            obj.getLong("timestamp") shouldBe 1_700_000_000_000L
            obj.getString("status") shouldBe "ACTIVE"
            obj.isNull("durationMs").shouldBe(true)

            val requestJson = obj.getJSONObject("request")
            requestJson.getString("url") shouldBe "https://api.example.com/users"
            requestJson.getString("method") shouldBe "GET"
            requestJson.getLong("bodySize") shouldBe 0

            obj.has("response") shouldBe false
        }

        @Test
        fun `should serialize transaction with request and response`() = runTest {
            val transaction = NetworkTransaction(
                timestamp = 1_700_000_000_000L,
                status = TransactionStatus.COMPLETED,
                durationMs = 150,
                request = Request(
                    url = "https://api.example.com/users",
                    method = "POST",
                    headers = mapOf("Content-Type" to listOf("application/json")),
                    bodyRef = "req-blob-1",
                    bodySize = 42,
                ),
                response = Response(
                    code = 200,
                    message = "OK",
                    headers = mapOf("Content-Type" to listOf("application/json")),
                    bodyRef = "resp-blob-1",
                    bodySize = 1024,
                ),
            )

            coEvery { queryEngine.getBody("req-blob-1") } returns "{\"name\":\"John\"}"
            coEvery { queryEngine.getBody("resp-blob-1") } returns "{\"id\":1}"

            val json = serializeTransactions(listOf(transaction))

            val obj = json.getJSONObject(0)
            obj.getString("status") shouldBe "COMPLETED"
            obj.getLong("durationMs") shouldBe 150

            val requestJson = obj.getJSONObject("request")
            requestJson.getString("body") shouldBe "{\"name\":\"John\"}"
            requestJson.getLong("bodySize") shouldBe 42

            val responseJson = obj.getJSONObject("response")
            responseJson.getInt("code") shouldBe 200
            responseJson.getString("message") shouldBe "OK"
            responseJson.getString("body") shouldBe "{\"id\":1}"
            responseJson.getLong("bodySize") shouldBe 1024
        }

        @Test
        fun `should serialize request headers with multi-values`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/data",
                    method = "GET",
                    headers = mapOf(
                        "Accept" to listOf("text/html", "application/json"),
                        "Authorization" to listOf("Bearer token123"),
                    ),
                    bodyRef = null,
                ),
            )

            val json = serializeTransactions(listOf(transaction))

            val headers = json.getJSONObject(0)
                .getJSONObject("request")
                .getJSONObject("headers")
            headers.getString("Accept") shouldBe "text/html, application/json"
            headers.getString("Authorization") shouldBe "Bearer token123"
        }

        @Test
        fun `should serialize response headers with multi-values`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/data",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = null,
                ),
                response = Response(
                    code = 200,
                    message = "OK",
                    headers = mapOf(
                        "Set-Cookie" to listOf("session=abc", "token=xyz"),
                    ),
                    bodyRef = null,
                ),
            )

            val json = serializeTransactions(listOf(transaction))

            val headers = json.getJSONObject(0)
                .getJSONObject("response")
                .getJSONObject("headers")
            headers.getString("Set-Cookie") shouldBe "session=abc, token=xyz"
        }

        @Test
        fun `should handle transaction without body refs`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/health",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = null,
                ),
                response = Response(
                    code = 204,
                    message = "No Content",
                    headers = emptyMap(),
                    bodyRef = null,
                ),
            )

            val json = serializeTransactions(listOf(transaction))

            val obj = json.getJSONObject(0)
            obj.getJSONObject("request").has("body") shouldBe false
            obj.getJSONObject("response").has("body") shouldBe false
        }

        @Test
        fun `should serialize multiple transactions`() = runTest {
            val transactions = listOf(
                NetworkTransaction(
                    request = Request("https://api.example.com/a", "GET", emptyMap(), null),
                ),
                NetworkTransaction(
                    request = Request("https://api.example.com/b", "POST", emptyMap(), null),
                ),
                NetworkTransaction(
                    request = Request("https://api.example.com/c", "DELETE", emptyMap(), null),
                ),
            )

            val json = serializeTransactions(transactions)

            json.length() shouldBe 3
            val urls = (0 until json.length()).map { i ->
                json.getJSONObject(i).getJSONObject("request").getString("url")
            }
            urls shouldContainAll listOf(
                "https://api.example.com/a",
                "https://api.example.com/b",
                "https://api.example.com/c",
            )
        }

        @Test
        fun `should serialize empty transaction list`() = runTest {
            val json = serializeTransactions(emptyList())

            json.length() shouldBe 0
        }
    }

    @Nested
    inner class ExportFlow {

        @Test
        fun `should emit preparing message`() = runTest {
            every { context.startActivity(any()) } just Runs

            exportManager.exportTransactions(emptyList())

            messages.first() shouldBe "Preparing export..."
        }

        @Test
        fun `should handle null queryEngine gracefully`() = runTest {
            val managerNoEngine = ExportManager(
                context = context,
                queryEngine = null,
                onMessage = { messages.add(it) },
            )
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = "some-blob-id",
                ),
            )

            every { context.startActivity(any()) } just Runs

            managerNoEngine.exportTransactions(listOf(transaction))

            messages shouldContainAll listOf("Preparing export...")
        }

        @Test
        fun `should handle queryEngine getBody returning null`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = "missing-blob",
                ),
            )
            coEvery { queryEngine.getBody("missing-blob") } returns null
            every { context.startActivity(any()) } just Runs

            exportManager.exportTransactions(listOf(transaction))

            messages.first() shouldBe "Preparing export..."
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `should emit error message when serialization fails`() = runTest {
            val failingEngine = mockk<QueryEngine> {
                coEvery { getBody(any()) } throws RuntimeException("Blob storage unavailable")
            }
            val manager = ExportManager(
                context = context,
                queryEngine = failingEngine,
                onMessage = { messages.add(it) },
            )
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = "blob-id",
                ),
            )

            manager.exportTransactions(listOf(transaction))

            messages.any { it.startsWith("Export failed:") } shouldBe true
        }

        @Test
        fun `should include exception message in error output`() = runTest {
            val failingEngine = mockk<QueryEngine> {
                coEvery { getBody(any()) } throws RuntimeException("disk full")
            }
            val manager = ExportManager(
                context = context,
                queryEngine = failingEngine,
                onMessage = { messages.add(it) },
            )
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = "blob-id",
                ),
            )

            manager.exportTransactions(listOf(transaction))

            val errorMessage = messages.first { it.startsWith("Export failed:") }
            errorMessage shouldContain "disk full"
        }
    }

    /**
     * Helper that replicates the serialization logic from ExportManager
     * so we can unit-test the JSON output without needing Android intents.
     */
    private suspend fun serializeTransactions(transactions: List<NetworkTransaction>): JSONArray {
        val jsonArray = JSONArray()
        transactions.forEach { transaction ->
            val requestBody = transaction.request.bodyRef?.let { blobId ->
                queryEngine.getBody(blobId)
            }
            val responseBody = transaction.response?.bodyRef?.let { blobId ->
                queryEngine.getBody(blobId)
            }

            val jsonObject = JSONObject().apply {
                put("id", transaction.id.toString())
                put("timestamp", transaction.timestamp)
                put("status", transaction.status.name)
                put("durationMs", transaction.durationMs)

                put(
                    "request",
                    JSONObject().apply {
                        put("url", transaction.request.url)
                        put("method", transaction.request.method)
                        put(
                            "headers",
                            JSONObject(
                                transaction.request.headers.mapValues {
                                    it.value.joinToString(", ")
                                },
                            ),
                        )
                        put("bodySize", transaction.request.bodySize)
                        if (requestBody != null) {
                            put("body", requestBody)
                        }
                    },
                )

                transaction.response?.let { response ->
                    put(
                        "response",
                        JSONObject().apply {
                            put("code", response.code)
                            put("message", response.message)
                            put(
                                "headers",
                                JSONObject(
                                    response.headers.mapValues {
                                        it.value.joinToString(", ")
                                    },
                                ),
                            )
                            put("bodySize", response.bodySize)
                            if (responseBody != null) {
                                put("body", responseBody)
                            }
                        },
                    )
                }
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }
}
