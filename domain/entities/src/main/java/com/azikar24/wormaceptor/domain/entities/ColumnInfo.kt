package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a database column with schema information.
 *
 * @property name Column name as defined in the schema.
 * @property type SQL data type of the column (e.g., "TEXT", "INTEGER").
 * @property isPrimaryKey Whether this column is part of the primary key.
 * @property isNullable Whether this column allows NULL values.
 */
data class ColumnInfo(
    val name: String,
    val type: String,
    val isPrimaryKey: Boolean,
    val isNullable: Boolean,
)
