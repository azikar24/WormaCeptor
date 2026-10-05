package com.azikar24.wormaceptor.api

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "WormaCeptorInterceptor"

// Matches OkHttp's own discard-on-close budget, which it spends anyway to reuse the connection.
private const val DrainTimeoutMillis = 100L

/** What a [CapturingResponseBody] saw by the time it completed. Not a data class: it holds a ByteArray. */
@Suppress("UseDataClass")
internal class BodyCaptureResult(
    val captured: ByteArray,
    val bytesRead: Long,
    val exhausted: Boolean,
    val failure: IOException?,
)

/**
 * Passes [delegate] through unchanged while copying the first [maxContentLength] bytes the caller reads.
 * [onComplete] runs once, on EOF, read failure or close. Closing a fixed-length body that fits the cap
 * before reading it drains the rest first, so `response.close()` without reading still captures the body.
 * A body the caller never reads or closes never completes.
 */
internal class CapturingResponseBody(
    private val delegate: ResponseBody,
    maxContentLength: Long,
    onComplete: (BodyCaptureResult) -> Unit,
) : ResponseBody() {

    private val source by lazy {
        CapturingSource(delegate.source(), delegate.contentLength(), maxContentLength, onComplete).buffer()
    }

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun source(): BufferedSource = source

    private class CapturingSource(
        delegate: Source,
        private val declaredLength: Long,
        private val maxContentLength: Long,
        private val onComplete: (BodyCaptureResult) -> Unit,
    ) : ForwardingSource(delegate) {
        private val lock = Any()
        private val captured = Buffer()
        private var bytesRead = 0L
        private var completed = false
        private val reading = AtomicBoolean(false)

        override fun read(
            sink: Buffer,
            byteCount: Long,
        ): Long {
            val read = try {
                reading.set(true)
                super.read(sink, byteCount)
            } catch (e: IOException) {
                complete(exhausted = false, failure = e)
                throw e
            } finally {
                reading.set(false)
            }
            if (read == -1L) {
                complete(exhausted = true, failure = null)
            } else {
                record(sink, read)
            }
            return read
        }

        override fun close() {
            val drained = drainIfSmall()
            try {
                super.close()
            } finally {
                complete(exhausted = drained, failure = null)
            }
        }

        // Capture is best effort: nothing it throws may surface in the host's read.
        @Suppress("TooGenericExceptionCaught")
        private fun record(
            sink: Buffer,
            read: Long,
        ) = synchronized(lock) {
            if (completed) return@synchronized
            try {
                val keep = minOf(read, (maxContentLength - captured.size).coerceAtLeast(0))
                if (keep > 0) sink.copyTo(captured, sink.size - read, keep)
                bytesRead += read
            } catch (e: RuntimeException) {
                Log.w(TAG, "Failed to copy response body for capture", e)
            }
        }

        /** Reads the rest of a body that fits the cap within [DrainTimeoutMillis]. Returns true on EOF. */
        private fun drainIfSmall(): Boolean {
            val eligible = synchronized(lock) { !completed } &&
                declaredLength in 1..maxContentLength &&
                reading.compareAndSet(false, true)
            if (!eligible) return false
            val timeout = delegate.timeout()
            val originalDeadline = if (timeout.hasDeadline()) timeout.deadlineNanoTime() else null
            val drainDeadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(DrainTimeoutMillis)
            timeout.deadlineNanoTime(minOf(originalDeadline ?: drainDeadline, drainDeadline))
            val sink = Buffer()
            return try {
                while (true) {
                    val read = delegate.read(sink, declaredLength)
                    if (read == -1L) break
                    record(sink, read)
                    sink.clear()
                }
                true
            } catch (e: IOException) {
                Log.d(TAG, "Response body drain on close stopped early", e)
                false
            } finally {
                if (originalDeadline != null) timeout.deadlineNanoTime(originalDeadline) else timeout.clearDeadline()
                reading.set(false)
            }
        }

        // The caller's thread may be main: only snapshot here, encoding happens on the provider's thread.
        @Suppress("TooGenericExceptionCaught")
        private fun complete(
            exhausted: Boolean,
            failure: IOException?,
        ) {
            val result = synchronized(lock) {
                if (completed) return
                completed = true
                BodyCaptureResult(captured.readByteArray(), bytesRead, exhausted, failure)
            }
            try {
                onComplete(result)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to capture response body", e)
            }
        }
    }
}
