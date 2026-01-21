/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.database.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo
import java.io.File

/**
 * Data source for reading SQLite databases using Android SQLite APIs.
 * All database operations are read-only for safety.
 */
class DatabaseDataSource(private val context: Context) {

    /**
     * Finds all SQLite databases in the app's data directory.
     */
    fun findDatabases(): List<DatabaseInfo> {
        return try {
            context.databaseList()
                .filter { it.endsWith(".db") || (!it.contains("-journal") && !it.contains("-wal") && !it.contains("-shm")) }
                .mapNotNull { dbName ->
                    try {
                        val dbPath = context.getDatabasePath(dbName).absolutePath
                        val file = File(dbPath)

                        if (!file.exists()) return@mapNotNull null

                        val sizeBytes = file.length()
                        val tableCount = countTables(dbPath)

                        DatabaseInfo(
                            name = dbName,
                            path = dbPath,
                            sizeBytes = sizeBytes,
                            tableCount = tableCount,
                        )
                    } catch (e: Exception) {
                        null // Skip databases that can't be opened
                    }
                }
                .sortedBy { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Gets all tables in a database.
     */
    fun getTables(databasePath: String): List<TableInfo> {
        return openDatabase(databasePath)?.use { db ->
            try {
                val cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' ORDER BY name",
                    null,
                )

                cursor.use {
                    val tables = mutableListOf<TableInfo>()
                    while (it.moveToNext()) {
                        val tableName = it.getString(0)
                        val rowCount = getRowCount(db, tableName)
                        val columnCount = getColumnCount(db, tableName)

                        tables.add(
                            TableInfo(
                                name = tableName,
                                rowCount = rowCount,
                                columnCount = columnCount,
                            ),
                        )
                    }
                    tables
                }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    /**
     * Gets schema information for a table.
     */
    fun getTableSchema(databasePath: String, tableName: String): List<ColumnInfo> {
        return openDatabase(databasePath)?.use { db ->
            try {
                val cursor = db.rawQuery("PRAGMA table_info('$tableName')", null)

                cursor.use {
                    val columns = mutableListOf<ColumnInfo>()
                    while (it.moveToNext()) {
                        val name = it.getString(it.getColumnIndexOrThrow("name"))
                        val type = it.getString(it.getColumnIndexOrThrow("type"))
                        val notNull = it.getInt(it.getColumnIndexOrThrow("notnull")) == 1
                        val pk = it.getInt(it.getColumnIndexOrThrow("pk")) > 0

                        columns.add(
                            ColumnInfo(
                                name = name,
                                type = type.ifEmpty { "TEXT" },
                                isPrimaryKey = pk,
                                isNullable = !notNull,
                            ),
                        )
                    }
                    columns
                }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    /**
     * Queries a table with pagination.
     */
    fun queryTable(databasePath: String, tableName: String, limit: Int, offset: Int): QueryResult {
        return openDatabase(databasePath)?.use { db ->
            try {
                val query = "SELECT * FROM `$tableName` LIMIT $limit OFFSET $offset"
                executeRawQuery(db, query)
            } catch (e: Exception) {
                QueryResult(
                    columns = emptyList(),
                    rows = emptyList(),
                    rowCount = 0,
                    error = e.message ?: "Unknown error",
                )
            }
        } ?: QueryResult(
            columns = emptyList(),
            rows = emptyList(),
            rowCount = 0,
            error = "Could not open database",
        )
    }

    /**
     * Executes a custom SQL query.
     */
    fun executeQuery(databasePath: String, query: String): QueryResult {
        return openDatabase(databasePath)?.use { db ->
            try {
                executeRawQuery(db, query)
            } catch (e: Exception) {
                QueryResult(
                    columns = emptyList(),
                    rows = emptyList(),
                    rowCount = 0,
                    error = e.message ?: "Query execution failed",
                )
            }
        } ?: QueryResult(
            columns = emptyList(),
            rows = emptyList(),
            rowCount = 0,
            error = "Could not open database",
        )
    }

    /**
     * Opens a database in read-only mode.
     */
    private fun openDatabase(path: String): SQLiteDatabase? {
        return try {
            SQLiteDatabase.openDatabase(
                path,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Counts the number of tables in a database.
     */
    private fun countTables(databasePath: String): Int {
        return openDatabase(databasePath)?.use { db ->
            try {
                val cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
                    null,
                )
                cursor.use {
                    if (it.moveToFirst()) it.getInt(0) else 0
                }
            } catch (e: Exception) {
                0
            }
        } ?: 0
    }

    /**
     * Gets the row count for a table.
     */
    private fun getRowCount(db: SQLiteDatabase, tableName: String): Long {
        return try {
            val cursor = db.rawQuery("SELECT COUNT(*) FROM `$tableName`", null)
            cursor.use {
                if (it.moveToFirst()) it.getLong(0) else 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Gets the column count for a table.
     */
    private fun getColumnCount(db: SQLiteDatabase, tableName: String): Int {
        return try {
            val cursor = db.rawQuery("SELECT * FROM `$tableName` LIMIT 0", null)
            cursor.use { it.columnCount }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Executes a raw query and converts the cursor to a QueryResult.
     */
    private fun executeRawQuery(db: SQLiteDatabase, query: String): QueryResult {
        val cursor = db.rawQuery(query, null)

        return cursor.use {
            val columns = it.columnNames.toList()
            val rows = mutableListOf<List<Any?>>()

            while (it.moveToNext() && rows.size < 1000) { // Hard limit to prevent OOM
                val row = mutableListOf<Any?>()
                for (i in 0 until it.columnCount) {
                    val value = when (it.getType(i)) {
                        Cursor.FIELD_TYPE_NULL -> null
                        Cursor.FIELD_TYPE_INTEGER -> it.getLong(i)
                        Cursor.FIELD_TYPE_FLOAT -> it.getDouble(i)
                        Cursor.FIELD_TYPE_STRING -> it.getString(i)
                        Cursor.FIELD_TYPE_BLOB -> "<BLOB ${it.getBlob(i)?.size ?: 0} bytes>"
                        else -> it.getString(i)
                    }
                    row.add(value)
                }
                rows.add(row)
            }

            QueryResult(
                columns = columns,
                rows = rows,
                rowCount = rows.size,
                error = null,
            )
        }
    }
}
