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
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                )
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
                                Icon(
                                    imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            }
                        }

                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
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
        Crossfade(
            targetState = selectedTabIndex,
            modifier = Modifier.padding(padding),
            label = "tab_fade"
        ) { targetIndex ->
            when (targetIndex) {
                0 -> OverviewTab(transaction, Modifier.fillMaxSize())
                1 -> RequestTab(transaction, searchQuery, Modifier.fillMaxSize())
                2 -> ResponseTab(transaction, searchQuery, Modifier.fillMaxSize())
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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

@Composable
private fun RequestTab(transaction: NetworkTransaction, searchQuery: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val blobId = transaction.request.bodyRef
    var requestBody by remember(blobId) { mutableStateOf<String?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }

    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            val formatted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                val raw = com.azikar24.wormaceptor.core.engine.CoreHolder.queryEngine?.getBody(blobId)
                if (raw != null) formatJson(raw) else null
            }
            requestBody = formatted
            isLoading = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            SectionHeader("Headers", onCopy = { copyToClipboard(context, "Request Headers", formatHeaders(transaction.request.headers)) })
            Headers(transaction.request.headers)
        }

        if (blobId != null) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(" Processing body...", style = MaterialTheme.typography.bodySmall)
                } else if (requestBody != null) {
                    SectionHeader("Body", onCopy = { copyToClipboard(context, "Request Body", requestBody!!) })
                    SelectionContainer {
                        Text(
                            text = if (searchQuery.isNotEmpty()) highlightMatches(requestBody!!, searchQuery) else androidx.compose.ui.text.AnnotatedString(requestBody!!),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Text("Failed to load body", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("No request body", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ResponseTab(transaction: NetworkTransaction, searchQuery: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val blobId = transaction.response?.bodyRef
    var responseBody by remember(blobId) { mutableStateOf<String?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }

    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            val formatted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                val raw = com.azikar24.wormaceptor.core.engine.CoreHolder.queryEngine?.getBody(blobId)
                if (raw != null) formatJson(raw) else null
            }
            responseBody = formatted
            isLoading = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (transaction.response != null) {
            item {
                SectionHeader("Headers", onCopy = { transaction.response?.headers?.let { copyToClipboard(context, "Response Headers", formatHeaders(it)) } })
                transaction.response?.headers?.let { Headers(it) }
            }

            if (blobId != null) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(" Processing body...", style = MaterialTheme.typography.bodySmall)
                    } else if (responseBody != null) {
                        SectionHeader("Body", onCopy = { copyToClipboard(context, "Response Body", responseBody!!) })
                        SelectionContainer {
                            Text(
                                text = if (searchQuery.isNotEmpty()) highlightMatches(responseBody!!, searchQuery) else androidx.compose.ui.text.AnnotatedString(responseBody!!),
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text("Failed to load body", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No response body", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            item {
                Text("No response received", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun highlightMatches(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var start = 0
        while (start < text.length) {
            val index = text.indexOf(query, start, ignoreCase = true)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, index))
            withStyle(style = androidx.compose.ui.text.SpanStyle(
                background = androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.5f),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
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
            append(" -H \"$key: $value\"")
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
