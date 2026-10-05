package com.azikar24.wormaceptor.mcp.server.serialization.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class TransactionSummaryDto(
    val id: String,
    val method: String,
    val host: String,
    val path: String,
    val code: Int?,
    val tookMs: Long?,
    val hasRequestBody: Boolean,
    val hasResponseBody: Boolean,
    val status: String,
    val timestamp: Long,
)

@Serializable
internal data class TransactionDetailDto(
    val id: String,
    val timestamp: Long,
    val durationMs: Long?,
    val status: String,
    val request: RequestDto,
    val response: ResponseDto?,
    val extensions: Map<String, String>,
)

@Serializable
internal data class RequestDto(
    val url: String,
    val method: String,
    val headers: Map<String, List<String>>,
    val bodySize: Long,
)

@Serializable
internal data class ResponseDto(
    val code: Int,
    val message: String,
    val headers: Map<String, List<String>>,
    val error: String?,
    val protocol: String?,
    val tlsVersion: String?,
    val bodySize: Long,
)

@Serializable
internal data class RateLimitConfigDto(
    val enabled: Boolean,
    val downloadSpeedKbps: Long,
    val uploadSpeedKbps: Long,
    val latencyMs: Long,
    val packetLossPercent: Float,
    val preset: String?,
)

/**
 * Partial rate-limit update. Every field is optional:
 * `enabled = false` turns throttling off, a [preset] applies that preset,
 * and any custom values are merged onto the current configuration.
 */
@Serializable
internal data class SetRateLimitRequestDto(
    val enabled: Boolean? = null,
    val preset: String? = null,
    val downloadSpeedKbps: Long? = null,
    val uploadSpeedKbps: Long? = null,
    val latencyMs: Long? = null,
    val packetLossPercent: Float? = null,
)
