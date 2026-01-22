package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * PDF metadata extracted from the document
 */
data class PdfMetadata(
    val pageCount: Int,
    val title: String?,
    val author: String?,
    val creator: String?,
    val creationDate: String?,
    val fileSize: Long,
    val version: String?,
    val isPasswordProtected: Boolean = false,
)

/**
 * State representing the PDF loading status
 */
sealed class PdfLoadState {
    data object Loading : PdfLoadState()
    data class Success(val thumbnail: Bitmap, val metadata: PdfMetadata) : PdfLoadState()
    data class Error(val message: String) : PdfLoadState()
    data class PasswordProtected(val metadata: PdfMetadata) : PdfLoadState()
}

/**
 * Preview card for PDF documents in the response body section.
 * Shows a thumbnail of the first page along with metadata and action buttons.
 *
 * Design: Modern document card with subtle depth, clean typography,
 * and a refined action bar. Inspired by document viewers in Notion and Figma.
 */
@Composable
fun PdfPreviewCard(
    pdfData: ByteArray,
    contentType: String?,
    onFullscreen: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var loadState by remember { mutableStateOf<PdfLoadState>(PdfLoadState.Loading) }
    var tempFile by remember { mutableStateOf<File?>(null) }

    // Load PDF on mount
    LaunchedEffect(pdfData) {
        loadState = PdfLoadState.Loading
        withContext(Dispatchers.IO) {
            try {
                // Write to temp file
                val file = File(context.cacheDir, "preview_${System.currentTimeMillis()}.pdf")
                FileOutputStream(file).use { it.write(pdfData) }
                tempFile = file

                // Open PDF renderer
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)

                val pageCount = renderer.pageCount
                if (pageCount == 0) {
                    loadState = PdfLoadState.Error("PDF has no pages")
                    renderer.close()
                    fd.close()
                    return@withContext
                }

                // Render first page as thumbnail
                val page = renderer.openPage(0)
                val scale = 2f // Higher resolution thumbnail
                val bitmap = Bitmap.createBitmap(
                    (page.width * scale).toInt(),
                    (page.height * scale).toInt(),
                    Bitmap.Config.ARGB_8888,
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val metadata = PdfMetadata(
                    pageCount = pageCount,
                    title = extractPdfTitle(pdfData),
                    author = null,
                    creator = null,
                    creationDate = null,
                    fileSize = pdfData.size.toLong(),
                    version = extractPdfVersion(pdfData),
                )

                renderer.close()
                fd.close()

                loadState = PdfLoadState.Success(bitmap, metadata)
            } catch (e: SecurityException) {
                // Password protected PDF
                loadState = PdfLoadState.PasswordProtected(
                    PdfMetadata(
                        pageCount = 0,
                        title = null,
                        author = null,
                        creator = null,
                        creationDate = null,
                        fileSize = pdfData.size.toLong(),
                        version = extractPdfVersion(pdfData),
                        isPasswordProtected = true,
                    ),
                )
            } catch (e: Exception) {
                loadState = PdfLoadState.Error(e.message ?: "Failed to load PDF")
            }
        }
    }

    // Cleanup temp file on dispose
    DisposableEffect(Unit) {
        onDispose {
            tempFile?.delete()
        }
    }

    // Card with gradient overlay effect
    val surfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = loadState is PdfLoadState.Success) { onFullscreen() },
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
        ),
    ) {
        when (val state = loadState) {
            is PdfLoadState.Loading -> LoadingContent()
            is PdfLoadState.Success -> SuccessContent(
                thumbnail = state.thumbnail,
                metadata = state.metadata,
                pdfData = pdfData,
                tempFile = tempFile,
                onFullscreen = onFullscreen,
                onDownload = onDownload,
            )
            is PdfLoadState.Error -> ErrorContent(message = state.message)
            is PdfLoadState.PasswordProtected -> PasswordProtectedContent(
                metadata = state.metadata,
                pdfData = pdfData,
                tempFile = tempFile,
                onDownload = onDownload,
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Rendering PDF...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SuccessContent(
    thumbnail: Bitmap,
    metadata: PdfMetadata,
    pdfData: ByteArray,
    tempFile: File?,
    onFullscreen: () -> Unit,
    onDownload: () -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Thumbnail Section with elegant overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // Slightly portrait for PDF pages
                .clip(
                    RoundedCornerShape(
                        topStart = WormaCeptorDesignSystem.CornerRadius.md,
                        topEnd = WormaCeptorDesignSystem.CornerRadius.md,
                    ),
                ),
        ) {
            // PDF page thumbnail
            Image(
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = "PDF Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Subtle vignette overlay at bottom for better text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                            ),
                        ),
                    ),
            )

            // Page count badge - top right
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                shape = WormaCeptorDesignSystem.Shapes.chip,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = WormaCeptorDesignSystem.Elevation.sm,
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Page count",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${metadata.pageCount} page${if (metadata.pageCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Fullscreen hint overlay - center
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Open fullscreen",
                    modifier = Modifier
                        .padding(WormaCeptorDesignSystem.Spacing.md)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Metadata Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            // Title or filename
            Text(
                text = metadata.title ?: "PDF Document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                MetadataChip(
                    icon = Icons.Default.Description,
                    text = formatFileSize(metadata.fileSize),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                metadata.version?.let { version ->
                    MetadataChip(
                        icon = null,
                        text = "PDF $version",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                metadata.author?.let { author ->
                    MetadataChip(
                        icon = Icons.Default.Person,
                        text = author,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                // Open button - primary action
                Button(
                    onClick = onFullscreen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    contentPadding = PaddingValues(
                        horizontal = WormaCeptorDesignSystem.Spacing.lg,
                        vertical = WormaCeptorDesignSystem.Spacing.md,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Open PDF fullscreen",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                    Text("Open", fontWeight = FontWeight.SemiBold)
                }

                // Download button
                OutlinedButton(
                    onClick = onDownload,
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    border = androidx.compose.foundation.BorderStroke(
                        WormaCeptorDesignSystem.BorderWidth.regular,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
                    contentPadding = PaddingValues(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(18.dp),
                    )
                }

                // Share button
                OutlinedButton(
                    onClick = { sharePdf(context, pdfData, tempFile) },
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    border = androidx.compose.foundation.BorderStroke(
                        WormaCeptorDesignSystem.BorderWidth.regular,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
                    contentPadding = PaddingValues(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(icon: ImageVector?, text: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = text,
                modifier = Modifier.size(14.dp),
                tint = tint.copy(alpha = 0.7f),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error loading PDF",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = "Failed to load PDF",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun PasswordProtectedContent(
    metadata: PdfMetadata,
    pdfData: ByteArray,
    tempFile: File?,
    onDownload: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WormaCeptorDesignSystem.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        // Lock icon with subtle background
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password protected",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            Text(
                text = "Password Protected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "This PDF requires a password to view",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // File size info
        MetadataChip(
            icon = Icons.Default.Description,
            text = formatFileSize(metadata.fileSize),
            tint = MaterialTheme.colorScheme.tertiary,
        )

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            OutlinedButton(
                onClick = onDownload,
                shape = WormaCeptorDesignSystem.Shapes.button,
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download PDF",
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text("Download")
            }

            OutlinedButton(
                onClick = { sharePdf(context, pdfData, tempFile) },
                shape = WormaCeptorDesignSystem.Shapes.button,
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share PDF",
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text("Share")
            }
        }
    }
}

// Utility functions

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    val digitGroup = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroup.toDouble()), units[digitGroup])
}

private fun extractPdfTitle(data: ByteArray): String? {
    return try {
        val text = data.decodeToString(throwOnInvalidSequence = false)
        val titleRegex = """/Title\s*\(([^)]+)\)""".toRegex()
        titleRegex.find(text)?.groupValues?.getOrNull(1)
    } catch (e: Exception) {
        null
    }
}

private fun extractPdfVersion(data: ByteArray): String? {
    return try {
        val header = data.take(20).toByteArray().decodeToString()
        if (header.startsWith("%PDF-")) {
            header.substring(5).takeWhile { it.isDigit() || it == '.' }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun sharePdf(context: Context, pdfData: ByteArray, existingFile: File?) {
    try {
        // Use existing file or create new one
        val file = existingFile ?: run {
            val newFile = File(context.cacheDir, "WormaCeptor_${System.currentTimeMillis()}.pdf")
            newFile.outputStream().use { it.write(pdfData) }
            newFile
        }

        // Get URI via FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.wormaceptor.fileprovider",
            file,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Checks if the response body appears to be a PDF document
 */
fun isPdfContent(contentType: String?, bodyBytes: ByteArray?): Boolean {
    // Check content type
    if (contentType?.contains("pdf", ignoreCase = true) == true) {
        return true
    }

    // Check magic bytes
    if (bodyBytes != null && bodyBytes.size >= 5) {
        val header = bodyBytes.take(5).toByteArray().decodeToString()
        return header == "%PDF-"
    }

    return false
}
