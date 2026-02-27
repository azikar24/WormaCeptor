package com.azikar24.wormaceptor.infra.parser.protobuf

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import java.nio.ByteBuffer
import java.util.Locale

/**
 * Parser for Protocol Buffers (protobuf) content.
 *
 * Features:
 * - Raw field decoding without schema
 * - Wire type display
 * - Field number extraction
 *
 * Note: Without a .proto schema, we can only decode the wire format structure.
 * Field names and semantic meaning are not available.
 */
class ProtobufBodyParser : BaseBodyParser() {

    override val supportedContentTypes: List<String> = listOf(
        "application/x-protobuf",
        "application/protobuf",
        "application/x-google-protobuf",
        "application/grpc",
        "application/grpc+proto",
    )

    override val priority: Int = 150

    override val defaultContentType: ContentType = ContentType.PROTOBUF

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        // Check content type first
        if (contentType != null) {
            val mimeType = contentType.split(";").firstOrNull()?.trim()?.lowercase()
            if (mimeType != null && supportedContentTypes.any { it == mimeType }) {
                return true
            }
            // Also check for +proto suffix
            if (mimeType?.endsWith("+proto") == true) {
                return true
            }
        }

        // Content inspection: check for valid protobuf wire format
        if (body.isEmpty()) return false

        return looksLikeProtobuf(body)
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        return try {
            val fields = decodeWireFormat(body)
            val formatted = formatFields(fields)

            ParsedBody(
                formatted = formatted,
                contentType = ContentType.PROTOBUF,
                metadata = mapOf(
                    "fieldCount" to fields.size.toString(),
                    "size" to body.size.toString(),
                ),
                isValid = true,
            )
        } catch (e: Exception) {
            // Fall back to hex dump
            val hexDump = formatHexDump(body)
            ParsedBody(
                formatted = hexDump,
                contentType = ContentType.PROTOBUF,
                metadata = mapOf("size" to body.size.toString()),
                isValid = false,
                errorMessage = "Protobuf decoding error: ${e.message}",
            )
        }
    }

    /**
     * Heuristic to detect if bytes might be protobuf.
     * Checks for valid wire format structure.
     */
    private fun looksLikeProtobuf(body: ByteArray): Boolean {
        if (body.isEmpty()) return false

        try {
            val buffer = ByteBuffer.wrap(body)
            var validFieldCount = 0
            var totalBytesRead = 0
            val maxFieldsToCheck = 5

            while (buffer.hasRemaining() && validFieldCount < maxFieldsToCheck) {
                val startPos = buffer.position()

                // Try to read a varint tag
                val tag = readVarint(buffer) ?: return false
                if (tag == 0L) return false

                val fieldNumber = (tag shr 3).toInt()
                val wireType = (tag and 0x7).toInt()

                // Field numbers should be reasonable (1-536870911)
                if (fieldNumber < 1 || fieldNumber > 536870911) return false

                // Wire type must be valid (0-5)
                if (wireType > 5) return false

                // Skip the field value
                val skipped = skipField(buffer, wireType)
                if (!skipped) return false

                totalBytesRead += buffer.position() - startPos
                validFieldCount++
            }

            // If we successfully parsed some fields and read a reasonable amount, likely protobuf
            return validFieldCount > 0 && totalBytesRead > 0
        } catch (_: Exception) {
            return false
        }
    }

    private fun decodeWireFormat(body: ByteArray): List<ProtobufField> {
        val fields = mutableListOf<ProtobufField>()
        val buffer = ByteBuffer.wrap(body)

        while (buffer.hasRemaining()) {
            val tag = readVarint(buffer) ?: break
            if (tag == 0L) break

            val fieldNumber = (tag shr 3).toInt()
            val wireType = (tag and 0x7).toInt()

            val field = readField(buffer, fieldNumber, wireType)
            if (field != null) {
                fields.add(field)
            } else {
                break
            }
        }

        return fields
    }

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

    private fun skipField(
        buffer: ByteBuffer,
        wireType: Int,
    ): Boolean {
        return try {
            when (wireType) {
                WIRE_TYPE_VARINT -> {
                    readVarint(buffer) != null
                }
                WIRE_TYPE_64BIT -> {
                    if (buffer.remaining() < 8) return false
                    buffer.position(buffer.position() + 8)
                    true
                }
                WIRE_TYPE_LENGTH_DELIMITED -> {
                    val length = readVarint(buffer)?.toInt() ?: return false
                    if (length < 0 || length > buffer.remaining()) return false
                    buffer.position(buffer.position() + length)
                    true
                }
                WIRE_TYPE_START_GROUP, WIRE_TYPE_END_GROUP -> true
                WIRE_TYPE_32BIT -> {
                    if (buffer.remaining() < 4) return false
                    buffer.position(buffer.position() + 4)
                    true
                }
                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }

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

    private fun formatVarint(value: Long): String {
        return buildString {
            append(value)
            // Also show as signed int if it might be negative
            if (value > Int.MAX_VALUE) {
                val signed = value.toInt()
                if (signed < 0) {
                    append(" (signed: $signed)")
                }
            }
            // Show as zigzag decoded for potential sint types
            val zigzag = value shr 1 xor -(value and 1)
            if (zigzag != value && zigzag != 0L) {
                append(" [zigzag: $zigzag]")
            }
        }
    }

    private fun format64Bit(bytes: ByteArray): String {
        val buffer = ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
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

    private fun format32Bit(bytes: ByteArray): String {
        val buffer = ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
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

    private fun formatLengthDelimited(bytes: ByteArray): String {
        // Try to interpret as UTF-8 string
        val asString = tryDecodeUtf8(bytes)
        if (asString != null && asString.length <= 100) {
            return "\"$asString\" (${bytes.size} bytes)"
        }

        // Try to decode as embedded message
        if (looksLikeProtobuf(bytes)) {
            return "[embedded message: ${bytes.size} bytes]"
        }

        // Show as bytes
        return if (bytes.size <= 32) {
            bytes.joinToString(" ") { String.format(Locale.US, "%02X", it) } + " (${bytes.size} bytes)"
        } else {
            val preview = bytes.take(32).joinToString(" ") { String.format(Locale.US, "%02X", it) }
            "$preview ... (${bytes.size} bytes)"
        }
    }

    private fun tryDecodeUtf8(bytes: ByteArray): String? {
        return try {
            val str = String(bytes, Charsets.UTF_8)
            // Check if it looks like valid text
            if (str.all { it.isLetterOrDigit() || it.isWhitespace() || it in ".,;:!?-_@#\$%&*()[]{}'\"/\\<>=+" }) {
                str
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun formatFields(fields: List<ProtobufField>): String {
        if (fields.isEmpty()) return "[No fields decoded]"

        return fields.joinToString("\n") { field ->
            "Field ${field.fieldNumber} [${field.wireTypeName}]: ${field.value}"
        }
    }

    private fun formatHexDump(body: ByteArray): String {
        val sb = StringBuilder()
        sb.appendLine("[Raw hex dump - ${body.size} bytes]")
        sb.appendLine()

        val bytesPerLine = 16
        for (i in body.indices step bytesPerLine) {
            // Offset
            sb.append(String.format(Locale.US, "%08X  ", i))

            // Hex bytes
            for (j in 0 until bytesPerLine) {
                if (i + j < body.size) {
                    sb.append(String.format(Locale.US, "%02X ", body[i + j]))
                } else {
                    sb.append("   ")
                }
                if (j == 7) sb.append(" ")
            }

            sb.append(" |")

            // ASCII representation
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

    /** Wire-type constants for the protobuf binary encoding. */
    companion object {
        private const val WIRE_TYPE_VARINT = 0
        private const val WIRE_TYPE_64BIT = 1
        private const val WIRE_TYPE_LENGTH_DELIMITED = 2
        private const val WIRE_TYPE_START_GROUP = 3
        private const val WIRE_TYPE_END_GROUP = 4
        private const val WIRE_TYPE_32BIT = 5
    }
}

/**
 * Represents a decoded protobuf field.
 *
 * @property fieldNumber The field number from the protobuf tag.
 * @property wireType The numeric wire type identifier (0-5).
 * @property wireTypeName Human-readable wire type name (e.g., "Varint", "64-bit").
 * @property value Formatted string representation of the field value.
 */
data class ProtobufField(
    val fieldNumber: Int,
    val wireType: Int,
    val wireTypeName: String,
    val value: String,
)
