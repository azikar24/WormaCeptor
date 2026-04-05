package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.pdf.PdfBottomNavigationBar
import com.azikar24.wormaceptor.feature.viewer.ui.components.pdf.PdfErrorOverlay
import com.azikar24.wormaceptor.feature.viewer.ui.components.pdf.PdfLoadingOverlay
import com.azikar24.wormaceptor.feature.viewer.ui.components.pdf.PdfPageJumpDialog
import com.azikar24.wormaceptor.feature.viewer.ui.components.pdf.PdfThumbnailStrip
import com.azikar24.wormaceptor.feature.viewer.ui.components.pdf.PdfTopControlBar
import com.azikar24.wormaceptor.feature.viewer.ui.components.pdf.ZoomablePage
import com.azikar24.wormaceptor.feature.viewer.vm.PdfViewerError
import com.azikar24.wormaceptor.feature.viewer.vm.PdfViewerViewEffect
import com.azikar24.wormaceptor.feature.viewer.vm.PdfViewerViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.PdfViewerViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.PdfViewerViewState
import kotlinx.coroutines.launch

/**
 * Full-screen PDF viewer with page navigation, pinch-to-zoom, and thumbnail strip.
 *
 * Renders as a dark immersive dialog with floating controls. All business logic
 * is managed by [PdfViewerViewModel]; this composable is a pure renderer.
 */
@Composable
fun PdfViewerScreen(
    pdfData: ByteArray,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    initialPage: Int = 0,
) {
    val context = LocalContext.current
    val viewModel: PdfViewerViewModel = viewModel()

    LaunchedEffect(pdfData) {
        viewModel.sendEvent(PdfViewerViewEvent.LoadPdf(pdfData, initialPage, context.cacheDir))
    }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is PdfViewerViewEffect.Dismiss -> onDismiss()
                is PdfViewerViewEffect.Download -> onDownload()
                is PdfViewerViewEffect.SharePdf -> {
                    sharePdf(context, effect.pdfData, effect.tempFile)?.let { message ->
                        scope.launch { snackBarHostState.showSnackbar(message) }
                    }
                }
                is PdfViewerViewEffect.ShowSnackBar -> {
                    scope.launch { snackBarHostState.showSnackbar(effect.message) }
                }
            }
        },
    ) { state, onEvent ->
        PdfViewerContent(
            state = state,
            onEvent = onEvent,
            snackBarHostState = snackBarHostState,
            onDismiss = onDismiss,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun PdfViewerContent(
    state: PdfViewerViewState,
    onEvent: (PdfViewerViewEvent) -> Unit,
    snackBarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { state.pageCount },
    )

    val thumbnailListState = rememberLazyListState()

    // Sync pager → ViewModel
    LaunchedEffect(pagerState.currentPage) {
        onEvent(PdfViewerViewEvent.PageChanged(pagerState.currentPage))
    }

    // Sync ViewModel → pager (for jump-to-page)
    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage && state.currentPage in 0 until state.pageCount) {
            pagerState.animateScrollToPage(state.currentPage)
        }
    }

    // Sync thumbnail scroll to current page
    LaunchedEffect(state.currentPage) {
        if (state.showThumbnails && state.pages.isNotEmpty()) {
            thumbnailListState.animateScrollToItem(
                index = state.currentPage,
                scrollOffset = -100,
            )
        }
    }

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
                .background(darkColors.background)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onEvent(PdfViewerViewEvent.ToggleControls) },
                    )
                },
        ) {
            val errorMessage = resolveErrorMessage(state.error)
            when {
                state.isLoading -> PdfLoadingOverlay()
                errorMessage != null -> PdfErrorOverlay(errorMessage, onDismiss)
                state.pages.isNotEmpty() -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 2,
                    ) { pageIndex ->
                        ZoomablePage(
                            bitmap = state.pages.getOrNull(pageIndex),
                            pageNumber = pageIndex + 1,
                            onTap = { onEvent(PdfViewerViewEvent.ToggleControls) },
                        )
                    }

                    // Top bar
                    AnimatedVisibility(
                        visible = state.showControls,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier.align(Alignment.TopCenter),
                    ) {
                        PdfTopControlBar(
                            title = state.metadata?.title ?: stringResource(R.string.viewer_pdf_document),
                            currentPage = state.currentPage + 1,
                            totalPages = state.pageCount,
                            onClose = { onEvent(PdfViewerViewEvent.Dismiss) },
                            onPageJump = { onEvent(PdfViewerViewEvent.ShowPageJumpDialog) },
                            onDownload = { onEvent(PdfViewerViewEvent.Download) },
                            onShare = { onEvent(PdfViewerViewEvent.Share) },
                        )
                    }

                    // Bottom bar
                    AnimatedVisibility(
                        visible = state.showControls,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        PdfBottomNavigationBar(
                            currentPage = state.currentPage,
                            totalPages = state.pageCount,
                            showThumbnails = state.showThumbnails,
                            onPreviousPage = { onEvent(PdfViewerViewEvent.GoToPreviousPage) },
                            onNextPage = { onEvent(PdfViewerViewEvent.GoToNextPage) },
                            onFirstPage = { onEvent(PdfViewerViewEvent.GoToFirstPage) },
                            onLastPage = { onEvent(PdfViewerViewEvent.GoToLastPage) },
                            onToggleThumbnails = { onEvent(PdfViewerViewEvent.ToggleThumbnails) },
                        )
                    }

                    // Thumbnail strip
                    AnimatedVisibility(
                        visible = state.showThumbnails && state.showControls,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp),
                    ) {
                        PdfThumbnailStrip(
                            pages = state.pages,
                            currentPage = state.currentPage,
                            listState = thumbnailListState,
                            onPageSelect = { index ->
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                        )
                    }
                }
            }

            // Page jump dialog
            if (state.showPageJumpDialog) {
                PdfPageJumpDialog(
                    currentPage = state.currentPage + 1,
                    totalPages = state.pageCount,
                    onDismiss = { onEvent(PdfViewerViewEvent.DismissPageJumpDialog) },
                    onPageSelected = { page -> onEvent(PdfViewerViewEvent.JumpToPage(page - 1)) },
                )
            }

            // Snackbar host
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
                    contentColor = darkColors.background,
                )
            }
        }
    }
}

@Composable
private fun resolveErrorMessage(error: PdfViewerError?): String? {
    return when (error) {
        is PdfViewerError.NoPages -> stringResource(R.string.viewer_pdf_no_pages)
        is PdfViewerError.PasswordProtected -> stringResource(R.string.viewer_pdf_password_message)
        is PdfViewerError.LoadFailed -> error.message ?: stringResource(R.string.viewer_pdf_load_failed)
        null -> null
    }
}

@Preview(showBackground = true)
@Composable
private fun PdfViewerScreenPreview() {
    WormaCeptorTheme {
        PdfViewerScreen(
            pdfData = byteArrayOf(),
            initialPage = 0,
            onDismiss = {},
            onDownload = {},
        )
    }
}
