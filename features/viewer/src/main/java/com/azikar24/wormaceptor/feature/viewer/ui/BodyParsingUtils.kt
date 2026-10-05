package com.azikar24.wormaceptor.feature.viewer.ui

import com.azikar24.wormaceptor.core.engine.ParserRegistry
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.contracts.ContentType

internal const val MaxParseBodySize = 500_000
internal const val TruncatedDisplaySize = 100_000

internal fun isProtobufContentType(contentType: String?): Boolean {
    return detectContentTypeViaRegistry(contentType, null) == ContentType.PROTOBUF
}

internal fun detectContentTypeViaRegistry(
    contentTypeHeader: String?,
    body: String?,
): ContentType {
    return try {
        val registry: ParserRegistry = WormaCeptorKoin.get(ParserRegistry::class.java)
        registry.detectContentType(contentTypeHeader, body)
    } catch (_: RuntimeException) {
        ContentType.UNKNOWN
    }
}

internal fun extractMultipartBoundaryViaRegistry(contentType: String): String? {
    return try {
        val registry: ParserRegistry = WormaCeptorKoin.get(ParserRegistry::class.java)
        registry.extractMultipartBoundary(contentType)
    } catch (_: RuntimeException) {
        null
    }
}

/** Caps raw text for on-screen rendering using the same thresholds as [parseBodyViaRegistry]. */
internal fun capRawBodyForDisplay(raw: String): String {
    return if (raw.length > MaxParseBodySize) raw.take(TruncatedDisplaySize) else raw
}

internal fun parseBodyViaRegistry(
    contentType: String?,
    bytes: ByteArray,
    rawFallback: String,
): Pair<String, ContentType> {
    if (bytes.size > MaxParseBodySize) {
        val truncated = rawFallback.take(TruncatedDisplaySize) +
            "\n\n... (Rest of content truncated for performance) ..."
        return truncated to ContentType.PLAIN_TEXT
    }
    return try {
        val registry: ParserRegistry =
            WormaCeptorKoin.get(ParserRegistry::class.java)
        val parsed = registry.parseBody(contentType, bytes)
        parsed.formatted to parsed.contentType
    } catch (_: RuntimeException) {
        rawFallback to ContentType.UNKNOWN
    }
}
