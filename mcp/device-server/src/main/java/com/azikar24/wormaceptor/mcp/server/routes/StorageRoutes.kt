package com.azikar24.wormaceptor.mcp.server.routes

import android.content.Context
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.domain.contracts.FileSystemRepository
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.feature.database.data.DatabaseDataSource
import com.azikar24.wormaceptor.feature.database.data.DatabaseRepositoryImpl
import com.azikar24.wormaceptor.feature.filebrowser.data.FileSystemDataSource
import com.azikar24.wormaceptor.feature.filebrowser.data.FileSystemRepositoryImpl
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesDataSource
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesRepositoryImpl
import com.azikar24.wormaceptor.mcp.server.security.resolveWithinRoots
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ApiResponse
import com.azikar24.wormaceptor.mcp.server.serialization.dto.DatabaseInfoDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.FileEntryDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.PreferenceFileDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.QueryResultDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ReadFileDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.dto.SecureStorageEntryDto
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import com.azikar24.wormaceptor.mcp.server.serialization.toReadFileDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 50
private const val DEFAULT_OFFSET = 0
private const val PATH_OUTSIDE_APP = "Path is outside the app's storage directories"

@Serializable
private data class SqlQueryRequest(
    val sql: String,
)

private fun resolveAppPath(
    path: String,
    context: Context,
    roots: List<FileEntry>,
): File? {
    val dataDir = File(context.applicationInfo.dataDir)
    return resolveWithinRoots(path, dataDir, roots.map { File(it.path) } + dataDir)
}

/** Built on first use: routes register before the host calls `WormaCeptor.init()`. */
private class StorageRepositories {
    val context: Context by lazy { WormaCeptorKoin.getKoin().get<Context>() }
    val preferences: PreferencesRepository by lazy { PreferencesRepositoryImpl(PreferencesDataSource(context)) }
    val database: DatabaseRepository by lazy { DatabaseRepositoryImpl(DatabaseDataSource(context)) }
    val files: FileSystemRepository by lazy { FileSystemRepositoryImpl(FileSystemDataSource(context)) }
}

internal fun Routing.storageRoutes() {
    val repositories = StorageRepositories()

    get("/api/preferences") {
        try {
            val prefsRepo = repositories.preferences

            val files = prefsRepo.observePreferenceFiles().first()
            val dtos = files.map { file ->
                val items = prefsRepo.observePreferenceItems(file.name).first()
                file.toDto(entries = items.map { it.toDto() })
            }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(PreferenceFileDto.serializer()),
                dtos,
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch preferences"),
            )
        }
    }

    get("/api/databases") {
        try {
            val dbRepo = repositories.database

            val databases = dbRepo.getDatabases()
            val dtos = databases.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(DatabaseInfoDto.serializer()),
                dtos,
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch databases"),
            )
        }
    }

    post("/api/databases/{name}/query") {
        try {
            val dbRepo = repositories.database

            val dbName = call.parameters["name"]
            if (dbName.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, error = "Missing database name"),
                )
                return@post
            }

            val request = call.receive<SqlQueryRequest>()
            val sql = request.sql.trim()

            if (!sql.uppercase().startsWith("SELECT")) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ApiResponse(
                        success = false,
                        error = "Only SELECT queries are allowed for safety",
                    ),
                )
                return@post
            }

            val result = dbRepo.executeQuery(dbName, sql)
            val dto = result.toDto()

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                QueryResultDto.serializer(),
                dto,
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Query execution failed"),
            )
        }
    }

    get("/api/files/browse") {
        try {
            val fileRepo = repositories.files

            val path = call.parameters["path"]
            val roots = fileRepo.getRootDirectories()

            val entries = if (path.isNullOrBlank() || path == "/") {
                roots
            } else {
                val dir = resolveAppPath(path, repositories.context, roots)
                if (dir == null) {
                    call.respond(HttpStatusCode.Forbidden, ApiResponse(success = false, error = PATH_OUTSIDE_APP))
                    return@get
                }
                fileRepo.listFiles(dir.path)
            }

            val dtos = entries.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(FileEntryDto.serializer()),
                dtos,
            )

            call.respond(
                ApiResponse(
                    success = true,
                    data = dataElement,
                    meta = ResponseMeta(total = entries.size),
                ),
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to browse files"),
            )
        }
    }

    get("/api/files/read") {
        try {
            val fileRepo = repositories.files

            val path = call.parameters["path"]
            if (path.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, error = "Missing path parameter"),
                )
                return@get
            }

            val file = resolveAppPath(path, repositories.context, fileRepo.getRootDirectories())
            if (file == null) {
                call.respond(HttpStatusCode.Forbidden, ApiResponse(success = false, error = PATH_OUTSIDE_APP))
                return@get
            }
            val dto = fileRepo.readFile(file.path).toReadFileDto()
            val dataElement: JsonElement = JsonConfig.instance.encodeToJsonElement(ReadFileDto.serializer(), dto)
            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to read file"),
            )
        }
    }

    get("/api/secure-storage") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val secureEngine = koin.getOrNull<SecureStorageEngine>()
            if (secureEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Secure storage engine not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET

            val typeFilter = call.parameters["type"]?.let { name ->
                SecureStorageEntry.StorageType.entries.find { it.name.equals(name, ignoreCase = true) }
            }

            if (secureEngine.entries.value.isEmpty()) awaitRefresh(secureEngine.isLoading, secureEngine::refresh)
            val allEntries = secureEngine.entries.value
                .let { entries -> if (typeFilter == null) entries else entries.filter { it.storageType == typeFilter } }
            val total = allEntries.size
            val paged = allEntries.drop(offset).take(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(SecureStorageEntryDto.serializer()),
                dtos,
            )

            call.respond(
                ApiResponse(
                    success = true,
                    data = dataElement,
                    meta = ResponseMeta(
                        total = total,
                        limit = limit,
                        offset = offset,
                    ),
                ),
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch secure storage"),
            )
        }
    }
}
