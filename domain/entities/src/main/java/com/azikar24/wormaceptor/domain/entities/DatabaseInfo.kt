/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a SQLite database file with metadata.
 */
data class DatabaseInfo(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val tableCount: Int,
)
