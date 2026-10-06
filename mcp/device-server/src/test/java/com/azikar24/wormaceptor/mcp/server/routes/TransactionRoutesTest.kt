package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class TransactionRoutesTest {

    private val queryEngine: QueryEngine = sharedQueryEngine

    private val getUsers = summary("GET", "api.example.com", "/users", 200)
    private val postLogin = summary("POST", "auth.example.com", "/login", 401)
    private val getOrders = summary("GET", "api.example.com", "/orders", 500)

    @BeforeEach
    fun setUp() {
        every { queryEngine.observeTransactions() } returns flowOf(listOf(getUsers, postLogin, getOrders))
    }

    private fun routeTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application {
            install(ContentNegotiation) { json(JsonConfig.instance) }
            routing { transactionRoutes(maxBodySize = 4) }
        }
        block()
    }

    private suspend fun ApplicationTestBuilder.getJson(path: String): JsonObject =
        Json.parseToJsonElement(client.get(path).bodyAsText()).jsonObject

    private fun JsonObject.ids() = this["data"]!!.jsonArray.map { it.jsonObject["id"]!!.jsonPrimitive.content }

    @Test
    fun `list without query returns everything`() = routeTest {
        getJson("/api/transactions").ids() shouldBe listOf(getUsers, postLogin, getOrders).map { it.id.toString() }
    }

    @Test
    fun `query matches URL substring case-insensitively`() = routeTest {
        getJson("/api/transactions?query=AUTH.example").ids() shouldBe listOf(postLogin.id.toString())
    }

    @Test
    fun `query matches HTTP method`() = routeTest {
        getJson("/api/transactions?query=post").ids() shouldBe listOf(postLogin.id.toString())
    }

    @Test
    fun `query matches status code`() = routeTest {
        val body = getJson("/api/transactions?query=500")
        body.ids() shouldBe listOf(getOrders.id.toString())
        body["meta"]!!.jsonObject["total"]!!.jsonPrimitive.int shouldBe 1
    }

    @Test
    fun `pagination applies after filtering`() = routeTest {
        getJson("/api/transactions?query=api.example.com&limit=1&offset=1").ids() shouldBe
            listOf(getOrders.id.toString())
    }

    @Test
    fun `unknown transaction returns 404`() = routeTest {
        coEvery { queryEngine.getDetails(any()) } returns null
        client.get("/api/transactions/${UUID.randomUUID()}").status shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `response body comes back in the JSON envelope with truncation info`() = routeTest {
        val tx = NetworkTransaction(
            status = TransactionStatus.COMPLETED,
            request = Request("https://api.example.com/users", "GET", emptyMap(), bodyRef = null),
            response = Response(
                code = 200,
                message = "OK",
                headers = mapOf("Content-Type" to listOf("application/json")),
                bodyRef = "blob-1",
            ),
        )
        coEvery { queryEngine.getDetails(tx.id) } returns tx
        coEvery { queryEngine.getBody("blob-1") } returns "{\"a\":1}"

        val data = getJson("/api/transactions/${tx.id}/response-body")["data"]!!.jsonObject

        data["body"]!!.jsonPrimitive.content shouldBe "{\"a\""
        data["contentType"]!!.jsonPrimitive.content shouldBe "application/json"
        data["truncated"]!!.jsonPrimitive.content shouldBe "true"
        data["totalSize"]!!.jsonPrimitive.int shouldBe 7
    }

    @Test
    fun `missing request body returns an empty envelope`() = routeTest {
        val tx = NetworkTransaction(request = Request("https://a.example", "GET", emptyMap(), bodyRef = null))
        coEvery { queryEngine.getDetails(tx.id) } returns tx

        val data = getJson("/api/transactions/${tx.id}/request-body")["data"]!!.jsonObject

        data["body"]!!.jsonPrimitive.content shouldBe ""
        data["truncated"]!!.jsonPrimitive.content shouldBe "false"
    }

    private fun summary(
        method: String,
        host: String,
        path: String,
        code: Int,
    ) = TransactionSummary(
        id = UUID.randomUUID(),
        method = method,
        host = host,
        path = path,
        code = code,
        tookMs = 10,
        hasRequestBody = false,
        hasResponseBody = true,
        status = TransactionStatus.COMPLETED,
        timestamp = 0,
        url = "https://$host$path",
    )

    companion object {
        // CoreHolder can only be initialized once per JVM, so every test shares one mock.
        val sharedQueryEngine: QueryEngine by lazy {
            mockk<QueryEngine>(relaxed = true).also {
                CoreHolder.initialize(mockk<CaptureEngine>(relaxed = true), it)
            }
        }
    }
}
