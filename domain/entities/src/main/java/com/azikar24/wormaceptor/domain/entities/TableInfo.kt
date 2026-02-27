package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a database table with basic statistics.
 *
 * @property name Table name as defined in the database schema.
 * @property rowCount Number of rows currently stored in the table.
 * @property columnCount Number of columns defined in the table schema.
 */
data class TableInfo(
    val name: String,
    val rowCount: Long,
    val columnCount: Int,
)
