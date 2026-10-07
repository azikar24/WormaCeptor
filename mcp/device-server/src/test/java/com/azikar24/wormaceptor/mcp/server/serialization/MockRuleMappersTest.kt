package com.azikar24.wormaceptor.mcp.server.serialization

import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.mock.MockDelay
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import com.azikar24.wormaceptor.mcp.protocol.CreateMockRuleRequestDto
import com.azikar24.wormaceptor.mcp.protocol.MockFromTransactionRequestDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class MockRuleMappersTest {

    private val transaction = NetworkTransaction(
        request = Request("https://api.test/users?page=1", "get", emptyMap(), bodyRef = null),
        response = Response(
            code = 200,
            message = "OK",
            headers = mapOf("content-type" to listOf("application/json; charset=utf-8")),
            bodyRef = "blob",
        ),
    )

    private fun CreateMockRuleRequestDto.valid() = toMockRule().shouldBeInstanceOf<MockRuleDraft.Valid>().rule

    private fun CreateMockRuleRequestDto.error() = toMockRule().shouldBeInstanceOf<MockRuleDraft.Invalid>().message

    @Test
    fun `create applies the app defaults for unset fields`() {
        val rule = CreateMockRuleRequestDto(urlPattern = " https://api.test/users ", status = 503).valid()

        rule.matcher.urlPattern shouldBe "https://api.test/users"
        rule.matcher.matchType shouldBe UrlMatchType.PREFIX
        rule.matcher.method shouldBe null
        rule.enabled shouldBe true
        rule.response.statusCode shouldBe 503
        rule.response.statusMessage shouldBe "Service Unavailable"
        rule.response.contentType shouldBe "application/json"
        rule.response.body shouldBe null
        rule.delay shouldBe MockDelay.None
        rule.name shouldBe "ANY https://api.test/users"
    }

    @Test
    fun `create maps every field`() {
        val rule = CreateMockRuleRequestDto(
            urlPattern = "~/users/\\d+",
            status = 404,
            method = "delete",
            matchType = "regex",
            body = "{}",
            contentType = "text/plain",
            delayMs = 300,
            enabled = false,
        ).valid()

        rule.matcher.matchType shouldBe UrlMatchType.REGEX
        rule.matcher.method shouldBe "DELETE"
        rule.response.body shouldBe "{}"
        rule.response.contentType shouldBe "text/plain"
        rule.delay shouldBe MockDelay.Fixed(300)
        rule.enabled shouldBe false
    }

    @Test
    fun `create rejects bad input with a message naming the parameter`() {
        CreateMockRuleRequestDto(urlPattern = " ", status = 200).error() shouldContain "url_pattern"
        CreateMockRuleRequestDto(urlPattern = "https://a", status = 99).error() shouldContain "status"
        CreateMockRuleRequestDto(urlPattern = "https://a", status = 200, delayMs = -1).error() shouldContain "delay_ms"
        CreateMockRuleRequestDto(urlPattern = "https://a", status = 200, matchType = "glob").error() shouldContain
            "EXACT, PREFIX, REGEX"
        CreateMockRuleRequestDto(urlPattern = "(", status = 200, matchType = "REGEX").error() shouldContain "regex"
    }

    @Test
    fun `a created rule makes MockEngine return the mock for matching requests only`() {
        val rule = CreateMockRuleRequestDto(
            urlPattern = "https://api.test/users",
            status = 500,
            method = "GET",
            body = """{"error":"boom"}""",
            delayMs = 250,
        ).valid()
        val engine = MockEngine(initialRulesTimeoutMs = 0).apply { setRules(listOf(rule)) }

        val match = engine.findMatchingRule("https://api.test/users?page=2", "GET", emptyMap())
        match shouldBe rule
        val response = engine.resolveResponse(rule)
        response?.statusCode shouldBe 500
        response?.body shouldBe """{"error":"boom"}"""
        engine.computeDelayMs(rule) shouldBe 250L

        engine.findMatchingRule("https://api.test/users", "POST", emptyMap()) shouldBe null
        engine.findMatchingRule("https://api.test/orders", "GET", emptyMap()) shouldBe null
        engine.setRules(listOf(rule.copy(enabled = false)))
        engine.findMatchingRule("https://api.test/users", "GET", emptyMap()) shouldBe null
    }

    @Test
    fun `from transaction copies the exact URL, method and captured response`() {
        val draft = mockRuleFromTransaction(
            transaction,
            """{"users":[]}""".toByteArray(),
            MockFromTransactionRequestDto(),
        ).shouldBeInstanceOf<MockRuleDraft.Valid>()

        draft.bodyOmitted shouldBe false
        with(draft.rule) {
            name shouldBe "GET /users"
            matcher.urlPattern shouldBe "https://api.test/users?page=1"
            matcher.matchType shouldBe UrlMatchType.EXACT
            matcher.method shouldBe "GET"
            response.statusCode shouldBe 200
            response.statusMessage shouldBe "OK"
            response.contentType shouldBe "application/json; charset=utf-8"
            response.body shouldBe """{"users":[]}"""
        }
        val engine = MockEngine(initialRulesTimeoutMs = 0).apply { setRules(listOf(draft.rule)) }
        engine.findMatchingRule("https://api.test/users?page=1", "GET", emptyMap()) shouldBe draft.rule
    }

    @Test
    fun `from transaction applies status and body overrides`() {
        val rule = mockRuleFromTransaction(
            transaction,
            "{}".toByteArray(),
            MockFromTransactionRequestDto(status = 500, body = "oops"),
        ).shouldBeInstanceOf<MockRuleDraft.Valid>().rule

        rule.response.statusCode shouldBe 500
        rule.response.statusMessage shouldBe "Internal Server Error"
        rule.response.body shouldBe "oops"
    }

    @Test
    fun `from transaction leaves binary bodies out and says so`() {
        val image = transaction.copy(
            response = transaction.response?.copy(headers = mapOf("Content-Type" to listOf("image/png"))),
        )
        val draft = mockRuleFromTransaction(image, byteArrayOf(1, 2, 3), MockFromTransactionRequestDto())
            .shouldBeInstanceOf<MockRuleDraft.Valid>()

        draft.rule.response.body shouldBe null
        draft.bodyOmitted shouldBe true
    }

    @Test
    fun `from an in-flight transaction mocks a 200 with no body`() {
        val inFlight = transaction.copy(response = null)
        val rule = mockRuleFromTransaction(inFlight, null, MockFromTransactionRequestDto())
            .shouldBeInstanceOf<MockRuleDraft.Valid>().rule

        rule.response.statusCode shouldBe 200
        rule.response.body shouldBe null
    }

    @Test
    fun `rule DTO describes delay and behavior`() {
        val rule = CreateMockRuleRequestDto(urlPattern = "https://a", status = 200, delayMs = 100).valid()
        val dto = rule.toDto()

        dto.delay shouldBe "100 ms"
        dto.behavior shouldBe "Always"
        dto.matchType shouldBe "PREFIX"
    }
}
