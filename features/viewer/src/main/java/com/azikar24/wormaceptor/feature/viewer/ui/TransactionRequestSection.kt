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
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.FormDataView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.JsonTreeView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.MultipartView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.ProtobufView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.XmlTreeView
import com.azikar24.wormaceptor.feature.viewer.ui.util.getFileInfoForContentType
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareAsFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
internal fun RequestTab(
    transaction: NetworkTransaction,
    queryEngine: QueryEngine?,
    searchQuery: String,
    currentMatchIndex: Int,
    onMatchCountChanged: (Int) -> Unit,
    isSearchActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val blobId = transaction.request.bodyRef
    var requestBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBodyBytes by remember(blobId) { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var copyRequested by remember { mutableStateOf(false) }
    var isCopying by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    var isPrettyMode by remember { mutableStateOf(true) }
    var headersExpanded by rememberSaveable { mutableStateOf(true) }
    var bodyExpanded by rememberSaveable { mutableStateOf(true) }
    var parsedContentType by remember(blobId) { mutableStateOf(ContentType.UNKNOWN) }

    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // Get content type for sharing
    val requestContentType = transaction.request.headers.entries
        .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
        ?.value?.firstOrNull()

    val shareRequestBodyTitle = stringResource(R.string.viewer_share_request_body)

    // 1. Body Loading - get raw bytes first to detect binary content
    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            val bytes = queryEngine?.getBodyBytes(blobId)
            rawBodyBytes = bytes

            if (bytes != null && isProtobufContentType(requestContentType)) {
                // Protobuf is binary - keep raw bytes, don't UTF-8 decode
            } else if (bytes != null) {
                val raw = String(bytes, Charsets.UTF_8)
                rawBody = raw
                val result = withContext(Dispatchers.Default) {
                    parseBodyViaRegistry(requestContentType, bytes, raw)
                }
                requestBody = result.first
                parsedContentType = result.second
            }
            isLoading = false
        } else {
            requestBody = null
            rawBody = null
            rawBodyBytes = null
        }
    }

    // Handle copy request - copies if small, shares as file if large
    LaunchedEffect(copyRequested) {
        if (copyRequested) {
            isCopying = true
            try {
                val bodyContent = if (isPrettyMode) requestBody ?: rawBody else rawBody ?: requestBody
                if (bodyContent != null) {
                    if (isContentTooLargeForClipboard(bodyContent)) {
                        val (ext, mime) = getFileInfoForContentType(requestContentType)
                        shareAsFile(
                            context = context,
                            content = bodyContent,
                            fileName = "request_body.$ext",
                            mimeType = mime,
                            title = shareRequestBodyTitle,
                        )
                    } else {
                        copyToClipboard(context, "Request Body", bodyContent)
                    }
                }
            } finally {
                isCopying = false
                copyRequested = false
            }
        }
    }

    // 2. Search: Find matches
    LaunchedEffect(requestBody, rawBody, searchQuery, isPrettyMode) {
        val body = if (isPrettyMode) requestBody else rawBody
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
                foundMatches.add(
                    MatchInfo(
                        globalPosition = index,
                        lineIndex = 0,
                    ),
                ) // lineIndex not used for pixel scroll
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
            // Only show Headers section if headers exist
            if (transaction.request.headers.isNotEmpty()) {
                CollapsibleSection(
                    title = stringResource(R.string.viewer_body_headers),
                    isExpanded = headersExpanded,
                    onToggle = { headersExpanded = !headersExpanded },
                    onCopy = {
                        copyToClipboard(
                            context,
                            "Request Headers",
                            formatHeaders(transaction.request.headers),
                        )
                    },
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
                        onToggle = { bodyExpanded = !bodyExpanded },
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
                onClick = {
                    val fullContent = buildString {
                        appendLine("=== REQUEST HEADERS ===")
                        appendLine(formatHeaders(transaction.request.headers))
                        if (requestBody != null || rawBody != null) {
                            appendLine("\n=== REQUEST BODY ===")
                            val body = if (isPrettyMode) requestBody ?: rawBody else rawBody ?: requestBody
                            body?.let { appendLine(it) }
                        }
                    }
                    copyToClipboard(context, "Request Content", fullContent)
                },
                icon = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.viewer_body_copy_all),
            )
        }
    }
}
