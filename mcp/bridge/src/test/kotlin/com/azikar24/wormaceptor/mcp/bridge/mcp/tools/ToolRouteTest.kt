package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceApiClient
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/** Every tool must call a route the device server actually registers, with the same HTTP method. */
class ToolRouteTest {

    private data class Call(val method: String, val path: String)

    private val calls = mutableListOf<Call>()
    private val ok = buildJsonObject { put("success", true) }

    private val apiClient = mockk<DeviceApiClient> {
        coEvery { get(any(), any()) } answers {
            calls += Call("get", firstArg())
            ok
        }
        coEvery { post(any(), any()) } answers {
            calls += Call("post", firstArg())
            ok
        }
        coEvery { delete(any()) } answers {
            calls += Call("delete", firstArg())
            ok
        }
    }
    private val connection = mockk<DeviceConnection> { every { apiClient } returns this@ToolRouteTest.apiClient }

    // Superset of required params; tools ignore the ones they don't declare.
    private val args = buildJsonObject {
        put("id", "3f0e0c8e-8a5b-4a8e-9a63-2b9f4f1f2c11")
        put("database", "app.db")
        put("query", "SELECT 1")
        put("path", "files")
        put("latitude", 1.0)
        put("longitude", 2.0)
        put("title", "t")
        put("body", "b")
    }

    // Gradle runs tests with the module dir (mcp/bridge) as working directory.
    private val serverRoutes: List<Pair<String, Regex>> = File("../device-server/src/main/java")
        .walkTopDown()
        .filter { it.extension == "kt" }
        .flatMap { ROUTE.findAll(it.readText()) }
        .map { match ->
            val pattern = match.groupValues[2].split(Regex("\\{[^}]+}")).joinToString("[^/]+") { Regex.escape(it) }
            match.groupValues[1] to Regex(pattern)
        }
        .toList()

    @Test
    fun `every tool calls a registered device-server route`() = runTest {
        val unmatched = ToolRegistry.allTools().flatMap { tool ->
            calls.clear()
            runCatching { tool.execute(args, connection) }
            if (calls.isEmpty()) return@flatMap listOf("${tool.name}: made no request")
            calls.filterNot { call ->
                serverRoutes.any { (method, regex) ->
                    method == call.method && regex.matches(
                        call.path,
                    )
                }
            }
                .map { "${tool.name}: ${it.method.uppercase()} ${it.path}" }
        }
        assertEquals("tools calling routes the server doesn't have", emptyList<String>(), unmatched)
    }

    private companion object {
        val ROUTE = Regex("""\b(get|post|delete)\("(/api/[^"]*)"\)""")
    }
}
