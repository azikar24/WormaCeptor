package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

/**
 * `GET /api/transactions/{id}/curl`. [redactedHeaders] appear in [command] with `[REDACTED]` values;
 * [bodyOmitted] is true when the request body was binary or too large and left out of [command].
 */
@Serializable
internal data class CurlDto(
    val command: String,
    val redactedHeaders: List<String>,
    val bodyOmitted: Boolean,
)

/** `POST /api/transactions/{id}/replay`. Set fields replace the stored request's; [headers] merge by name. */
@Serializable
internal data class ReplayRequestDto(
    val url: String? = null,
    val headers: Map<String, String>? = null,
    val body: String? = null,
)

/** The replayed request, captured as a new transaction [transactionId]. [code] is null when it failed. */
@Serializable
internal data class ReplayResultDto(
    val transactionId: String,
    val method: String,
    val url: String,
    val code: Int?,
    val message: String?,
    val durationMs: Long,
    val error: String?,
)
