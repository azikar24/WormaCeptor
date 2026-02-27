package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a SQLite database file with metadata.
 *
 * @property name Database file name (e.g., "app.db").
 * @property path Absolute path to the database file on disk.
 * @property sizeBytes Size of the database file in bytes.
 * @property tableCount Number of user tables in the database.
 */
data class DatabaseInfo(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val tableCount: Int,
)
