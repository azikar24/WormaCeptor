package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.domain.entities.PaddingScheme
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CryptoEngineTest {

    @Nested
    inner class IvLength {

        @Test
        fun `AES CBC uses 16 byte IV`() {
            CryptoEngine.ivLengthFor(CryptoAlgorithm.AES_256, CipherMode.CBC) shouldBe 16
        }

        @Test
        fun `GCM uses 12 byte IV`() {
            CryptoEngine.ivLengthFor(CryptoAlgorithm.AES_128, CipherMode.GCM) shouldBe 12
        }

        @Test
        fun `DES and 3DES use 8 byte IV`() {
            CryptoEngine.ivLengthFor(CryptoAlgorithm.DES, CipherMode.CBC) shouldBe 8
            CryptoEngine.ivLengthFor(CryptoAlgorithm.TRIPLE_DES, CipherMode.CBC) shouldBe 8
        }

        @Test
        fun `generated hex IV for 3DES is 8 bytes`() {
            val engine = CryptoEngine()
            engine.setAlgorithm(CryptoAlgorithm.TRIPLE_DES)
            engine.setKeyFormat(KeyFormat.HEX)

            engine.generateIv().length shouldBe 16
        }
    }

    @Nested
    inner class Utf8Generation {

        @Test
        fun `UTF8 key encodes to exactly the key length in bytes`() {
            val engine = CryptoEngine()
            engine.setAlgorithm(CryptoAlgorithm.AES_256)
            engine.setKeyFormat(KeyFormat.UTF8)

            repeat(20) {
                val key = engine.generateKey()
                key.toByteArray(Charsets.UTF_8).size shouldBe 32
                key.all { it in '!'..'~' } shouldBe true
            }
        }

        @Test
        fun `UTF8 IV encodes to exactly the IV length in bytes`() {
            val engine = CryptoEngine()
            engine.setKeyFormat(KeyFormat.UTF8)
            engine.setMode(CipherMode.CBC)

            engine.generateIv().toByteArray(Charsets.UTF_8).size shouldBe 16
        }
    }

    @Nested
    inner class Transformation {

        private val pkcs5Config = CryptoConfig.default().copy(padding = PaddingScheme.PKCS5)

        @Test
        fun `block modes keep configured padding`() {
            CryptoEngine.transformationFor(pkcs5Config.copy(mode = CipherMode.CBC)) shouldBe "AES/CBC/PKCS5Padding"
        }

        @Test
        fun `stream and AEAD modes force NoPadding`() {
            listOf(CipherMode.GCM, CipherMode.CTR, CipherMode.CFB, CipherMode.OFB).forEach { mode ->
                CryptoEngine.transformationFor(pkcs5Config.copy(mode = mode)) shouldBe "AES/${mode.modeName}/NoPadding"
            }
        }
    }
}
