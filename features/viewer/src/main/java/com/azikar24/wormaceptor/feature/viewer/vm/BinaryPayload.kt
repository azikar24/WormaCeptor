package com.azikar24.wormaceptor.feature.viewer.vm

/**
 * Wrapper around [ByteArray] with proper value-based equality.
 * Eliminates duplicated `equals`/`hashCode` overrides across effect classes.
 */
data class BinaryPayload(
    val bytes: ByteArray,
    val format: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BinaryPayload) return false
        return bytes.contentEquals(other.bytes) && format == other.format
    }

    override fun hashCode(): Int = bytes.contentHashCode() * 31 + format.hashCode()
}
