package com.azikar24.wormaceptor.domain.entities

/**
 * Detailed information about a file.
 *
 * @property name File name including extension.
 * @property path Absolute path to the file.
 * @property sizeBytes Size of the file in bytes.
 * @property lastModified Epoch millis when the file was last modified.
 * @property mimeType Detected MIME type of the file, or null if unknown.
 * @property isReadable Whether the current process can read this file.
 * @property isWritable Whether the current process can write to this file.
 * @property extension File extension without the leading dot, or null if absent.
 * @property parentPath Absolute path of the parent directory, or null for root.
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
