package com.azikar24.wormaceptor.feature.crypto

import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoOperation
import com.azikar24.wormaceptor.domain.entities.CryptoPreset
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.domain.entities.PaddingScheme
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private class UnrelatedViewModel : androidx.lifecycle.ViewModel()

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val configFlow = MutableStateFlow(CryptoConfig.default())
    private val currentResultFlow = MutableStateFlow<CryptoResult?>(null)
    private val historyFlow = MutableStateFlow<List<CryptoResult>>(emptyList())
    private val isProcessingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)

    private val engine = mockk<CryptoEngine>(relaxed = true) {
        every { config } returns configFlow
        every { currentResult } returns currentResultFlow
        every { history } returns historyFlow
        every { isProcessing } returns isProcessingFlow
        every { error } returns errorFlow
    }

    private lateinit var viewModel: CryptoViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CryptoViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleResult(
        id: String = "result_1",
        operation: CryptoOperation = CryptoOperation.ENCRYPT,
        input: String = "Hello World",
        output: String? = "encrypted_output",
        success: Boolean = true,
    ) = CryptoResult(
        id = id,
        operation = operation,
        input = input,
        output = output,
        algorithm = CryptoAlgorithm.AES_256,
        mode = CipherMode.CBC,
        success = success,
        errorMessage = if (success) null else "Error",
        timestamp = System.currentTimeMillis(),
        durationMs = 10L,
    )

    @Nested
    inner class `initial state` {

        @Test
        fun `inputText is empty`() = runTest {
            viewModel.inputText shouldBe ""
        }

        @Test
        fun `config reflects engine default`() = runTest {
            viewModel.config.value shouldBe CryptoConfig.default()
        }

        @Test
        fun `currentResult is null`() = runTest {
            viewModel.currentResult.value shouldBe null
        }

        @Test
        fun `history is empty`() = runTest {
            viewModel.history.value shouldBe emptyList()
        }

        @Test
        fun `isProcessing is false`() = runTest {
            viewModel.isProcessing.value shouldBe false
        }

        @Test
        fun `error is null`() = runTest {
            viewModel.error.value shouldBe null
        }
    }

    @Nested
    inner class `updateInputText` {

        @Test
        fun `updates inputText`() = runTest {
            viewModel.updateInputText("Hello World")

            viewModel.inputText shouldBe "Hello World"
        }

        @Test
        fun `allows empty text`() = runTest {
            viewModel.updateInputText("something")
            viewModel.updateInputText("")

            viewModel.inputText shouldBe ""
        }
    }

    @Nested
    inner class `setAlgorithm` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.setAlgorithm(CryptoAlgorithm.AES_128)

            verify { engine.setAlgorithm(CryptoAlgorithm.AES_128) }
        }

        @Test
        fun `sets DES algorithm`() = runTest {
            viewModel.setAlgorithm(CryptoAlgorithm.DES)

            verify { engine.setAlgorithm(CryptoAlgorithm.DES) }
        }

        @Test
        fun `sets TRIPLE_DES algorithm`() = runTest {
            viewModel.setAlgorithm(CryptoAlgorithm.TRIPLE_DES)

            verify { engine.setAlgorithm(CryptoAlgorithm.TRIPLE_DES) }
        }
    }

    @Nested
    inner class `setMode` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.setMode(CipherMode.GCM)

            verify { engine.setMode(CipherMode.GCM) }
        }

        @Test
        fun `sets ECB mode`() = runTest {
            viewModel.setMode(CipherMode.ECB)

            verify { engine.setMode(CipherMode.ECB) }
        }

        @Test
        fun `sets CTR mode`() = runTest {
            viewModel.setMode(CipherMode.CTR)

            verify { engine.setMode(CipherMode.CTR) }
        }
    }

    @Nested
    inner class `setPadding` {

        @Test
        fun `delegates to engine updateConfig`() = runTest {
            viewModel.setPadding(PaddingScheme.NO_PADDING)

            verify { engine.updateConfig(any()) }
        }

        @Test
        fun `sets PKCS5 padding`() = runTest {
            viewModel.setPadding(PaddingScheme.PKCS5)

            verify { engine.updateConfig(any()) }
        }
    }

    @Nested
    inner class `setKey` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.setKey("my-secret-key")

            verify { engine.setKey("my-secret-key") }
        }
    }

    @Nested
    inner class `setIv` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.setIv("my-iv-value")

            verify { engine.setIv("my-iv-value") }
        }
    }

    @Nested
    inner class `setKeyFormat` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.setKeyFormat(KeyFormat.HEX)

            verify { engine.setKeyFormat(KeyFormat.HEX) }
        }

        @Test
        fun `sets UTF8 format`() = runTest {
            viewModel.setKeyFormat(KeyFormat.UTF8)

            verify { engine.setKeyFormat(KeyFormat.UTF8) }
        }
    }

    @Nested
    inner class `applyPreset` {

        @Test
        fun `applies preset config to engine`() = runTest {
            viewModel.applyPreset(CryptoPreset.AES_256_GCM)

            verify { engine.setConfig(CryptoPreset.AES_256_GCM.config) }
        }

        @Test
        fun `applies AES_128_CBC preset`() = runTest {
            viewModel.applyPreset(CryptoPreset.AES_128_CBC)

            verify { engine.setConfig(CryptoPreset.AES_128_CBC.config) }
        }

        @Test
        fun `applies TRIPLE_DES_CBC preset`() = runTest {
            viewModel.applyPreset(CryptoPreset.TRIPLE_DES_CBC)

            verify { engine.setConfig(CryptoPreset.TRIPLE_DES_CBC.config) }
        }
    }

    @Nested
    inner class `generateKey` {

        @Test
        fun `generates key via engine and sets it`() = runTest {
            every { engine.generateKey() } returns "generated-key-base64"

            val result = viewModel.generateKey()

            result shouldBe "generated-key-base64"
            verify { engine.generateKey() }
            verify { engine.setKey("generated-key-base64") }
        }

        @Test
        fun `returns non-blank key`() = runTest {
            every { engine.generateKey() } returns "abc123"

            val key = viewModel.generateKey()

            key.shouldNotBeBlank()
        }
    }

    @Nested
    inner class `generateIv` {

        @Test
        fun `generates IV via engine and sets it`() = runTest {
            every { engine.generateIv() } returns "generated-iv-base64"

            val result = viewModel.generateIv()

            result shouldBe "generated-iv-base64"
            verify { engine.generateIv() }
            verify { engine.setIv("generated-iv-base64") }
        }

        @Test
        fun `returns non-blank IV`() = runTest {
            every { engine.generateIv() } returns "iv-value"

            val iv = viewModel.generateIv()

            iv.shouldNotBeBlank()
        }
    }

    @Nested
    inner class `encrypt` {

        @Test
        fun `calls engine encrypt with input text`() = runTest {
            viewModel.updateInputText("Hello World")

            viewModel.encrypt()

            verify { engine.encrypt("Hello World") }
        }

        @Test
        fun `does not call engine when input is blank`() = runTest {
            viewModel.updateInputText("")

            viewModel.encrypt()

            verify(exactly = 0) { engine.encrypt(any()) }
        }

        @Test
        fun `does not call engine when input is whitespace only`() = runTest {
            viewModel.updateInputText("   ")

            viewModel.encrypt()

            verify(exactly = 0) { engine.encrypt(any()) }
        }
    }

    @Nested
    inner class `decrypt` {

        @Test
        fun `calls engine decrypt with input text`() = runTest {
            viewModel.updateInputText("encrypted_base64_data")

            viewModel.decrypt()

            verify { engine.decrypt("encrypted_base64_data") }
        }

        @Test
        fun `does not call engine when input is blank`() = runTest {
            viewModel.updateInputText("")

            viewModel.decrypt()

            verify(exactly = 0) { engine.decrypt(any()) }
        }

        @Test
        fun `does not call engine when input is whitespace only`() = runTest {
            viewModel.updateInputText("   ")

            viewModel.decrypt()

            verify(exactly = 0) { engine.decrypt(any()) }
        }
    }

    @Nested
    inner class `clearResult` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.clearResult()

            verify { engine.clearResult() }
        }
    }

    @Nested
    inner class `clearHistory` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.clearHistory()

            verify { engine.clearHistory() }
        }
    }

    @Nested
    inner class `removeFromHistory` {

        @Test
        fun `delegates to engine with correct id`() = runTest {
            viewModel.removeFromHistory("result_42")

            verify { engine.removeFromHistory("result_42") }
        }
    }

    @Nested
    inner class `loadFromHistory` {

        @Test
        fun `restores input text from result`() = runTest {
            val result = sampleResult(input = "Original Input Text")

            viewModel.loadFromHistory(result)

            viewModel.inputText shouldBe "Original Input Text"
        }

        @Test
        fun `overwrites existing input text`() = runTest {
            viewModel.updateInputText("Existing text")
            val result = sampleResult(input = "New Input Text")

            viewModel.loadFromHistory(result)

            viewModel.inputText shouldBe "New Input Text"
        }

        @Test
        fun `handles empty input from history`() = runTest {
            val result = sampleResult(input = "")

            viewModel.loadFromHistory(result)

            viewModel.inputText shouldBe ""
        }
    }

    @Nested
    inner class `state flows reflect engine` {

        @Test
        fun `config updates when engine config changes`() = runTest {
            val newConfig = CryptoConfig(
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.ECB,
                padding = PaddingScheme.NO_PADDING,
                key = "test-key",
                iv = "",
                keyFormat = KeyFormat.HEX,
            )
            configFlow.value = newConfig

            viewModel.config.value shouldBe newConfig
        }

        @Test
        fun `currentResult updates when engine result changes`() = runTest {
            val result = sampleResult()
            currentResultFlow.value = result

            viewModel.currentResult.value shouldBe result
        }

        @Test
        fun `history updates when engine history changes`() = runTest {
            val results = listOf(sampleResult("r1"), sampleResult("r2"))
            historyFlow.value = results

            viewModel.history.value shouldBe results
        }

        @Test
        fun `isProcessing updates when engine processing changes`() = runTest {
            isProcessingFlow.value = true

            viewModel.isProcessing.value shouldBe true
        }

        @Test
        fun `error updates when engine error changes`() = runTest {
            errorFlow.value = "Something went wrong"

            viewModel.error.value shouldBe "Something went wrong"
        }
    }

    @Nested
    inner class `CryptoViewModelFactory` {

        @Test
        fun `creates CryptoViewModel instance`() = runTest {
            val factory = CryptoViewModelFactory(engine)

            val vm = factory.create(CryptoViewModel::class.java)

            vm.inputText shouldBe ""
        }

        @Test
        fun `throws for unrelated ViewModel class`() = runTest {
            val factory = CryptoViewModelFactory(engine)

            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                factory.create(UnrelatedViewModel::class.java)
            }
        }
    }
}
