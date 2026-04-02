package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CryptoConfigTest {

    @Nested
    inner class DefaultFactory {

        @Test
        fun `default uses AES_256`() {
            val config = CryptoConfig.default()

            config.algorithm shouldBe CryptoAlgorithm.AES_256
        }

        @Test
        fun `default uses CBC mode`() {
            val config = CryptoConfig.default()

            config.mode shouldBe CipherMode.CBC
        }

        @Test
        fun `default uses PKCS5 padding`() {
            val config = CryptoConfig.default()

            config.padding shouldBe PaddingScheme.PKCS5
        }

        @Test
        fun `default has empty key and iv`() {
            val config = CryptoConfig.default()

            config.key shouldBe ""
            config.iv shouldBe ""
        }

        @Test
        fun `default uses BASE64 key format`() {
            val config = CryptoConfig.default()

            config.keyFormat shouldBe KeyFormat.BASE64
        }
    }

    @Nested
    inner class GetTransformation {

        @Test
        fun `returns correct transformation for AES CBC PKCS5`() {
            val config = CryptoConfig.default()

            config.getTransformation() shouldBe "AES/CBC/PKCS5Padding"
        }

        @Test
        fun `returns correct transformation for AES GCM NoPadding`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.GCM,
                padding = PaddingScheme.NO_PADDING,
                key = "",
                iv = "",
                keyFormat = KeyFormat.BASE64,
            )

            config.getTransformation() shouldBe "AES/GCM/NoPadding"
        }

        @Test
        fun `returns correct transformation for DES ECB PKCS5`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.DES,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "",
                iv = "",
                keyFormat = KeyFormat.BASE64,
            )

            config.getTransformation() shouldBe "DES/ECB/PKCS5Padding"
        }

        @Test
        fun `returns correct transformation for 3DES CBC PKCS7`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.TRIPLE_DES,
                mode = CipherMode.CBC,
                padding = PaddingScheme.PKCS7,
                key = "",
                iv = "",
                keyFormat = KeyFormat.BASE64,
            )

            config.getTransformation() shouldBe "DESede/CBC/PKCS7Padding"
        }
    }

    @Nested
    inner class IsValid {

        @Test
        fun `returns false for blank key`() {
            val config = CryptoConfig.default().copy(key = "")

            config.isValid() shouldBe false
        }

        @Test
        fun `returns false for blank key with whitespace only`() {
            val config = CryptoConfig.default().copy(key = "   ")

            config.isValid() shouldBe false
        }

        @Test
        fun `returns false when CBC mode requires iv but iv is blank`() {
            // AES-256 with a 32-byte key in UTF-8
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.CBC,
                padding = PaddingScheme.PKCS5,
                key = "12345678901234567890123456789012", // 32 bytes
                iv = "",
                keyFormat = KeyFormat.UTF8,
            )

            config.isValid() shouldBe false
        }

        @Test
        fun `returns true for valid AES-256 UTF8 key with IV`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.CBC,
                padding = PaddingScheme.PKCS5,
                key = "12345678901234567890123456789012", // 32 bytes
                iv = "1234567890123456", // 16 bytes
                keyFormat = KeyFormat.UTF8,
            )

            config.isValid() shouldBe true
        }

        @Test
        fun `returns false for wrong key length AES-256 UTF8`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "short", // only 5 bytes, not 32
                iv = "",
                keyFormat = KeyFormat.UTF8,
            )

            config.isValid() shouldBe false
        }

        @Test
        fun `returns true for valid AES-128 UTF8 key in ECB mode`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "1234567890123456", // 16 bytes
                iv = "",
                keyFormat = KeyFormat.UTF8,
            )

            config.isValid() shouldBe true
        }

        @Test
        fun `returns true for valid DES key in ECB mode`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.DES,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "12345678", // 8 bytes
                iv = "",
                keyFormat = KeyFormat.UTF8,
            )

            config.isValid() shouldBe true
        }

        @Test
        fun `returns true for valid TRIPLE_DES key in ECB mode`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.TRIPLE_DES,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "123456789012345678901234", // 24 bytes
                iv = "",
                keyFormat = KeyFormat.UTF8,
            )

            config.isValid() shouldBe true
        }

        @Test
        fun `returns true for RSA with any key length`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.RSA,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "anykey",
                iv = "",
                keyFormat = KeyFormat.UTF8,
            )

            config.isValid() shouldBe true
        }

        @Test
        fun `returns false for invalid hex key`() {
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "ZZZZ", // not valid hex
                iv = "",
                keyFormat = KeyFormat.HEX,
            )

            config.isValid() shouldBe false
        }

        @Test
        fun `returns true for valid hex key with correct length`() {
            // 16 bytes = 32 hex chars for AES-128
            val config = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.ECB,
                padding = PaddingScheme.PKCS5,
                key = "00112233445566778899aabbccddeeff",
                iv = "",
                keyFormat = KeyFormat.HEX,
            )

            config.isValid() shouldBe true
        }
    }

    @Nested
    inner class EqualityAndCopy {

        @Test
        fun `equal configs have the same hashCode`() {
            val c1 = CryptoConfig.default()
            val c2 = CryptoConfig.default()

            c1 shouldBe c2
            c1.hashCode() shouldBe c2.hashCode()
        }

        @Test
        fun `copy can change algorithm`() {
            val original = CryptoConfig.default()
            val changed = original.copy(algorithm = CryptoAlgorithm.AES_128)

            changed.algorithm shouldBe CryptoAlgorithm.AES_128
            changed.mode shouldBe original.mode
        }
    }
}

class CryptoAlgorithmTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly five values`() {
            CryptoAlgorithm.entries.size shouldBe 5
        }

        @Test
        fun `should contain all algorithms`() {
            CryptoAlgorithm.entries.map { it.name } shouldContainExactly listOf(
                "AES_128",
                "AES_256",
                "DES",
                "TRIPLE_DES",
                "RSA",
            )
        }

        @Test
        fun `AES_128 properties`() {
            val algo = CryptoAlgorithm.AES_128

            algo.algorithmName shouldBe "AES"
            algo.displayName shouldBe "AES-128"
            algo.keyLengthBits shouldBe 128
        }

        @Test
        fun `AES_256 properties`() {
            val algo = CryptoAlgorithm.AES_256

            algo.algorithmName shouldBe "AES"
            algo.displayName shouldBe "AES-256"
            algo.keyLengthBits shouldBe 256
        }

        @Test
        fun `DES properties`() {
            val algo = CryptoAlgorithm.DES

            algo.algorithmName shouldBe "DES"
            algo.keyLengthBits shouldBe 64
        }

        @Test
        fun `TRIPLE_DES properties`() {
            val algo = CryptoAlgorithm.TRIPLE_DES

            algo.algorithmName shouldBe "DESede"
            algo.keyLengthBits shouldBe 192
        }

        @Test
        fun `RSA properties`() {
            val algo = CryptoAlgorithm.RSA

            algo.algorithmName shouldBe "RSA"
            algo.keyLengthBits shouldBe 2048
        }
    }
}

class CipherModeTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly six values`() {
            CipherMode.entries.size shouldBe 6
        }

        @Test
        fun `CBC requires IV`() {
            CipherMode.CBC.requiresIv shouldBe true
        }

        @Test
        fun `ECB does not require IV`() {
            CipherMode.ECB.requiresIv shouldBe false
        }

        @Test
        fun `GCM requires IV`() {
            CipherMode.GCM.requiresIv shouldBe true
        }

        @Test
        fun `CTR requires IV`() {
            CipherMode.CTR.requiresIv shouldBe true
        }

        @Test
        fun `mode names match expected values`() {
            CipherMode.CBC.modeName shouldBe "CBC"
            CipherMode.ECB.modeName shouldBe "ECB"
            CipherMode.GCM.modeName shouldBe "GCM"
            CipherMode.CTR.modeName shouldBe "CTR"
            CipherMode.CFB.modeName shouldBe "CFB"
            CipherMode.OFB.modeName shouldBe "OFB"
        }
    }
}

class PaddingSchemeTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly four values`() {
            PaddingScheme.entries.size shouldBe 4
        }

        @Test
        fun `padding names match expected values`() {
            PaddingScheme.PKCS5.paddingName shouldBe "PKCS5Padding"
            PaddingScheme.PKCS7.paddingName shouldBe "PKCS7Padding"
            PaddingScheme.NO_PADDING.paddingName shouldBe "NoPadding"
            PaddingScheme.ISO10126.paddingName shouldBe "ISO10126Padding"
        }

        @Test
        fun `display names match expected values`() {
            PaddingScheme.PKCS5.displayName shouldBe "PKCS5"
            PaddingScheme.NO_PADDING.displayName shouldBe "None"
        }
    }
}

class KeyFormatTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly three values`() {
            KeyFormat.entries.size shouldBe 3
        }

        @Test
        fun `display names match expected values`() {
            KeyFormat.BASE64.displayName shouldBe "Base64"
            KeyFormat.HEX.displayName shouldBe "Hex"
            KeyFormat.UTF8.displayName shouldBe "UTF-8 Text"
        }
    }
}
