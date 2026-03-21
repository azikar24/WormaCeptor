package com.azikar24.wormaceptor.domain.entities

/**
 * A decoded protobuf field extracted from wire format data.
 *
 * @property fieldNumber The field number from the protobuf tag.
 * @property wireType The wire type identifier (0=varint, 1=64-bit, 2=length-delimited, 5=32-bit).
 * @property wireTypeName Human-readable wire type name (e.g. "Varint", "64-bit").
 * @property value The decoded and formatted field value.
 */
data class ProtobufField(
    val fieldNumber: Int,
    val wireType: Int,
    val wireTypeName: String,
    val value: String,
)

/**
 * Result of decoding protobuf wire format data.
 */
sealed class ProtobufDecodeResult {
    /**
     * Successful decode containing extracted fields.
     *
     * @property fields The list of decoded protobuf fields.
     */
    data class Success(val fields: List<ProtobufField>) : ProtobufDecodeResult()

    /**
     * Failed decode with a hex dump fallback.
     *
     * @property hexDump Hex dump representation of the raw bytes.
     */
    data class Failure(val hexDump: String) : ProtobufDecodeResult()
}
