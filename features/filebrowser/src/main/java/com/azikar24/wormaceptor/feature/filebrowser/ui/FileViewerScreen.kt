/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.ui

import android.annotation.SuppressLint
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.feature.filebrowser.ui.theme.FileBrowserDesignSystem
import java.io.File

/**
 * Screen for viewing file content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(filePath: String, content: FileContent, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val fileName = File(filePath).name

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (content) {
                is FileContent.Text -> {
                    TextFileContent(content)
                }
                is FileContent.Json -> {
                    JsonFileContent(content)
                }
                is FileContent.Xml -> {
                    XmlFileContent(content)
                }
                is FileContent.Binary -> {
                    BinaryFileContent(content)
                }
                is FileContent.Image -> {
                    ImageFileContent(content)
                }
                is FileContent.Pdf -> {
                    PdfFileContent(content)
                }
                is FileContent.TooLarge -> {
                    ErrorContent(
                        "File too large: ${formatBytes(content.sizeBytes)}\nMax size: ${formatBytes(content.maxSize)}",
                    )
                }
                is FileContent.Error -> {
                    ErrorContent(content.message)
                }
            }
        }
    }
}

@Composable
private fun TextFileContent(content: FileContent.Text) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(FileBrowserDesignSystem.Spacing.lg),
    ) {
        Text(
            text = content.content,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun JsonFileContent(content: FileContent.Json) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with validity indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FileBrowserDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "JSON (${content.lineCount} lines)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (content.isValid) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = if (content.isValid) "Valid JSON" else "Invalid JSON",
                tint = if (content.isValid) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (content.isValid) "Valid" else "Invalid",
                style = MaterialTheme.typography.labelSmall,
                color = if (content.isValid) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.padding(start = FileBrowserDesignSystem.Spacing.xs),
            )
        }

        // JSON content with syntax highlighting
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = FileBrowserDesignSystem.Spacing.lg),
        ) {
            Text(
                text = highlightJson(content.formattedContent),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun XmlFileContent(content: FileContent.Xml) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with validity indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FileBrowserDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "XML (${content.lineCount} lines)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (content.isValid) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = if (content.isValid) "Valid XML" else "Invalid XML",
                tint = if (content.isValid) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (content.isValid) "Valid" else "Invalid",
                style = MaterialTheme.typography.labelSmall,
                color = if (content.isValid) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.padding(start = FileBrowserDesignSystem.Spacing.xs),
            )
        }

        // XML content with syntax highlighting
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = FileBrowserDesignSystem.Spacing.lg),
        ) {
            Text(
                text = highlightXml(content.formattedContent),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

// Syntax highlighting colors
private val JsonKeyColor = Color(0xFF9C27B0) // Purple for keys
private val JsonStringColor = Color(0xFF4CAF50) // Green for string values
private val JsonNumberColor = Color(0xFF2196F3) // Blue for numbers
private val JsonBoolNullColor = Color(0xFFFF5722) // Orange for booleans/null
private val JsonBracketColor = Color(0xFF757575) // Gray for brackets

private val XmlTagColor = Color(0xFF2196F3) // Blue for tags
private val XmlAttrNameColor = Color(0xFF9C27B0) // Purple for attribute names
private val XmlAttrValueColor = Color(0xFF4CAF50) // Green for attribute values
private val XmlContentColor = Color(0xFF212121) // Dark for content

@Composable
private fun highlightJson(json: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val text = json
        while (i < text.length) {
            when {
                // String (key or value)
                text[i] == '"' -> {
                    val start = i
                    i++
                    while (i < text.length && text[i] != '"') {
                        if (text[i] == '\\' && i + 1 < text.length) i++
                        i++
                    }
                    i++ // Include closing quote
                    val str = text.substring(start, minOf(i, text.length))

                    // Check if this is a key (followed by colon)
                    var j = i
                    while (j < text.length && text[j].isWhitespace()) j++
                    val isKey = j < text.length && text[j] == ':'

                    withStyle(SpanStyle(color = if (isKey) JsonKeyColor else JsonStringColor)) {
                        append(str)
                    }
                }
                // Number
                text[i].isDigit() || (text[i] == '-' && i + 1 < text.length && text[i + 1].isDigit()) -> {
                    val start = i
                    if (text[i] == '-') i++
                    while (i < text.length && (text[i].isDigit() || text[i] == '.' || text[i] == 'e' || text[i] == 'E' || text[i] == '+' || text[i] == '-')) {
                        i++
                    }
                    withStyle(SpanStyle(color = JsonNumberColor)) {
                        append(text.substring(start, i))
                    }
                }
                // Boolean or null
                text.substring(i).startsWith("true") -> {
                    withStyle(SpanStyle(color = JsonBoolNullColor, fontWeight = FontWeight.Bold)) {
                        append("true")
                    }
                    i += 4
                }
                text.substring(i).startsWith("false") -> {
                    withStyle(SpanStyle(color = JsonBoolNullColor, fontWeight = FontWeight.Bold)) {
                        append("false")
                    }
                    i += 5
                }
                text.substring(i).startsWith("null") -> {
                    withStyle(SpanStyle(color = JsonBoolNullColor, fontWeight = FontWeight.Bold)) {
                        append("null")
                    }
                    i += 4
                }
                // Brackets and braces
                text[i] in "{}[]" -> {
                    withStyle(SpanStyle(color = JsonBracketColor, fontWeight = FontWeight.Bold)) {
                        append(text[i])
                    }
                    i++
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

@Composable
private fun highlightXml(xml: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val text = xml
        while (i < text.length) {
            when {
                // Tag start
                text[i] == '<' -> {
                    val start = i
                    i++

                    // Check for comment, CDATA, or processing instruction
                    if (i < text.length && text[i] == '!') {
                        // Comment or CDATA
                        while (i < text.length && text[i] != '>') i++
                        i++
                        withStyle(SpanStyle(color = Color.Gray)) {
                            append(text.substring(start, minOf(i, text.length)))
                        }
                    } else if (i < text.length && text[i] == '?') {
                        // Processing instruction
                        while (i < text.length && !(text[i - 1] == '?' && text[i] == '>')) i++
                        i++
                        withStyle(SpanStyle(color = Color.Gray)) {
                            append(text.substring(start, minOf(i, text.length)))
                        }
                    } else {
                        // Regular tag
                        withStyle(SpanStyle(color = XmlTagColor)) {
                            append("<")
                        }

                        // Closing tag slash
                        if (i < text.length && text[i] == '/') {
                            withStyle(SpanStyle(color = XmlTagColor)) {
                                append("/")
                            }
                            i++
                        }

                        // Tag name
                        val nameStart = i
                        while (i < text.length && !text[i].isWhitespace() && text[i] != '>' && text[i] != '/') i++
                        withStyle(SpanStyle(color = XmlTagColor, fontWeight = FontWeight.Bold)) {
                            append(text.substring(nameStart, i))
                        }

                        // Attributes
                        while (i < text.length && text[i] != '>') {
                            if (text[i].isWhitespace()) {
                                append(text[i])
                                i++
                            } else if (text[i] == '/') {
                                withStyle(SpanStyle(color = XmlTagColor)) {
                                    append("/")
                                }
                                i++
                            } else if (text[i] == '=') {
                                append("=")
                                i++
                            } else if (text[i] == '"' || text[i] == '\'') {
                                val quote = text[i]
                                val attrStart = i
                                i++
                                while (i < text.length && text[i] != quote) i++
                                i++
                                withStyle(SpanStyle(color = XmlAttrValueColor)) {
                                    append(text.substring(attrStart, minOf(i, text.length)))
                                }
                            } else {
                                // Attribute name
                                val attrNameStart = i
                                while (i < text.length && !text[i].isWhitespace() && text[i] != '=' && text[i] != '>' && text[i] != '/') i++
                                withStyle(SpanStyle(color = XmlAttrNameColor)) {
                                    append(text.substring(attrNameStart, i))
                                }
                            }
                        }

                        if (i < text.length && text[i] == '>') {
                            withStyle(SpanStyle(color = XmlTagColor)) {
                                append(">")
                            }
                            i++
                        }
                    }
                }
                else -> {
                    // Text content
                    val contentStart = i
                    while (i < text.length && text[i] != '<') i++
                    val content = text.substring(contentStart, i)
                    if (content.isNotBlank()) {
                        withStyle(SpanStyle(color = XmlContentColor)) {
                            append(content)
                        }
                    } else {
                        append(content)
                    }
                }
            }
        }
    }
}

private const val BYTES_PER_LINE = 16

@Composable
private fun BinaryFileContent(content: FileContent.Binary) {
    val bytes = content.bytes
    val lineCount = (bytes.size + BYTES_PER_LINE - 1) / BYTES_PER_LINE

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Binary file (${formatBytes(content.displaySize.toLong())})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(FileBrowserDesignSystem.Spacing.lg),
        )

        // Hex dump using LazyColumn for efficient scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FileBrowserDesignSystem.Spacing.md),
        ) {
            items(lineCount) { lineIndex ->
                HexDumpLine(bytes, lineIndex)
            }
            item {
                Spacer(modifier = Modifier.height(FileBrowserDesignSystem.Spacing.lg))
            }
        }
    }
}

@Composable
private fun HexDumpLine(bytes: ByteArray, lineIndex: Int) {
    val lineStart = lineIndex * BYTES_PER_LINE
    val lineText = remember(lineIndex) {
        buildHexLine(bytes, lineStart)
    }

    Text(
        text = lineText,
        style = MaterialTheme.typography.bodySmall.copy(
            fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
            lineHeight = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
        ),
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

private fun buildHexLine(bytes: ByteArray, lineStart: Int): String {
    val builder = StringBuilder()

    // Address column
    builder.append(String.format("%08X  ", lineStart))

    // Hex bytes - first 8 bytes
    for (i in 0 until 8) {
        val index = lineStart + i
        if (index < bytes.size) {
            builder.append(String.format("%02X ", bytes[index].toInt() and 0xFF))
        } else {
            builder.append("   ")
        }
    }

    builder.append(" ")

    // Hex bytes - second 8 bytes
    for (i in 8 until BYTES_PER_LINE) {
        val index = lineStart + i
        if (index < bytes.size) {
            builder.append(String.format("%02X ", bytes[index].toInt() and 0xFF))
        } else {
            builder.append("   ")
        }
    }

    builder.append(" ")

    // ASCII representation
    for (i in 0 until BYTES_PER_LINE) {
        val index = lineStart + i
        if (index < bytes.size) {
            val byte = bytes[index].toInt() and 0xFF
            builder.append(if (byte in 32..126) byte.toChar() else '.')
        }
    }

    return builder.toString()
}

@Composable
private fun ImageFileContent(content: FileContent.Image) {
    val context = LocalContext.current
    val isAnimated = content.mimeType == "image/gif" || content.mimeType == "image/webp"

    // Create image loader with GIF support
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FileBrowserDesignSystem.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${content.width} x ${content.height}${if (isAnimated) " (animated)" else ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = FileBrowserDesignSystem.Spacing.md),
        )

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(content.bytes)
                .build(),
            contentDescription = "Image preview",
            imageLoader = imageLoader,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PdfFileContent(content: FileContent.Pdf) {
    val pdfFile = remember(content.filePath) { File(content.filePath) }

    val pdfState = remember(content.filePath) {
        try {
            val parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(parcelFileDescriptor)
            PdfState.Ready(renderer, parcelFileDescriptor)
        } catch (e: Exception) {
            PdfState.Error(e.message ?: "Failed to open PDF")
        }
    }

    DisposableEffect(pdfState) {
        onDispose {
            if (pdfState is PdfState.Ready) {
                pdfState.renderer.close()
                pdfState.fileDescriptor.close()
            }
        }
    }

    when (pdfState) {
        is PdfState.Ready -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with PDF info
                Text(
                    text = "PDF: ${content.pageCount} pages (${formatBytes(content.sizeBytes)})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(FileBrowserDesignSystem.Spacing.lg),
                )

                // PDF pages
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = FileBrowserDesignSystem.Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(FileBrowserDesignSystem.Spacing.md),
                ) {
                    itemsIndexed(List(pdfState.renderer.pageCount) { it }) { index, _ ->
                        PdfPageCard(pdfState.renderer, index)
                    }
                    item {
                        Spacer(modifier = Modifier.height(FileBrowserDesignSystem.Spacing.lg))
                    }
                }
            }
        }
        is PdfState.Error -> {
            ErrorContent(pdfState.message)
        }
    }
}

@Composable
private fun PdfPageCard(renderer: PdfRenderer, pageIndex: Int) {
    val bitmap = remember(pageIndex) {
        val page = renderer.openPage(pageIndex)
        val scale = 2f // Render at 2x for better quality
        val bitmap = createBitmap((page.width * scale).toInt(), (page.height * scale).toInt())
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(FileBrowserDesignSystem.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Page ${pageIndex + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = FileBrowserDesignSystem.Spacing.xs),
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "PDF page ${pageIndex + 1}",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private sealed class PdfState {
    data class Ready(val renderer: PdfRenderer, val fileDescriptor: ParcelFileDescriptor) : PdfState()
    data class Error(val message: String) : PdfState()
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
