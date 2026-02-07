package com.azikar24.wormaceptor.domain.entities

/**
 * Detailed information about a file.
 */
data class FileInfo(
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
