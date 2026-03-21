package com.azikar24.wormaceptor.infra.parser.protobuf

import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.entities.ProtobufDecodeResult
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ProtobufBodyParserTest {

    private val parser = ProtobufBodyParser()

    @Nested
    inner class SupportedContentTypes {
        @Test
        fun `supportedContentTypes contains protobuf and grpc types`() {
            parser.supportedContentTypes shouldBe listOf(
                "application/protobuf",
                "application/x-protobuf",
                "application/grpc",
                "application/grpc+proto",
            )
        }

        @Test
        fun `priority is 100`() {
            parser.priority shouldBe 100
        }
    }

    @Nested
    inner class CanParse {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/protobuf",
                "application/x-protobuf",
                "application/grpc",
                "application/grpc+proto",
                "application/protobuf; charset=utf-8",
                "application/x-protobuf; charset=binary",
                "Application/Protobuf",
                "APPLICATION/GRPC",
                "application/vnd.example+proto",
            ],
        )
        fun `canParse returns true for protobuf and grpc MIME types`(contentType: String) {
            parser.canParse(contentType, byteArrayOf()).shouldBeTrue()
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/plain",
                "application/xml",
                "application/octet-stream",
                "image/png",
            ],
        )
        fun `canParse returns false for non-protobuf MIME types`(contentType: String) {
            parser.canParse(contentType, byteArrayOf()).shouldBeFalse()
        }

        @Test
        fun `canParse returns false when contentType is null`() {
            parser.canParse(null, byteArrayOf()).shouldBeFalse()
        }

        @Test
        fun `canParse returns false for null contentType even with valid protobuf body`() {
            val body = encodeVarintField(1, 150)
            parser.canParse(null, body).shouldBeFalse()
        }
    }

    @Nested
    inner class Decode {

        @Test
        fun `decode returns Success with empty list for empty data`() {
            val result = parser.decode(byteArrayOf())
            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields.shouldBeEmpty()
        }

        @Test
        fun `decode returns Success for varint field`() {
            // field 1, wire type 0 (varint), value = 150
            val body = encodeVarintField(1, 150)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 1
            result.fields[0].fieldNumber shouldBe 1
            result.fields[0].wireType shouldBe 0
            result.fields[0].wireTypeName shouldBe "Varint"
            result.fields[0].value shouldContain "150"
        }

        @Test
        fun `decode returns Success for simple varint value 1`() {
            // field 1, wire type 0, value = 1
            val body = byteArrayOf(0x08, 0x01) // tag=(1<<3)|0=8, value=1
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 1
            result.fields[0].fieldNumber shouldBe 1
            result.fields[0].value shouldContain "1"
        }

        @Test
        fun `decode returns Success for 64-bit field`() {
            // field 2, wire type 1 (64-bit)
            val tag = ((2 shl 3) or 1).toByte() // 0x11
            val valueBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(42L).array()
            val body = byteArrayOf(tag) + valueBytes

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 1
            result.fields[0].fieldNumber shouldBe 2
            result.fields[0].wireType shouldBe 1
            result.fields[0].wireTypeName shouldBe "64-bit"
            result.fields[0].value shouldContain "int64: 42"
        }

        @Test
        fun `decode returns Success for length-delimited UTF-8 string`() {
            // field 3, wire type 2 (length-delimited), value = "hello"
            val tag = ((3 shl 3) or 2).toByte() // 0x1A
            val strBytes = "hello".toByteArray(Charsets.UTF_8)
            val length = strBytes.size.toByte()
            val body = byteArrayOf(tag, length) + strBytes

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 1
            result.fields[0].fieldNumber shouldBe 3
            result.fields[0].wireType shouldBe 2
            result.fields[0].wireTypeName shouldBe "Length-delimited"
            result.fields[0].value shouldContain "\"hello\""
            result.fields[0].value shouldContain "5 bytes"
        }

        @Test
        fun `decode returns Success for length-delimited binary data`() {
            // field 4, wire type 2, value = binary bytes with non-printable chars
            val tag = ((4 shl 3) or 2).toByte() // 0x22
            val binaryData = byteArrayOf(0x00, 0x01, 0x02, 0x80.toByte(), 0xFF.toByte())
            val length = binaryData.size.toByte()
            val body = byteArrayOf(tag, length) + binaryData

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 1
            result.fields[0].fieldNumber shouldBe 4
            result.fields[0].wireType shouldBe 2
            // Binary data should not be quoted like a string
            result.fields[0].value shouldContain "5 bytes"
        }

        @Test
        fun `decode returns Success for 32-bit field`() {
            // field 5, wire type 5 (32-bit)
            val tag = ((5 shl 3) or 5).toByte() // 0x2D
            val valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(100).array()
            val body = byteArrayOf(tag) + valueBytes

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 1
            result.fields[0].fieldNumber shouldBe 5
            result.fields[0].wireType shouldBe 5
            result.fields[0].wireTypeName shouldBe "32-bit"
            result.fields[0].value shouldContain "int32: 100"
        }

        @Test
        fun `decode returns Success for multiple fields`() {
            // field 1, varint=42 + field 2, varint=100
            val body = byteArrayOf(
                0x08,
                0x2A, // field 1, varint, value=42
                0x10,
                0x64.toByte(), // field 2, varint, value=100
            )
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 2
            result.fields[0].fieldNumber shouldBe 1
            result.fields[0].value shouldContain "42"
            result.fields[1].fieldNumber shouldBe 2
            result.fields[1].value shouldContain "100"
        }

        @Test
        fun `decode returns Failure with hex dump for incomplete varint`() {
            // A single byte with MSB set (indicating continuation) but no following byte
            val body = byteArrayOf(0x08, 0x80.toByte())
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Failure>()
            result.hexDump shouldContain "08"
        }

        @Test
        fun `decode returns Failure with hex dump for tag zero`() {
            // tag=0 means fieldNumber=0, which terminates parsing with empty list -> Failure
            val body = byteArrayOf(0x00)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Failure>()
        }

        @Test
        fun `decode returns Failure for insufficient 64-bit data`() {
            // field 1, wire type 1 (64-bit) but only 3 bytes of data
            val tag = ((1 shl 3) or 1).toByte() // 0x09
            val body = byteArrayOf(tag, 0x01, 0x02, 0x03)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Failure>()
        }

        @Test
        fun `decode returns Failure for insufficient 32-bit data`() {
            // field 1, wire type 5 (32-bit) but only 2 bytes of data
            val tag = ((1 shl 3) or 5).toByte() // 0x0D
            val body = byteArrayOf(tag, 0x01, 0x02)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Failure>()
        }

        @Test
        fun `decode handles start group and end group wire types`() {
            // field 1, wire type 3 (start group) + field 1, wire type 4 (end group)
            val startTag = ((1 shl 3) or 3).toByte() // 0x0B
            val endTag = ((1 shl 3) or 4).toByte() // 0x0C
            val body = byteArrayOf(startTag, endTag)

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 2
            result.fields[0].wireTypeName shouldBe "Start group"
            result.fields[0].value shouldBe "(deprecated)"
            result.fields[1].wireTypeName shouldBe "End group"
            result.fields[1].value shouldBe "(deprecated)"
        }
    }

    @Nested
    inner class VarintEncoding {

        @Test
        fun `decode handles multi-byte varint encoding`() {
            // field 1, varint = 300 = 0xAC 0x02
            val body = byteArrayOf(0x08, 0xAC.toByte(), 0x02)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields shouldHaveSize 1
            result.fields[0].value shouldContain "300"
        }

        @Test
        fun `decode formats zigzag interpretation for odd varint values`() {
            // field 1, varint = 3 -> zigzag = -2 (3 >> 1 xor -(3 & 1) = 1 xor -1 = -2)
            val body = byteArrayOf(0x08, 0x03)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields[0].value shouldContain "zigzag: -2"
        }

        @Test
        fun `decode does not show zigzag for value 0`() {
            // field 1, varint = 0 -> zigzag = 0 (same value, should not be shown)
            val body = byteArrayOf(0x08, 0x00)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields[0].value shouldBe "0"
        }

        @Test
        fun `decode does not show zigzag when zigzag equals value`() {
            // field 1, varint = 2 -> zigzag = 1 (different and non-zero -> shown)
            val body = byteArrayOf(0x08, 0x02)
            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields[0].value shouldContain "zigzag: 1"
        }
    }

    @Nested
    inner class ParseBody {

        @Test
        fun `parse returns empty body for empty input`() {
            val result = parser.parse(byteArrayOf())
            result.contentType shouldBe ContentType.PROTOBUF
            result.isValid.shouldBeTrue()
        }

        @Test
        fun `parseBody returns valid ParsedBody for decodable data`() {
            val body = byteArrayOf(0x08, 0x2A) // field 1, varint, value=42
            val result = parser.parse(body)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.PROTOBUF
            result.formatted shouldContain "Field 1"
            result.formatted shouldContain "Varint"
            result.formatted shouldContain "42"
            result.metadata["fieldCount"] shouldBe "1"
        }

        @Test
        fun `parseBody returns invalid ParsedBody for non-decodable data`() {
            // tag=0 -> empty fields -> Failure path
            val body = byteArrayOf(0x00)
            val result = parser.parse(body)

            result.isValid.shouldBeFalse()
            result.contentType shouldBe ContentType.PROTOBUF
            result.errorMessage shouldBe "Failed to decode protobuf wire format"
        }
    }

    @Nested
    inner class FormatOutput {

        @Test
        fun `64-bit field shows hex and int64 value`() {
            val tag = ((1 shl 3) or 1).toByte()
            val valueBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(12_345L).array()
            val body = byteArrayOf(tag) + valueBytes

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields[0].value shouldContain "0x"
            result.fields[0].value shouldContain "int64: 12345"
        }

        @Test
        fun `32-bit field shows hex and int32 value`() {
            val tag = ((1 shl 3) or 5).toByte()
            val valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(999).array()
            val body = byteArrayOf(tag) + valueBytes

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields[0].value shouldContain "0x"
            result.fields[0].value shouldContain "int32: 999"
        }

        @Test
        fun `64-bit field shows double when finite`() {
            val tag = ((1 shl 3) or 1).toByte()
            val valueBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(3.14).array()
            val body = byteArrayOf(tag) + valueBytes

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields[0].value shouldContain "double: 3.14"
        }

        @Test
        fun `32-bit field shows float when finite`() {
            val tag = ((1 shl 3) or 5).toByte()
            val valueBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(2.5f).array()
            val body = byteArrayOf(tag) + valueBytes

            val result = parser.decode(body)

            result.shouldBeInstanceOf<ProtobufDecodeResult.Success>()
            result.fields[0].value shouldContain "float: 2.5"
        }
    }

    /**
     * Helper to encode a single varint field in protobuf wire format.
     * Tag = (fieldNumber << 3) | 0 (wire type varint)
     * Value = varint encoding of [value]
     */
    private fun encodeVarintField(
        fieldNumber: Int,
        value: Long,
    ): ByteArray {
        val result = mutableListOf<Byte>()
        // Encode tag
        encodeVarint(result, ((fieldNumber shl 3) or 0).toLong())
        // Encode value
        encodeVarint(result, value)
        return result.toByteArray()
    }

    private fun encodeVarint(
        output: MutableList<Byte>,
        value: Long,
    ) {
        var v = value
        while (v and 0x7F.inv() != 0L) {
            output.add(((v and 0x7F) or 0x80).toByte())
            v = v ushr 7
        }
        output.add((v and 0x7F).toByte())
    }
}
