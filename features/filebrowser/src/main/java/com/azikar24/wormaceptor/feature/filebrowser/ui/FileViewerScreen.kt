package com.azikar24.wormaceptor.feature.filebrowser.ui

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.azikar24.wormaceptor.core.ui.components.CardStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.feature.filebrowser.R
import com.azikar24.wormaceptor.feature.filebrowser.ui.util.BytesPerLine
import com.azikar24.wormaceptor.feature.filebrowser.ui.util.buildHexLine
import com.azikar24.wormaceptor.feature.filebrowser.ui.util.highlightJson
import com.azikar24.wormaceptor.feature.filebrowser.ui.util.highlightXml
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewEvent
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Renders the selected file's content based on its [FileContent] type. */
@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(
    state: FileBrowserViewState,
    onEvent: (FileBrowserViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filePath = state.selectedFile ?: return
    val content = state.fileContent ?: return
    val fileName = File(filePath).name

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(fileName) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(FileBrowserViewEvent.CloseFileViewer) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.filebrowser_back),
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
                .padding(padding)
                .navigationBarsPadding(),
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
                        stringResource(
                            R.string.filebrowser_file_too_large,
                            formatBytes(content.sizeBytes),
                            formatBytes(content.maxSize),
                        ),
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
            .padding(WormaCeptorTokens.Spacing.lg),
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
    val validJsonDesc = stringResource(R.string.filebrowser_valid_json)
    val invalidJsonDesc = stringResource(R.string.filebrowser_invalid_json)
    val validText = stringResource(R.string.filebrowser_valid)
    val invalidText = stringResource(R.string.filebrowser_invalid)
    val validColor = WormaCeptorTokens.semantic().success
    val invalidColor = WormaCeptorTokens.semantic().warning

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with validity indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.filebrowser_json_lines, content.lineCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (content.isValid) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = if (content.isValid) validJsonDesc else invalidJsonDesc,
                tint = if (content.isValid) validColor else invalidColor,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
            )
            Text(
                text = if (content.isValid) validText else invalidText,
                style = MaterialTheme.typography.labelSmall,
                color = if (content.isValid) validColor else invalidColor,
                modifier = Modifier.padding(start = WormaCeptorTokens.Spacing.xs),
            )
        }

        // JSON content with syntax highlighting
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = WormaCeptorTokens.Spacing.lg),
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
    val validXmlDesc = stringResource(R.string.filebrowser_valid_xml)
    val invalidXmlDesc = stringResource(R.string.filebrowser_invalid_xml)
    val validText = stringResource(R.string.filebrowser_valid)
    val invalidText = stringResource(R.string.filebrowser_invalid)
    val validColor = WormaCeptorTokens.semantic().success
    val invalidColor = WormaCeptorTokens.semantic().warning

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with validity indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.filebrowser_xml_lines, content.lineCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (content.isValid) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = if (content.isValid) validXmlDesc else invalidXmlDesc,
                tint = if (content.isValid) validColor else invalidColor,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
            )
            Text(
                text = if (content.isValid) validText else invalidText,
                style = MaterialTheme.typography.labelSmall,
                color = if (content.isValid) validColor else invalidColor,
                modifier = Modifier.padding(start = WormaCeptorTokens.Spacing.xs),
            )
        }

        // XML content with syntax highlighting
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = WormaCeptorTokens.Spacing.lg),
        ) {
            Text(
                text = highlightXml(content.formattedContent),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun BinaryFileContent(content: FileContent.Binary) {
    val bytes = content.bytes
    val lineCount = (bytes.size + BytesPerLine - 1) / BytesPerLine

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = stringResource(R.string.filebrowser_binary_file, formatBytes(content.displaySize.toLong())),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
        )

        // Hex dump using LazyColumn for efficient scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = WormaCeptorTokens.Spacing.md),
        ) {
            items(lineCount) { lineIndex ->
                HexDumpLine(bytes, lineIndex)
            }
            item {
                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))
            }
        }
    }
}

@Composable
private fun HexDumpLine(
    bytes: ByteArray,
    lineIndex: Int,
) {
    val lineStart = lineIndex * BytesPerLine
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

    val dimensionsText = if (isAnimated) {
        stringResource(R.string.filebrowser_image_dimensions_animated, content.width, content.height)
    } else {
        stringResource(R.string.filebrowser_image_dimensions, content.width, content.height)
    }
    val imagePreviewDesc = stringResource(R.string.filebrowser_image_preview)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WormaCeptorTokens.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dimensionsText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.md),
        )

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(content.bytes)
                .build(),
            contentDescription = imagePreviewDesc,
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
            try {
                val renderer = PdfRenderer(parcelFileDescriptor)
                PdfState.Ready(renderer, parcelFileDescriptor)
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                parcelFileDescriptor.close()
                PdfState.Error(e.message ?: "Failed to open PDF")
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
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
                    text = stringResource(
                        R.string.filebrowser_pdf_info,
                        content.pageCount,
                        formatBytes(content.sizeBytes),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                )

                // PDF pages
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = WormaCeptorTokens.Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
                ) {
                    items(pdfState.renderer.pageCount) { index ->
                        PdfPageCard(pdfState.renderer, pdfState.mutex, index)
                    }
                    item {
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))
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
private fun PdfPageCard(
    renderer: PdfRenderer,
    mutex: Mutex,
    pageIndex: Int,
) {
    val pageNumber = pageIndex + 1

    @Suppress("InjectDispatcher")
    val bitmap by produceState<Bitmap?>(initialValue = null, pageIndex) {
        value = withContext(Dispatchers.IO) {
            mutex.withLock {
                val page = renderer.openPage(pageIndex)
                val scale = 2f
                val bmp = createBitmap((page.width * scale).toInt(), (page.height * scale).toInt())
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bmp
            }
        }
    }

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        style = CardStyle.Elevated,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.filebrowser_page_number, pageNumber),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.xs),
            )
            val currentBitmap = bitmap
            if (currentBitmap != null) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.filebrowser_pdf_page, pageNumber),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WormaCeptorTokens.ComponentSize.textAreaHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                    )
                }
            }
        }
    }
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
