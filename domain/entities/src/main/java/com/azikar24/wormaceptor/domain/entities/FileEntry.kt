package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a file or directory entry in the file system.
 *
 * @property name File or directory name (without parent path).
 * @property path Absolute path to the file or directory.
 * @property isDirectory Whether this entry is a directory rather than a file.
 * @property sizeBytes Size of the file in bytes (0 for directories).
 * @property lastModified Epoch millis when the entry was last modified.
 * @property permissions Unix-style permission string (e.g., "rwxr-xr-x").
 * @property isReadable Whether the current process can read this entry.
 * @property isWritable Whether the current process can write to this entry.
 */
data class FileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val permissions: String,
    val isReadable: Boolean = true,
    val isWritable: Boolean = false,
)
