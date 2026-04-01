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
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.FormDataView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.JsonTreeView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.MultipartView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.ProtobufView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.XmlTreeView
import com.azikar24.wormaceptor.feature.viewer.vm.BodySectionState
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
internal fun RequestTab(
    transaction: NetworkTransaction,
    requestState: BodySectionState,
    searchQuery: String,
    currentMatchIndex: Int,
    isSearchActive: Boolean,
    onEvent: (TransactionDetailViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val blobId = transaction.request.bodyRef

    // Derive local vals from requestState
    val isLoading = requestState.isLoading
    val requestBody = requestState.parsedBody
    val rawBody = requestState.rawBody
    val rawBodyBytes = requestState.rawBodyBytes
    val isPrettyMode = requestState.isPrettyMode
    val headersExpanded = requestState.headersExpanded
    val bodyExpanded = requestState.bodyExpanded
    val matches = requestState.matches
    val parsedContentType = requestState.parsedContentType

    // Pixel-based scrolling (rendering concerns stay local)
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // Get content type from headers for multipart boundary extraction
    val requestContentType = transaction.request.headers.entries
        .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
        ?.value?.firstOrNull()

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
            // Only show Headers section if headers exist
            if (transaction.request.headers.isNotEmpty()) {
                CollapsibleSection(
                    title = stringResource(R.string.viewer_body_headers),
                    isExpanded = headersExpanded,
                    onToggle = { onEvent(TransactionDetailViewEvent.Request.ToggleHeadersExpanded) },
                    onCopy = { onEvent(TransactionDetailViewEvent.Request.CopyHeaders) },
                ) {
                    HeadersView(transaction.request.headers)
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
            }

            // Only show Body section if body exists
            if (blobId != null) {
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
                } else if (isProtobufContentType(requestContentType)) {
                    val protobufBytes = rawBodyBytes ?: return@Column
                    CollapsibleSection(
                        title = stringResource(R.string.viewer_body_body),
                        isExpanded = bodyExpanded,
                        onToggle = { onEvent(TransactionDetailViewEvent.Request.ToggleBodyExpanded) },
                        onCopy = null,
                    ) {
                        ProtobufView(data = protobufBytes)
                    }
                } else if (requestBody != null || rawBody != null) {
                    val detectedContentType = parsedContentType
                    val colors = syntaxColors()

                    CollapsibleSection(
                        title = stringResource(R.string.viewer_body_body),
                        isExpanded = bodyExpanded,
                        onToggle = { onEvent(TransactionDetailViewEvent.Request.ToggleBodyExpanded) },
                        onCopy = { onEvent(TransactionDetailViewEvent.Request.CopyBody) },
                        trailingContent = {
                            BodyControlsRow(
                                contentType = detectedContentType,
                                isPrettyMode = isPrettyMode,
                                onPrettyModeToggle = { onEvent(TransactionDetailViewEvent.Request.TogglePrettyMode) },
                            )
                        },
                    ) {
                        val displayBody = if (isPrettyMode) {
                            requestBody ?: requireNotNull(rawBody) { "Body must be available" }
                        } else {
                            rawBody ?: requireNotNull(requestBody) { "Body must be available" }
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
                                    val boundary = requestContentType?.let {
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
            if (transaction.request.headers.isEmpty() && blobId == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No request data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Floating Action Button for Copy All - hidden when search is active
        AnimatedVisibility(
            visible = isScrolling && requestBody != null && !isSearchActive,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            WormaCeptorFAB(
                onClick = { onEvent(TransactionDetailViewEvent.Request.CopyAllContent) },
                icon = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.viewer_body_copy_all),
            )
        }
    }
}
