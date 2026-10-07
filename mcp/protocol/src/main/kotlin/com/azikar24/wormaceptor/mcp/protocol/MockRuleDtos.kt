package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

/**
 * A mock rule as the app stores it. [matchType] is `EXACT`, `PREFIX` or `REGEX`; [method] null matches any;
 * [delay] and [behavior] are human-readable (`none`, `300 ms`, `100-500 ms`; `Always`, `FirstN(3)`, ...).
 */
@Serializable
internal data class MockRuleDto(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val method: String?,
    val urlPattern: String,
    val matchType: String,
    val status: Int,
    val statusMessage: String,
    val contentType: String,
    val body: String?,
    val delay: String,
    val behavior: String,
    val priority: Int,
    val createdAt: Long,
)

/** `GET /api/mock-rules`. Rules only apply while [mockingEnabled] is true. */
@Serializable
internal data class MockRulesDto(
    val mockingEnabled: Boolean,
    val rules: List<MockRuleDto>,
)

/** `POST /api/mock-rules`. Unset fields take the app's defaults (PREFIX match, any method, application/json). */
@Serializable
internal data class CreateMockRuleRequestDto(
    val urlPattern: String,
    val status: Int,
    val method: String? = null,
    val matchType: String? = null,
    val body: String? = null,
    val contentType: String? = null,
    val delayMs: Long? = null,
    val enabled: Boolean? = null,
)

/** `POST /api/transactions/{id}/mock`: overrides for the copied response. */
@Serializable
internal data class MockFromTransactionRequestDto(
    val status: Int? = null,
    val body: String? = null,
)

/** The created rule; [bodyOmitted] is true when the captured response body was binary or too large to copy. */
@Serializable
internal data class MockFromTransactionResultDto(
    val rule: MockRuleDto,
    val bodyOmitted: Boolean,
)

/** `POST /api/mock-rules/{id}/enabled`. */
@Serializable
internal data class SetMockRuleEnabledRequestDto(
    val enabled: Boolean,
)
