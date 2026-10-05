package com.azikar24.wormaceptor.mcp.server.serialization.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LeakInfoDto(
    val timestamp: Long,
    val objectClass: String,
    val leakDescription: String,
    val retainedSize: Long,
    val referencePath: List<String>,
    val severity: String,
)

@Serializable
internal data class LeakSummaryDto(
    val totalLeaks: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val totalRetainedBytes: Long,
)

@Serializable
internal data class ThreadViolationDto(
    val id: Long,
    val timestamp: Long,
    val violationType: String,
    val description: String,
    val stackTrace: List<String>,
    val durationMs: Long?,
    val threadName: String,
)

@Serializable
internal data class ViolationStatsDto(
    val totalViolations: Int,
    val diskReadCount: Int,
    val diskWriteCount: Int,
    val networkCount: Int,
    val slowCallCount: Int,
    val customSlowCodeCount: Int,
)
