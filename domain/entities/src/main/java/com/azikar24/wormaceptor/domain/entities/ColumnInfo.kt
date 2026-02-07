package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a database column with schema information.
 */
data class ColumnInfo(
    val name: String,
    val type: String,
    val isPrimaryKey: Boolean,
    val isNullable: Boolean,
)
