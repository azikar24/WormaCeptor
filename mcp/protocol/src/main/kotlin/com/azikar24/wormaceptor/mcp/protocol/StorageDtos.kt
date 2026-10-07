package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

@Serializable
internal data class PreferenceDto(
    val key: String,
    val value: String,
    val type: String,
)

@Serializable
internal data class PreferenceFileDto(
    val name: String,
    val itemCount: Int,
    val entries: List<PreferenceDto>,
)

@Serializable
internal data class DatabaseInfoDto(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val tableCount: Int,
)

@Serializable
internal data class TableInfoDto(
    val name: String,
    val rowCount: Long,
    val columnCount: Int,
)

@Serializable
internal data class ColumnInfoDto(
    val name: String,
    val type: String,
    val isPrimaryKey: Boolean,
    val isNullable: Boolean,
)

@Serializable
internal data class SqlQueryRequestDto(
    val sql: String,
)

@Serializable
internal data class QueryResultDto(
    val columns: List<String>,
    val rows: List<List<String?>>,
    val rowCount: Int,
    val error: String?,
)

@Serializable
internal data class FileEntryDto(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val permissions: String,
    val isReadable: Boolean,
    val isWritable: Boolean,
)

@Serializable
internal data class FileInfoDto(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val mimeType: String?,
    val isReadable: Boolean,
    val isWritable: Boolean,
    val extension: String?,
    val parentPath: String?,
)

@Serializable
internal data class ReadFileDto(
    val content: String,
    val mimeType: String?,
)
