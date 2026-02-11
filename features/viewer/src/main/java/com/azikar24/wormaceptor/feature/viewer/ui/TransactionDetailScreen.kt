package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.core.ui.util.isContentTooLargeForClipboard
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.FullscreenImageViewer
import com.azikar24.wormaceptor.feature.viewer.ui.components.ImageMetadata
import com.azikar24.wormaceptor.feature.viewer.ui.components.ImagePreviewCard
import com.azikar24.wormaceptor.feature.viewer.ui.components.PdfPreviewCard
import com.azikar24.wormaceptor.feature.viewer.ui.components.PdfViewerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.components.TextWithStartEllipsis
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.BodyParsingUtils
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.ContentTypeChip
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.FormDataView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.JsonTreeView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.MultipartView
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.XmlTreeView
import com.azikar24.wormaceptor.feature.viewer.ui.components.detectImageFormat
import com.azikar24.wormaceptor.feature.viewer.ui.components.extractImageMetadata
import com.azikar24.wormaceptor.feature.viewer.ui.components.gestures.SwipeBackContainer
import com.azikar24.wormaceptor.feature.viewer.ui.components.isImageContentType
import com.azikar24.wormaceptor.feature.viewer.ui.components.isImageData
import com.azikar24.wormaceptor.feature.viewer.ui.components.isPdfContent
import com.azikar24.wormaceptor.feature.viewer.ui.components.saveImageToGallery
import com.azikar24.wormaceptor.feature.viewer.ui.components.shareImage
import com.azikar24.wormaceptor.feature.viewer.ui.theme.syntaxColors
import com.azikar24.wormaceptor.feature.viewer.ui.util.extractUrlPath
import com.azikar24.wormaceptor.feature.viewer.ui.util.getFileInfoForContentType
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareAsFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Transaction detail screen with swipe navigation.
 *
 * UX Behavior:
 * - Swipe on toolbar/topBar area: Switch between transactions
 * - Swipe on content area: Switch between Overview/Request/Response tabs
 *
 * @param transactionIds List of all transaction IDs for pager navigation
 * @param initialTransactionIndex Initial index to display
 * @param getTransaction Function to get transaction details by UUID
 * @param onBack Callback to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailPagerScreen(
    transactionIds: List<UUID>,
    initialTransactionIndex: Int,
    getTransaction: suspend (UUID) -> NetworkTransaction?,
    onBack: () -> Unit,
) {
    val view = LocalView.current

    // Current transaction index state with direction tracking
    var currentTransactionIndex by remember {
        mutableIntStateOf(initialTransactionIndex.coerceIn(0, (transactionIds.size - 1).coerceAtLeast(0)))
    }
    var navigationDirection by remember { mutableIntStateOf(0) } // -1 = prev, 1 = next, 0 = none

    // Current transaction data
    val currentTransactionId = transactionIds.getOrNull(currentTransactionIndex)
    var transaction by remember(currentTransactionId) { mutableStateOf<NetworkTransaction?>(null) }
    var isLoading by remember(currentTransactionId) { mutableStateOf(true) }

    // Load transaction data when index changes
    LaunchedEffect(currentTransactionId) {
        if (currentTransactionId != null) {
            isLoading = true
            transaction = getTransaction(currentTransactionId)
            isLoading = false
        }
    }

    // Navigation functions for transactions
    val canNavigatePrev = currentTransactionIndex > 0
    val canNavigateNext = currentTransactionIndex < transactionIds.size - 1

    fun navigateToPrevTransaction() {
        if (canNavigatePrev) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            navigationDirection = -1
            currentTransactionIndex--
        }
    }

    fun navigateToNextTransaction() {
        if (canNavigateNext) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            navigationDirection = 1
            currentTransactionIndex++
        }
    }

    // Smooth animation config
    val animDuration = 250
    val slideOffset = 100

    SwipeBackContainer(
        onBack = onBack,
        enabled = currentTransactionIndex == 0, // Only enable swipe-back on first transaction
    ) {
        // Smooth directional slide transition
        AnimatedContent(
            targetState = currentTransactionIndex to transaction,
            transitionSpec = {
                val slideSpec = tween<IntOffset>(animDuration, easing = FastOutSlowInEasing)
                if (navigationDirection >= 0) {
                    // Going forward (next) - content slides in from right
                    slideInHorizontally(slideSpec) { slideOffset } togetherWith
                        slideOutHorizontally(slideSpec) { -slideOffset }
                } else {
                    // Going backward (prev) - content slides in from left
                    slideInHorizontally(slideSpec) { -slideOffset } togetherWith
                        slideOutHorizontally(slideSpec) { slideOffset }
                }
            },
            label = "transaction_transition",
        ) { (_, currentTransaction) ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (currentTransaction != null) {
                TransactionDetailContent(
                    transaction = currentTransaction,
                    onBack = onBack,
                    onNavigatePrevTransaction = ::navigateToPrevTransaction,
                    onNavigateNextTransaction = ::navigateToNextTransaction,
                    canNavigatePrev = canNavigatePrev,
                    canNavigateNext = canNavigateNext,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Transaction not found",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

/**
 * Original TransactionDetailScreen - kept for backward compatibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(transaction: NetworkTransaction, onBack: () -> Unit) {
    SwipeBackContainer(onBack = onBack) {
        TransactionDetailContent(
            transaction = transaction,
            onBack = onBack,
        )
    }
}

/**
 * Transaction detail content - the actual content without swipe-back wrapper
 *
 * UX Behavior:
 * - Swipe on toolbar/topBar area: Switch between transactions
 * - Swipe on content area (HorizontalPager): Switch between Overview/Request/Response tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailContent(
    transaction: NetworkTransaction,
    onBack: () -> Unit,
    onNavigatePrevTransaction: () -> Unit = {},
    onNavigateNextTransaction: () -> Unit = {},
    canNavigatePrev: Boolean = false,
    canNavigateNext: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tabs = listOf("Overview", "Request", "Response")
    val snackbarHostState = remember { SnackbarHostState() }

    // Tab pager state for content swipe
    val tabPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size },
    )

    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    // Debounce search query for performance
    LaunchedEffect(searchQuery) {
        if (searchQuery.isEmpty()) {
            debouncedSearchQuery = ""
        } else {
            delay(150)
            debouncedSearchQuery = searchQuery
        }
    }

    // Search navigation state
    var currentMatchIndex by remember(debouncedSearchQuery, tabPagerState.currentPage) { mutableIntStateOf(0) }
    var matchCount by remember(debouncedSearchQuery, tabPagerState.currentPage) { mutableIntStateOf(0) }

    // Check if current tab has searchable content
    val requestHasContent = transaction.request.headers.isNotEmpty() || transaction.request.bodyRef != null
    val responseHasContent = (transaction.response?.headers?.isNotEmpty() == true) ||
        (transaction.response?.bodyRef != null)
    val currentTabHasContent = when (tabPagerState.currentPage) {
        1 -> requestHasContent
        2 -> responseHasContent
        else -> false
    }

    LaunchedEffect(showSearch) {
        if (showSearch) {
            focusRequester.requestFocus()
        }
    }

    // Close search when tabs change
    LaunchedEffect(tabPagerState.currentPage) {
        if (showSearch) {
            showSearch = false
            searchQuery = ""
            debouncedSearchQuery = ""
        }
    }

    val title = remember(transaction.request.url) {
        extractUrlPath(transaction.request.url)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                // Swipeable TopAppBar for transaction navigation
                SwipeableTopBar(
                    onSwipeLeft = onNavigateNextTransaction,
                    onSwipeRight = onNavigatePrevTransaction,
                    canSwipeLeft = canNavigateNext,
                    canSwipeRight = canNavigatePrev,
                ) {
                    TopAppBar(
                        title = {
                            TextWithStartEllipsis(
                                text = title,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.viewer_transaction_detail_back),
                                )
                            }
                        },
                        actions = {
                            if (showSearch) {
                                IconButton(
                                    onClick = {
                                        showSearch = false
                                        searchQuery = ""
                                        debouncedSearchQuery = ""
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(
                                            R.string.viewer_transaction_detail_close_search,
                                        ),
                                    )
                                }
                            } else if (tabPagerState.currentPage > 0 && currentTabHasContent) {
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            R.string.viewer_transaction_detail_search,
                                        ),
                                    )
                                }
                            }

                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.viewer_transaction_detail_options),
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.viewer_transaction_detail_copy_as_text),
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        copyToClipboard(context, "Transaction", generateTextSummary(transaction))
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.viewer_transaction_detail_copy_as_curl),
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        copyToClipboard(context, "cURL", generateCurlCommand(transaction))
                                    },
                                )
                                WormaCeptorDivider()
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.viewer_transaction_detail_share_as_json),
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        val exportManager =
                                            com.azikar24.wormaceptor.feature.viewer.export.ExportManager(
                                                context,
                                            )
                                        scope.launch {
                                            exportManager.exportTransactions(listOf(transaction))
                                        }
                                    },
                                )
                            }
                        },
                    )
                }
                // Tab row with click support (swipe is handled by the content pager)
                TabRow(
                    selectedTabIndex = tabPagerState.currentPage,
                ) {
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = tabPagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    tabPagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (tabPagerState.currentPage == index) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.ime),
            ) {
                // Inline search bar in content area
                AnimatedVisibility(visible = showSearch) {
                    WormaCeptorSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = stringResource(R.string.viewer_transaction_detail_search_placeholder),
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                                vertical = WormaCeptorDesignSystem.Spacing.sm,
                            ),
                    )
                }

                // HorizontalPager for tab content - swipe here switches between Overview/Request/Response
                HorizontalPager(
                    state = tabPagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                ) { page ->
                    when (page) {
                        0 -> OverviewTab(transaction, Modifier.fillMaxSize())
                        1 -> RequestTab(
                            transaction = transaction,
                            searchQuery = debouncedSearchQuery,
                            currentMatchIndex = currentMatchIndex,
                            onMatchCountChanged = { matchCount = it },
                            isSearchActive = showSearch,
                            modifier = Modifier.fillMaxSize(),
                        )

                        2 -> ResponseTab(
                            transaction = transaction,
                            searchQuery = debouncedSearchQuery,
                            currentMatchIndex = currentMatchIndex,
                            onMatchCountChanged = { matchCount = it },
                            isSearchActive = showSearch,
                            onShowMessage = { message ->
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // Search Navigation Controllers in Bottom Right - show immediately when search is active
            AnimatedVisibility(
                visible = showSearch && debouncedSearchQuery.isNotEmpty(),
                enter = fadeIn(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
                ) + scaleIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
                exit = fadeOut(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.ultraFast),
                ) + scaleOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.ultraFast)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .imePadding()
                    .padding(bottom = WormaCeptorDesignSystem.Spacing.xxl, end = WormaCeptorDesignSystem.Spacing.lg),
            ) {
                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.pill,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    shadowElevation = WormaCeptorDesignSystem.Elevation.lg,
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.md,
                            vertical = WormaCeptorDesignSystem.Spacing.sm,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (matchCount > 0) {
                            Text(
                                text = "${currentMatchIndex + 1}/$matchCount",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(end = WormaCeptorDesignSystem.Spacing.xs),
                            )
                            IconButton(
                                onClick = {
                                    currentMatchIndex = (currentMatchIndex - 1 + matchCount) % matchCount
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    stringResource(R.string.viewer_search_previous_match),
                                )
                            }
                            IconButton(
                                onClick = {
                                    currentMatchIndex = (currentMatchIndex + 1) % matchCount
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.viewer_search_next_match))
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.viewer_search_no_matches),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Swipeable container for the TopAppBar that handles horizontal swipes
 * to navigate between transactions.
 */
@Composable
private fun SwipeableTopBar(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    canSwipeLeft: Boolean,
    canSwipeRight: Boolean,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(canSwipeLeft, canSwipeRight) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = size.width * 0.15f
                        when {
                            dragOffset < -threshold && canSwipeLeft -> {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeLeft()
                            }

                            dragOffset > threshold && canSwipeRight -> {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeRight()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    },
                )
            },
    ) {
        content()
    }
}

@Composable
private fun OverviewTab(transaction: NetworkTransaction, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        // Status & Timing Card with Timeline
        EnhancedOverviewCard(
            title = stringResource(R.string.viewer_overview_timing),
            icon = Icons.Default.Schedule,
            iconTint = MaterialTheme.colorScheme.tertiary,
        ) {
            DetailRow("URL", transaction.request.url)
            DetailRow("Method", transaction.request.method)
            DetailRow("Status", transaction.status.name)
            DetailRow("Response Code", transaction.response?.code?.toString() ?: "-")
            DetailRow("Duration", formatDuration(transaction.durationMs))
            DetailRow(
                "Timestamp",
                java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault(),
                ).format(java.util.Date(transaction.timestamp)),
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Visual Timeline
            TransactionTimeline(
                durationMs = transaction.durationMs ?: 0,
                hasResponse = transaction.response != null,
            )
        }

        // Security Card with Badge
        EnhancedOverviewCard(
            title = stringResource(R.string.viewer_overview_security),
            icon = Icons.Default.Security,
            iconTint = MaterialTheme.colorScheme.secondary,
        ) {
            DetailRow("Protocol", transaction.response?.protocol ?: "Unknown")

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Enhanced SSL/TLS Badge
            val isSsl = transaction.response?.tlsVersion != null
            SslBadge(
                isSsl = isSsl,
                tlsVersion = transaction.response?.tlsVersion,
            )
        }

        // Data Transfer Card
        EnhancedOverviewCard(
            title = stringResource(R.string.viewer_overview_data_transfer),
            icon = Icons.Default.Storage,
            iconTint = MaterialTheme.colorScheme.primary,
        ) {
            val reqSize = transaction.request.bodySize
            val resSize = transaction.response?.bodySize ?: 0
            val totalSize = reqSize + resSize

            DetailRow("Request Size", formatBytes(reqSize))
            DetailRow("Response Size", formatBytes(resSize))
            DetailRow("Total Transfer", formatBytes(totalSize))
        }

        // Extensions Card - only shown when extensions exist
        if (transaction.extensions.isNotEmpty()) {
            EnhancedOverviewCard(
                title = stringResource(R.string.viewer_overview_extensions),
                icon = Icons.Default.Extension,
                iconTint = MaterialTheme.colorScheme.tertiary,
            ) {
                transaction.extensions.forEach { (key, value) ->
                    DetailRow(key, value)
                }
            }
        }
    }
}

@Composable
private fun TransactionTimeline(durationMs: Long, hasResponse: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .border(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .padding(WormaCeptorDesignSystem.Spacing.md),
    ) {
        Text(
            text = "Request/Response Timeline",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.xs),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Request Phase
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
                        shape = RoundedCornerShape(
                            topStart = WormaCeptorDesignSystem.CornerRadius.xs,
                            bottomStart = WormaCeptorDesignSystem.CornerRadius.xs,
                        ),
                    ),
            )

            // Processing/Network Phase
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = WormaCeptorDesignSystem.Alpha.intense),
                    ),
            )

            // Response Phase
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        if (hasResponse) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = WormaCeptorDesignSystem.Alpha.strong)
                        },
                        shape = RoundedCornerShape(
                            topEnd = WormaCeptorDesignSystem.CornerRadius.xs,
                            bottomEnd = WormaCeptorDesignSystem.CornerRadius.xs,
                        ),
                    ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WormaCeptorDesignSystem.Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Request",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (hasResponse) "Response" else "Failed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SslBadge(isSsl: Boolean, tlsVersion: String?) {
    Surface(
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = if (isSsl) {
            MaterialTheme.colorScheme.primary.asSubtleBackground()
        } else {
            MaterialTheme.colorScheme.error.asSubtleBackground()
        },
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            if (isSsl) {
                MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
            },
        ),
        modifier = Modifier.wrapContentSize(),
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
                imageVector = if (isSsl) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isSsl) "Secure" else "Insecure",
                tint = if (isSsl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            )
            Text(
                text = if (isSsl) (tlsVersion ?: "Secure Connection") else "Insecure Connection",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (isSsl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun EnhancedOverviewCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm),
        ),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Column(modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg)) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.md),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

/**
 * Data class holding information about a single search match
 */
private data class MatchInfo(
    val globalPosition: Int,
    val lineIndex: Int,
)

@Composable
private fun RequestTab(
    transaction: NetworkTransaction,
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
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var copyRequested by remember { mutableStateOf(false) }
    var isCopying by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    var isPrettyMode by remember { mutableStateOf(true) }
    var headersExpanded by remember { mutableStateOf(true) }
    var bodyExpanded by remember { mutableStateOf(true) }

    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // Get content type for sharing
    val requestContentType = transaction.request.headers.entries
        .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
        ?.value?.firstOrNull()

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

    // Handle copy request - copies if small, shares as file if large
    LaunchedEffect(copyRequested) {
        if (copyRequested) {
            isCopying = true
            try {
                val bodyContent = if (isPrettyMode) (requestBody ?: rawBody) else (rawBody ?: requestBody)
                if (bodyContent != null) {
                    if (isContentTooLargeForClipboard(bodyContent)) {
                        val (ext, mime) = getFileInfoForContentType(requestContentType)
                        shareAsFile(
                            context = context,
                            content = bodyContent,
                            fileName = "request_body.$ext",
                            mimeType = mime,
                            title = "Share Request Body",
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

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
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
                .padding(WormaCeptorDesignSystem.Spacing.lg),
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
                    Headers(transaction.request.headers)
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
                } else if (requestBody != null || rawBody != null) {
                    val detectedContentType = remember(rawBody, requestContentType) {
                        BodyParsingUtils.detectContentType(requestContentType, rawBody)
                    }
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
                                        initiallyExpanded = true,
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
                                        BodyParsingUtils.extractMultipartBoundary(
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
            FloatingActionButton(
                onClick = {
                    val fullContent = buildString {
                        appendLine("=== REQUEST HEADERS ===")
                        appendLine(formatHeaders(transaction.request.headers))
                        if (requestBody != null || rawBody != null) {
                            appendLine("\n=== REQUEST BODY ===")
                            val body = if (isPrettyMode) (requestBody ?: rawBody) else (rawBody ?: requestBody)
                            body?.let { appendLine(it) }
                        }
                    }
                    copyToClipboard(context, "Request Content", fullContent)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.viewer_body_copy_all))
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
    isSearchActive: Boolean,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val blobId = transaction.response?.bodyRef
    var responseBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBodyBytes by remember(blobId) { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
    var copyRequested by remember { mutableStateOf(false) }
    var isCopying by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    var isPrettyMode by remember { mutableStateOf(true) }
    var headersExpanded by remember { mutableStateOf(true) }
    var bodyExpanded by remember { mutableStateOf(true) }
    var showImageViewer by remember { mutableStateOf(false) }
    var showPdfViewer by remember { mutableStateOf(false) }
    var imageMetadata by remember(blobId) { mutableStateOf<ImageMetadata?>(null) }

    // Syntax highlighting colors
    val colors = syntaxColors()

    // Extract content type from headers
    val contentType = remember(transaction.response?.headers) {
        transaction.response?.headers?.entries
            ?.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
            ?.value?.firstOrNull()
    }

    // Handle copy request - copies if small, shares as file if large
    LaunchedEffect(copyRequested) {
        if (copyRequested) {
            isCopying = true
            try {
                val bodyContent = if (isPrettyMode) (responseBody ?: rawBody) else (rawBody ?: responseBody)
                if (bodyContent != null) {
                    if (isContentTooLargeForClipboard(bodyContent)) {
                        val (ext, mime) = getFileInfoForContentType(contentType)
                        shareAsFile(
                            context = context,
                            content = bodyContent,
                            fileName = "response_body.$ext",
                            mimeType = mime,
                            title = "Share Response Body",
                        )
                    } else {
                        copyToClipboard(context, "Response Body", bodyContent)
                    }
                }
            } finally {
                isCopying = false
                copyRequested = false
            }
        }
    }

    // Determine if content is an image
    val isImageContent = remember(contentType, rawBodyBytes) {
        isImageContentType(contentType) || rawBodyBytes?.let { isImageData(it) } == true
    }

    // Determine if content is a PDF
    val isPdfContentDetected = remember(contentType, rawBodyBytes) {
        isPdfContent(contentType, rawBodyBytes)
    }

    // Pixel-based scrolling
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val isScrolling = scrollState.value > 100

    // 1. Body Loading - get raw bytes first to detect binary content
    LaunchedEffect(blobId) {
        if (blobId != null) {
            isLoading = true
            // Get raw bytes first to detect binary content like images or PDFs
            val bytes = com.azikar24.wormaceptor.core.engine.CoreHolder.queryEngine?.getBodyBytes(blobId)
            rawBodyBytes = bytes

            // Check for image content and extract metadata
            if (bytes != null && (isImageContentType(contentType) || isImageData(bytes))) {
                imageMetadata = extractImageMetadata(bytes)
            } else if (bytes != null && isPdfContent(contentType, bytes)) {
                // PDF content - raw bytes are stored, no text decoding needed
                // Just keep rawBodyBytes for the PDF viewer
            } else if (bytes != null) {
                // Decode as text if not image or PDF
                val raw = String(bytes, Charsets.UTF_8)
                rawBody = raw
                val formatted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    formatJson(raw)
                }
                responseBody = formatted
            }
            isLoading = false
        } else {
            responseBody = null
            rawBody = null
            rawBodyBytes = null
            imageMetadata = null
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

        delay(250) // Debounce

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
        } catch (_: Exception) {
            // Offset out of bounds, ignore
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            if (transaction.response != null) {
                val hasHeaders = transaction.response?.headers?.isNotEmpty() == true
                val hasBody = blobId != null

                // Only show Headers section if headers exist
                if (hasHeaders) {
                    CollapsibleSection(
                        title = stringResource(R.string.viewer_body_headers),
                        isExpanded = headersExpanded,
                        onToggle = { headersExpanded = !headersExpanded },
                        onCopy = {
                            transaction.response?.headers?.let {
                                copyToClipboard(
                                    context,
                                    "Response Headers",
                                    formatHeaders(it),
                                )
                            }
                        },
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
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg))
                            Text(
                                stringResource(R.string.viewer_body_processing),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else if (isImageContent) {
                        val imageBytes = rawBodyBytes ?: return@Column
                        // Image content - show Image preview card
                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body_image),
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = null,
                        ) {
                            ImagePreviewCard(
                                imageData = imageBytes,
                                onFullscreen = { showImageViewer = true },
                                onDownload = {
                                    val format = imageMetadata?.format ?: detectImageFormat(imageBytes)
                                    val message = saveImageToGallery(context, imageBytes, format)
                                    onShowMessage(message)
                                },
                                onShare = {
                                    val format = imageMetadata?.format ?: detectImageFormat(imageBytes)
                                    shareImage(context, imageBytes, format)?.let { onShowMessage(it) }
                                },
                            )
                        }
                    } else if (isPdfContentDetected) {
                        val pdfBytes = rawBodyBytes ?: return@Column
                        // PDF content - show PDF preview card
                        CollapsibleSection(
                            title = stringResource(R.string.viewer_body_body_pdf),
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = null,
                        ) {
                            PdfPreviewCard(
                                pdfData = pdfBytes,
                                contentType = contentType,
                                onFullscreen = { showPdfViewer = true },
                                onDownload = {
                                    val message = savePdfToDownloads(context, pdfBytes)
                                    onShowMessage(message)
                                },
                                onShowMessage = onShowMessage,
                            )
                        }
                    } else if (responseBody != null || rawBody != null) {
                        // Detect content type
                        val detectedContentType = remember(rawBody, contentType) {
                            BodyParsingUtils.detectContentType(contentType, rawBody)
                        }

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
                                responseBody ?: requireNotNull(rawBody) { "Body must be available" }
                            } else {
                                rawBody ?: requireNotNull(responseBody) { "Body must be available" }
                            }
                            val currentMatchGlobalPos = matches.getOrNull(currentMatchIndex)?.globalPosition
                            val hasActiveSearch = searchQuery.isNotEmpty()

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
                                            initiallyExpanded = true,
                                            colors = colors,
                                        )
                                    }

                                    ContentType.FORM_DATA -> {
                                        FormDataView(
                                            formData = displayBody,
                                        )
                                    }

                                    ContentType.MULTIPART -> {
                                        val boundary = contentType?.let {
                                            BodyParsingUtils.extractMultipartBoundary(
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
                                        onTextLayout = { textLayoutResult = it },
                                    )
                                }
                            }
                        }
                    }
                }

                // Show empty state if no headers and no body
                if (!hasHeaders && !hasBody) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No response data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No response received",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // Floating Action Button for Copy All - hidden when search is active
        AnimatedVisibility(
            visible = isScrolling && responseBody != null && !isSearchActive,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            FloatingActionButton(
                onClick = {
                    val fullContent = buildString {
                        transaction.response?.headers?.let {
                            appendLine("=== RESPONSE HEADERS ===")
                            appendLine(formatHeaders(it))
                        }
                        if (responseBody != null || rawBody != null) {
                            appendLine("\n=== RESPONSE BODY ===")
                            val body = if (isPrettyMode) (responseBody ?: rawBody) else (rawBody ?: responseBody)
                            body?.let { appendLine(it) }
                        }
                    }
                    copyToClipboard(context, "Response Content", fullContent)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.viewer_body_copy_all))
            }
        }

        // Fullscreen Image Viewer dialog
        rawBodyBytes?.let { bytes ->
            if (showImageViewer) {
                FullscreenImageViewer(
                    imageData = bytes,
                    metadata = imageMetadata,
                    onDismiss = { showImageViewer = false },
                    onDownload = {
                        val format = imageMetadata?.format ?: detectImageFormat(bytes)
                        val message = saveImageToGallery(context, bytes, format)
                        onShowMessage(message)
                    },
                    onShare = {
                        val format = imageMetadata?.format ?: detectImageFormat(bytes)
                        shareImage(context, bytes, format)?.let { onShowMessage(it) }
                    },
                )
            }

            // Fullscreen PDF Viewer dialog
            if (showPdfViewer) {
                PdfViewerScreen(
                    pdfData = bytes,
                    onDismiss = { showPdfViewer = false },
                    onDownload = {
                        val message = savePdfToDownloads(context, bytes)
                        onShowMessage(message)
                    },
                )
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
    isCopyLoading: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailingContent?.invoke()

                if (onCopy != null) {
                    IconButton(
                        onClick = onCopy,
                        enabled = !isCopyLoading,
                    ) {
                        if (isCopyLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.viewer_body_copy),
                                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 200),
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 200),
            ) + fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WormaCeptorDesignSystem.Spacing.sm,
                        bottom = WormaCeptorDesignSystem.Spacing.md,
                    ),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PrettyRawToggle(isPretty: Boolean, onToggle: () -> Unit) {
    val activeColor = MaterialTheme.colorScheme.primary
    val radius = WormaCeptorDesignSystem.CornerRadius.xs
    val shape = RoundedCornerShape(radius)

    val borderColor by animateColorAsState(
        targetValue = activeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        label = "segment_border",
    )

    Row(
        modifier = Modifier
            .clip(shape)
            .border(WormaCeptorDesignSystem.BorderWidth.thin, borderColor, shape),
    ) {
        SegmentOption(
            text = "Pretty",
            isSelected = isPretty,
            activeColor = activeColor,
            onClick = { if (!isPretty) onToggle() },
        )
        SegmentOption(
            text = "Raw",
            isSelected = !isPretty,
            activeColor = activeColor,
            onClick = { if (isPretty) onToggle() },
        )
    }
}

@Composable
private fun SegmentOption(text: String, isSelected: Boolean, activeColor: Color, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            activeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light)
        } else {
            Color.Transparent
        },
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        label = "segment_bg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        label = "segment_text",
    )

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = textColor,
        modifier = Modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
    )
}

/**
 * Responsive row of body controls that wraps on small screens.
 * Uses FlowRow to allow badges to flow to multiple lines when space is constrained.
 *
 * Layout priority (from most to least important):
 * 1. Pretty/Raw toggle - primary interaction for viewing mode
 * 2. Zoom button - quick access to fullscreen view
 * 3. Content type chip - informational, can wrap to next row
 */
@Composable
private fun BodyControlsRow(
    contentType: ContentType,
    isPrettyMode: Boolean,
    onPrettyModeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Content type chip - informational, can wrap first
        ContentTypeChip(contentType = contentType)
        // Pretty/Raw toggle - most important, always visible
        PrettyRawToggle(
            isPretty = isPrettyMode,
            onToggle = onPrettyModeToggle,
        )
    }
}

/**
 * Find all match positions using indexOf - faster than regex for simple substring matching.
 * O(n) where n = text length.
 */
private fun findMatchRanges(text: String, query: String): List<IntRange> {
    if (query.isEmpty()) return emptyList()
    val matches = ArrayList<IntRange>(64) // Pre-size for typical case
    var index = 0
    while (true) {
        index = text.indexOf(query, index, ignoreCase = true)
        if (index < 0) break
        matches.add(index until (index + query.length))
        index++
    }
    return matches
}

/**
 * Build base AnnotatedString with ALL matches highlighted (yellow).
 * This is cached and reused - only rebuilt when text/query changes.
 * Current match is NOT highlighted here - it uses an overlay for true O(1).
 */
private fun buildBaseHighlightedText(
    text: String,
    matchRanges: List<IntRange>,
): androidx.compose.ui.text.AnnotatedString {
    if (matchRanges.isEmpty()) return androidx.compose.ui.text.AnnotatedString(text)

    return androidx.compose.ui.text.buildAnnotatedString {
        append(text)
        val defaultStyle = androidx.compose.ui.text.SpanStyle(
            background = Color.Yellow.copy(alpha = WormaCeptorDesignSystem.Alpha.strong),
        )
        matchRanges.forEach { range ->
            addStyle(defaultStyle, range.first, range.last + 1)
        }
    }
}

/**
 * True O(1) highlighted text component.
 * Uses overlay for current match instead of rebuilding AnnotatedString.
 */
@Composable
private fun HighlightedBodyText(
    text: String,
    query: String,
    currentMatchGlobalPos: Int?,
    modifier: Modifier = Modifier,
    onTextLayout: (androidx.compose.ui.text.TextLayoutResult) -> Unit = {},
) {
    // Level 1: Cache match ranges - only when text/query changes
    val matchRanges = remember(text, query) {
        findMatchRanges(text, query)
    }

    // Level 2: Cache base text with ALL matches in yellow - only when text/query changes
    val baseHighlighted = remember(text, matchRanges) {
        buildBaseHighlightedText(text, matchRanges)
    }

    // Track layout for overlay positioning
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    // Current match range for overlay - O(1) lookup
    val currentMatchRange = remember(currentMatchGlobalPos, matchRanges) {
        if (currentMatchGlobalPos == null) {
            null
        } else {
            matchRanges.find { it.first == currentMatchGlobalPos }
        }
    }

    // Current match PATH (not bounds) - handles wrapped text correctly
    val currentMatchPath = remember(textLayoutResult, currentMatchRange) {
        val layout = textLayoutResult ?: return@remember null
        val range = currentMatchRange ?: return@remember null

        try {
            layout.getPathForRange(range.first, range.last + 1)
        } catch (_: Exception) {
            null
        }
    }

    val highlightColor = Color.Cyan.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy)

    Box(modifier = modifier) {
        // Current match overlay using Canvas to draw the actual path shape
        // This correctly handles text that wraps across multiple lines
        currentMatchPath?.let { path ->
            Canvas(
                modifier = Modifier.matchParentSize(),
            ) {
                drawPath(path, highlightColor)
            }
        }

        // Base text with yellow highlights (cached, never changes on "next")
        Text(
            text = baseHighlighted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = {
                textLayoutResult = it
                onTextLayout(it)
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.xs)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
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

private fun generateTextSummary(transaction: NetworkTransaction): String = buildString {
    appendLine("--- WormaCeptor Transaction ---")
    appendLine("URL: ${transaction.request.url}")
    appendLine("Method: ${transaction.request.method}")
    appendLine("Status: ${transaction.status.name}")
    appendLine("Code: ${transaction.response?.code ?: "-"}")
    appendLine("Duration: ${formatDuration(transaction.durationMs)}")
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
        if (trimmed.startsWith("{")) {
            JSONObject(trimmed).toString(4)
        } else if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString(4)
        } else {
            json
        }
    } catch (_: Exception) {
        json
    }
}

/**
 * Saves PDF data to the device's Downloads directory.
 *
 * @return Message describing the result of the operation
 */
private fun savePdfToDownloads(context: Context, pdfData: ByteArray): String {
    return try {
        val fileName = "wormaceptor_${System.currentTimeMillis()}.pdf"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ use MediaStore
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues,
            )

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pdfData)
                }

                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                "PDF saved to Downloads"
            } else {
                "Failed to save PDF"
            }
        } else {
            // Legacy approach for older Android versions
            @Suppress("DEPRECATION")
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS,
            )
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { it.write(pdfData) }
            "PDF saved to Downloads"
        }
    } catch (e: Exception) {
        "Failed to save PDF: ${e.message}"
    }
}
