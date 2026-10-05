package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ApiResponse
import com.azikar24.wormaceptor.mcp.server.serialization.dto.DependencyInfoDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.LoadedLibraryDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 100
private const val DEFAULT_OFFSET = 0

internal fun Routing.inspectionRoutes() {
    get("/api/dependencies") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val depsEngine = koin.getOrNull<DependenciesInspectorEngine>()
            if (depsEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Dependencies inspector not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET
            val categoryFilter = call.parameters["category"]

            var dependencies = depsEngine.dependencies.value

            if (categoryFilter != null) {
                val category = try {
                    DependencyCategory.valueOf(categoryFilter.uppercase())
                } catch (_: IllegalArgumentException) {
                    null
                }
                if (category != null) {
                    dependencies = dependencies.filter { it.category == category }
                }
            }

            val total = dependencies.size
            val paged = dependencies.drop(offset).take(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(DependencyInfoDto.serializer()),
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
                ApiResponse(success = false, error = e.message ?: "Failed to fetch dependencies"),
            )
        }
    }

    get("/api/loaded-libraries") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val libEngine = koin.getOrNull<LoadedLibrariesEngine>()
            if (libEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Loaded libraries engine not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET
            val typeFilter = call.parameters["type"]
            val systemFilter = call.parameters["system"]?.toBooleanStrictOrNull()

            var libraries = libEngine.libraries.value

            if (typeFilter != null) {
                val libraryType = try {
                    LoadedLibrary.LibraryType.valueOf(typeFilter.uppercase())
                } catch (_: IllegalArgumentException) {
                    null
                }
                if (libraryType != null) {
                    libraries = libraries.filter { it.type == libraryType }
                }
            }

            if (systemFilter != null) {
                libraries = libraries.filter { it.isSystemLibrary == systemFilter }
            }

            val total = libraries.size
            val paged = libraries.drop(offset).take(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(LoadedLibraryDto.serializer()),
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
                ApiResponse(success = false, error = e.message ?: "Failed to fetch loaded libraries"),
            )
        }
    }
}
