package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the result of a SQL query execution.
 */
data class QueryResult(
    val columns: List<String>,
    val rows: List<List<Any?>>,
    val rowCount: Int,
    val error: String? = null,
) {
    val isSuccess: Boolean get() = error == null
}
