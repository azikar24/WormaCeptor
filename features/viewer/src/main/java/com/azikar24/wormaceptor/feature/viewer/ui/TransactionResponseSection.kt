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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import com.azikar24.wormaceptor.core.engine.HighlighterRegistry
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.buildHighlightedString
import com.azikar24.wormaceptor.core.ui.theme.syntaxColors
import com.azikar24.wormaceptor.domain.contracts.ContentType
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
import com.azikar24.wormaceptor.feature.viewer.vm.BodySectionState
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
internal fun ResponseTab(
    transaction: NetworkTransaction,
    responseState: BodySectionState,
    searchQuery: String,
    currentMatchIndex: Int,
    isSearchActive: Boolean,
    onEvent: (TransactionDetailViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val blobId = transaction.response?.bodyRef

    // Derive local vals from responseState
    val isLoading = responseState.isLoading
    val responseBody = responseState.parsedBody
    val rawBody = responseState.rawBody
    val rawBodyBytes = responseState.rawBodyBytes
    val isPrettyMode = responseState.isPrettyMode
    val headersExpanded = responseState.headersExpanded
    val bodyExpanded = responseState.bodyExpanded
    val matches = responseState.matches
    val parsedContentType = responseState.parsedContentType
    val imageMetadata = responseState.imageMetadata
    val showImageViewer = responseState.showImageViewer
    val showPdfViewer = responseState.showPdfViewer

    // Syntax highlighting colors
    val colors = syntaxColors()

    // Extract content type from headers
    val contentType = remember(transaction.response?.headers) {
        transaction.response?.headers?.entries
            ?.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
            ?.value?.firstOrNull()
    }

    // Content type detection (pure computation from state)
    val isImageContent = remember(contentType, rawBodyBytes) {
        isImageContentType(contentType) || rawBodyBytes?.let { isImageData(it) } == true
    }
    val isPdfContentDetected = remember(contentType, rawBodyBytes) {
        isPdfContent(contentType, rawBodyBytes)
    }
    val isProtobufContentDetected = remember(contentType) {
        isProtobufContentType(contentType)
    }

    // Pixel-based scrolling (rendering concerns stay local)
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // Scroll to current match using TextLayoutResult
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
                        onToggle = { onEvent(TransactionDetailViewEvent.Response.ToggleHeadersExpanded) },
                        onCopy = { onEvent(TransactionDetailViewEvent.Response.CopyHeaders) },
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
                            onToggle = { onEvent(TransactionDetailViewEvent.Response.ToggleBodyExpanded) },
                            onCopy = null,
                        ) {
                            ImagePreviewCard(
                                imageData = imageBytes,
                                onFullscreen = { onEvent(TransactionDetailViewEvent.Response.ShowImageViewer(true)) },
                                onDownload = { onEvent(TransactionDetailViewEvent.Response.DownloadImage) },
                                onShare = { onEvent(TransactionDetailViewEvent.Response.ShareImage) },
                            )
                        }
                    } else if (isPdfContentDetected) {
                        val pdfBytes = rawBodyBytes ?: return@Column
                        // PDF content - show PDF preview card
                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body_pdf),
                            isExpanded = bodyExpanded,
                            onToggle = { onEvent(TransactionDetailViewEvent.Response.ToggleBodyExpanded) },
                            onCopy = null,
                        ) {
                            PdfPreviewCard(
                                pdfData = pdfBytes,
                                contentType = contentType,
                                onFullscreen = { onEvent(TransactionDetailViewEvent.Response.ShowPdfViewer(true)) },
                                onDownload = { onEvent(TransactionDetailViewEvent.Response.DownloadPdf) },
                                onShowMessage = { onEvent(TransactionDetailViewEvent.ShowMessage(it)) },
                            )
                        }
                    } else if (isProtobufContentDetected) {
                        val protobufBytes = rawBodyBytes ?: return@Column
                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body),
                            isExpanded = bodyExpanded,
                            onToggle = { onEvent(TransactionDetailViewEvent.Response.ToggleBodyExpanded) },
                            onCopy = null,
                        ) {
                            ProtobufView(data = protobufBytes)
                        }
                    } else if (responseBody != null || rawBody != null) {
                        val detectedContentType = parsedContentType

                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body),
                            isExpanded = bodyExpanded,
                            onToggle = { onEvent(TransactionDetailViewEvent.Response.ToggleBodyExpanded) },
                            onCopy = { onEvent(TransactionDetailViewEvent.Response.CopyBody) },
                            trailingContent = {
                                BodyControlsRow(
                                    contentType = detectedContentType,
                                    isPrettyMode = isPrettyMode,
                                    onPrettyModeToggle = {
                                        onEvent(TransactionDetailViewEvent.Response.TogglePrettyMode)
                                    },
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
                onClick = { onEvent(TransactionDetailViewEvent.Response.CopyAllContent) },
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
                    onDismiss = { onEvent(TransactionDetailViewEvent.Response.ShowImageViewer(false)) },
                    onDownload = { onEvent(TransactionDetailViewEvent.Response.DownloadImage) },
                    onShare = { onEvent(TransactionDetailViewEvent.Response.ShareImage) },
                )
            }

            // Fullscreen PDF Viewer dialog
            if (showPdfViewer) {
                PdfViewerScreen(
                    pdfData = bytes,
                    onDismiss = { onEvent(TransactionDetailViewEvent.Response.ShowPdfViewer(false)) },
                    onDownload = { onEvent(TransactionDetailViewEvent.Response.DownloadPdf) },
                )
            }
        }
    }
}
