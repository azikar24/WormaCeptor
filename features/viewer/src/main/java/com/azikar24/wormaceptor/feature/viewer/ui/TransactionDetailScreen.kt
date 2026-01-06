package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.*
import androidx.compose.animation.*
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & Timing Card
        OverviewCard(title = "Transaction Info") {
            DetailRow("URL", transaction.request.url)
            DetailRow("Method", transaction.request.method)
            DetailRow("Status", transaction.status.name)
            DetailRow("Response Code", transaction.response?.code?.toString() ?: "-")
            DetailRow("Duration", "${transaction.durationMs ?: "?"}ms")
            DetailRow("Timestamp", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(transaction.timestamp)))
        }

        // Connection Card
        OverviewCard(title = "Network Telemetry") {
            DetailRow("Protocol", transaction.response?.protocol ?: "Unknown")
            
            val isSsl = transaction.response?.tlsVersion != null
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SSL/TLS: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (isSsl) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.response?.tlsVersion ?: "Secure",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Insecure",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Insecure",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Size Card
        OverviewCard(title = "Data Transfer") {
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
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    
    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    // 1. Body Loading
    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            val raw = com.azikar24.wormaceptor.core.engine.CoreHolder.queryEngine?.getBody(blobId)
            val formatted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                if (raw != null) formatJson(raw) else null
            }
            requestBody = formatted
            isLoading = false
        } else {
            requestBody = null
        }
    }

    // 2. Search: Find matches
    LaunchedEffect(requestBody, searchQuery) {
        val body = requestBody
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader("Headers", onCopy = { copyToClipboard(context, "Request Headers", formatHeaders(transaction.request.headers)) })
        Headers(transaction.request.headers)

        Spacer(modifier = Modifier.height(24.dp))

        if (blobId != null) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(" Processing body...", style = MaterialTheme.typography.bodySmall)
            } else if (requestBody != null) {
                SectionHeader("Body", onCopy = { copyToClipboard(context, "Request Body", requestBody!!) })
                
                val currentMatchGlobalPos = matches.getOrNull(currentMatchIndex)?.globalPosition
                val annotatedBody = remember(requestBody, searchQuery, currentMatchGlobalPos) {
                    highlightMatchesInText(requestBody!!, searchQuery, currentMatchGlobalPos)
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
        } else {
            Text("No request body", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    
    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    // 1. Body Loading
    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            val raw = com.azikar24.wormaceptor.core.engine.CoreHolder.queryEngine?.getBody(blobId)
            val formatted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                if (raw != null) formatJson(raw) else null
            }
            responseBody = formatted
            isLoading = false
        } else {
            responseBody = null
        }
    }

    // 2. Search: Find matches
    LaunchedEffect(responseBody, searchQuery) {
        val body = responseBody
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (transaction.response != null) {
            SectionHeader("Headers", onCopy = { transaction.response?.headers?.let { copyToClipboard(context, "Response Headers", formatHeaders(it)) } })
            transaction.response?.headers?.let { Headers(it) }

            Spacer(modifier = Modifier.height(24.dp))

            if (blobId != null) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(" Processing body...", style = MaterialTheme.typography.bodySmall)
                } else if (responseBody != null) {
                    SectionHeader("Body", onCopy = { copyToClipboard(context, "Response Body", responseBody!!) })
                    
                    val currentMatchGlobalPos = matches.getOrNull(currentMatchIndex)?.globalPosition
                    val annotatedBody = remember(responseBody, searchQuery, currentMatchGlobalPos) {
                        highlightMatchesInText(responseBody!!, searchQuery, currentMatchGlobalPos)
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
            } else {
                Text("No response body", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text("No response received", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
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
