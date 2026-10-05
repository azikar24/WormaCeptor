package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

/**
 * Editor state pre-filled from a captured transaction.
 *
 * @property editor The unsaved rule draft.
 * @property bodyOmitted True when the response had a body that was not copied (binary or too large).
 */
internal data class TransactionPrefill(
    val editor: EditorState,
    val bodyOmitted: Boolean,
)

/**
 * Maps [transaction] and its raw [responseBody] into a new-rule draft that matches exactly this request.
 *
 * The URL pattern is the full request URL with [UrlMatchType.EXACT], which is the same string the
 * interceptor passes to `MockEngine.findMatchingRule`. An in-flight transaction yields a 200 with no body.
 */
internal fun buildTransactionPrefill(
    transaction: NetworkTransaction,
    responseBody: ByteArray?,
): TransactionPrefill {
    val request = transaction.request
    val response = transaction.response
    val defaults = EditorState()
    val contentType = response?.headers?.headerValue("Content-Type")
    val body = responseBody?.takeIf { it.isNotEmpty() }?.let { decodeTextBody(it, contentType) }
    return TransactionPrefill(
        editor = defaults.copy(
            name = ruleName(request.method, request.url),
            urlPattern = request.url,
            matchType = UrlMatchType.EXACT,
            method = request.method.uppercase(),
            statusCodeText = (response?.code ?: DefaultStatusCode).toString(),
            statusMessage = response?.message ?: defaults.statusMessage,
            contentType = contentType ?: defaults.contentType,
            responseBody = body.orEmpty(),
            isLoaded = true,
        ),
        bodyOmitted = responseBody != null && responseBody.isNotEmpty() && body == null,
    )
}

private fun ruleName(
    method: String,
    url: String,
): String {
    val path = try {
        URI(url).rawPath?.takeIf { it.isNotEmpty() } ?: "/"
    } catch (_: URISyntaxException) {
        url
    }
    val name = "${method.uppercase()} $path"
    return if (name.length > MaxNameLength) name.take(MaxNameLength - 1) + "…" else name
}

private fun Map<String, List<String>>.headerValue(name: String): String? =
    entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.firstOrNull()

/** Returns the body as text, or null if it is binary or too large to edit. */
private fun decodeTextBody(
    bytes: ByteArray,
    contentType: String?,
): String? {
    if (contentType != null && isBinaryContentType(contentType)) return null
    val text = try {
        Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (_: CharacterCodingException) {
        return null
    }
    return text.takeIf { it.length <= MaxPrefillBodyChars && '\u0000' !in it }
}

private fun isBinaryContentType(contentType: String): Boolean {
    val type = contentType.lowercase()
    return BinaryTypePrefixes.any { type.startsWith(it) } || BinaryTypeMarkers.any { it in type }
}

private val BinaryTypePrefixes = listOf("image/", "audio/", "video/", "font/")
private val BinaryTypeMarkers = listOf("octet-stream", "pdf", "protobuf", "zip", "grpc", "msgpack")

private const val DefaultStatusCode = 200
private const val MaxNameLength = 80

/** Matches the viewer's raw-body display cap; larger bodies are left for the user to paste. */
private const val MaxPrefillBodyChars = 100_000
