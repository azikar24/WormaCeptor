package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

@Serializable
internal data class CrashDto(
    val id: Long,
    val timestamp: Long,
    val exceptionType: String,
    val message: String?,
    val stackTrace: String,
)

@Serializable
internal data class CrashSummaryDto(
    val id: Long,
    val timestamp: Long,
    val exceptionType: String,
    val message: String?,
)
