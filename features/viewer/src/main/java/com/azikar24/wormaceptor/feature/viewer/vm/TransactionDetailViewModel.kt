package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.core.ui.util.isContentTooLargeForClipboard
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ImageMetadataExtractor
import com.azikar24.wormaceptor.domain.entities.ExportFormat
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.ui.MatchInfo
import com.azikar24.wormaceptor.feature.viewer.ui.components.isImageContentType
import com.azikar24.wormaceptor.feature.viewer.ui.components.isImageData
import com.azikar24.wormaceptor.feature.viewer.ui.components.isPdfContent
import com.azikar24.wormaceptor.feature.viewer.ui.generateTextSummary
import com.azikar24.wormaceptor.feature.viewer.ui.isProtobufContentType
import com.azikar24.wormaceptor.feature.viewer.ui.parseBodyViaRegistry
import com.azikar24.wormaceptor.feature.viewer.ui.util.CurlGenerator
import com.azikar24.wormaceptor.feature.viewer.ui.util.getFileInfoForContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MVI ViewModel for the transaction detail screen.
 *
 * Manages body loading, search with debounce, copy/share logic,
 * and image/PDF actions, exposing all state via [TransactionDetailViewState]
 * and one-time side-effects via [TransactionDetailViewEffect].
 */
internal class TransactionDetailViewModel(
    private val queryEngine: QueryEngine,
) : BaseViewModel<TransactionDetailViewState, TransactionDetailViewEffect, TransactionDetailViewEvent>(
    TransactionDetailViewState(),
) {

    private var searchDebounceJob: Job? = null
    private var activeTabIndex: Int = 0

    override fun handleEvent(event: TransactionDetailViewEvent) {
        when (event) {
            is TransactionDetailViewEvent.TransactionLoaded -> handleTransactionLoaded(event.transaction)

            is TransactionDetailViewEvent.SearchVisibilityChanged -> handleSearchVisibilityChanged(event.visible)
            is TransactionDetailViewEvent.SearchQueryChanged -> handleSearchQueryChanged(event.query)
            is TransactionDetailViewEvent.NavigateToNextMatch -> handleNavigateToNextMatch()
            is TransactionDetailViewEvent.NavigateToPreviousMatch -> handleNavigateToPreviousMatch()
            is TransactionDetailViewEvent.ActiveTabChanged -> handleActiveTabChanged(event.tabIndex)

            is TransactionDetailViewEvent.MenuVisibilityChanged ->
                updateState { copy(showMenu = event.visible) }

            is TransactionDetailViewEvent.CopyAsText -> handleCopyAsText()
            is TransactionDetailViewEvent.CopyAsCurl -> handleCopyAsCurl()
            is TransactionDetailViewEvent.ShareAsJson -> handleExport(ExportFormat.JSON)
            is TransactionDetailViewEvent.ShareAsHar -> handleExport(ExportFormat.HAR)

            is TransactionDetailViewEvent.ToggleRequestPrettyMode -> handleToggleRequestPrettyMode()
            is TransactionDetailViewEvent.ToggleRequestHeadersExpanded ->
                updateState { copy(requestState = requestState.copy(headersExpanded = !requestState.headersExpanded)) }

            is TransactionDetailViewEvent.ToggleRequestBodyExpanded ->
                updateState { copy(requestState = requestState.copy(bodyExpanded = !requestState.bodyExpanded)) }

            is TransactionDetailViewEvent.CopyRequestBody -> handleCopyBody(isRequest = true)
            is TransactionDetailViewEvent.CopyRequestHeaders -> handleCopyHeaders(isRequest = true)

            is TransactionDetailViewEvent.ToggleResponsePrettyMode -> handleToggleResponsePrettyMode()
            is TransactionDetailViewEvent.ToggleResponseHeadersExpanded ->
                updateState { copy(responseState = responseState.copy(headersExpanded = !responseState.headersExpanded)) }

            is TransactionDetailViewEvent.ToggleResponseBodyExpanded ->
                updateState { copy(responseState = responseState.copy(bodyExpanded = !responseState.bodyExpanded)) }

            is TransactionDetailViewEvent.CopyResponseBody -> handleCopyBody(isRequest = false)
            is TransactionDetailViewEvent.CopyResponseHeaders -> handleCopyHeaders(isRequest = false)
            is TransactionDetailViewEvent.ShowImageViewer ->
                updateState { copy(responseState = responseState.copy(showImageViewer = event.show)) }

            is TransactionDetailViewEvent.ShowPdfViewer ->
                updateState { copy(responseState = responseState.copy(showPdfViewer = event.show)) }

            is TransactionDetailViewEvent.DownloadImage -> handleDownloadImage()
            is TransactionDetailViewEvent.ShareImage -> handleShareImage()
            is TransactionDetailViewEvent.DownloadPdf -> handleDownloadPdf()

            is TransactionDetailViewEvent.ShowMessage ->
                emitEffect(TransactionDetailViewEffect.ShowSnackbar(event.message))
        }
    }

    // -- Lifecycle ----------------------------------------------------------------

    private fun handleTransactionLoaded(transaction: NetworkTransaction) {
        updateState {
            TransactionDetailViewState(transaction = transaction)
        }
        activeTabIndex = 0
        loadRequestBody(transaction)
        loadResponseBody(transaction)
    }

    // -- Body Loading -------------------------------------------------------------

    private fun loadRequestBody(transaction: NetworkTransaction) {
        val blobId = transaction.request.bodyRef ?: return
        updateState { copy(requestState = requestState.copy(isLoading = true)) }

        val requestContentType = transaction.request.headers.entries
            .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
            ?.value?.firstOrNull()

        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                queryEngine.getBodyBytes(blobId)
            }

            if (bytes != null && isProtobufContentType(requestContentType)) {
                updateState {
                    copy(
                        requestState = requestState.copy(
                            rawBodyBytes = bytes,
                            parsedContentType = ContentType.PROTOBUF,
                            isLoading = false,
                        ),
                    )
                }
            } else if (bytes != null) {
                val raw = String(bytes, Charsets.UTF_8)
                val result = withContext(Dispatchers.Default) {
                    parseBodyViaRegistry(requestContentType, bytes, raw)
                }
                updateState {
                    copy(
                        requestState = requestState.copy(
                            parsedBody = result.first,
                            rawBody = raw,
                            rawBodyBytes = bytes,
                            parsedContentType = result.second,
                            isLoading = false,
                        ),
                    )
                }
            } else {
                updateState {
                    copy(requestState = requestState.copy(isLoading = false))
                }
            }
        }
    }

    private fun loadResponseBody(transaction: NetworkTransaction) {
        val blobId = transaction.response?.bodyRef ?: return
        updateState { copy(responseState = responseState.copy(isLoading = true)) }

        val contentType = transaction.response?.headers?.entries
            ?.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
            ?.value?.firstOrNull()

        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                queryEngine.getBodyBytes(blobId)
            }

            when {
                bytes != null && (isImageContentType(contentType) || isImageData(bytes)) -> {
                    val metadata = withContext(Dispatchers.Default) {
                        try {
                            val extractor: ImageMetadataExtractor =
                                org.koin.java.KoinJavaComponent.get(ImageMetadataExtractor::class.java)
                            val meta = extractor.extractMetadata(bytes)
                            if (meta.width > 0 && meta.height > 0) meta else null
                        } catch (_: Exception) {
                            null
                        }
                    }
                    updateState {
                        copy(
                            responseState = responseState.copy(
                                rawBodyBytes = bytes,
                                imageMetadata = metadata,
                                isLoading = false,
                            ),
                        )
                    }
                }

                bytes != null && isPdfContent(contentType, bytes) -> {
                    updateState {
                        copy(
                            responseState = responseState.copy(
                                rawBodyBytes = bytes,
                                isLoading = false,
                            ),
                        )
                    }
                }

                bytes != null && isProtobufContentType(contentType) -> {
                    updateState {
                        copy(
                            responseState = responseState.copy(
                                rawBodyBytes = bytes,
                                parsedContentType = ContentType.PROTOBUF,
                                isLoading = false,
                            ),
                        )
                    }
                }

                bytes != null -> {
                    val raw = String(bytes, Charsets.UTF_8)
                    val result = withContext(Dispatchers.Default) {
                        parseBodyViaRegistry(contentType, bytes, raw)
                    }
                    updateState {
                        copy(
                            responseState = responseState.copy(
                                parsedBody = result.first,
                                rawBody = raw,
                                rawBodyBytes = bytes,
                                parsedContentType = result.second,
                                isLoading = false,
                            ),
                        )
                    }
                }

                else -> {
                    updateState {
                        copy(responseState = responseState.copy(isLoading = false))
                    }
                }
            }
        }
    }

    // -- Search -------------------------------------------------------------------

    private fun handleSearchVisibilityChanged(visible: Boolean) {
        if (visible) {
            updateState { copy(showSearch = true) }
        } else {
            searchDebounceJob?.cancel()
            updateState {
                copy(
                    showSearch = false,
                    searchQuery = "",
                    debouncedSearchQuery = "",
                    currentMatchIndex = 0,
                    requestState = requestState.copy(matches = emptyList()),
                    responseState = responseState.copy(matches = emptyList()),
                )
            }
        }
    }

    private fun handleSearchQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
        searchDebounceJob?.cancel()

        if (query.isEmpty()) {
            updateState {
                copy(
                    debouncedSearchQuery = "",
                    currentMatchIndex = 0,
                    requestState = requestState.copy(matches = emptyList()),
                    responseState = responseState.copy(matches = emptyList()),
                )
            }
            return
        }

        searchDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            updateState { copy(debouncedSearchQuery = query) }
            findMatches(query)
        }
    }

    private fun findMatches(query: String) {
        if (query.isEmpty()) {
            updateState {
                copy(
                    currentMatchIndex = 0,
                    requestState = requestState.copy(matches = emptyList()),
                    responseState = responseState.copy(matches = emptyList()),
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val state = uiState.value

            val requestMatches = findMatchesInBody(
                query = query,
                parsedBody = state.requestState.parsedBody,
                rawBody = state.requestState.rawBody,
                isPrettyMode = state.requestState.isPrettyMode,
            )

            val responseMatches = findMatchesInBody(
                query = query,
                parsedBody = state.responseState.parsedBody,
                rawBody = state.responseState.rawBody,
                isPrettyMode = state.responseState.isPrettyMode,
            )

            updateState {
                copy(
                    currentMatchIndex = 0,
                    requestState = requestState.copy(matches = requestMatches),
                    responseState = responseState.copy(matches = responseMatches),
                )
            }
        }
    }

    private fun findMatchesInBody(
        query: String,
        parsedBody: String?,
        rawBody: String?,
        isPrettyMode: Boolean,
    ): List<MatchInfo> {
        val body = if (isPrettyMode) parsedBody ?: rawBody else rawBody ?: parsedBody
        if (body.isNullOrEmpty()) return emptyList()

        val result = mutableListOf<MatchInfo>()
        var index = body.indexOf(query, ignoreCase = true)
        while (index >= 0) {
            result.add(MatchInfo(globalPosition = index, lineIndex = 0))
            index = body.indexOf(query, index + 1, ignoreCase = true)
        }
        return result
    }

    private fun handleNavigateToNextMatch() {
        val state = uiState.value
        val matchCount = getActiveTabMatchCount(state)
        if (matchCount <= 0) return
        updateState { copy(currentMatchIndex = (currentMatchIndex + 1) % matchCount) }
    }

    private fun handleNavigateToPreviousMatch() {
        val state = uiState.value
        val matchCount = getActiveTabMatchCount(state)
        if (matchCount <= 0) return
        updateState { copy(currentMatchIndex = (currentMatchIndex - 1 + matchCount) % matchCount) }
    }

    private fun getActiveTabMatchCount(state: TransactionDetailViewState): Int {
        return when (activeTabIndex) {
            TAB_REQUEST -> state.requestMatchCount
            TAB_RESPONSE -> state.responseMatchCount
            else -> 0
        }
    }

    private fun handleActiveTabChanged(tabIndex: Int) {
        activeTabIndex = tabIndex
        // Close search when switching tabs (matches existing behavior)
        if (uiState.value.showSearch) {
            handleSearchVisibilityChanged(false)
        }
    }

    // -- Pretty Mode Toggle -------------------------------------------------------

    private fun handleToggleRequestPrettyMode() {
        updateState {
            copy(requestState = requestState.copy(isPrettyMode = !requestState.isPrettyMode))
        }
        val debouncedQuery = uiState.value.debouncedSearchQuery
        if (debouncedQuery.isNotEmpty()) {
            findMatches(debouncedQuery)
        }
    }

    private fun handleToggleResponsePrettyMode() {
        updateState {
            copy(responseState = responseState.copy(isPrettyMode = !responseState.isPrettyMode))
        }
        val debouncedQuery = uiState.value.debouncedSearchQuery
        if (debouncedQuery.isNotEmpty()) {
            findMatches(debouncedQuery)
        }
    }

    // -- Copy / Share -------------------------------------------------------------

    private fun handleCopyBody(isRequest: Boolean) {
        val state = uiState.value
        val section = if (isRequest) state.requestState else state.responseState
        val bodyContent = if (section.isPrettyMode) {
            section.parsedBody ?: section.rawBody
        } else {
            section.rawBody ?: section.parsedBody
        }
        if (bodyContent == null) return

        val transaction = state.transaction ?: return
        val contentTypeHeader = if (isRequest) {
            transaction.request.headers.entries
                .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
                ?.value?.firstOrNull()
        } else {
            transaction.response?.headers?.entries
                ?.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
                ?.value?.firstOrNull()
        }

        if (isContentTooLargeForClipboard(bodyContent)) {
            val (ext, mime) = getFileInfoForContentType(contentTypeHeader)
            val prefix = if (isRequest) "request" else "response"
            emitEffect(
                TransactionDetailViewEffect.ShareAsFile(
                    content = bodyContent,
                    fileName = "${prefix}_body.$ext",
                    mimeType = mime,
                    title = if (isRequest) "Share Request Body" else "Share Response Body",
                ),
            )
        } else {
            val label = if (isRequest) "Request Body" else "Response Body"
            emitEffect(TransactionDetailViewEffect.CopyToClipboard(label = label, content = bodyContent))
        }
    }

    private fun handleCopyHeaders(isRequest: Boolean) {
        val transaction = uiState.value.transaction ?: return
        val headers = if (isRequest) {
            transaction.request.headers
        } else {
            transaction.response?.headers ?: return
        }
        val formatted = ViewerViewModel.formatHeaders(headers)
        val label = if (isRequest) "Request Headers" else "Response Headers"
        emitEffect(TransactionDetailViewEffect.CopyToClipboard(label = label, content = formatted))
    }

    private fun handleCopyAsText() {
        updateState { copy(showMenu = false) }
        val transaction = uiState.value.transaction ?: return

        viewModelScope.launch {
            val (requestBody, responseBody) = withContext(Dispatchers.IO) {
                val reqBody = transaction.request.bodyRef?.let { queryEngine.getBody(it) }
                val resBody = transaction.response?.bodyRef?.let { queryEngine.getBody(it) }
                reqBody to resBody
            }
            val summary = generateTextSummary(transaction, requestBody, responseBody)
            emitEffect(TransactionDetailViewEffect.CopyToClipboard(label = "Transaction", content = summary))
        }
    }

    private fun handleCopyAsCurl() {
        updateState { copy(showMenu = false) }
        val transaction = uiState.value.transaction ?: return

        viewModelScope.launch {
            val body = withContext(Dispatchers.IO) {
                transaction.request.bodyRef?.let { queryEngine.getBody(it) }
            }
            val curl = CurlGenerator.generate(
                method = transaction.request.method,
                url = transaction.request.url,
                headers = transaction.request.headers,
                body = body,
            )
            emitEffect(TransactionDetailViewEffect.CopyToClipboard(label = "cURL", content = curl))
        }
    }

    private fun handleExport(format: ExportFormat) {
        updateState { copy(showMenu = false) }
        val transaction = uiState.value.transaction ?: return
        emitEffect(TransactionDetailViewEffect.ExportTransactions(listOf(transaction), format))
    }

    // -- Image / PDF Actions ------------------------------------------------------

    private fun handleDownloadImage() {
        val state = uiState.value
        val bytes = state.responseState.rawBodyBytes ?: return
        val format = state.responseState.imageMetadata?.format ?: "Unknown"
        emitEffect(TransactionDetailViewEffect.SaveImageToGallery(bytes = bytes, format = format))
    }

    private fun handleShareImage() {
        val state = uiState.value
        val bytes = state.responseState.rawBodyBytes ?: return
        val format = state.responseState.imageMetadata?.format ?: "Unknown"
        emitEffect(TransactionDetailViewEffect.ShareImageBytes(bytes = bytes, format = format))
    }

    private fun handleDownloadPdf() {
        val state = uiState.value
        val bytes = state.responseState.rawBodyBytes ?: return
        emitEffect(TransactionDetailViewEffect.SavePdfToDownloads(bytes = bytes))
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 150L
        private const val TAB_REQUEST = 1
        private const val TAB_RESPONSE = 2
    }
}
