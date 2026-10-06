package com.azikar24.wormaceptor.feature.viewer.vm

import android.graphics.Bitmap
import android.util.Log
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PdfViewerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @TempDir
    lateinit var cacheDir: File

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `a page whose render throws is marked failed and not re-rendered`() {
        val document = FakeDocument(pageCount = 3, failingPages = setOf(1))
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(1))
        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(1))
        viewModel.sendEvent(PdfViewerViewEvent.RequestThumbnail(1))
        viewModel.sendEvent(PdfViewerViewEvent.RequestThumbnail(1))

        viewModel.uiState.value.failedPages shouldContain 1
        viewModel.uiState.value.failedThumbnails shouldContain 1
        viewModel.uiState.value.pages.containsKey(1) shouldBe false
        // Once per cache: page and thumbnail failures are tracked separately.
        document.renderCalls shouldBe listOf(1, 1)
    }

    @Test
    fun `a failed page does not block rendering other pages`() {
        val document = FakeDocument(pageCount = 3, failingPages = setOf(0))
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(0))
        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(2))

        viewModel.uiState.value.pages shouldContainKey 2
    }

    @Test
    fun `thumbnails render into their own cache at thumbnail width`() {
        val document = FakeDocument(pageCount = 3)
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestThumbnail(2))

        val state = viewModel.uiState.value
        state.thumbnails shouldContainKey 2
        state.pages.isEmpty() shouldBe true
        document.lastMaxWidthPx shouldBe 180
    }

    @Test
    fun `release closes the document, recycles bitmaps and lets the same data reopen`() {
        val document = FakeDocument(pageCount = 2)
        var opens = 0
        val viewModel = PdfViewerViewModel(
            {
                opens++
                document
            },
            testDispatcher,
        )
        val data = byteArrayOf(1)
        viewModel.sendEvent(PdfViewerViewEvent.LoadPdf(data, initialPage = 0, cacheDir = cacheDir))
        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(0))
        val bitmap = viewModel.uiState.value.pages.getValue(0)

        viewModel.sendEvent(PdfViewerViewEvent.Release)

        document.closed shouldBe true
        verify { bitmap.recycle() }
        viewModel.uiState.value shouldBe PdfViewerViewState()
        cacheDir.listFiles().orEmpty().size shouldBe 0

        viewModel.sendEvent(PdfViewerViewEvent.LoadPdf(data, initialPage = 0, cacheDir = cacheDir))
        opens shouldBe 2
        viewModel.uiState.value.pageCount shouldBe 2
    }

    @Test
    fun `a failed thumbnail does not mark the full page failed`() {
        val document = FakeDocument(pageCount = 2, failingThumbnails = setOf(1))
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestThumbnail(1))
        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(1))

        val state = viewModel.uiState.value
        state.failedThumbnails shouldContain 1
        state.failedPages.contains(1) shouldBe false
        state.pages shouldContainKey 1
    }

    @Test
    fun `cancelling a pending render drops it without marking the page failed`() {
        val gate = CompletableDeferred<Unit>()
        val document = FakeDocument(pageCount = 3, gate = gate)
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestThumbnail(0))
        viewModel.sendEvent(PdfViewerViewEvent.CancelThumbnail(0))
        gate.complete(Unit)

        val state = viewModel.uiState.value
        state.thumbnails.containsKey(0) shouldBe false
        state.failedThumbnails.isEmpty() shouldBe true
    }

    @Test
    fun `a cancelled page can be requested again`() {
        val gate = CompletableDeferred<Unit>()
        val document = FakeDocument(pageCount = 3, gate = gate)
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(2))
        viewModel.sendEvent(PdfViewerViewEvent.CancelPage(2))
        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(2))
        gate.complete(Unit)

        viewModel.uiState.value.pages shouldContainKey 2
        document.renderCalls shouldBe listOf(2, 2)
    }

    @Test
    fun `a duplicate request while rendering does not start a second render`() {
        val gate = CompletableDeferred<Unit>()
        val document = FakeDocument(pageCount = 3, gate = gate)
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(1))
        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(1))
        gate.complete(Unit)

        document.renderCalls shouldBe listOf(1)
    }

    @Test
    fun `full pages are capped in width`() {
        val document = FakeDocument(pageCount = 1)
        val viewModel = loadedViewModel(document)

        viewModel.sendEvent(PdfViewerViewEvent.RequestPage(0))

        document.lastMaxWidthPx shouldBe 2048
    }

    @Test
    fun `rendered size never collapses a side to zero`() {
        renderedSize(pageWidth = 4096, pageHeight = 8, scale = 2f, maxWidthPx = 256) shouldBe (256 to 1)
    }

    @Test
    fun `rendered size caps total pixels for very tall pages`() {
        val (width, height) = renderedSize(pageWidth = 600, pageHeight = 60_000, scale = 2f, maxWidthPx = 2048)

        width.toLong() * height shouldBeLessThanOrEqual MaxRenderedPixels
        width shouldBeLessThanOrEqual 1200
    }

    @Test
    fun `rendered size keeps the scale when under the width cap`() {
        renderedSize(pageWidth = 600, pageHeight = 800, scale = 2f, maxWidthPx = 2048) shouldBe (1200 to 1600)
    }

    private fun loadedViewModel(document: FakeDocument): PdfViewerViewModel {
        val viewModel = PdfViewerViewModel({ document }, testDispatcher)
        viewModel.sendEvent(PdfViewerViewEvent.LoadPdf(byteArrayOf(1), initialPage = 0, cacheDir = cacheDir))
        return viewModel
    }

    private companion object {
        const val THUMBNAIL_WIDTH = 180
    }

    private class FakeDocument(
        override val pageCount: Int,
        private val failingPages: Set<Int> = emptySet(),
        private val failingThumbnails: Set<Int> = emptySet(),
        private val gate: CompletableDeferred<Unit>? = null,
    ) : PdfDocument {
        val renderCalls = mutableListOf<Int>()
        var lastMaxWidthPx: Int? = null
        var closed = false

        override suspend fun render(
            index: Int,
            scale: Float,
            maxWidthPx: Int,
        ): Bitmap {
            renderCalls += index
            lastMaxWidthPx = maxWidthPx
            gate?.await()
            check(index !in failingPages) { "corrupt page" }
            require(!(maxWidthPx == THUMBNAIL_WIDTH && index in failingThumbnails)) { "zero-height thumbnail" }
            return mockk(relaxed = true)
        }

        override fun close() {
            closed = true
        }
    }
}
