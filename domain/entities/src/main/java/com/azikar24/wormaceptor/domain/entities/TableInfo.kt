package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a database table with basic statistics.
 */
data class TableInfo(
    val name: String,
    val rowCount: Long,
    val columnCount: Int,
)
