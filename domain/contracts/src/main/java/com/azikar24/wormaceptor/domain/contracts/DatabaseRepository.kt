package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo

/**
 * Repository interface for accessing SQLite databases in the app.
 */
interface DatabaseRepository {

    /**
     * Gets all SQLite databases in the app's data directory.
     */
    fun getDatabases(): List<DatabaseInfo>

    /**
     * Gets all tables in a specific database.
     *
     * @param databaseName The name of the database file
     */
    fun getTables(databaseName: String): List<TableInfo>

    /**
     * Gets the schema information for a specific table.
     *
     * @param databaseName The name of the database file
     * @param tableName The name of the table
     */
    fun getTableSchema(databaseName: String, tableName: String): List<ColumnInfo>

    /**
     * Queries a table with pagination support.
     *
     * @param databaseName The name of the database file
     * @param tableName The name of the table
     * @param limit Maximum number of rows to return
     * @param offset Number of rows to skip
     */
    fun queryTable(databaseName: String, tableName: String, limit: Int = 100, offset: Int = 0): QueryResult

    /**
     * Executes a custom SQL query.
     *
     * @param databaseName The name of the database file
     * @param query The SQL query to execute
     */
    fun executeQuery(databaseName: String, query: String): QueryResult
}
