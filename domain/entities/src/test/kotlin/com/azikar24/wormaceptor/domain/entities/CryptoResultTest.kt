package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CryptoResultTest {

    @Nested
    inner class EncryptSuccessFactory {

        @Test
        fun `creates successful encryption result`() {
            val result = CryptoResult.encryptSuccess(
                id = "enc-001",
                input = "hello",
                output = "encrypted-output",
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.CBC,
                durationMs = 5L,
            )

            result.id shouldBe "enc-001"
            result.operation shouldBe CryptoOperation.ENCRYPT
            result.input shouldBe "hello"
            result.output shouldBe "encrypted-output"
            result.algorithm shouldBe CryptoAlgorithm.AES_256
            result.mode shouldBe CipherMode.CBC
            result.success shouldBe true
            result.errorMessage shouldBe null
            result.durationMs shouldBe 5L
        }

        @Test
        fun `timestamp is set to current time`() {
            val before = System.currentTimeMillis()
            val result = CryptoResult.encryptSuccess(
                id = "enc-001",
                input = "test",
                output = "out",
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.GCM,
                durationMs = 1L,
            )
            val after = System.currentTimeMillis()

            (result.timestamp in before..after) shouldBe true
        }
    }

    @Nested
    inner class DecryptSuccessFactory {

        @Test
        fun `creates successful decryption result`() {
            val result = CryptoResult.decryptSuccess(
                id = "dec-001",
                input = "encrypted-input",
                output = "hello",
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.GCM,
                durationMs = 3L,
            )

            result.operation shouldBe CryptoOperation.DECRYPT
            result.success shouldBe true
            result.output shouldBe "hello"
            result.errorMessage shouldBe null
        }
    }

    @Nested
    inner class FailureFactory {

        @Test
        fun `creates failed result with null output`() {
            val result = CryptoResult.failure(
                id = "fail-001",
                operation = CryptoOperation.ENCRYPT,
                input = "hello",
                algorithm = CryptoAlgorithm.DES,
                mode = CipherMode.ECB,
                errorMessage = "Invalid key length",
                durationMs = 1L,
            )

            result.success shouldBe false
            result.output shouldBe null
            result.errorMessage shouldBe "Invalid key length"
            result.operation shouldBe CryptoOperation.ENCRYPT
        }

        @Test
        fun `creates failed decryption result`() {
            val result = CryptoResult.failure(
                id = "fail-002",
                operation = CryptoOperation.DECRYPT,
                input = "bad-ciphertext",
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.CBC,
                errorMessage = "Padding error",
                durationMs = 2L,
            )

            result.operation shouldBe CryptoOperation.DECRYPT
            result.success shouldBe false
        }
    }

    @Nested
    inner class EqualityAndCopy {

        @Test
        fun `copy can change success flag`() {
            val original = CryptoResult.encryptSuccess(
                id = "enc-001",
                input = "hello",
                output = "out",
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.CBC,
                durationMs = 1L,
            )

            val failed = original.copy(success = false, errorMessage = "Error", output = null)

            failed.success shouldBe false
            failed.errorMessage shouldBe "Error"
            failed.output shouldBe null
            failed.id shouldBe original.id
        }
    }
}

class CryptoOperationTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly two values`() {
            CryptoOperation.entries.size shouldBe 2
        }

        @Test
        fun `should contain ENCRYPT and DECRYPT`() {
            CryptoOperation.entries.map { it.name } shouldContainExactly listOf(
                "ENCRYPT",
                "DECRYPT",
            )
        }

        @Test
        fun `ENCRYPT display name`() {
            CryptoOperation.ENCRYPT.displayName shouldBe "Encrypt"
        }

        @Test
        fun `DECRYPT display name`() {
            CryptoOperation.DECRYPT.displayName shouldBe "Decrypt"
        }
    }
}

class CryptoPresetTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly four presets`() {
            CryptoPreset.entries.size shouldBe 4
        }

        @Test
        fun `AES_256_GCM preset uses correct algorithm and mode`() {
            val preset = CryptoPreset.AES_256_GCM

            preset.displayName shouldBe "AES-256 GCM"
            preset.config.algorithm shouldBe CryptoAlgorithm.AES_256
            preset.config.mode shouldBe CipherMode.GCM
            preset.config.padding shouldBe PaddingScheme.NO_PADDING
        }

        @Test
        fun `AES_256_CBC preset uses correct algorithm and mode`() {
            val preset = CryptoPreset.AES_256_CBC

            preset.config.algorithm shouldBe CryptoAlgorithm.AES_256
            preset.config.mode shouldBe CipherMode.CBC
            preset.config.padding shouldBe PaddingScheme.PKCS5
        }

        @Test
        fun `AES_128_CBC preset uses correct algorithm`() {
            val preset = CryptoPreset.AES_128_CBC

            preset.config.algorithm shouldBe CryptoAlgorithm.AES_128
            preset.config.mode shouldBe CipherMode.CBC
        }

        @Test
        fun `TRIPLE_DES_CBC preset uses correct algorithm`() {
            val preset = CryptoPreset.TRIPLE_DES_CBC

            preset.config.algorithm shouldBe CryptoAlgorithm.TRIPLE_DES
            preset.config.mode shouldBe CipherMode.CBC
        }

        @Test
        fun `all presets have empty key and iv`() {
            CryptoPreset.entries.forEach { preset ->
                preset.config.key shouldBe ""
                preset.config.iv shouldBe ""
            }
        }

        @Test
        fun `all presets use BASE64 key format`() {
            CryptoPreset.entries.forEach { preset ->
                preset.config.keyFormat shouldBe KeyFormat.BASE64
            }
        }

        @Test
        fun `all presets have non-blank description`() {
            CryptoPreset.entries.forEach { preset ->
                (preset.description.isNotBlank()) shouldBe true
            }
        }
    }
}
