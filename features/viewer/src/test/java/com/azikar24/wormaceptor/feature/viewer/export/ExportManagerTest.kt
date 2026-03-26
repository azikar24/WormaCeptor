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
import java.io.File
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

            val json = exportManager.serializeTransactions(listOf(transaction))

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

            coEvery { queryEngine.getBody("req-blob-1") } returns """{"name":"John"}"""
            coEvery { queryEngine.getBody("resp-blob-1") } returns """{"id":1}"""

            val json = exportManager.serializeTransactions(listOf(transaction))

            val obj = json.getJSONObject(0)
            obj.getString("status") shouldBe "COMPLETED"
            obj.getLong("durationMs") shouldBe 150

            val requestJson = obj.getJSONObject("request")
            // Body should be parsed as nested JSON object, not a string
            val reqBody = requestJson.getJSONObject("body")
            reqBody.getString("name") shouldBe "John"
            requestJson.getLong("bodySize") shouldBe 42

            val responseJson = obj.getJSONObject("response")
            responseJson.getInt("code") shouldBe 200
            responseJson.getString("message") shouldBe "OK"
            val respBody = responseJson.getJSONObject("body")
            respBody.getInt("id") shouldBe 1
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

            val json = exportManager.serializeTransactions(listOf(transaction))

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

            val json = exportManager.serializeTransactions(listOf(transaction))

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

            val json = exportManager.serializeTransactions(listOf(transaction))

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

            val json = exportManager.serializeTransactions(transactions)

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
            val json = exportManager.serializeTransactions(emptyList())

            json.length() shouldBe 0
        }
    }

    @Nested
    inner class ResponseMetadata {

        @Test
        fun `should serialize error field when present`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/timeout",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = null,
                ),
                response = Response(
                    code = 0,
                    message = "",
                    headers = emptyMap(),
                    bodyRef = null,
                    error = "java.net.SocketTimeoutException: connect timed out",
                ),
            )

            val json = exportManager.serializeTransactions(listOf(transaction))

            val responseJson = json.getJSONObject(0).getJSONObject("response")
            responseJson.getString("error") shouldBe "java.net.SocketTimeoutException: connect timed out"
        }

        @Test
        fun `should serialize protocol field when present`() = runTest {
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
                    headers = emptyMap(),
                    bodyRef = null,
                    protocol = "h2",
                ),
            )

            val json = exportManager.serializeTransactions(listOf(transaction))

            val responseJson = json.getJSONObject(0).getJSONObject("response")
            responseJson.getString("protocol") shouldBe "h2"
        }

        @Test
        fun `should serialize tlsVersion field when present`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/secure",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = null,
                ),
                response = Response(
                    code = 200,
                    message = "OK",
                    headers = emptyMap(),
                    bodyRef = null,
                    tlsVersion = "TLSv1.3",
                ),
            )

            val json = exportManager.serializeTransactions(listOf(transaction))

            val responseJson = json.getJSONObject(0).getJSONObject("response")
            responseJson.getString("tlsVersion") shouldBe "TLSv1.3"
        }

        @Test
        fun `should serialize all response metadata fields together`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/full",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = null,
                ),
                response = Response(
                    code = 200,
                    message = "OK",
                    headers = emptyMap(),
                    bodyRef = null,
                    error = null,
                    protocol = "http/1.1",
                    tlsVersion = "TLSv1.2",
                ),
            )

            val json = exportManager.serializeTransactions(listOf(transaction))

            val responseJson = json.getJSONObject(0).getJSONObject("response")
            responseJson.has("error") shouldBe false // null should be omitted
            responseJson.getString("protocol") shouldBe "http/1.1"
            responseJson.getString("tlsVersion") shouldBe "TLSv1.2"
        }

        @Test
        fun `should omit null metadata fields`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/plain",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = null,
                ),
                response = Response(
                    code = 200,
                    message = "OK",
                    headers = emptyMap(),
                    bodyRef = null,
                    error = null,
                    protocol = null,
                    tlsVersion = null,
                ),
            )

            val json = exportManager.serializeTransactions(listOf(transaction))

            val responseJson = json.getJSONObject(0).getJSONObject("response")
            responseJson.has("error") shouldBe false
            responseJson.has("protocol") shouldBe false
            responseJson.has("tlsVersion") shouldBe false
        }
    }

    @Nested
    inner class JsonBodyParsing {

        @Test
        fun `should parse JSON object body as nested object`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/users",
                    method = "POST",
                    headers = emptyMap(),
                    bodyRef = "req-json-obj",
                ),
                response = null,
            )
            coEvery { queryEngine.getBody("req-json-obj") } returns """{"name":"Alice","age":30}"""

            val json = exportManager.serializeTransactions(listOf(transaction))

            val body = json.getJSONObject(0).getJSONObject("request").getJSONObject("body")
            body.getString("name") shouldBe "Alice"
            body.getInt("age") shouldBe 30
        }

        @Test
        fun `should parse JSON array body as nested array`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/batch",
                    method = "POST",
                    headers = emptyMap(),
                    bodyRef = "req-json-arr",
                ),
                response = null,
            )
            coEvery { queryEngine.getBody("req-json-arr") } returns """[1,2,3]"""

            val json = exportManager.serializeTransactions(listOf(transaction))

            val body = json.getJSONObject(0).getJSONObject("request").getJSONArray("body")
            body.length() shouldBe 3
            body.getInt(0) shouldBe 1
        }

        @Test
        fun `should keep non-JSON body as string`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/upload",
                    method = "POST",
                    headers = emptyMap(),
                    bodyRef = "req-plain",
                ),
                response = null,
            )
            coEvery { queryEngine.getBody("req-plain") } returns "key1=value1&key2=value2"

            val json = exportManager.serializeTransactions(listOf(transaction))

            val body = json.getJSONObject(0).getJSONObject("request").getString("body")
            body shouldBe "key1=value1&key2=value2"
        }

        @Test
        fun `should keep XML body as string`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/soap",
                    method = "POST",
                    headers = emptyMap(),
                    bodyRef = "req-xml",
                ),
                response = null,
            )
            coEvery { queryEngine.getBody("req-xml") } returns "<root><item>test</item></root>"

            val json = exportManager.serializeTransactions(listOf(transaction))

            val body = json.getJSONObject(0).getJSONObject("request").getString("body")
            body shouldBe "<root><item>test</item></root>"
        }

        @Test
        fun `should handle malformed JSON gracefully as string`() = runTest {
            val transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/broken",
                    method = "POST",
                    headers = emptyMap(),
                    bodyRef = "req-broken",
                ),
                response = null,
            )
            coEvery { queryEngine.getBody("req-broken") } returns """{"unclosed": "bracket" """

            val json = exportManager.serializeTransactions(listOf(transaction))

            val body = json.getJSONObject(0).getJSONObject("request").getString("body")
            body shouldBe """{"unclosed": "bracket" """
        }

        @Test
        fun `should parse response JSON body as nested object`() = runTest {
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
                    headers = emptyMap(),
                    bodyRef = "resp-json",
                    bodySize = 100,
                ),
            )
            coEvery { queryEngine.getBody("resp-json") } returns """{"status":"ok","count":42}"""

            val json = exportManager.serializeTransactions(listOf(transaction))

            val body = json.getJSONObject(0).getJSONObject("response").getJSONObject("body")
            body.getString("status") shouldBe "ok"
            body.getInt("count") shouldBe 42
        }
    }

    @Nested
    inner class ParseJsonOrString {

        @Test
        fun `should parse valid JSON object`() {
            val result = ExportManager.parseJsonOrString("""{"key":"value"}""")
            (result is JSONObject) shouldBe true
            (result as JSONObject).getString("key") shouldBe "value"
        }

        @Test
        fun `should parse valid JSON array`() {
            val result = ExportManager.parseJsonOrString("""[1, 2, 3]""")
            (result is JSONArray) shouldBe true
            (result as JSONArray).length() shouldBe 3
        }

        @Test
        fun `should return string for plain text`() {
            val result = ExportManager.parseJsonOrString("hello world")
            (result is String) shouldBe true
            result shouldBe "hello world"
        }

        @Test
        fun `should return string for form-encoded data`() {
            val result = ExportManager.parseJsonOrString("a=1&b=2")
            (result is String) shouldBe true
        }

        @Test
        fun `should handle whitespace around JSON`() {
            val result = ExportManager.parseJsonOrString("""  {"key": "value"}  """)
            (result is JSONObject) shouldBe true
        }

        @Test
        fun `should return string for malformed JSON starting with brace`() {
            val result = ExportManager.parseJsonOrString("""{not json""")
            (result is String) shouldBe true
        }

        @Test
        fun `should return string for malformed JSON starting with bracket`() {
            val result = ExportManager.parseJsonOrString("""[not json""")
            (result is String) shouldBe true
        }

        @Test
        fun `should return string for empty input`() {
            val result = ExportManager.parseJsonOrString("")
            (result is String) shouldBe true
            result shouldBe ""
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
            every { context.cacheDir } returns File(System.getProperty("java.io.tmpdir")!!)
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
}
