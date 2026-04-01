package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import com.azikar24.wormaceptor.core.engine.HighlighterRegistry
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.buildHighlightedString
import com.azikar24.wormaceptor.core.ui.theme.syntaxColors
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.core.ui.util.isContentTooLargeForClipboard
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ImageMetadataExtractor
import com.azikar24.wormaceptor.domain.entities.ImageMetadata
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.FullscreenImageViewer
import com.azikar24.wormaceptor.feature.viewer.ui.components.ImagePreviewCard
import com.azikar24.wormaceptor.feature.viewer.ui.components.PdfPreviewCard
import com.azikar24.wormaceptor.feature.viewer.ui.components.PdfViewerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.FormDataView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.JsonTreeView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.MultipartView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.ProtobufView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.XmlTreeView
import com.azikar24.wormaceptor.feature.viewer.ui.components.isImageContentType
import com.azikar24.wormaceptor.feature.viewer.ui.components.isImageData
import com.azikar24.wormaceptor.feature.viewer.ui.components.isPdfContent
import com.azikar24.wormaceptor.feature.viewer.ui.components.saveImageToGallery
import com.azikar24.wormaceptor.feature.viewer.ui.components.shareImage
import com.azikar24.wormaceptor.feature.viewer.ui.util.getFileInfoForContentType
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareAsFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
internal fun ResponseTab(
    transaction: NetworkTransaction,
    queryEngine: QueryEngine?,
    searchQuery: String,
    currentMatchIndex: Int,
    onMatchCountChanged: (Int) -> Unit,
    isSearchActive: Boolean,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val blobId = transaction.response?.bodyRef
    var responseBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBodyBytes by remember(blobId) { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var copyRequested by remember { mutableStateOf(false) }
    var isCopying by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    var isPrettyMode by remember { mutableStateOf(true) }
    var headersExpanded by rememberSaveable { mutableStateOf(true) }
    var bodyExpanded by rememberSaveable { mutableStateOf(true) }
    var showImageViewer by remember { mutableStateOf(false) }
    var showPdfViewer by remember { mutableStateOf(false) }
    var imageMetadata by remember(blobId) { mutableStateOf<ImageMetadata?>(null) }
    var parsedContentType by remember(blobId) { mutableStateOf(ContentType.UNKNOWN) }

    // Syntax highlighting colors
    val colors = syntaxColors()

    // Extract content type from headers
    val contentType = remember(transaction.response?.headers) {
        transaction.response?.headers?.entries
            ?.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
            ?.value?.firstOrNull()
    }

    val shareResponseBodyTitle = stringResource(R.string.viewer_share_response_body)

    // Handle copy request - copies if small, shares as file if large
    LaunchedEffect(copyRequested) {
        if (copyRequested) {
            isCopying = true
            try {
                val bodyContent = if (isPrettyMode) responseBody ?: rawBody else rawBody ?: responseBody
                if (bodyContent != null) {
                    if (isContentTooLargeForClipboard(bodyContent)) {
                        val (ext, mime) = getFileInfoForContentType(contentType)
                        shareAsFile(
                            context = context,
                            content = bodyContent,
                            fileName = "response_body.$ext",
                            mimeType = mime,
                            title = shareResponseBodyTitle,
                        )
                    } else {
                        copyToClipboard(context, "Response Body", bodyContent)
                    }
                }
            } finally {
                isCopying = false
                copyRequested = false
            }
        }
    }

    // Determine if content is an image
    val isImageContent = remember(contentType, rawBodyBytes) {
        isImageContentType(contentType) || rawBodyBytes?.let { isImageData(it) } == true
    }

    // Determine if content is a PDF
    val isPdfContentDetected = remember(contentType, rawBodyBytes) {
        isPdfContent(contentType, rawBodyBytes)
    }

    // Determine if content is protobuf
    val isProtobufContentDetected = remember(contentType) {
        isProtobufContentType(contentType)
    }

    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // 1. Body Loading - get raw bytes first to detect binary content
    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            // Get raw bytes first to detect binary content like images or PDFs
            val bytes = queryEngine?.getBodyBytes(blobId)
            rawBodyBytes = bytes

            // Check for image content and extract metadata
            if (bytes != null && (isImageContentType(contentType) || isImageData(bytes))) {
                imageMetadata = try {
                    val extractor: ImageMetadataExtractor =
                        org.koin.java.KoinJavaComponent.get(ImageMetadataExtractor::class.java)
                    val meta = extractor.extractMetadata(bytes)
                    if (meta.width > 0 && meta.height > 0) meta else null
                } catch (_: Exception) {
                    null
                }
            } else if (bytes != null && isPdfContent(contentType, bytes)) {
                // PDF content - raw bytes are stored, no text decoding needed
                // Just keep rawBodyBytes for the PDF viewer
            } else if (bytes != null && isProtobufContentType(contentType)) {
                // Protobuf is binary - keep raw bytes, don't UTF-8 decode
            } else if (bytes != null) {
                // Decode as text if not image or PDF
                val raw = String(bytes, Charsets.UTF_8)
                rawBody = raw
                val result = withContext(Dispatchers.Default) {
                    parseBodyViaRegistry(contentType, bytes, raw)
                }
                responseBody = result.first
                parsedContentType = result.second
            }
            isLoading = false
        } else {
            responseBody = null
            rawBody = null
            rawBodyBytes = null
            imageMetadata = null
        }
    }

    // 2. Search: Find matches
    LaunchedEffect(responseBody, rawBody, searchQuery, isPrettyMode) {
        val body = if (isPrettyMode) responseBody else rawBody
        if (body == null || searchQuery.isEmpty()) {
            matches = emptyList()
            onMatchCountChanged(0)
            return@LaunchedEffect
        }

        delay(250) // Debounce

        withContext(Dispatchers.Default) {
            val foundMatches = mutableListOf<MatchInfo>()
            var index = body.indexOf(searchQuery, ignoreCase = true)
            while (index >= 0) {
                foundMatches.add(MatchInfo(globalPosition = index, lineIndex = 0))
                index = body.indexOf(searchQuery, index + 1, ignoreCase = true)
            }
            matches = foundMatches
            onMatchCountChanged(foundMatches.size)
        }
    }

    // 3. Scroll to current match using TextLayoutResult
    LaunchedEffect(currentMatchIndex, matches, textLayoutResult) {
        if (matches.isEmpty()) return@LaunchedEffect
        val layout = textLayoutResult ?: return@LaunchedEffect

        val match = matches.getOrNull(currentMatchIndex) ?: return@LaunchedEffect

        try {
            val lineNumber = layout.getLineForOffset(match.globalPosition)
            val pixelOffset = layout.getLineTop(lineNumber).toInt()
            scrollState.animateScrollTo(pixelOffset)
        } catch (_: Exception) {
            // Offset out of bounds, ignore
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = WormaCeptorDesignSystem.Spacing.lg,
                    top = WormaCeptorDesignSystem.Spacing.lg,
                    end = WormaCeptorDesignSystem.Spacing.lg,
                    bottom = WormaCeptorDesignSystem.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
        ) {
            if (transaction.response != null) {
                val hasHeaders = transaction.response?.headers?.isNotEmpty() == true
                val hasBody = blobId != null

                // Only show Headers section if headers exist
                if (hasHeaders) {
                    CollapsibleSection(
                        title = stringResource(R.string.viewer_body_headers),
                        isExpanded = headersExpanded,
                        onToggle = { headersExpanded = !headersExpanded },
                        onCopy = {
                            transaction.response?.headers?.let {
                                copyToClipboard(
                                    context,
                                    "Response Headers",
                                    formatHeaders(it),
                                )
                            }
                        },
                    ) {
                        transaction.response?.headers?.let { HeadersView(it) }
                    }

                    if (hasBody) {
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
                    }
                }

                // Only show Body section if body exists
                if (hasBody) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg))
                            Text(
                                stringResource(R.string.viewer_body_processing),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else if (isImageContent) {
                        val imageBytes = rawBodyBytes ?: return@Column
                        // Image content - show Image preview card
                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body_image),
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = null,
                        ) {
                            ImagePreviewCard(
                                imageData = imageBytes,
                                onFullscreen = { showImageViewer = true },
                                onDownload = {
                                    val format = imageMetadata?.format ?: "Unknown"
                                    val message = saveImageToGallery(context, imageBytes, format)
                                    onShowMessage(message)
                                },
                                onShare = {
                                    val format = imageMetadata?.format ?: "Unknown"
                                    shareImage(context, imageBytes, format)?.let { onShowMessage(it) }
                                },
                            )
                        }
                    } else if (isPdfContentDetected) {
                        val pdfBytes = rawBodyBytes ?: return@Column
                        // PDF content - show PDF preview card
                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body_pdf),
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = null,
                        ) {
                            PdfPreviewCard(
                                pdfData = pdfBytes,
                                contentType = contentType,
                                onFullscreen = { showPdfViewer = true },
                                onDownload = {
                                    val message = savePdfToDownloads(context, pdfBytes)
                                    onShowMessage(message)
                                },
                                onShowMessage = onShowMessage,
                            )
                        }
                    } else if (isProtobufContentDetected) {
                        val protobufBytes = rawBodyBytes ?: return@Column
                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body),
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = null,
                        ) {
                            ProtobufView(data = protobufBytes)
                        }
                    } else if (responseBody != null || rawBody != null) {
                        val detectedContentType = parsedContentType

                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body),
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = { copyRequested = true },
                            isCopyLoading = isCopying,
                            trailingContent = {
                                BodyControlsRow(
                                    contentType = detectedContentType,
                                    isPrettyMode = isPrettyMode,
                                    onPrettyModeToggle = { isPrettyMode = !isPrettyMode },
                                )
                            },
                        ) {
                            val displayBody = if (isPrettyMode) {
                                responseBody ?: requireNotNull(rawBody) { "Body must be available" }
                            } else {
                                rawBody ?: requireNotNull(responseBody) { "Body must be available" }
                            }
                            val currentMatchGlobalPos = matches.getOrNull(currentMatchIndex)?.globalPosition
                            val hasActiveSearch = searchQuery.isNotEmpty()

                            // Syntax highlighting for raw/search mode
                            var syntaxHighlighted by remember {
                                mutableStateOf<androidx.compose.ui.text.AnnotatedString?>(null)
                            }
                            LaunchedEffect(displayBody, detectedContentType) {
                                syntaxHighlighted = null
                                if (displayBody.length > MAX_SYNTAX_HIGHLIGHT_SIZE) return@LaunchedEffect
                                val language = when (detectedContentType) {
                                    ContentType.JSON -> "json"
                                    ContentType.XML, ContentType.HTML -> "xml"
                                    else -> return@LaunchedEffect
                                }
                                val highlighter = try {
                                    val registry: HighlighterRegistry =
                                        org.koin.java.KoinJavaComponent.get(HighlighterRegistry::class.java)
                                    registry.getHighlighter(language)
                                } catch (_: RuntimeException) {
                                    null
                                } ?: return@LaunchedEffect
                                withContext(Dispatchers.Default) {
                                    val tokens = highlighter.tokenize(displayBody)
                                    syntaxHighlighted = colors.buildHighlightedString(displayBody, tokens)
                                }
                            }

                            // Format-specific rendering in pretty mode (use flat text when searching)
                            if (isPrettyMode && !hasActiveSearch) {
                                when (detectedContentType) {
                                    ContentType.JSON -> {
                                        JsonTreeView(
                                            jsonString = displayBody,
                                            initiallyExpanded = true,
                                            colors = colors,
                                        )
                                    }

                                    ContentType.XML, ContentType.HTML -> {
                                        XmlTreeView(
                                            xmlString = displayBody,
                                            colors = colors,
                                        )
                                    }

                                    ContentType.FORM_DATA -> {
                                        FormDataView(
                                            formData = displayBody,
                                        )
                                    }

                                    ContentType.MULTIPART -> {
                                        val boundary = contentType?.let {
                                            extractMultipartBoundaryViaRegistry(
                                                it,
                                            )
                                        }
                                        MultipartView(
                                            multipartData = displayBody,
                                            boundary = boundary,
                                        )
                                    }

                                    else -> {
                                        SelectionContainer {
                                            HighlightedBodyText(
                                                text = displayBody,
                                                query = searchQuery,
                                                currentMatchGlobalPos = currentMatchGlobalPos,
                                                modifier = Modifier.fillMaxWidth(),
                                                syntaxHighlighted = syntaxHighlighted,
                                                onTextLayout = { textLayoutResult = it },
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Raw mode or searching - show flat text with highlighting
                                SelectionContainer {
                                    HighlightedBodyText(
                                        text = displayBody,
                                        query = searchQuery,
                                        currentMatchGlobalPos = currentMatchGlobalPos,
                                        modifier = Modifier.fillMaxWidth(),
                                        syntaxHighlighted = syntaxHighlighted,
                                        onTextLayout = { textLayoutResult = it },
                                    )
                                }
                            }
                        }
                    }
                }

                // Show empty state if no headers and no body
                if (!hasHeaders && !hasBody) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No response data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No response received",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // Floating Action Button for Copy All - hidden when search is active
        AnimatedVisibility(
            visible = isScrolling && responseBody != null && !isSearchActive,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            WormaCeptorFAB(
                onClick = {
                    val fullContent = buildString {
                        transaction.response?.headers?.let {
                            appendLine("=== RESPONSE HEADERS ===")
                            appendLine(formatHeaders(it))
                        }
                        if (responseBody != null || rawBody != null) {
                            appendLine("\n=== RESPONSE BODY ===")
                            val body = if (isPrettyMode) responseBody ?: rawBody else rawBody ?: responseBody
                            body?.let { appendLine(it) }
                        }
                    }
                    copyToClipboard(context, "Response Content", fullContent)
                },
                icon = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.viewer_body_copy_all),
            )
        }

        // Fullscreen Image Viewer dialog
        rawBodyBytes?.let { bytes ->
            if (showImageViewer) {
                FullscreenImageViewer(
                    imageData = bytes,
                    metadata = imageMetadata,
                    onDismiss = { showImageViewer = false },
                    onDownload = {
                        val format = imageMetadata?.format ?: "Unknown"
                        val message = saveImageToGallery(context, bytes, format)
                        onShowMessage(message)
                    },
                    onShare = {
                        val format = imageMetadata?.format ?: "Unknown"
                        shareImage(context, bytes, format)?.let { onShowMessage(it) }
                    },
                )
            }

            // Fullscreen PDF Viewer dialog
            if (showPdfViewer) {
                PdfViewerScreen(
                    pdfData = bytes,
                    onDismiss = { showPdfViewer = false },
                    onDownload = {
                        val message = savePdfToDownloads(context, bytes)
                        onShowMessage(message)
                    },
                )
            }
        }
    }
}
