package com.azikar24.wormaceptorapp.sampleservice

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Mock interceptor that returns sample protobuf binary data for testing the ProtobufView.
 *
 * Intercepts requests to the `/protobuf` path and returns a hand-crafted protobuf wire format
 * response with Content-Type `application/x-protobuf`.
 */
internal class MockProtobufInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!request.url.encodedPath.endsWith("/protobuf")) {
            return chain.proceed(request)
        }

        val protobufBytes = buildSampleProtobuf()

        return Response.Builder()
            .code(200)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .body(protobufBytes.toResponseBody(PROTOBUF_MEDIA_TYPE))
            .addHeader("Content-Type", "application/x-protobuf")
            .build()
    }

    companion object {
        private val PROTOBUF_MEDIA_TYPE = "application/x-protobuf".toMediaType()

        /**
         * Builds sample protobuf binary data representing a mock "User" message:
         * - field 1 (varint): id = 42
         * - field 2 (length-delimited): name = "WormaCeptor"
         * - field 3 (length-delimited): email = "test@example.com"
         * - field 4 (varint): age = 28
         * - field 5 (fixed32): score = 3.14f
         * - field 6 (fixed64): timestamp = 1700000000000
         * - field 7 (varint): is_active = 1 (true)
         */
        @Suppress("MagicNumber")
        private fun buildSampleProtobuf(): ByteArray {
            val buffer = mutableListOf<Byte>()

            // Field 1: id = 42 (varint)
            buffer.addTag(fieldNumber = 1, wireType = 0)
            buffer.addVarint(42)

            // Field 2: name = "WormaCeptor" (length-delimited)
            val name = "WormaCeptor".toByteArray(Charsets.UTF_8)
            buffer.addTag(fieldNumber = 2, wireType = 2)
            buffer.addVarint(name.size.toLong())
            buffer.addAll(name.toList())

            // Field 3: email = "test@example.com" (length-delimited)
            val email = "test@example.com".toByteArray(Charsets.UTF_8)
            buffer.addTag(fieldNumber = 3, wireType = 2)
            buffer.addVarint(email.size.toLong())
            buffer.addAll(email.toList())

            // Field 4: age = 28 (varint)
            buffer.addTag(fieldNumber = 4, wireType = 0)
            buffer.addVarint(28)

            // Field 5: score = 3.14f (fixed32)
            buffer.addTag(fieldNumber = 5, wireType = 5)
            val scoreBits = java.lang.Float.floatToIntBits(3.14f)
            buffer.add((scoreBits and 0xFF).toByte())
            buffer.add((scoreBits shr 8 and 0xFF).toByte())
            buffer.add((scoreBits shr 16 and 0xFF).toByte())
            buffer.add((scoreBits shr 24 and 0xFF).toByte())

            // Field 6: timestamp = 1700000000000 (fixed64)
            buffer.addTag(fieldNumber = 6, wireType = 1)
            val timestamp = 1_700_000_000_000L
            for (i in 0 until 8) {
                buffer.add((timestamp shr (i * 8) and 0xFF).toByte())
            }

            // Field 7: is_active = 1 (varint, boolean)
            buffer.addTag(fieldNumber = 7, wireType = 0)
            buffer.addVarint(1)

            return buffer.toByteArray()
        }

        @Suppress("MagicNumber")
        private fun MutableList<Byte>.addTag(
            fieldNumber: Int,
            wireType: Int,
        ) {
            addVarint(((fieldNumber shl 3) or wireType).toLong())
        }

        @Suppress("MagicNumber")
        private fun MutableList<Byte>.addVarint(value: Long) {
            var v = value
            while (v > 0x7F) {
                add((v.toInt() and 0x7F or 0x80).toByte())
                v = v ushr 7
            }
            add(v.toByte())
        }
    }
}
