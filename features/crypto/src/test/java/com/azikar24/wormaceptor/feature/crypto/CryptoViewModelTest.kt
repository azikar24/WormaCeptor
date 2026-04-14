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
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewModel
import io.kotest.matchers.shouldBe
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
            viewModel.uiState.value.inputText shouldBe ""
        }

        @Test
        fun `config reflects engine default`() = runTest {
            viewModel.uiState.value.config shouldBe CryptoConfig.default()
        }

        @Test
        fun `currentResult is null`() = runTest {
            viewModel.uiState.value.currentResult shouldBe null
        }

        @Test
        fun `history is empty`() = runTest {
            viewModel.uiState.value.history shouldBe emptyList()
        }

        @Test
        fun `isProcessing is false`() = runTest {
            viewModel.uiState.value.isProcessing shouldBe false
        }

        @Test
        fun `error is null`() = runTest {
            viewModel.uiState.value.error shouldBe null
        }
    }

    @Nested
    inner class `UpdateText` {

        @Test
        fun `updates inputText`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText("Hello World"))

            viewModel.uiState.value.inputText shouldBe "Hello World"
        }

        @Test
        fun `allows empty text`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText("something"))
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText(""))

            viewModel.uiState.value.inputText shouldBe ""
        }
    }

    @Nested
    inner class `SetAlgorithm` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetAlgorithm(CryptoAlgorithm.AES_128))

            verify { engine.setAlgorithm(CryptoAlgorithm.AES_128) }
        }

        @Test
        fun `sets DES algorithm`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetAlgorithm(CryptoAlgorithm.DES))

            verify { engine.setAlgorithm(CryptoAlgorithm.DES) }
        }

        @Test
        fun `sets TRIPLE_DES algorithm`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetAlgorithm(CryptoAlgorithm.TRIPLE_DES))

            verify { engine.setAlgorithm(CryptoAlgorithm.TRIPLE_DES) }
        }
    }

    @Nested
    inner class `SetMode` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetMode(CipherMode.GCM))

            verify { engine.setMode(CipherMode.GCM) }
        }

        @Test
        fun `sets ECB mode`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetMode(CipherMode.ECB))

            verify { engine.setMode(CipherMode.ECB) }
        }

        @Test
        fun `sets CTR mode`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetMode(CipherMode.CTR))

            verify { engine.setMode(CipherMode.CTR) }
        }
    }

    @Nested
    inner class `SetPadding` {

        @Test
        fun `delegates to engine updateConfig`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetPadding(PaddingScheme.NO_PADDING))

            verify { engine.updateConfig(any()) }
        }

        @Test
        fun `sets PKCS5 padding`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetPadding(PaddingScheme.PKCS5))

            verify { engine.updateConfig(any()) }
        }
    }

    @Nested
    inner class `SetKey` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetKey("my-secret-key"))

            verify { engine.setKey("my-secret-key") }
        }
    }

    @Nested
    inner class `SetIv` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetIv("my-iv-value"))

            verify { engine.setIv("my-iv-value") }
        }
    }

    @Nested
    inner class `SetKeyFormat` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetKeyFormat(KeyFormat.HEX))

            verify { engine.setKeyFormat(KeyFormat.HEX) }
        }

        @Test
        fun `sets UTF8 format`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.SetKeyFormat(KeyFormat.UTF8))

            verify { engine.setKeyFormat(KeyFormat.UTF8) }
        }
    }

    @Nested
    inner class `ApplyPreset` {

        @Test
        fun `applies preset config to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.ApplyPreset(CryptoPreset.AES_256_GCM))

            verify { engine.setConfig(CryptoPreset.AES_256_GCM.config) }
        }

        @Test
        fun `applies AES_128_CBC preset`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.ApplyPreset(CryptoPreset.AES_128_CBC))

            verify { engine.setConfig(CryptoPreset.AES_128_CBC.config) }
        }

        @Test
        fun `applies TRIPLE_DES_CBC preset`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Config.ApplyPreset(CryptoPreset.TRIPLE_DES_CBC))

            verify { engine.setConfig(CryptoPreset.TRIPLE_DES_CBC.config) }
        }
    }

    @Nested
    inner class `GenerateKey` {

        @Test
        fun `generates key via engine and sets it`() = runTest {
            every { engine.generateKey() } returns "generated-key-base64"

            viewModel.sendEvent(CryptoViewEvent.Config.GenerateKey)

            verify { engine.generateKey() }
            verify { engine.setKey("generated-key-base64") }
        }
    }

    @Nested
    inner class `GenerateIv` {

        @Test
        fun `generates IV via engine and sets it`() = runTest {
            every { engine.generateIv() } returns "generated-iv-base64"

            viewModel.sendEvent(CryptoViewEvent.Config.GenerateIv)

            verify { engine.generateIv() }
            verify { engine.setIv("generated-iv-base64") }
        }
    }

    @Nested
    inner class `Encrypt` {

        @Test
        fun `calls engine encrypt with input text`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText("Hello World"))

            viewModel.sendEvent(CryptoViewEvent.Operation.Encrypt)

            verify { engine.encrypt("Hello World") }
        }

        @Test
        fun `does not call engine when input is blank`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText(""))

            viewModel.sendEvent(CryptoViewEvent.Operation.Encrypt)

            verify(exactly = 0) { engine.encrypt(any()) }
        }

        @Test
        fun `does not call engine when input is whitespace only`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText("   "))

            viewModel.sendEvent(CryptoViewEvent.Operation.Encrypt)

            verify(exactly = 0) { engine.encrypt(any()) }
        }
    }

    @Nested
    inner class `Decrypt` {

        @Test
        fun `calls engine decrypt with input text`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText("encrypted_base64_data"))

            viewModel.sendEvent(CryptoViewEvent.Operation.Decrypt)

            verify { engine.decrypt("encrypted_base64_data") }
        }

        @Test
        fun `does not call engine when input is blank`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText(""))

            viewModel.sendEvent(CryptoViewEvent.Operation.Decrypt)

            verify(exactly = 0) { engine.decrypt(any()) }
        }

        @Test
        fun `does not call engine when input is whitespace only`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText("   "))

            viewModel.sendEvent(CryptoViewEvent.Operation.Decrypt)

            verify(exactly = 0) { engine.decrypt(any()) }
        }
    }

    @Nested
    inner class `ClearResult` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Result.Clear)

            verify { engine.clearResult() }
        }
    }

    @Nested
    inner class `ClearHistory` {

        @Test
        fun `delegates to engine`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.History.ConfirmClearAll)

            verify { engine.clearHistory() }
        }
    }

    @Nested
    inner class `RemoveFromHistory` {

        @Test
        fun `delegates to engine with correct id`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.History.Remove("result_42"))

            verify { engine.removeFromHistory("result_42") }
        }
    }

    @Nested
    inner class `LoadFromHistory` {

        @Test
        fun `restores input text from result`() = runTest {
            val result = sampleResult(input = "Original Input Text")

            viewModel.sendEvent(CryptoViewEvent.History.Load(result))

            viewModel.uiState.value.inputText shouldBe "Original Input Text"
        }

        @Test
        fun `overwrites existing input text`() = runTest {
            viewModel.sendEvent(CryptoViewEvent.Input.UpdateText("Existing text"))
            val result = sampleResult(input = "New Input Text")

            viewModel.sendEvent(CryptoViewEvent.History.Load(result))

            viewModel.uiState.value.inputText shouldBe "New Input Text"
        }

        @Test
        fun `handles empty input from history`() = runTest {
            val result = sampleResult(input = "")

            viewModel.sendEvent(CryptoViewEvent.History.Load(result))

            viewModel.uiState.value.inputText shouldBe ""
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

            viewModel.uiState.value.config shouldBe newConfig
        }

        @Test
        fun `currentResult updates when engine result changes`() = runTest {
            val result = sampleResult()
            currentResultFlow.value = result

            viewModel.uiState.value.currentResult shouldBe result
        }

        @Test
        fun `history updates when engine history changes`() = runTest {
            val results = listOf(sampleResult("r1"), sampleResult("r2"))
            historyFlow.value = results

            viewModel.uiState.value.history shouldBe results
        }

        @Test
        fun `isProcessing updates when engine processing changes`() = runTest {
            isProcessingFlow.value = true

            viewModel.uiState.value.isProcessing shouldBe true
        }

        @Test
        fun `error updates when engine error changes`() = runTest {
            errorFlow.value = "Something went wrong"

            viewModel.uiState.value.error shouldBe "Something went wrong"
        }
    }

    @Nested
    inner class `CryptoViewModelFactory` {

        @Test
        fun `creates CryptoViewModel instance`() = runTest {
            val factory = CryptoViewModelFactory(engine)

            val vm = factory.create(CryptoViewModel::class.java)

            vm.uiState.value.inputText shouldBe ""
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
