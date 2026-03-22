package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the result of a SQL query execution.
 *
 * @property columns Ordered list of column names returned by the query.
 * @property rows Result rows, where each row is a list of column values (nullable).
 * @property rowCount Number of rows in the result set.
 * @property error Error message if the query failed, or null on success.
 */
data class QueryResult(
    val columns: List<String>,
    val rows: List<List<Any?>>,
    val rowCount: Int,
    val error: String? = null,
) {
    /** Whether the query executed successfully (no error). */
    val isSuccess: Boolean get() = error == null
}
