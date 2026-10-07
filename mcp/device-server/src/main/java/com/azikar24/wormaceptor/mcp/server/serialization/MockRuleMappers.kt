package com.azikar24.wormaceptor.mcp.server.serialization

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.mock.MockBehavior
import com.azikar24.wormaceptor.domain.entities.mock.MockDelay
import com.azikar24.wormaceptor.domain.entities.mock.MockResponse
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.RequestMatcher
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import com.azikar24.wormaceptor.mcp.protocol.CreateMockRuleRequestDto
import com.azikar24.wormaceptor.mcp.protocol.MockFromTransactionRequestDto
import com.azikar24.wormaceptor.mcp.protocol.MockRuleDto
import io.ktor.http.HttpStatusCode
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.util.regex.PatternSyntaxException

private const val MinStatus = 100
private const val MaxStatus = 599
private const val DefaultStatus = 200
private const val MaxNameLength = 80

/** Same cap as the in-app "Add to Mock" prefill; larger bodies are left out. */
private const val MaxCopiedBodyChars = 100_000

private val BinaryTypePrefixes = listOf("image/", "audio/", "video/", "font/")
private val BinaryTypeMarkers = listOf("octet-stream", "pdf", "protobuf", "zip", "grpc", "msgpack")

/** Outcome of turning an MCP request into a [MockRule]. */
internal sealed class MockRuleDraft {
    data class Valid(val rule: MockRule, val bodyOmitted: Boolean = false) : MockRuleDraft()
    data class Invalid(val message: String) : MockRuleDraft()
}

internal fun MockRule.toDto() = MockRuleDto(
    id = id,
    name = name,
    enabled = enabled,
    method = matcher.method,
    urlPattern = matcher.urlPattern,
    matchType = matcher.matchType.name,
    status = response.statusCode,
    statusMessage = response.statusMessage,
    contentType = response.contentType,
    body = response.body,
    delay = when (val d = delay) {
        MockDelay.None -> "none"
        is MockDelay.Fixed -> "${d.ms} ms"
        is MockDelay.Range -> "${d.minMs}-${d.maxMs} ms"
    },
    behavior = when (val b = behavior) {
        MockBehavior.Always -> "Always"
        MockBehavior.Passthrough -> "Passthrough"
        is MockBehavior.NthRequest -> "NthRequest(${b.n})"
        is MockBehavior.FirstN -> "FirstN(${b.count})"
        is MockBehavior.Sequential -> "Sequential(${b.responses.size} responses)"
    },
    priority = priority,
    createdAt = createdAt,
)

/** Validates the request and maps it onto the app's [MockRule], with the app's defaults for unset fields. */
internal fun CreateMockRuleRequestDto.toMockRule(): MockRuleDraft {
    val pattern = urlPattern.trim()
    val parsedType = matchType?.let { raw ->
        UrlMatchType.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }
    validationError(pattern, parsedType)?.let { return MockRuleDraft.Invalid(it) }
    val type = parsedType ?: RequestMatcher(pattern).matchType

    val httpMethod = method?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
    val defaults = MockResponse()
    val rule = MockRule(
        name = "${httpMethod ?: "ANY"} $pattern".clipName(),
        enabled = enabled ?: true,
        matcher = RequestMatcher(urlPattern = pattern, matchType = type, method = httpMethod),
        response = MockResponse(
            statusCode = status,
            statusMessage = reasonPhrase(status),
            body = body,
            contentType = contentType?.takeIf { it.isNotBlank() } ?: defaults.contentType,
        ),
        delay = delayMs?.takeIf { it > 0 }?.let { MockDelay.Fixed(it) } ?: MockDelay.None,
    )
    return MockRuleDraft.Valid(rule)
}

/**
 * A rule that replays [transaction]'s response for exactly its URL and method, like the in-app "Add to Mock".
 * [responseBody] is the raw captured body; binary or oversized bodies are left out unless [overrides] sets one.
 */
internal fun mockRuleFromTransaction(
    transaction: NetworkTransaction,
    responseBody: ByteArray?,
    overrides: MockFromTransactionRequestDto,
): MockRuleDraft {
    overrides.status?.let { status -> statusError(status)?.let { return MockRuleDraft.Invalid(it) } }
    val request = transaction.request
    val response = transaction.response
    val status = overrides.status ?: response?.code?.takeIf { it in MinStatus..MaxStatus } ?: DefaultStatus
    val message = response?.message?.takeIf { overrides.status == null && it.isNotBlank() } ?: reasonPhrase(status)
    val contentType = response?.headers?.entries
        ?.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }?.value?.firstOrNull()
    val capturedBody = responseBody?.takeIf { it.isNotEmpty() }?.let { decodeTextBody(it, contentType) }
    val method = request.method.uppercase()
    val rule = MockRule(
        name = "$method ${pathOf(request.url)}".clipName(),
        matcher = RequestMatcher(urlPattern = request.url, matchType = UrlMatchType.EXACT, method = method),
        response = MockResponse(
            statusCode = status,
            statusMessage = message,
            body = overrides.body ?: capturedBody,
            contentType = contentType ?: MockResponse().contentType,
        ),
    )
    val omitted = overrides.body == null && responseBody != null && responseBody.isNotEmpty() && capturedBody == null
    return MockRuleDraft.Valid(rule, bodyOmitted = omitted)
}

private fun CreateMockRuleRequestDto.validationError(
    pattern: String,
    parsedType: UrlMatchType?,
): String? = when {
    pattern.isEmpty() -> "'url_pattern' must not be empty"
    statusError(status) != null -> statusError(status)
    delayMs != null && delayMs < 0 -> "'delay_ms' must be 0 or more"
    matchType != null && parsedType == null ->
        "Unknown match_type '$matchType'. Valid: ${UrlMatchType.entries.joinToString { it.name }}"
    parsedType == UrlMatchType.REGEX -> regexError(pattern)
    else -> null
}

private fun statusError(status: Int): String? =
    "'status' must be between $MinStatus and $MaxStatus (got $status)".takeIf { status !in MinStatus..MaxStatus }

/** MockEngine strips a leading `~` and drops patterns that don't compile, so reject those up front. */
private fun regexError(pattern: String): String? = try {
    Regex(pattern.trimStart('~').trim())
    null
} catch (e: PatternSyntaxException) {
    "Invalid regex in 'url_pattern': ${e.description}"
}

private fun reasonPhrase(status: Int): String = HttpStatusCode.fromValue(status).description

private fun String.clipName(): String = if (length > MaxNameLength) take(MaxNameLength - 1) + "…" else this

private fun pathOf(url: String): String = try {
    URI(url).rawPath?.takeIf { it.isNotEmpty() } ?: "/"
} catch (_: URISyntaxException) {
    url
}

/** The body as text, or null if it is binary or too large, mirroring the in-app prefill. */
private fun decodeTextBody(
    bytes: ByteArray,
    contentType: String?,
): String? {
    val type = contentType?.lowercase()
    if (type != null && (BinaryTypePrefixes.any { type.startsWith(it) } || BinaryTypeMarkers.any { it in type })) {
        return null
    }
    val text = try {
        Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (_: CharacterCodingException) {
        return null
    }
    return text.takeIf { it.length <= MaxCopiedBodyChars && '\u0000' !in it }
}
