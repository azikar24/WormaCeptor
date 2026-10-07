package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

@Serializable
internal data class LogEntryDto(
    val id: Long,
    val timestamp: Long,
    val level: String,
    val tag: String,
    val pid: Int,
    val tid: Int,
    val message: String,
)
