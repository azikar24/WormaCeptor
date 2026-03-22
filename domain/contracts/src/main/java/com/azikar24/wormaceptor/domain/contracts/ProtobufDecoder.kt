package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.ProtobufDecodeResult

/**
 * Decoder for protobuf wire format data without a schema.
 *
 * Implementations parse raw protobuf bytes and extract field information
 * including field numbers, wire types, and decoded values.
 */
interface ProtobufDecoder {
    /**
     * Decodes protobuf wire format bytes into structured field data.
     *
     * @param data The raw protobuf bytes
     * @return A [ProtobufDecodeResult] containing decoded fields or a hex dump on failure
     */
    fun decode(data: ByteArray): ProtobufDecodeResult
}
