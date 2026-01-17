package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 * Full-screen PDF viewer with page navigation, pinch-to-zoom, and thumbnail strip.
 *
 * Design: Dark immersive viewer with floating controls, inspired by document
 * readers in Adobe Acrobat and Apple Books. Clean, distraction-free reading
 * experience with contextual UI that appears on interaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfData: ByteArray,
    initialPage: Int = 0,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // PDF rendering state
    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var pageCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tempFile by remember { mutableStateOf<File?>(null) }
    var metadata by remember { mutableStateOf<PdfMetadata?>(null) }

    // UI state
    var showControls by remember { mutableStateOf(true) }
    var showThumbnails by remember { mutableStateOf(false) }
    var showPageJumpDialog by remember { mutableStateOf(false) }

    // Pager state for page navigation
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    // Thumbnail list state
    val thumbnailListState = rememberLazyListState()

    // Auto-hide controls after 3 seconds of inactivity
    LaunchedEffect(showControls) {
        if (showControls) {
            kotlinx.coroutines.delay(4000)
            showControls = false
        }
    }

    // Scroll thumbnails to current page
    LaunchedEffect(pagerState.currentPage) {
        if (showThumbnails && pages.isNotEmpty()) {
            thumbnailListState.animateScrollToItem(
                index = pagerState.currentPage,
                scrollOffset = -100
            )
        }
    }

    // Load PDF pages
    LaunchedEffect(pdfData) {
        isLoading = true
        error = null

        withContext(Dispatchers.IO) {
            try {
                // Write to temp file
                val file = File(context.cacheDir, "viewer_${System.currentTimeMillis()}.pdf")
                FileOutputStream(file).use { it.write(pdfData) }
                tempFile = file

                // Open PDF renderer
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)

                pageCount = renderer.pageCount

                if (pageCount == 0) {
                    error = "PDF has no pages"
                    renderer.close()
                    fd.close()
                    return@withContext
                }

                // Extract metadata
                metadata = PdfMetadata(
                    pageCount = pageCount,
                    title = extractPdfTitle(pdfData),
                    author = null,
                    creator = null,
                    creationDate = null,
                    fileSize = pdfData.size.toLong(),
                    version = extractPdfVersion(pdfData)
                )

                // Render all pages (for smaller PDFs) or on-demand for larger ones
                val renderedPages = mutableListOf<Bitmap>()
                val maxPagesToPreload = minOf(pageCount, 20)

                for (i in 0 until maxPagesToPreload) {
                    val page = renderer.openPage(i)
                    val scale = 3f // High quality render
                    val bitmap = Bitmap.createBitmap(
                        (page.width * scale).toInt(),
                        (page.height * scale).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderedPages.add(bitmap)
                }

                pages = renderedPages
                renderer.close()
                fd.close()
                isLoading = false
            } catch (e: SecurityException) {
                error = "PDF is password protected"
                isLoading = false
            } catch (e: Exception) {
                error = e.message ?: "Failed to load PDF"
                isLoading = false
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            tempFile?.delete()
            pages.forEach { it.recycle() }
        }
    }

    // Fullscreen dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Get navigation bar padding explicitly for Dialog context
        val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)) // Deep dark background
                .padding(top = statusBarPadding, bottom = navigationBarPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls }
                    )
                }
        ) {
            when {
                isLoading -> LoadingOverlay()
                error != null -> ErrorOverlay(error!!, onDismiss)
                pages.isNotEmpty() -> {
                    // Main PDF viewer
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 2
                    ) { pageIndex ->
                        ZoomablePage(
                            bitmap = pages.getOrNull(pageIndex),
                            pageIndex = pageIndex,
                            onTap = { showControls = !showControls }
                        )
                    }

                    // Top bar with title and close
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        TopControlBar(
                            title = metadata?.title ?: "PDF Document",
                            currentPage = pagerState.currentPage + 1,
                            totalPages = pageCount,
                            onClose = onDismiss,
                            onPageJump = { showPageJumpDialog = true },
                            onDownload = onDownload,
                            onShare = { sharePdfFromViewer(context, pdfData, tempFile) }
                        )
                    }

                    // Bottom navigation bar
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        BottomNavigationBar(
                            currentPage = pagerState.currentPage,
                            totalPages = pageCount,
                            showThumbnails = showThumbnails,
                            onPreviousPage = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            onNextPage = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            onFirstPage = {
                                scope.launch { pagerState.animateScrollToPage(0) }
                            },
                            onLastPage = {
                                scope.launch { pagerState.animateScrollToPage(pageCount - 1) }
                            },
                            onToggleThumbnails = { showThumbnails = !showThumbnails }
                        )
                    }

                    // Thumbnail strip
                    AnimatedVisibility(
                        visible = showThumbnails && showControls,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp)
                    ) {
                        ThumbnailStrip(
                            pages = pages,
                            currentPage = pagerState.currentPage,
                            listState = thumbnailListState,
                            onPageSelect = { index ->
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                        )
                    }
                }
            }

            // Page jump dialog
            if (showPageJumpDialog) {
                PageJumpDialog(
                    currentPage = pagerState.currentPage + 1,
                    totalPages = pageCount,
                    onDismiss = { showPageJumpDialog = false },
                    onPageSelected = { page ->
                        scope.launch {
                            pagerState.animateScrollToPage(page - 1)
                        }
                        showPageJumpDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ZoomablePage(
    bitmap: Bitmap?,
    pageIndex: Int,
    onTap: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val minScale = 1f
    val maxScale = 5f

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { tapOffset ->
                        // Toggle between 1x and 2.5x zoom on double tap
                        if (scale > 1.5f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            // Center zoom on tap location
                            offset = Offset(
                                x = (size.width / 2f - tapOffset.x) * (scale - 1),
                                y = (size.height / 2f - tapOffset.y) * (scale - 1)
                            )
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(minScale, maxScale)

                    if (scale > 1f) {
                        val maxX = (size.width * (scale - 1)) / 2
                        val maxY = (size.height * (scale - 1)) / 2
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit
            )
        } else {
            // Placeholder for pages not yet loaded
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun TopControlBar(
    title: String,
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit,
    onPageJump: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.sm
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Title and page indicator
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "Page $currentPage of $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onPageJump() }
                )
            }

            // Action buttons
            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = Color.White
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentPage: Int,
    totalPages: Int,
    showThumbnails: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onFirstPage: () -> Unit,
    onLastPage: () -> Unit,
    onToggleThumbnails: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.md,
                    vertical = WormaCeptorDesignSystem.Spacing.md
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // First page
            IconButton(
                onClick = onFirstPage,
                enabled = currentPage > 0
            ) {
                Icon(
                    imageVector = Icons.Default.FirstPage,
                    contentDescription = "First page",
                    tint = if (currentPage > 0) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }

            // Previous page
            IconButton(
                onClick = onPreviousPage,
                enabled = currentPage > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous page",
                    tint = if (currentPage > 0) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }

            // Page indicator
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.lg,
                        vertical = WormaCeptorDesignSystem.Spacing.sm
                    )
                )
            }

            // Next page
            IconButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages - 1
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next page",
                    tint = if (currentPage < totalPages - 1) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }

            // Last page
            IconButton(
                onClick = onLastPage,
                enabled = currentPage < totalPages - 1
            ) {
                Icon(
                    imageVector = Icons.Default.LastPage,
                    contentDescription = "Last page",
                    tint = if (currentPage < totalPages - 1) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }

            // Thumbnail toggle
            IconButton(onClick = onToggleThumbnails) {
                Icon(
                    imageVector = if (showThumbnails) Icons.Default.GridOff else Icons.Default.GridOn,
                    contentDescription = if (showThumbnails) "Hide thumbnails" else "Show thumbnails",
                    tint = if (showThumbnails) MaterialTheme.colorScheme.primary else Color.White
                )
            }
        }
    }
}

@Composable
private fun ThumbnailStrip(
    pages: List<Bitmap>,
    currentPage: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onPageSelect: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.9f),
        shape = RoundedCornerShape(
            topStart = WormaCeptorDesignSystem.CornerRadius.lg,
            topEnd = WormaCeptorDesignSystem.CornerRadius.lg
        )
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            contentPadding = PaddingValues(horizontal = WormaCeptorDesignSystem.Spacing.md)
        ) {
            itemsIndexed(pages) { index, bitmap ->
                ThumbnailItem(
                    bitmap = bitmap,
                    pageNumber = index + 1,
                    isSelected = index == currentPage,
                    onClick = { onPageSelect(index) }
                )
            }
        }
    }
}

@Composable
private fun ThumbnailItem(
    bitmap: Bitmap,
    pageNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm)
                )
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Page $pageNumber",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        Text(
            text = "$pageNumber",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PageJumpDialog(
    currentPage: Int,
    totalPages: Int,
    onDismiss: () -> Unit,
    onPageSelected: (Int) -> Unit
) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Go to Page",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)
            ) {
                Text(
                    "Enter a page number (1-$totalPages)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { value ->
                        pageInput = value.filter { it.isDigit() }
                        isError = false
                    },
                    label = { Text("Page number") },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            val page = pageInput.toIntOrNull()
                            if (page != null && page in 1..totalPages) {
                                onPageSelected(page)
                            } else {
                                isError = true
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = "Please enter a valid page number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val page = pageInput.toIntOrNull()
                    if (page != null && page in 1..totalPages) {
                        onPageSelected(page)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Go", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg)
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = "Loading PDF...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ErrorOverlay(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.xl)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Failed to open PDF",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Text("Close", color = Color.White)
            }
        }
    }
}

// Utility functions

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
        } else null
    } catch (e: Exception) {
        null
    }
}

private fun sharePdfFromViewer(context: Context, pdfData: ByteArray, existingFile: File?) {
    try {
        val file = existingFile ?: run {
            val shareFile = File(context.cacheDir, "share_${System.currentTimeMillis()}.pdf")
            FileOutputStream(shareFile).use { it.write(pdfData) }
            shareFile
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
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
