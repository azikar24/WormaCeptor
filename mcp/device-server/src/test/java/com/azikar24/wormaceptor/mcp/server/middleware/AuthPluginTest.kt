package com.azikar24.wormaceptor.mcp.server.middleware

import com.azikar24.wormaceptor.mcp.server.routes.healthRoutes
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test

class AuthPluginTest {

    private fun authTest(block: suspend HttpClient.() -> Unit) = testApplication {
        application {
            install(ContentNegotiation) { json() }
            install(LocalRequestGuard)
            install(AuthPlugin) {
                enabled = true
                token = "s3cret"
            }
            routing {
                healthRoutes()
                get("/api/transactions") { call.respondText("data") }
            }
        }
        // The test engine sends no Host header; the bridge (via adb forward) always sends localhost.
        createClient { defaultRequest { header(HttpHeaders.Host, "localhost") } }.block()
    }

    @Test
    fun `rejects requests without a bearer token`() = authTest {
        get("/api/transactions").status shouldBe HttpStatusCode.Unauthorized
        get("/api/transactions") { header(HttpHeaders.Authorization, "Basic s3cret") }.status shouldBe
            HttpStatusCode.Unauthorized
    }

    @Test
    fun `rejects a wrong token`() = authTest {
        get("/api/transactions") { header(HttpHeaders.Authorization, "Bearer nope") }.status shouldBe
            HttpStatusCode.Forbidden
    }

    @Test
    fun `accepts the right token`() = authTest {
        val response = get("/api/transactions") { header(HttpHeaders.Authorization, "Bearer s3cret") }
        response.status shouldBe HttpStatusCode.OK
        response.bodyAsText() shouldBe "data"
    }

    @Test
    fun `health stays open so the bridge can probe before it has a token`() = authTest {
        get("/api/health").status shouldBe HttpStatusCode.OK
    }

    @Test
    fun `local guard rejects browser requests even with a valid token`() = authTest {
        val response = get("/api/transactions") {
            header(HttpHeaders.Authorization, "Bearer s3cret")
            header(HttpHeaders.Origin, "https://evil.example")
        }
        response.status shouldBe HttpStatusCode.Forbidden
        response.bodyAsText() shouldContain "Request not allowed"
    }
}
