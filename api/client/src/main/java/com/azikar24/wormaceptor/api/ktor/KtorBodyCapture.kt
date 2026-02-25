package com.azikar24.wormaceptor.api.ktor

import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent

/**
 * Internal utility for capturing Ktor request body bytes.
 *
 * Supported subtypes:
 * - [TextContent]: Extracts text and converts to bytes
 * - [ByteArrayContent]: Extracts raw byte array
 *
 * Unsupported subtypes (WriteChannelContent, FormDataContent, MultiPartFormDataContent)
 * return null — these may be added in a future version.
 */
internal object KtorBodyCapture {

    fun captureRequestBody(
        content: OutgoingContent,
        maxContentLength: Long,
    ): ByteArray? {
        return when (content) {
            is TextContent -> content.text.toByteArray(Charsets.UTF_8).truncate(maxContentLength)
            is ByteArrayContent -> content.bytes().truncate(maxContentLength)
            else -> null
        }
    }

    private fun ByteArray.truncate(maxLength: Long): ByteArray =
        if (size > maxLength) copyOf(maxLength.toInt()) else this
}
