/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.database.data

import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo

/**
 * Implementation of DatabaseRepository.
 */
class DatabaseRepositoryImpl(
    private val dataSource: DatabaseDataSource,
) : DatabaseRepository {

    private val databasePaths = mutableMapOf<String, String>()

    override fun getDatabases(): List<DatabaseInfo> {
        val databases = dataSource.findDatabases()
        // Cache database paths for later use
        databases.forEach { databasePaths[it.name] = it.path }
        return databases
    }

    override fun getTables(databaseName: String): List<TableInfo> {
        val path = databasePaths[databaseName] ?: return emptyList()
        return dataSource.getTables(path)
    }

    override fun getTableSchema(databaseName: String, tableName: String): List<ColumnInfo> {
        val path = databasePaths[databaseName] ?: return emptyList()
        return dataSource.getTableSchema(path, tableName)
    }

    override fun queryTable(
        databaseName: String,
        tableName: String,
        limit: Int,
        offset: Int,
    ): QueryResult {
        val path = databasePaths[databaseName] ?: return QueryResult(
            columns = emptyList(),
            rows = emptyList(),
            rowCount = 0,
            error = "Database not found",
        )
        return dataSource.queryTable(path, tableName, limit, offset)
    }

    override fun executeQuery(databaseName: String, query: String): QueryResult {
        val path = databasePaths[databaseName] ?: return QueryResult(
            columns = emptyList(),
            rows = emptyList(),
            rowCount = 0,
            error = "Database not found",
        )
        return dataSource.executeQuery(path, query)
    }
}
