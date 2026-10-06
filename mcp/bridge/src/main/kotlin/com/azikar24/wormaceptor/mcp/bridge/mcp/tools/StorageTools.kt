package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class ListPreferencesTool : McpTool() {

    override val name = "list_preferences"

    override val description = "List all SharedPreferences files and their key-value entries from the Android app. " +
        "Returns file names with each entry's key, value, and data type (String, Int, Boolean, etc.). " +
        "Use to inspect app configuration, user settings, cached flags, " +
        "or debug preference-related behavior."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/preferences")
        val data = response.jsonObject.arrayOrNull("data") ?: return response.serverError() ?: "No preferences found."
        return TextFormatter.formatPreferences(data)
    }
}

internal class ListDatabasesTool : McpTool() {

    override val name = "list_databases"

    override val description = "List all SQLite databases in the running Android app, including their size and table names. " +
        "Returns database name, file size in bytes, and a list of table names for each database. " +
        "Use to discover which databases exist before querying them, " +
        "or to get an overview of the app's data layer."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/databases")
        val data = response.jsonObject.arrayOrNull("data") ?: return response.serverError() ?: "No databases found."
        return TextFormatter.formatDatabaseList(data)
    }
}

internal class QueryDatabaseTool : McpTool() {

    override val name = "query_database"

    override val description = "Execute a SQL SELECT query against a specific SQLite database in the Android app. " +
        "Only SELECT queries are allowed, so the app's data can't be modified: INSERT, UPDATE, DELETE, DROP, " +
        "PRAGMA, and other statements are rejected. " +
        "Returns results as a formatted table with columns and rows. " +
        "Use to inspect database contents, debug data issues, verify data integrity, " +
        "or explore table schemas (e.g., 'SELECT * FROM sqlite_master')."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("database") {
                put("type", "string")
                put("description", "Database name from list_databases (e.g., 'app_database', 'my_db')")
            }
            putJsonObject("query") {
                put("type", "string")
                put(
                    "description",
                    "SQL SELECT query to execute. Only SELECT statements are allowed. " +
                        "Example: 'SELECT * FROM users WHERE active = 1 LIMIT 50'",
                )
            }
        }
        putJsonArray("required") {
            add(JsonPrimitive("database"))
            add(JsonPrimitive("query"))
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val database = arguments["database"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'database' parameter is required."
        if (database.contains("..")) {
            return "Error: 'database' parameter must not contain path traversal sequences (..)"
        }
        val query = arguments["query"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'query' parameter is required."
        val normalizedQuery = query.trim().uppercase()
        if (!normalizedQuery.startsWith("SELECT")) {
            return "Error: Only SELECT queries are allowed."
        }
        val forbiddenKeywords = listOf("DROP", "DELETE", "INSERT", "UPDATE", "ALTER")
        for (keyword in forbiddenKeywords) {
            if (KEYWORD_BOUNDARY_REGEX(keyword).containsMatchIn(normalizedQuery)) {
                return "Error: $keyword operations are not allowed. Only SELECT queries are permitted."
            }
        }

        val body = buildJsonObject {
            // Field name of the device server's SqlQueryRequest
            put("sql", query)
        }

        val response = connection.apiClient.post("/api/databases/$database/query", body)
        val data = response.jsonObject.objectOrNull("data") ?: return response.serverError() ?: "No results returned."
        return TextFormatter.formatQueryResult(data)
    }

    companion object {
        private fun KEYWORD_BOUNDARY_REGEX(keyword: String) = "\\b$keyword\\b".toRegex(RegexOption.IGNORE_CASE)
    }
}

internal class ListFilesTool : McpTool() {

    override val name = "list_files"

    override val description = "Browse files in the Android app's internal or external storage directories. " +
        "Returns file names, sizes, and whether each entry is a file or directory. " +
        "Use to explore app storage, find cached files, locate downloaded content, " +
        "or verify file creation/deletion."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("path") {
                put("type", "string")
                put(
                    "description",
                    "Directory path to browse, relative to app's data directory. " +
                        "Omit or use '/' for the root of the app's data directory.",
                )
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val path = arguments["path"]?.jsonPrimitive?.contentOrNull
        if (path != null && path.contains("..")) {
            return "Error: 'path' parameter must not contain path traversal sequences (..)"
        }
        val params = buildMap {
            path?.let { put("path", it) }
        }

        val response = connection.apiClient.get("/api/files/browse", params)
        val data = response.jsonObject.arrayOrNull("data") ?: return response.serverError() ?: "No files found."
        return TextFormatter.formatFileList(data)
    }
}

internal class ReadFileTool : McpTool() {

    override val name = "read_file"

    override val description = "Read the contents of a specific file from the Android app's storage. " +
        "Returns text files as-is and JSON/XML pretty-printed. Binary files, images, and PDFs return " +
        "a one-line summary (size, dimensions, or page count) instead of their content, and files " +
        "over the size limit return a size notice. " +
        "Use to inspect configuration files, log files, cached data, " +
        "or any other text-based files in the app's storage."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("path") {
                put("type", "string")
                put(
                    "description",
                    "File path relative to app's data directory (e.g., 'files/config.json', 'cache/data.txt')",
                )
            }
        }
        putJsonArray("required") { add(JsonPrimitive("path")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val path = arguments["path"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'path' parameter is required."
        if (path.contains("..")) {
            return "Error: 'path' parameter must not contain path traversal sequences (..)"
        }

        val response = connection.apiClient.get("/api/files/read", mapOf("path" to path))
        val data = response.jsonObject.objectOrNull(
            "data",
        ) ?: return response.serverError() ?: "File not found or unreadable."
        val content = data["content"]?.jsonPrimitive?.contentOrNull ?: return "File is empty."
        val mimeType = data["mimeType"]?.jsonPrimitive?.contentOrNull

        val sb = StringBuilder()
        if (mimeType != null) sb.appendLine("MIME Type: $mimeType")
        sb.appendLine()
        sb.append(content)
        return sb.toString()
    }
}

internal class BrowseSecureStorageTool : McpTool() {

    override val name = "browse_secure_storage"

    override val description = "Browse entries in the app's secure storage " +
        "(EncryptedSharedPreferences, Android Keystore, DataStore). " +
        "Returns key names, storage type, and encryption flags; values are never sent off the device. " +
        "Use to verify that sensitive data is stored securely, " +
        "check for expected encryption keys, or audit secure storage usage."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("type") {
                put("type", "string")
                putJsonArray("enum") { SecureStorageEntry.StorageType.entries.forEach { add(JsonPrimitive(it.name)) } }
                put("description", "Only return entries from this storage backend")
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            arguments["type"]?.jsonPrimitive?.contentOrNull?.let { put("type", it) }
        }
        val response = connection.apiClient.get("/api/secure-storage", params)
        val data = response.jsonObject.arrayOrNull(
            "data",
        ) ?: return response.serverError() ?: "No secure storage entries found."
        return TextFormatter.formatGenericList(data, "secure storage entry(ies)")
    }
}

internal class ListDependenciesTool : McpTool() {

    override val name = "list_dependencies"

    override val description = "List third-party dependencies (libraries) included in the running Android app. " +
        "Returns library names, versions, and group IDs where available. " +
        "Use to audit what libraries are bundled, check for outdated dependencies, " +
        "or verify that expected libraries are included in the build."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("category") {
                put("type", "string")
                putJsonArray("enum") { DependencyCategory.entries.forEach { add(JsonPrimitive(it.name)) } }
                put("description", "Only return dependencies in this category")
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            arguments["category"]?.jsonPrimitive?.contentOrNull?.let { put("category", it) }
        }
        val response = connection.apiClient.get("/api/dependencies", params)
        val data = response.jsonObject.arrayOrNull(
            "data",
        ) ?: return response.serverError() ?: "No dependency information available."
        return TextFormatter.formatGenericList(data, "dependency(ies)")
    }
}

internal class ListLoadedLibrariesTool : McpTool() {

    override val name = "list_loaded_libraries"

    override val description = "List libraries loaded by the running Android app process: " +
        "native .so files, DEX files, JARs, and AAR resources. " +
        "Returns names, paths, types, and whether each is a system library. " +
        "Use to verify native library loading, debug UnsatisfiedLinkError crashes, " +
        "or audit which native code is active in the app."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("type") {
                put("type", "string")
                putJsonArray("enum") { LoadedLibrary.LibraryType.entries.forEach { add(JsonPrimitive(it.name)) } }
                put("description", "Only return libraries of this type")
            }
            putJsonObject("system") {
                put("type", "boolean")
                put("description", "true for system libraries only, false for app libraries only; omit for both")
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            arguments["type"]?.jsonPrimitive?.contentOrNull?.let { put("type", it) }
            arguments["system"]?.jsonPrimitive?.booleanOrNull?.let { put("system", it.toString()) }
        }
        val response = connection.apiClient.get("/api/loaded-libraries", params)
        val data = response.jsonObject.arrayOrNull(
            "data",
        ) ?: return response.serverError() ?: "No loaded libraries information available."
        return TextFormatter.formatGenericList(data, "loaded library(ies)")
    }
}
