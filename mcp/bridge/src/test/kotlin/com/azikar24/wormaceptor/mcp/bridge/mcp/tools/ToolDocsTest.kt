package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ToolDocsTest {

    // Gradle runs tests with the module dir (mcp/bridge) as working directory.
    private val docRows: Map<String, String> = File("../../docs/MCP.md").readLines()
        .dropWhile { !it.startsWith("## MCP Tools") }
        .drop(1)
        .takeWhile { !it.startsWith("## ") }
        .mapNotNull { TOOL_ROW.matchEntire(it.trim()) }
        .associate { it.groupValues[1] to it.groupValues[2] }

    private fun McpTool.documentedParams(): Set<String> {
        val required = inputSchema["required"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty().toSet()
        return inputSchema["properties"]?.jsonObject?.keys.orEmpty()
            .map { if (it in required) it else "$it?" }
            .toSet()
    }

    @Test
    fun `docs list exactly the registered tools`() {
        assertEquals(ToolRegistry.allTools().map { it.name }.toSet(), docRows.keys)
    }

    @Test
    fun `docs list each tool's params with correct optionality`() {
        val mismatches = ToolRegistry.allTools().mapNotNull { tool ->
            val documented = docRows[tool.name].orEmpty()
                .split(",")
                .map { it.trim().trim('`') }
                .filter { it.isNotEmpty() && it != "—" }
                .toSet()
            "${tool.name}: code=${tool.documentedParams()} docs=$documented"
                .takeIf { tool.documentedParams() != documented }
        }
        assertEquals("docs/MCP.md param drift", emptyList<String>(), mismatches)
    }

    private companion object {
        val TOOL_ROW = Regex("""^\| `([a-z_]+)` \| ([^|]*) \|.*$""")
    }
}
