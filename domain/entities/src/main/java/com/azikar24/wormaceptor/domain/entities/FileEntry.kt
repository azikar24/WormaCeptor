/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a file or directory entry in the file system.
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
