package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import com.azikar24.wormaceptor.mcp.bridge.util.dataAs
import com.azikar24.wormaceptor.mcp.bridge.util.errorText
import com.azikar24.wormaceptor.mcp.bridge.util.toApiResponse
import com.azikar24.wormaceptor.mcp.bridge.util.toRequestBody
import com.azikar24.wormaceptor.mcp.protocol.CreateMockRuleRequestDto
import com.azikar24.wormaceptor.mcp.protocol.MockFromTransactionRequestDto
import com.azikar24.wormaceptor.mcp.protocol.MockFromTransactionResultDto
import com.azikar24.wormaceptor.mcp.protocol.MockRuleDto
import com.azikar24.wormaceptor.mcp.protocol.MockRulesDto
import com.azikar24.wormaceptor.mcp.protocol.SetMockRuleEnabledRequestDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private const val AppliesTo = "Mocks apply to OkHttp and Ktor clients that use the WormaCeptor interceptor or plugin."

private val Writes = ToolAnnotations(readOnlyHint = false, destructiveHint = false, idempotentHint = false)

private fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

private fun idSchema(description: String) = buildJsonObject {
    put("type", "string")
    put("description", description)
}

internal class ListMockRulesTool : McpTool() {

    override val name = "list_mock_rules"

    override val description = "List the app's mock rules: URL pattern and match type, method, mocked status, " +
        "content type, body preview, delay, and whether each rule is enabled. Also shows whether mocking is " +
        "on globally. $AppliesTo"

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/mock-rules").toApiResponse()
        val data = response.dataAs<MockRulesDto>() ?: return response.errorText() ?: "Mock rules are empty."
        return TextFormatter.formatMockRules(data)
    }
}

internal class CreateMockRuleTool : McpTool() {

    override val name = "create_mock_rule"

    override val annotations = Writes

    override val description = "Create a mock rule so matching requests get a canned response instead of " +
        "hitting the network. Use to force error, empty, or slow states (status 500, an empty list, " +
        "delay_ms 5000) and then check how the UI handles them. $AppliesTo " +
        "The pattern is matched against the full request URL including scheme and query."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("url_pattern") {
                put("type", "string")
                put(
                    "description",
                    "PREFIX (default) and EXACT compare against the full URL, e.g. 'https://api.example.com/users'. " +
                        "REGEX searches anywhere in the URL, e.g. '/users/\\d+'.",
                )
            }
            putJsonObject("match_type") {
                put("type", "string")
                putJsonArray("enum") { MATCH_TYPES.forEach { add(JsonPrimitive(it)) } }
                put("description", "How url_pattern is matched (default: PREFIX)")
            }
            putJsonObject("method") {
                put("type", "string")
                put("description", "HTTP method to match, e.g. 'GET'. Omit to match any method.")
            }
            putJsonObject("status") {
                put("type", "integer")
                put("description", "HTTP status code to return, 100 to 599")
            }
            putJsonObject("body") {
                put("type", "string")
                put("description", "Response body to return (default: empty)")
            }
            putJsonObject("content_type") {
                put("type", "string")
                put("description", "Response Content-Type (default: application/json)")
            }
            putJsonObject("delay_ms") {
                put("type", "integer")
                put("description", "Delay before the mock response is returned, in milliseconds (default: 0)")
            }
            putJsonObject("enabled") {
                put("type", "boolean")
                put("description", "Whether the rule is active right away (default: true)")
            }
        }
        putJsonArray("required") {
            add(JsonPrimitive("url_pattern"))
            add(JsonPrimitive("status"))
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val urlPattern = arguments.string("url_pattern")?.takeIf { it.isNotBlank() }
            ?: return "Error: 'url_pattern' parameter is required and must be non-empty."
        val status = arguments["status"]?.jsonPrimitive?.intOrNull
            ?: return "Error: 'status' parameter is required and must be an integer."
        val body = CreateMockRuleRequestDto(
            urlPattern = urlPattern,
            status = status,
            method = arguments.string("method"),
            matchType = arguments.string("match_type"),
            body = arguments.string("body"),
            contentType = arguments.string("content_type"),
            delayMs = arguments["delay_ms"]?.jsonPrimitive?.longOrNull,
            enabled = arguments["enabled"]?.jsonPrimitive?.booleanOrNull,
        )
        val response = connection.apiClient.post("/api/mock-rules", body.toRequestBody()).toApiResponse()
        val rule = response.dataAs<MockRuleDto>() ?: return response.errorText() ?: "Mock rule created."
        return "Mock rule created.\n\n" + TextFormatter.formatMockRule(rule)
    }

    companion object {
        /** Mirrors `UrlMatchType` in the app. */
        internal val MATCH_TYPES = listOf("EXACT", "PREFIX", "REGEX")
    }
}

internal class MockFromTransactionTool : McpTool() {

    override val name = "mock_from_transaction"

    override val annotations = Writes

    override val description = "Create a mock rule from a captured transaction, like the in-app 'Add to Mock': " +
        "the rule matches exactly that URL and method and returns the captured response (status, " +
        "content type, body). Optionally override the status or body, e.g. replay a real payload with " +
        "status 500, or the real endpoint with an edited body. $AppliesTo"

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            put("id", idSchema("Transaction ID from list_transactions"))
            putJsonObject("status") {
                put("type", "integer")
                put("description", "Status code to return instead of the captured one, 100 to 599")
            }
            putJsonObject("body") {
                put("type", "string")
                put("description", "Body to return instead of the captured one")
            }
        }
        putJsonArray("required") { add(JsonPrimitive("id")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments.string("id")?.takeIf { it.isNotBlank() }
            ?: return "Error: 'id' parameter is required and must be non-empty."
        val body = MockFromTransactionRequestDto(
            status = arguments["status"]?.jsonPrimitive?.intOrNull,
            body = arguments.string("body"),
        )
        val response = connection.apiClient.post("/api/transactions/$id/mock", body.toRequestBody()).toApiResponse()
        val result = response.dataAs<MockFromTransactionResultDto>()
            ?: return response.errorText() ?: "Mock rule created."
        val note = if (result.bodyOmitted) {
            "\nThe captured body was binary or too large to copy; the mock returns an empty body. " +
                "Pass 'body' to set one."
        } else {
            ""
        }
        return "Mock rule created from transaction $id.$note\n\n" + TextFormatter.formatMockRule(result.rule)
    }
}

internal class SetMockRuleEnabledTool : McpTool() {

    override val name = "set_mock_rule_enabled"

    override val annotations = ToolAnnotations(readOnlyHint = false, destructiveHint = false, idempotentHint = true)

    override val description = "Turn a mock rule on or off without deleting it. " +
        "Use to switch between the mocked and the real response while testing."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            put("id", idSchema("Mock rule ID from list_mock_rules"))
            putJsonObject("enabled") {
                put("type", "boolean")
                put("description", "true to activate the rule, false to deactivate it")
            }
        }
        putJsonArray("required") {
            add(JsonPrimitive("id"))
            add(JsonPrimitive("enabled"))
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments.string("id")?.takeIf { it.isNotBlank() }
            ?: return "Error: 'id' parameter is required and must be non-empty."
        val enabled = arguments["enabled"]?.jsonPrimitive?.booleanOrNull
            ?: return "Error: 'enabled' parameter is required and must be true or false."
        val body = SetMockRuleEnabledRequestDto(enabled).toRequestBody()
        val response = connection.apiClient.post("/api/mock-rules/$id/enabled", body).toApiResponse()
        val rule = response.dataAs<MockRuleDto>() ?: return response.errorText() ?: "Mock rule updated."
        return "Mock rule ${if (rule.enabled) "enabled" else "disabled"}.\n\n" + TextFormatter.formatMockRule(rule)
    }
}

internal class DeleteMockRuleTool : McpTool() {

    override val name = "delete_mock_rule"

    override val annotations = ToolAnnotations.Destructive

    override val description = "Delete a mock rule permanently. Matching requests go to the network again. " +
        "Use set_mock_rule_enabled instead to keep the rule for later."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            put("id", idSchema("Mock rule ID from list_mock_rules"))
        }
        putJsonArray("required") { add(JsonPrimitive("id")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments.string("id")?.takeIf { it.isNotBlank() }
            ?: return "Error: 'id' parameter is required and must be non-empty."
        return connection.apiClient.delete("/api/mock-rules/$id").toApiResponse().errorText()
            ?: "Mock rule $id deleted."
    }
}
