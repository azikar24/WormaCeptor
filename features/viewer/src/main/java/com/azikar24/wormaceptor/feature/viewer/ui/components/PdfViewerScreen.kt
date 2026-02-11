package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem.ThemeColors
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Full-screen PDF viewer with page navigation, pinch-to-zoom, and thumbnail strip.
 *
 * Design: Dark immersive viewer with floating controls, inspired by document
 * readers in Adobe Acrobat and Apple Books. Clean, distraction-free reading
 * experience with contextual UI that appears on interaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(pdfData: ByteArray, initialPage: Int = 0, onDismiss: () -> Unit, onDownload: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
        pageCount = { pageCount },
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
                scrollOffset = -100,
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
                    version = extractPdfVersion(pdfData),
                )

                // Render all pages (for smaller PDFs) or on-demand for larger ones
                val renderedPages = mutableListOf<Bitmap>()
                val maxPagesToPreload = minOf(pageCount, 20)

                for (i in 0 until maxPagesToPreload) {
                    val page = renderer.openPage(i)
                    val scale = 3f // High quality render
                    val bitmap = createBitmap((page.width * scale).toInt(), (page.height * scale).toInt())
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
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ThemeColors.DarkBackground)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls },
                    )
                },
        ) {
            val currentError = error
            when {
                isLoading -> LoadingOverlay()
                currentError != null -> ErrorOverlay(currentError, onDismiss)
                pages.isNotEmpty() -> {
                    // Main PDF viewer
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 2,
                    ) { pageIndex ->
                        ZoomablePage(
                            bitmap = pages.getOrNull(pageIndex),
                            pageIndex = pageIndex,
                            onTap = { showControls = !showControls },
                        )
                    }

                    // Top bar with title and close
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier.align(Alignment.TopCenter),
                    ) {
                        TopControlBar(
                            title = metadata?.title ?: "PDF Document",
                            currentPage = pagerState.currentPage + 1,
                            totalPages = pageCount,
                            onClose = onDismiss,
                            onPageJump = { showPageJumpDialog = true },
                            onDownload = onDownload,
                            onShare = {
                                sharePdfFromViewer(context, pdfData, tempFile)?.let { message ->
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            },
                        )
                    }

                    // Bottom navigation bar
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.BottomCenter),
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
                            onToggleThumbnails = { showThumbnails = !showThumbnails },
                        )
                    }

                    // Thumbnail strip
                    AnimatedVisibility(
                        visible = showThumbnails && showControls,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp),
                    ) {
                        ThumbnailStrip(
                            pages = pages,
                            currentPage = pagerState.currentPage,
                            listState = thumbnailListState,
                            onPageSelect = { index ->
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
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
                    },
                )
            }

            // Snackbar host for messages
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = ThemeColors.DarkTextPrimary.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
                    contentColor = ThemeColors.DarkBackground,
                )
            }
        }
    }
}

@Composable
private fun ZoomablePage(bitmap: Bitmap?, pageIndex: Int, onTap: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val minScale = 1f
    val maxScale = 5f

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale",
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
                                y = (size.height / 2f - tapOffset.y) * (scale - 1),
                            )
                        }
                    },
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
                            y = (offset.y + pan.y).coerceIn(-maxY, maxY),
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.viewer_pdf_page_description, pageIndex + 1),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit,
            )
        } else {
            // Placeholder for pages not yet loaded
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ThemeColors.DarkSurface),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = ThemeColors.DarkTextPrimary.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
                    modifier = Modifier.size(32.dp),
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
    onShare: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ThemeColors.DarkBackground.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Close button
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.viewer_pdf_close),
                    tint = ThemeColors.DarkTextPrimary,
                )
            }

            // Title and page indicator
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.DarkTextPrimary,
                    maxLines = 1,
                )
                Text(
                    text = "Page $currentPage of $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = ThemeColors.DarkTextPrimary.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
                    modifier = Modifier.clickable { onPageJump() },
                )
            }

            // Action buttons
            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.viewer_pdf_download),
                    tint = ThemeColors.DarkTextPrimary,
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.viewer_pdf_share),
                    tint = ThemeColors.DarkTextPrimary,
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
    onToggleThumbnails: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = WormaCeptorDesignSystem.Spacing.xxl),
        color = ThemeColors.DarkBackground.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.md,
                    vertical = WormaCeptorDesignSystem.Spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // First page
            IconButton(
                onClick = onFirstPage,
                enabled = currentPage > 0,
            ) {
                Icon(
                    imageVector = Icons.Default.FirstPage,
                    contentDescription = stringResource(R.string.viewer_pdf_first_page),
                    tint = if (currentPage > 0) {
                        ThemeColors.DarkTextPrimary
                    } else {
                        ThemeColors.DarkTextPrimary.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.moderate,
                        )
                    },
                )
            }

            // Previous page
            IconButton(
                onClick = onPreviousPage,
                enabled = currentPage > 0,
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.viewer_pdf_previous_page),
                    tint = if (currentPage > 0) {
                        ThemeColors.DarkTextPrimary
                    } else {
                        ThemeColors.DarkTextPrimary.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.moderate,
                        )
                    },
                )
            }

            // Page indicator
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                color = ThemeColors.DarkTextPrimary.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
            ) {
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.DarkTextPrimary,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.lg,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                )
            }

            // Next page
            IconButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages - 1,
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.viewer_pdf_next_page),
                    tint = if (currentPage < totalPages - 1) {
                        ThemeColors.DarkTextPrimary
                    } else {
                        ThemeColors.DarkTextPrimary.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.moderate,
                        )
                    },
                )
            }

            // Last page
            IconButton(
                onClick = onLastPage,
                enabled = currentPage < totalPages - 1,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.LastPage,
                    contentDescription = stringResource(R.string.viewer_pdf_last_page),
                    tint = if (currentPage < totalPages - 1) {
                        ThemeColors.DarkTextPrimary
                    } else {
                        ThemeColors.DarkTextPrimary.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.moderate,
                        )
                    },
                )
            }

            // Thumbnail toggle
            IconButton(onClick = onToggleThumbnails) {
                Icon(
                    imageVector = if (showThumbnails) Icons.Default.GridOff else Icons.Default.GridOn,
                    contentDescription = if (showThumbnails) "Hide thumbnails" else "Show thumbnails",
                    tint = if (showThumbnails) MaterialTheme.colorScheme.primary else ThemeColors.DarkTextPrimary,
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
    onPageSelect: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ThemeColors.DarkBackground.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
        shape = RoundedCornerShape(
            topStart = WormaCeptorDesignSystem.CornerRadius.lg,
            topEnd = WormaCeptorDesignSystem.CornerRadius.lg,
        ),
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            contentPadding = PaddingValues(horizontal = WormaCeptorDesignSystem.Spacing.md),
        ) {
            itemsIndexed(pages) { index, bitmap ->
                ThumbnailItem(
                    bitmap = bitmap,
                    pageNumber = index + 1,
                    isSelected = index == currentPage,
                    onClick = { onPageSelect(index) },
                )
            }
        }
    }
}

@Composable
private fun ThumbnailItem(bitmap: Bitmap, pageNumber: Int, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "border",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                ),
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.viewer_pdf_page_description, pageNumber),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        Text(
            text = "$pageNumber",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                ThemeColors.DarkTextPrimary.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.heavy,
                )
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun PageJumpDialog(currentPage: Int, totalPages: Int, onDismiss: () -> Unit, onPageSelected: (Int) -> Unit) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Go to Page",
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Text(
                    "Enter a page number (1-$totalPages)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { value ->
                        pageInput = value.filter { it.isDigit() }
                        isError = false
                    },
                    label = { Text(stringResource(R.string.viewer_pdf_page_number)) },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Go,
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            val page = pageInput.toIntOrNull()
                            if (page != null && page in 1..totalPages) {
                                onPageSelected(page)
                            } else {
                                isError = true
                            }
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isError) {
                    Text(
                        text = "Please enter a valid page number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
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
                },
            ) {
                Text(stringResource(R.string.viewer_pdf_go), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.viewer_pdf_cancel))
            }
        },
    )
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            CircularProgressIndicator(
                color = ThemeColors.DarkTextPrimary,
                modifier = Modifier.size(WormaCeptorDesignSystem.TouchTarget.comfortable),
                strokeWidth = WormaCeptorDesignSystem.BorderWidth.bold,
            )
            Text(
                text = "Loading PDF...",
                style = MaterialTheme.typography.bodyLarge,
                color = ThemeColors.DarkTextPrimary.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
            )
        }
    }
}

@Composable
private fun ErrorOverlay(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.xl),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.viewer_pdf_error),
                modifier = Modifier.size(WormaCeptorDesignSystem.TouchTarget.large),
                tint = MaterialTheme.colorScheme.error,
            )

            Text(
                text = "Failed to open PDF",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ThemeColors.DarkTextPrimary,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ThemeColors.DarkTextPrimary.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemeColors.DarkTextPrimary.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                ),
            ) {
                Text(stringResource(R.string.viewer_pdf_close), color = ThemeColors.DarkTextPrimary)
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
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun sharePdfFromViewer(context: Context, pdfData: ByteArray, existingFile: File?): String? {
    return try {
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
        null // Success - share sheet handles it
    } catch (e: Exception) {
        "Failed to share PDF: ${e.message}"
    }
}
