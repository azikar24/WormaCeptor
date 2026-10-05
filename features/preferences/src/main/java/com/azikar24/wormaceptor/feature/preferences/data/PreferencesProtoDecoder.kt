package com.azikar24.wormaceptor.feature.preferences.data

import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue

/**
 * Decodes a Preferences DataStore file (`.preferences_pb`) without depending on DataStore.
 *
 * Wire format, from androidx.datastore `PreferencesProto`:
 * ```
 * message PreferenceMap { map<string, Value> preferences = 1; }
 * message Value {
 *   oneof { bool boolean = 1; float float = 2; int32 integer = 3; int64 long = 4;
 *           string string = 5; StringSet string_set = 6; double double = 7; bytes bytes = 8; }
 * }
 * message StringSet { repeated string strings = 1; }
 * ```
 */
internal object PreferencesProtoDecoder {

    private const val FIELD_PREFERENCES = 1
    private const val FIELD_MAP_KEY = 1
    private const val FIELD_MAP_VALUE = 2
    private const val FIELD_BOOLEAN = 1
    private const val FIELD_FLOAT = 2
    private const val FIELD_INTEGER = 3
    private const val FIELD_LONG = 4
    private const val FIELD_STRING = 5
    private const val FIELD_STRING_SET = 6
    private const val FIELD_DOUBLE = 7
    private const val FIELD_BYTES = 8
    private const val FIELD_STRINGS = 1

    private const val WIRE_VARINT = 0
    private const val WIRE_FIXED64 = 1
    private const val WIRE_LENGTH_DELIMITED = 2
    private const val WIRE_FIXED32 = 5

    private const val TAG_TYPE_BITS = 3
    private const val TAG_TYPE_MASK = 0x7
    private const val VARINT_PAYLOAD_MASK = 0x7F
    private const val VARINT_CONTINUATION = 0x80
    private const val BITS_PER_VARINT_BYTE = 7
    private const val MAX_VARINT_SHIFT = 63
    private const val BITS_PER_BYTE = 8
    private const val BYTE_MASK = 0xFF
    private const val FIXED32_SIZE = 4
    private const val FIXED64_SIZE = 8

    /**
     * @throws IllegalArgumentException if [bytes] is not a valid preferences proto
     */
    fun decode(bytes: ByteArray): List<PreferenceItem> {
        val reader = ProtoReader(bytes)
        val items = buildList {
            while (reader.hasMore()) {
                val tag = reader.readTag()
                if (tag.field == FIELD_PREFERENCES && tag.wireType == WIRE_LENGTH_DELIMITED) {
                    decodeEntry(reader.readBytes())?.let(::add)
                } else {
                    reader.skip(tag.wireType)
                }
            }
        }
        return items.sortedBy { it.key.lowercase() }
    }

    private fun decodeEntry(bytes: ByteArray): PreferenceItem? {
        val reader = ProtoReader(bytes)
        var key: String? = null
        var value: PreferenceValue? = null
        while (reader.hasMore()) {
            val tag = reader.readTag()
            when {
                tag.field == FIELD_MAP_KEY && tag.wireType == WIRE_LENGTH_DELIMITED -> key = reader.readString()
                tag.field == FIELD_MAP_VALUE && tag.wireType == WIRE_LENGTH_DELIMITED ->
                    value = decodeValue(reader.readBytes())
                else -> reader.skip(tag.wireType)
            }
        }
        return if (key != null && value != null) PreferenceItem(key, value) else null
    }

    @Suppress("CyclomaticComplexMethod")
    private fun decodeValue(bytes: ByteArray): PreferenceValue? {
        val reader = ProtoReader(bytes)
        var value: PreferenceValue? = null
        while (reader.hasMore()) {
            val tag = reader.readTag()
            value = when (tag.field) {
                FIELD_BOOLEAN -> PreferenceValue.BooleanValue(reader.readVarint() != 0L)
                FIELD_FLOAT -> PreferenceValue.FloatValue(Float.fromBits(reader.readFixed32()))
                FIELD_INTEGER -> PreferenceValue.IntValue(reader.readVarint().toInt())
                FIELD_LONG -> PreferenceValue.LongValue(reader.readVarint())
                FIELD_STRING -> PreferenceValue.StringValue(reader.readString())
                FIELD_STRING_SET -> PreferenceValue.StringSetValue(decodeStringSet(reader.readBytes()))
                FIELD_DOUBLE -> PreferenceValue.DoubleValue(Double.fromBits(reader.readFixed64()))
                FIELD_BYTES -> PreferenceValue.BytesValue(reader.readBytes())
                else -> {
                    reader.skip(tag.wireType)
                    value
                }
            }
        }
        return value
    }

    private fun decodeStringSet(bytes: ByteArray): Set<String> {
        val reader = ProtoReader(bytes)
        return buildSet {
            while (reader.hasMore()) {
                val tag = reader.readTag()
                if (tag.field == FIELD_STRINGS && tag.wireType == WIRE_LENGTH_DELIMITED) {
                    add(reader.readString())
                } else {
                    reader.skip(tag.wireType)
                }
            }
        }
    }

    private data class Tag(val field: Int, val wireType: Int)

    private class ProtoReader(private val bytes: ByteArray) {
        private var position = 0

        fun hasMore(): Boolean = position < bytes.size

        fun readTag(): Tag {
            val raw = readVarint().toInt()
            return Tag(field = raw ushr TAG_TYPE_BITS, wireType = raw and TAG_TYPE_MASK)
        }

        fun readVarint(): Long {
            var result = 0L
            var shift = 0
            while (true) {
                require(shift <= MAX_VARINT_SHIFT) { "Malformed varint" }
                val byte = readByte()
                result = result or ((byte and VARINT_PAYLOAD_MASK).toLong() shl shift)
                if (byte and VARINT_CONTINUATION == 0) return result
                shift += BITS_PER_VARINT_BYTE
            }
        }

        fun readFixed32(): Int = (0 until FIXED32_SIZE).fold(0) { acc, i ->
            acc or (readByte() shl i * BITS_PER_BYTE)
        }

        fun readFixed64(): Long = (0 until FIXED64_SIZE).fold(0L) { acc, i ->
            acc or (readByte().toLong() shl i * BITS_PER_BYTE)
        }

        fun readBytes(): ByteArray {
            val length = readVarint().toInt()
            require(length >= 0 && position + length <= bytes.size) { "Truncated field" }
            return bytes.copyOfRange(position, position + length).also { position += length }
        }

        fun readString(): String = readBytes().decodeToString()

        fun skip(wireType: Int) {
            when (wireType) {
                WIRE_VARINT -> readVarint()
                WIRE_FIXED64 -> advance(FIXED64_SIZE)
                WIRE_LENGTH_DELIMITED -> readBytes()
                WIRE_FIXED32 -> advance(FIXED32_SIZE)
                else -> throw IllegalArgumentException("Unsupported wire type $wireType")
            }
        }

        private fun advance(count: Int) {
            require(position + count <= bytes.size) { "Truncated field" }
            position += count
        }

        private fun readByte(): Int {
            require(position < bytes.size) { "Truncated field" }
            return bytes[position++].toInt() and BYTE_MASK
        }
    }
}
