package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.DependencyInfoDto
import com.azikar24.wormaceptor.mcp.protocol.LoadedLibraryDto
import com.azikar24.wormaceptor.mcp.protocol.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 100
private const val DEFAULT_OFFSET = 0

private const val RefreshTimeoutMs = 10_000L
private const val RefreshStartDelayMs = 100L

/** Engines that scan lazily only fill after the in-app screen calls `refresh()`, so trigger it on first read. */
internal suspend fun awaitRefresh(
    isLoading: StateFlow<Boolean>,
    refresh: () -> Unit,
) {
    refresh()
    withTimeoutOrNull(RefreshTimeoutMs) {
        delay(RefreshStartDelayMs)
        isLoading.first { !it }
    }
}

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

            // Koin creates the engine on first use, so its startup scan may not have finished (or started) yet.
            if (depsEngine.dependencies.value.isEmpty()) awaitRefresh(depsEngine.isLoading, depsEngine::refresh)
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

            if (libEngine.libraries.value.isEmpty()) awaitRefresh(libEngine.isLoading, libEngine::refresh)
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
