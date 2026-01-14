package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.asSubtleBackground

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transaction: NetworkTransaction,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val tabs = listOf("Overview", "Request", "Response")
    
    val focusRequester = remember { FocusRequester() }
    
    // Search navigation state - now using matchCount instead of positions list
    var currentMatchIndex by remember(searchQuery, selectedTabIndex) { mutableIntStateOf(0) }
    var matchCount by remember(searchQuery, selectedTabIndex) { mutableIntStateOf(0) }

    LaunchedEffect(showSearch) {
        if (showSearch) {
            focusRequester.requestFocus()
        }
    }


    val title = remember(transaction.request.url) {
        try {
            val uri = java.net.URI(transaction.request.url)
            uri.path ?: transaction.request.url
        } catch (e: Exception) {
            transaction.request.url
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        if (showSearch) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search in body...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                }
                            )
                        } else {
                            Text(title, maxLines = 1)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (selectedTabIndex > 0) { // Show search for Request/Response tabs
                            IconButton(onClick = {
                                showSearch = !showSearch
                                if (!showSearch) searchQuery = ""
                            }) {
                                AnimatedVisibility(!showSearch) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                }
                            }
                        }

                        if (!showSearch) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy as Text") },
                                onClick = {
                                    showMenu = false
                                    copyToClipboard(context, "Transaction", generateTextSummary(transaction))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Copy as cURL") },
                                onClick = {
                                    showMenu = false
                                    copyToClipboard(context, "cURL", generateCurlCommand(transaction))
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Share as JSON") },
                                onClick = {
                                    showMenu = false
                                    val exportManager = com.azikar24.wormaceptor.feature.viewer.export.ExportManager(context)
                                    scope.launch {
                                        exportManager.exportTransactions(listOf(transaction))
                                    }
                                }
                            )
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = selectedTabIndex,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.ime),
                animationSpec = tween(durationMillis = 200),
                label = "tab_fade"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> OverviewTab(transaction, Modifier.fillMaxSize())
                    1 -> RequestTab(
                        transaction = transaction,
                        searchQuery = searchQuery,
                        currentMatchIndex = currentMatchIndex,
                        onMatchCountChanged = { matchCount = it },
                        onScrollToMatch = { /* Scroll handled internally */ },
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> ResponseTab(
                        transaction = transaction,
                        searchQuery = searchQuery,
                        currentMatchIndex = currentMatchIndex,
                        onMatchCountChanged = { matchCount = it },
                        onScrollToMatch = { /* Scroll handled internally */ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Search Navigation Controllers in Bottom Right
            if (showSearch && matchCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .imePadding() // Key fix for keyboard overlap
                        .padding(bottom = 32.dp, end = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentMatchIndex + 1}/$matchCount",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { 
                            currentMatchIndex = (currentMatchIndex - 1 + matchCount) % matchCount 
                        }) {
                            Icon(Icons.Default.KeyboardArrowUp, "Prev")
                        }
                        IconButton(onClick = { 
                            currentMatchIndex = (currentMatchIndex + 1) % matchCount 
                        }) {
                            Icon(Icons.Default.KeyboardArrowDown, "Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(transaction: NetworkTransaction, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg)
    ) {
        // Status & Timing Card with Timeline
        EnhancedOverviewCard(
            title = "Timing",
            icon = Icons.Default.Schedule,
            iconTint = MaterialTheme.colorScheme.tertiary
        ) {
            DetailRow("URL", transaction.request.url)
            DetailRow("Method", transaction.request.method)
            DetailRow("Status", transaction.status.name)
            DetailRow("Response Code", transaction.response?.code?.toString() ?: "-")
            DetailRow("Duration", "${transaction.durationMs ?: "?"}ms")
            DetailRow("Timestamp", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(transaction.timestamp)))

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Visual Timeline
            TransactionTimeline(
                durationMs = transaction.durationMs ?: 0,
                hasResponse = transaction.response != null
            )
        }

        // Security Card with Badge
        EnhancedOverviewCard(
            title = "Security",
            icon = Icons.Default.Security,
            iconTint = MaterialTheme.colorScheme.secondary
        ) {
            DetailRow("Protocol", transaction.response?.protocol ?: "Unknown")

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Enhanced SSL/TLS Badge
            val isSsl = transaction.response?.tlsVersion != null
            SslBadge(
                isSsl = isSsl,
                tlsVersion = transaction.response?.tlsVersion
            )
        }

        // Data Transfer Card
        EnhancedOverviewCard(
            title = "Data Transfer",
            icon = Icons.Default.Storage,
            iconTint = MaterialTheme.colorScheme.primary
        ) {
            val reqSize = transaction.request.bodySize
            val resSize = transaction.response?.bodySize ?: 0
            val totalSize = reqSize + resSize

            DetailRow("Request Size", formatBytes(reqSize))
            DetailRow("Response Size", formatBytes(resSize))
            DetailRow("Total Transfer", formatBytes(totalSize))
        }
    }
}

@Composable
private fun TransactionTimeline(durationMs: Long, hasResponse: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = WormaCeptorDesignSystem.Shapes.chip
            )
            .padding(WormaCeptorDesignSystem.Spacing.md)
    ) {
        Text(
            text = "Request/Response Timeline",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.xs)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Request Phase
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = WormaCeptorDesignSystem.CornerRadius.xs,
                            bottomStart = WormaCeptorDesignSystem.CornerRadius.xs
                        )
                    )
            )

            // Processing/Network Phase
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
            )

            // Response Phase
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        if (hasResponse)
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topEnd = WormaCeptorDesignSystem.CornerRadius.xs,
                            bottomEnd = WormaCeptorDesignSystem.CornerRadius.xs
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WormaCeptorDesignSystem.Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Request",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${durationMs}ms",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (hasResponse) "Response" else "Failed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SslBadge(isSsl: Boolean, tlsVersion: String?) {
    Surface(
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = if (isSsl)
            MaterialTheme.colorScheme.primary.asSubtleBackground()
        else
            MaterialTheme.colorScheme.error.asSubtleBackground(),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            if (isSsl)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        ),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.md,
                vertical = WormaCeptorDesignSystem.Spacing.sm
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)
        ) {
            Icon(
                imageVector = if (isSsl) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isSsl) "Secure" else "Insecure",
                tint = if (isSsl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isSsl) (tlsVersion ?: "Secure Connection") else "Insecure Connection",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = if (isSsl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EnhancedOverviewCard(
    title: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val gradientColors = listOf(
        iconTint.copy(alpha = 0.03f),
        iconTint.copy(alpha = 0.01f),
        androidx.compose.ui.graphics.Color.Transparent
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm)
        ),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        shape = WormaCeptorDesignSystem.Shapes.card
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(gradientColors)
                )
        ) {
            Column(modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg)) {
                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.md)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                content()
            }
        }
    }
}

@Composable
private fun OverviewCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroup = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroup.toDouble()), units[digitGroup])
}

/**
 * Data class holding information about a single search match
 */
private data class MatchInfo(
    val globalPosition: Int,
    val lineIndex: Int
)

@Composable
private fun RequestTab(
    transaction: NetworkTransaction,
    searchQuery: String,
    currentMatchIndex: Int,
    onMatchCountChanged: (Int) -> Unit,
    onScrollToMatch: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val blobId = transaction.request.bodyRef
    var requestBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBody by remember(blobId) { mutableStateOf<String?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    var isPrettyMode by remember { mutableStateOf(true) }
    var headersExpanded by remember { mutableStateOf(true) }
    var bodyExpanded by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // 1. Body Loading
    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            val raw = com.azikar24.wormaceptor.core.engine.CoreHolder.queryEngine?.getBody(blobId)
            rawBody = raw
            val formatted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                if (raw != null) formatJson(raw) else null
            }
            requestBody = formatted
            isLoading = false
        } else {
            requestBody = null
            rawBody = null
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

        kotlinx.coroutines.delay(250) // Debounce

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val foundMatches = mutableListOf<MatchInfo>()
            var index = body.indexOf(searchQuery, ignoreCase = true)
            while (index >= 0) {
                foundMatches.add(MatchInfo(globalPosition = index, lineIndex = 0)) // lineIndex not used for pixel scroll
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
        } catch (e: Exception) {
            // Offset out of bounds, ignore
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(WormaCeptorDesignSystem.Spacing.lg)
        ) {
            // Only show Headers section if headers exist
            if (transaction.request.headers.isNotEmpty()) {
                CollapsibleSection(
                    title = "Headers",
                    isExpanded = headersExpanded,
                    onToggle = { headersExpanded = !headersExpanded },
                    onCopy = { copyToClipboard(context, "Request Headers", formatHeaders(transaction.request.headers)) }
                ) {
                    Headers(transaction.request.headers)
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
            }

            // Only show Body section if body exists
            if (blobId != null) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Processing body...", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (requestBody != null || rawBody != null) {
                    CollapsibleSection(
                        title = "Body",
                        isExpanded = bodyExpanded,
                        onToggle = { bodyExpanded = !bodyExpanded },
                        onCopy = { copyToClipboard(context, "Request Body", if (isPrettyMode) (requestBody ?: rawBody!!) else (rawBody ?: requestBody!!)) },
                        trailingContent = {
                            PrettyRawToggle(
                                isPretty = isPrettyMode,
                                onToggle = { isPrettyMode = !isPrettyMode }
                            )
                        }
                    ) {
                        val displayBody = if (isPrettyMode) (requestBody ?: rawBody!!) else (rawBody ?: requestBody!!)
                        val currentMatchGlobalPos = matches.getOrNull(currentMatchIndex)?.globalPosition
                        val annotatedBody = remember(displayBody, searchQuery, currentMatchGlobalPos) {
                            enhancedHighlightMatches(displayBody, searchQuery, currentMatchGlobalPos)
                        }

                        SelectionContainer {
                            Text(
                                text = annotatedBody,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                onTextLayout = { textLayoutResult = it }
                            )
                        }
                    }
                }
            }

            // Show empty state if no headers and no body
            if (transaction.request.headers.isEmpty() && blobId == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No request data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Floating Action Button for Copy All
        AnimatedVisibility(
            visible = isScrolling && requestBody != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg)
        ) {
            FloatingActionButton(
                onClick = {
                    val fullContent = buildString {
                        appendLine("=== REQUEST HEADERS ===")
                        appendLine(formatHeaders(transaction.request.headers))
                        if (requestBody != null) {
                            appendLine("\n=== REQUEST BODY ===")
                            appendLine(if (isPrettyMode) requestBody!! else rawBody!!)
                        }
                    }
                    copyToClipboard(context, "Request Content", fullContent)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy All")
            }
        }
    }
}

@Composable
private fun ResponseTab(
    transaction: NetworkTransaction,
    searchQuery: String,
    currentMatchIndex: Int,
    onMatchCountChanged: (Int) -> Unit,
    onScrollToMatch: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val blobId = transaction.response?.bodyRef
    var responseBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBody by remember(blobId) { mutableStateOf<String?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    var isPrettyMode by remember { mutableStateOf(true) }
    var headersExpanded by remember { mutableStateOf(true) }
    var bodyExpanded by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // 1. Body Loading
    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            val raw = com.azikar24.wormaceptor.core.engine.CoreHolder.queryEngine?.getBody(blobId)
            rawBody = raw
            val formatted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                if (raw != null) formatJson(raw) else null
            }
            responseBody = formatted
            isLoading = false
        } else {
            responseBody = null
            rawBody = null
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

        kotlinx.coroutines.delay(250) // Debounce

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
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
        } catch (e: Exception) {
            // Offset out of bounds, ignore
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(WormaCeptorDesignSystem.Spacing.lg)
        ) {
            if (transaction.response != null) {
                val hasHeaders = transaction.response?.headers?.isNotEmpty() == true
                val hasBody = blobId != null

                // Only show Headers section if headers exist
                if (hasHeaders) {
                    CollapsibleSection(
                        title = "Headers",
                        isExpanded = headersExpanded,
                        onToggle = { headersExpanded = !headersExpanded },
                        onCopy = { transaction.response?.headers?.let { copyToClipboard(context, "Response Headers", formatHeaders(it)) } }
                    ) {
                        transaction.response?.headers?.let { Headers(it) }
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
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("Processing body...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (responseBody != null || rawBody != null) {
                        CollapsibleSection(
                            title = "Body",
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = { copyToClipboard(context, "Response Body", if (isPrettyMode) (responseBody ?: rawBody!!) else (rawBody ?: responseBody!!)) },
                            trailingContent = {
                                PrettyRawToggle(
                                    isPretty = isPrettyMode,
                                    onToggle = { isPrettyMode = !isPrettyMode }
                                )
                            }
                        ) {
                            val displayBody = if (isPrettyMode) (responseBody ?: rawBody!!) else (rawBody ?: responseBody!!)
                            val currentMatchGlobalPos = matches.getOrNull(currentMatchIndex)?.globalPosition
                            val annotatedBody = remember(displayBody, searchQuery, currentMatchGlobalPos) {
                                enhancedHighlightMatches(displayBody, searchQuery, currentMatchGlobalPos)
                            }

                            SelectionContainer {
                                Text(
                                    text = annotatedBody,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth(),
                                    onTextLayout = { textLayoutResult = it }
                                )
                            }
                        }
                    }
                }

                // Show empty state if no headers and no body
                if (!hasHeaders && !hasBody) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No response data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No response received",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Floating Action Button for Copy All
        AnimatedVisibility(
            visible = isScrolling && responseBody != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg)
        ) {
            FloatingActionButton(
                onClick = {
                    val fullContent = buildString {
                        transaction.response?.headers?.let {
                            appendLine("=== RESPONSE HEADERS ===")
                            appendLine(formatHeaders(it))
                        }
                        if (responseBody != null) {
                            appendLine("\n=== RESPONSE BODY ===")
                            appendLine(if (isPrettyMode) responseBody!! else rawBody!!)
                        }
                    }
                    copyToClipboard(context, "Response Content", fullContent)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy All")
            }
        }
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trailingContent?.invoke()

                if (onCopy != null) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 200)
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 200)
            ) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WormaCeptorDesignSystem.Spacing.sm,
                        bottom = WormaCeptorDesignSystem.Spacing.md
                    )
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PrettyRawToggle(
    isPretty: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs
            ),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs)
        ) {
            Text(
                text = "Pretty",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isPretty)
                        androidx.compose.ui.text.font.FontWeight.Bold
                    else
                        androidx.compose.ui.text.font.FontWeight.Normal
                ),
                color = if (isPretty)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "|",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Raw",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (!isPretty)
                        androidx.compose.ui.text.font.FontWeight.Bold
                    else
                        androidx.compose.ui.text.font.FontWeight.Normal
                ),
                color = if (!isPretty)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enhanced highlight with better visual distinction for current match
 */
private fun enhancedHighlightMatches(
    text: String,
    query: String,
    currentMatchGlobalPos: Int?
): androidx.compose.ui.text.AnnotatedString {
    if (query.isEmpty()) return androidx.compose.ui.text.AnnotatedString(text)

    return androidx.compose.ui.text.buildAnnotatedString {
        var start = 0
        while (start < text.length) {
            val index = text.indexOf(query, start, ignoreCase = true)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, index))

            val isCurrent = index == currentMatchGlobalPos

            withStyle(
                style = androidx.compose.ui.text.SpanStyle(
                    background = if (isCurrent)
                        androidx.compose.ui.graphics.Color.Cyan.copy(alpha = 0.6f)
                    else
                        androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.4f),
                    fontWeight = if (isCurrent)
                        androidx.compose.ui.text.font.FontWeight.Bold
                    else
                        androidx.compose.ui.text.font.FontWeight.Normal,
                    color = if (isCurrent)
                        androidx.compose.ui.graphics.Color.Black
                    else
                        androidx.compose.ui.graphics.Color.Unspecified
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
        }
    }
}

/**
 * Highlights all matches in the full text, using global position for current match detection
 */
private fun highlightMatchesInText(
    text: String,
    query: String,
    currentMatchGlobalPos: Int?
): androidx.compose.ui.text.AnnotatedString {
    if (query.isEmpty()) return androidx.compose.ui.text.AnnotatedString(text)
    
    return androidx.compose.ui.text.buildAnnotatedString {
        var start = 0
        while (start < text.length) {
            val index = text.indexOf(query, start, ignoreCase = true)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, index))
            
            val isCurrent = index == currentMatchGlobalPos

            withStyle(style = androidx.compose.ui.text.SpanStyle(
                background = if (isCurrent) androidx.compose.ui.graphics.Color.Cyan else androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.5f),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
        }
    }
}


private fun highlightMatches(text: String, query: String, currentIndex: Int = -1): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var start = 0
        var count = 0
        while (start < text.length) {
            val index = text.indexOf(query, start, ignoreCase = true)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, index))
            val isCurrent = count == currentIndex
            withStyle(style = androidx.compose.ui.text.SpanStyle(
                background = if (isCurrent) androidx.compose.ui.graphics.Color.Cyan else androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.5f),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
            count++
        }
    }
}

private fun highlightMatchesInLine(
    line: String,
    lineStart: Int,
    query: String,
    currentIndex: Int,
    matchPositions: List<Int>
): androidx.compose.ui.text.AnnotatedString {
    if (query.isEmpty()) return androidx.compose.ui.text.AnnotatedString(line)
    
    return androidx.compose.ui.text.buildAnnotatedString {
        var start = 0
        while (start < line.length) {
            val indexInLine = line.indexOf(query, start, ignoreCase = true)
            if (indexInLine == -1) {
                append(line.substring(start))
                break
            }
            append(line.substring(start, indexInLine))
            
            val globalIndex = lineStart + indexInLine
            val isCurrent = matchPositions.getOrNull(currentIndex) == globalIndex

            withStyle(style = androidx.compose.ui.text.SpanStyle(
                background = if (isCurrent) androidx.compose.ui.graphics.Color.Cyan else androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.5f),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )) {
                append(line.substring(indexInLine, indexInLine + query.length))
            }
            start = indexInLine + query.length
        }
    }
}


@Composable
private fun SectionHeader(title: String, onCopy: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (onCopy != null) {
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        SelectionContainer {
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Headers(headers: Map<String, List<String>>) {
    Column {
        headers.forEach { (key, values) ->
            DetailRow(key, values.joinToString(", "))
        }
    }
}

private fun formatHeaders(headers: Map<String, List<String>>): String {
    return headers.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label Copied", Toast.LENGTH_SHORT).show()
}

private fun generateTextSummary(transaction: NetworkTransaction): String = buildString {
    appendLine("--- WormaCeptor Transaction ---")
    appendLine("URL: ${transaction.request.url}")
    appendLine("Method: ${transaction.request.method}")
    appendLine("Status: ${transaction.status.name}")
    appendLine("Code: ${transaction.response?.code ?: "-"}")
    appendLine("Duration: ${transaction.durationMs ?: "?"}ms")
    appendLine("\n[Request Headers]")
    appendLine(formatHeaders(transaction.request.headers))
    transaction.response?.let { res ->
        appendLine("\n[Response Headers]")
        appendLine(formatHeaders(res.headers))
    }
}

private fun generateCurlCommand(transaction: NetworkTransaction): String = buildString {
    append("curl -X ${transaction.request.method} \"${transaction.request.url}\"")
    transaction.request.headers.forEach { (key, values) ->
        values.forEach { value ->
            val escapedKey = key.replace("'", "'\\''")
            val escapedValue = value.replace("'", "'\\''")
            append(" -H '$escapedKey: $escapedValue'")
        }
    }
    // Note: We don't include the body in the cURL command here as it might be binary or huge,
    // and we only have the blobId in the domain entity. In a future version, 
    // we could fetch the body if small.
}

private fun formatJson(json: String): String {
    // Skip formatting for massive strings to prevent UI freeze and memory pressure
    if (json.length > 500_000) {
        return json.take(100_000) + "\n\n... (Rest of content truncated for performance) ..."
    }
    
    return try {
        val trimmed = json.trim()
        if (trimmed.startsWith("{")) JSONObject(trimmed).toString(4)
        else if (trimmed.startsWith("[")) JSONArray(trimmed).toString(4)
        else json
    } catch (e: Exception) {
        json
    }
}
