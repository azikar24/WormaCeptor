package com.azikar24.wormaceptor.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import okio.Sink
import okio.Timeout
import okio.buffer
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.CharBuffer

/**
 * A captured body ready for storage, with [size] being the real byte count on the wire when known.
 * [error] is set when reading the body failed partway.
 */
internal data class CapturedBody(val stream: InputStream?, val size: Long, val error: String? = null)

/** Reads OkHttp request/response bodies for capture without consuming or blocking on them. */
internal object OkHttpBodyCapture {

    private val TextSubtypes = setOf("json", "xml", "x-www-form-urlencoded", "graphql", "javascript")
    private val GzipEncodings = setOf("gzip", "x-gzip")
    private val GzipMagic = byteArrayOf(0x1F, 0x8B.toByte())

    /**
     * Request headers as OkHttp will send them. Content-Type and Content-Length live on the
     * [RequestBody] until BridgeInterceptor copies them into headers, after application interceptors run.
     */
    fun requestHeaders(request: Request): Map<String, List<String>> {
        val headers = request.headers.toMultimap()
        val body = request.body ?: return headers
        // Mirrors BridgeInterceptor: body values win, and an unknown length is sent chunked.
        val contentLength = body.contentLength()
        val bodyHeaders = buildMap {
            body.contentType()?.let { put("content-type", listOf(it.toString())) }
            if (contentLength != -1L) {
                put("content-length", listOf(contentLength.toString()))
            } else {
                put("transfer-encoding", listOf("chunked"))
            }
        }
        val replaced = bodyHeaders.keys + if (contentLength != -1L) "transfer-encoding" else "content-length"
        return headers.filterKeys { it.lowercase() !in replaced } + bodyHeaders
    }

    fun captureRequest(
        body: RequestBody?,
        contentEncoding: String?,
        maxContentLength: Long,
        redaction: RedactionConfig,
    ): CapturedBody {
        if (body == null) return CapturedBody(null, 0)
        // Writing a one-shot or duplex body here would leave nothing for the real network call.
        if (body.isOneShot() || body.isDuplex()) return CapturedBody(null, 0)

        val sink = CappedSink(maxContentLength)
        val buffered = sink.buffer()
        body.writeTo(buffered)
        // Some bodies close the sink themselves (e.g. outputStream().use {}), which already emitted everything.
        if (buffered.isOpen) buffered.flush()
        if (sink.totalBytes == 0L) return CapturedBody(null, 0)

        val bytes = sink.captured.readByteArray()
        val encoded = encodeForStorage(bytes, body.contentType(), contentEncoding, redaction, maxContentLength)
        return CapturedBody(encoded, sink.totalBytes)
    }

    /**
     * Returns [response] with its body teed: [onCaptured] receives the body once the caller reads it to EOF,
     * fails reading it, or closes it. Peeking instead would block until maxContentLength bytes or EOF arrive,
     * which hangs long-poll and streaming responses. Bodies that are absent, empty, or SSE complete immediately.
     * Decoding and redaction are deferred until the returned stream is read, off the caller's thread.
     */
    fun captureResponse(
        response: Response,
        maxContentLength: Long,
        redaction: RedactionConfig,
        onCaptured: (CapturedBody) -> Unit,
    ): Response {
        val body = response.body
        val contentType = body?.contentType()
        val isEventStream = contentType?.let { it.type == "text" && it.subtype == "event-stream" } == true
        if (body == null || body.contentLength() == 0L || isEventStream) {
            onCaptured(CapturedBody(null, 0))
            return response
        }

        val declaredLength = body.contentLength()
        val contentEncoding = response.header("Content-Encoding")
        val tee = CapturingResponseBody(body, response.protocol, maxContentLength) { result ->
            // Closing early without storing anything still reports the declared size. A stored body cut short
            // below the cap (failed read, early close, drain out of time) reports only the bytes it holds.
            val partiallyStored = result.captured.isNotEmpty() &&
                result.captured.size < minOf(declaredLength, maxContentLength)
            val stoppedEarly = result.failure == null && !result.exhausted
            val size = if (stoppedEarly && declaredLength >= 0 && !partiallyStored) declaredLength else result.bytesRead
            val stream = result.captured.takeIf { it.isNotEmpty() }?.let { bytes ->
                LazyInputStream { encodeForStorage(bytes, contentType, contentEncoding, redaction, maxContentLength) }
            }
            onCaptured(CapturedBody(stream, size, result.failure?.toString()))
        }
        return response.newBuilder().body(tee).build()
    }

    /**
     * Turns captured bytes into what gets stored: text is decoded and redacted, binary is kept as is.
     * A gzip [contentEncoding] (the host asked for gzip itself, so OkHttp did not unzip) is inflated up to
     * [maxContentLength] first; any other non-identity encoding is stored raw unless the bytes are plain text.
     */
    @Suppress("LongParameterList")
    fun encodeForStorage(
        bytes: ByteArray,
        contentType: MediaType?,
        contentEncoding: String?,
        redaction: RedactionConfig,
        maxContentLength: Long,
    ): InputStream {
        val encoding = contentEncoding?.trim()?.lowercase()?.takeUnless { it.isEmpty() || it == "identity" }
        val decoded = when {
            encoding == null -> bytes
            encoding in GzipEncodings && bytes.startsWith(GzipMagic) -> gunzip(bytes, maxContentLength)
            encoding in GzipEncodings || isStrictText(bytes, contentType) -> bytes
            else -> null
        } ?: return ByteArrayInputStream(bytes)
        return encodeDecoded(decoded, contentType, redaction)
    }

    private fun encodeDecoded(
        bytes: ByteArray,
        contentType: MediaType?,
        redaction: RedactionConfig,
    ): InputStream {
        val isBinary = !isTextContentType(contentType) &&
            (
                BinaryContentDetector.isBinaryContentType(contentType?.toString()) ||
                    BinaryContentDetector.isBinaryByMagicBytes(bytes)
                )
        if (isBinary) return ByteArrayInputStream(bytes)

        val text = String(bytes, contentType?.charset() ?: Charsets.UTF_8)
        return redaction.applyRedactions(text).byteInputStream()
    }

    /** Inflates up to [limit] bytes; a capture cut short mid-stream yields what inflated, or null if nothing. */
    private fun gunzip(
        bytes: ByteArray,
        limit: Long,
    ): ByteArray? {
        val out = Buffer()
        try {
            GzipSource(Buffer().write(bytes)).use { source ->
                while (out.size < limit && source.read(out, limit - out.size) != -1L) Unit
            }
        } catch (_: IOException) {
            if (out.size == 0L) return null
        }
        return out.readByteArray()
    }

    // Already-decoded text under a stale Content-Encoding header must still reach redaction.
    private fun isStrictText(
        bytes: ByteArray,
        contentType: MediaType?,
    ): Boolean {
        if (!isTextContentType(contentType)) return false
        // newDecoder() reports malformed input instead of replacing it.
        val decoder = (contentType?.charset() ?: Charsets.UTF_8).newDecoder()
        return !decoder.decode(ByteBuffer.wrap(bytes), CharBuffer.allocate(bytes.size), true).isError
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    // Text bodies must reach redaction even when a vendor type (application/vnd.api+json) or
    // leading bytes ("BM...") would look binary to the detector.
    private fun isTextContentType(contentType: MediaType?): Boolean {
        if (contentType == null) return false
        val subtype = contentType.subtype.lowercase()
        return contentType.type.equals("text", ignoreCase = true) ||
            subtype in TextSubtypes ||
            subtype.endsWith("+json") ||
            subtype.endsWith("+xml")
    }

    /** Defers [produce] (decoding and redaction) until the provider's background thread reads the body. */
    private class LazyInputStream(produce: () -> InputStream) : InputStream() {
        private val delegate = lazy(produce)

        override fun read(): Int = delegate.value.read()

        override fun read(
            b: ByteArray,
            off: Int,
            len: Int,
        ): Int = delegate.value.read(b, off, len)

        override fun close() {
            if (delegate.isInitialized()) delegate.value.close()
        }
    }

    /** Keeps the first [limit] bytes written and counts the rest without buffering them. */
    private class CappedSink(private val limit: Long) : Sink {
        val captured = Buffer()
        var totalBytes = 0L
            private set

        override fun write(
            source: Buffer,
            byteCount: Long,
        ) {
            val keep = minOf(byteCount, (limit - captured.size).coerceAtLeast(0))
            captured.write(source, keep)
            source.skip(byteCount - keep)
            totalBytes += byteCount
        }

        override fun flush() = Unit

        override fun timeout(): Timeout = Timeout.NONE

        override fun close() = Unit
    }
}
