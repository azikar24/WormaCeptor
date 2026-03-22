package com.azikar24.wormaceptor.infra.parser.protobuf

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.contracts.ProtobufDecoder
import com.azikar24.wormaceptor.domain.entities.ProtobufDecodeResult
import com.azikar24.wormaceptor.domain.entities.ProtobufField
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale

/**
 * Parser for protobuf wire format data.
 *
 * Decodes protobuf binary data without a schema, extracting field numbers,
 * wire types, and decoded values. Also implements [ProtobufDecoder] for
 * typed access from viewer composables.
 */
class ProtobufBodyParser : BaseBodyParser(), ProtobufDecoder {

    override val supportedContentTypes: List<String> = listOf(
        "application/protobuf",
        "application/x-protobuf",
        "application/grpc",
        "application/grpc+proto",
    )

    override val priority: Int = PRIORITY

    override val defaultContentType: ContentType = ContentType.PROTOBUF

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        if (contentType != null) {
            val mime = contentType.split(";").firstOrNull()?.trim()?.lowercase() ?: ""
            if (mime.contains("protobuf") || mime.contains("grpc") || mime.endsWith("+proto")) {
                return true
            }
        }
        return false
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        val result = decode(body)
        return when (result) {
            is ProtobufDecodeResult.Success -> ParsedBody(
                formatted = result.fields.joinToString("\n") { field ->
                    "Field ${field.fieldNumber} (${field.wireTypeName}): ${field.value}"
                },
                contentType = ContentType.PROTOBUF,
                metadata = mapOf("fieldCount" to result.fields.size.toString()),
                isValid = true,
            )
            is ProtobufDecodeResult.Failure -> ParsedBody(
                formatted = result.hexDump,
                contentType = ContentType.PROTOBUF,
                isValid = false,
                errorMessage = "Failed to decode protobuf wire format",
            )
        }
    }

    override fun decode(data: ByteArray): ProtobufDecodeResult {
        if (data.isEmpty()) {
            return ProtobufDecodeResult.Success(emptyList())
        }

        return try {
            val fields = decodeWireFormat(data)
            if (fields.isEmpty()) {
                ProtobufDecodeResult.Failure(formatHexDump(data))
            } else {
                ProtobufDecodeResult.Success(fields)
            }
        } catch (_: Exception) {
            ProtobufDecodeResult.Failure(formatHexDump(data))
        }
    }

    companion object {
        private const val PRIORITY = 100

        private const val WIRE_TYPE_VARINT = 0
        private const val WIRE_TYPE_64BIT = 1
        private const val WIRE_TYPE_LENGTH_DELIMITED = 2
        private const val WIRE_TYPE_START_GROUP = 3
        private const val WIRE_TYPE_END_GROUP = 4
        private const val WIRE_TYPE_32BIT = 5
    }

    private fun decodeWireFormat(body: ByteArray): List<ProtobufField> {
        val fields = mutableListOf<ProtobufField>()
        val buffer = ByteBuffer.wrap(body)

        while (buffer.hasRemaining()) {
            val tag = readVarint(buffer) ?: break
            if (tag == 0L) break

            val fieldNumber = (tag shr 3).toInt()
            val wireType = (tag and 0x7).toInt()

            val field = readField(buffer, fieldNumber, wireType) ?: break
            fields.add(field)
        }

        return fields
    }

    @Suppress("MagicNumber")
    private fun readField(
        buffer: ByteBuffer,
        fieldNumber: Int,
        wireType: Int,
    ): ProtobufField? {
        return try {
            when (wireType) {
                WIRE_TYPE_VARINT -> {
                    val value = readVarint(buffer) ?: return null
                    ProtobufField(fieldNumber, wireType, "Varint", formatVarint(value))
                }

                WIRE_TYPE_64BIT -> {
                    if (buffer.remaining() < 8) return null
                    val bytes = ByteArray(8)
                    buffer.get(bytes)
                    ProtobufField(fieldNumber, wireType, "64-bit", format64Bit(bytes))
                }

                WIRE_TYPE_LENGTH_DELIMITED -> {
                    val length = readVarint(buffer)?.toInt() ?: return null
                    if (length < 0 || length > buffer.remaining()) return null
                    val bytes = ByteArray(length)
                    buffer.get(bytes)
                    ProtobufField(fieldNumber, wireType, "Length-delimited", formatLengthDelimited(bytes))
                }

                WIRE_TYPE_START_GROUP -> {
                    ProtobufField(fieldNumber, wireType, "Start group", "(deprecated)")
                }

                WIRE_TYPE_END_GROUP -> {
                    ProtobufField(fieldNumber, wireType, "End group", "(deprecated)")
                }

                WIRE_TYPE_32BIT -> {
                    if (buffer.remaining() < 4) return null
                    val bytes = ByteArray(4)
                    buffer.get(bytes)
                    ProtobufField(fieldNumber, wireType, "32-bit", format32Bit(bytes))
                }

                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("MagicNumber")
    private fun readVarint(buffer: ByteBuffer): Long? {
        var result = 0L
        var shift = 0

        while (buffer.hasRemaining() && shift < 64) {
            val b = buffer.get().toInt() and 0xFF
            result = result or (b.toLong() and 0x7F shl shift)
            if (b and 0x80 == 0) {
                return result
            }
            shift += 7
        }

        return null
    }

    @Suppress("MagicNumber")
    private fun formatVarint(value: Long): String {
        return buildString {
            append(value)
            if (value > Int.MAX_VALUE) {
                val signed = value.toInt()
                if (signed < 0) {
                    append(" (signed: $signed)")
                }
            }
            val zigzag = value shr 1 xor -(value and 1)
            if (zigzag != value && zigzag != 0L) {
                append(" [zigzag: $zigzag]")
            }
        }
    }

    @Suppress("MagicNumber")
    private fun format64Bit(bytes: ByteArray): String {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val asDouble = buffer.double
        buffer.rewind()
        val asLong = buffer.long

        return buildString {
            append("0x")
            bytes.forEach { append(String.format(Locale.US, "%02X", it)) }
            append(" (int64: $asLong")
            if (asDouble.isFinite()) {
                append(", double: $asDouble")
            }
            append(")")
        }
    }

    @Suppress("MagicNumber")
    private fun format32Bit(bytes: ByteArray): String {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val asFloat = buffer.float
        buffer.rewind()
        val asInt = buffer.int

        return buildString {
            append("0x")
            bytes.forEach { append(String.format(Locale.US, "%02X", it)) }
            append(" (int32: $asInt")
            if (asFloat.isFinite()) {
                append(", float: $asFloat")
            }
            append(")")
        }
    }

    @Suppress("MagicNumber")
    private fun formatLengthDelimited(bytes: ByteArray): String {
        val asString = tryDecodeUtf8(bytes)
        if (asString != null && asString.length <= 100) {
            return "\"$asString\" (${bytes.size} bytes)"
        }

        return if (bytes.size <= 32) {
            bytes.joinToString(" ") { String.format(Locale.US, "%02X", it) } + " (${bytes.size} bytes)"
        } else {
            val preview = bytes.take(32).joinToString(" ") { String.format(Locale.US, "%02X", it) }
            "$preview ... (${bytes.size} bytes)"
        }
    }

    @Suppress("MagicNumber")
    private fun tryDecodeUtf8(bytes: ByteArray): String? {
        return try {
            val str = String(bytes, Charsets.UTF_8)
            if (str.all {
                    it.isLetterOrDigit() || it.isWhitespace() ||
                        it in ".,;:!?-_@#\$%&*()[]{}'\"/\\<>=+"
                }
            ) {
                str
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("MagicNumber")
    private fun formatHexDump(body: ByteArray): String {
        val sb = StringBuilder()
        val bytesPerLine = 16
        for (i in body.indices step bytesPerLine) {
            sb.append(String.format(Locale.US, "%08X  ", i))

            for (j in 0 until bytesPerLine) {
                if (i + j < body.size) {
                    sb.append(String.format(Locale.US, "%02X ", body[i + j]))
                } else {
                    sb.append("   ")
                }
                if (j == 7) sb.append(" ")
            }

            sb.append(" |")

            for (j in 0 until bytesPerLine) {
                if (i + j < body.size) {
                    val b = body[i + j].toInt() and 0xFF
                    sb.append(if (b in 32..126) b.toChar() else '.')
                }
            }

            sb.appendLine("|")
        }

        return sb.toString()
    }
}
