package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.CreateMockRuleRequestDto
import com.azikar24.wormaceptor.mcp.protocol.MockFromTransactionRequestDto
import com.azikar24.wormaceptor.mcp.protocol.MockFromTransactionResultDto
import com.azikar24.wormaceptor.mcp.protocol.MockRuleDto
import com.azikar24.wormaceptor.mcp.protocol.MockRulesDto
import com.azikar24.wormaceptor.mcp.protocol.SetMockRuleEnabledRequestDto
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.MockRuleDraft
import com.azikar24.wormaceptor.mcp.server.serialization.mockRuleFromTransaction
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import com.azikar24.wormaceptor.mcp.server.serialization.toMockRule
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.flow.first
import java.util.UUID

/** Responds with an error itself and returns null when the app has no mock rule repository. */
private suspend fun ApplicationCall.mockRepository(): MockRuleRepository? {
    val repository = WormaCeptorKoin.getKoin().getOrNull<MockRuleRepository>()
    if (repository == null) respond(ApiResponse(success = false, error = "Mock rules not available"))
    return repository
}

private suspend fun ApplicationCall.respondRule(dto: MockRuleDto) =
    respond(ApiResponse(success = true, data = JsonConfig.instance.encodeToJsonElement(MockRuleDto.serializer(), dto)))

private suspend fun ApplicationCall.respondInvalid(message: String) =
    respond(HttpStatusCode.BadRequest, ApiResponse(success = false, error = message))

private suspend fun ApplicationCall.respondRuleNotFound() =
    respond(HttpStatusCode.NotFound, ApiResponse(success = false, error = "Mock rule not found"))

// Writes go through the repository; the app syncs it into MockEngine, which the interceptor and Ktor plugin read.
internal fun Routing.mockRoutes() {
    get("/api/mock-rules") { call.listMockRules() }
    post("/api/mock-rules") { call.createMockRule() }
    post("/api/mock-rules/{id}/enabled") { call.setMockRuleEnabled() }
    delete("/api/mock-rules/{id}") { call.deleteMockRule() }
    post("/api/transactions/{id}/mock") { call.mockFromTransaction() }
}

private suspend fun ApplicationCall.listMockRules() {
    val repository = mockRepository() ?: return
    val dto = MockRulesDto(
        mockingEnabled = WormaCeptorKoin.getKoin().getOrNull<MockEngine>()?.mockingEnabled?.value ?: false,
        rules = repository.getAll().first().map { it.toDto() },
    )
    respond(ApiResponse(success = true, data = JsonConfig.instance.encodeToJsonElement(MockRulesDto.serializer(), dto)))
}

private suspend fun ApplicationCall.createMockRule() {
    val repository = mockRepository() ?: return
    when (val draft = receive<CreateMockRuleRequestDto>().toMockRule()) {
        is MockRuleDraft.Invalid -> respondInvalid(draft.message)
        is MockRuleDraft.Valid -> {
            repository.insert(draft.rule)
            respondRule(draft.rule.toDto())
        }
    }
}

private suspend fun ApplicationCall.setMockRuleEnabled() {
    val repository = mockRepository() ?: return
    val enabled = receive<SetMockRuleEnabledRequestDto>().enabled
    val rule = parameters["id"]?.let { repository.getById(it) } ?: return respondRuleNotFound()
    val updated = rule.copy(enabled = enabled)
    repository.update(updated)
    respondRule(updated.toDto())
}

private suspend fun ApplicationCall.deleteMockRule() {
    val repository = mockRepository() ?: return
    val id = parameters["id"]?.takeIf { repository.getById(it) != null } ?: return respondRuleNotFound()
    repository.delete(id)
    respond(ApiResponse(success = true))
}

private suspend fun ApplicationCall.mockFromTransaction() {
    val repository = mockRepository() ?: return
    val queryEngine = CoreHolder.queryEngine
        ?: return respond(ApiResponse(success = false, error = "Transaction engine not available"))
    val overrides = receive<MockFromTransactionRequestDto>()
    val transaction = parameters["id"]
        ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?.let { queryEngine.getDetails(it) }
        ?: return respond(HttpStatusCode.NotFound, ApiResponse(success = false, error = "Transaction not found"))
    val body = transaction.response?.bodyRef?.let { queryEngine.getBodyBytes(it) }
    when (val draft = mockRuleFromTransaction(transaction, body, overrides)) {
        is MockRuleDraft.Invalid -> respondInvalid(draft.message)
        is MockRuleDraft.Valid -> {
            repository.insert(draft.rule)
            val result = MockFromTransactionResultDto(draft.rule.toDto(), draft.bodyOmitted)
            val data = JsonConfig.instance.encodeToJsonElement(MockFromTransactionResultDto.serializer(), result)
            respond(ApiResponse(success = true, data = data))
        }
    }
}
