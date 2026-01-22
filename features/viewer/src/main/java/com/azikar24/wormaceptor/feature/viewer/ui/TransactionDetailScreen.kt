package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
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
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.feature.viewer.ui.theme.syntaxColors
import com.azikar24.wormaceptor.feature.viewer.ui.util.copyToClipboard
import com.azikar24.wormaceptor.feature.viewer.ui.util.copyToClipboardWithSizeCheck
import com.azikar24.wormaceptor.feature.viewer.ui.util.extractUrlPath
import com.azikar24.wormaceptor.feature.viewer.ui.util.formatBytes
import com.azikar24.wormaceptor.feature.viewer.ui.util.getFileInfoForContentType
import com.azikar24.wormaceptor.feature.viewer.ui.util.isContentTooLargeForClipboard
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareAsFile
import com.azikar24.wormaceptor.feature.viewer.ui.util.formatDuration
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
                    currentTransactionIndex = currentTransactionIndex,
                    totalTransactions = transactionIds.size,
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
    currentTransactionIndex: Int = 0,
    totalTransactions: Int = 1,
    onNavigatePrevTransaction: () -> Unit = {},
    onNavigateNextTransaction: () -> Unit = {},
    canNavigatePrev: Boolean = false,
    canNavigateNext: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tabs = listOf("Overview", "Request", "Response")

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
        topBar = {
            Column {
                // Swipeable TopAppBar for transaction navigation
                SwipeableTopBar(
                    onSwipeLeft = onNavigateNextTransaction,
                    onSwipeRight = onNavigatePrevTransaction,
                    canSwipeLeft = canNavigateNext,
                    canSwipeRight = canNavigatePrev,
                ) {
                    if (showSearch) {
                        // Custom search bar that takes full width
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .height(64.dp)
                                    .padding(start = 4.dp, end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = {
                                    showSearch = false
                                    searchQuery = ""
                                    debouncedSearchQuery = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close search",
                                    )
                                }
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search in body...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                                    ),
                                    singleLine = true,
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    } else {
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
                                        contentDescription = "Back",
                                    )
                                }
                            },
                            actions = {
                                // Show search icon only for Request/Response tabs with content
                                if (tabPagerState.currentPage > 0 && currentTabHasContent) {
                                    IconButton(onClick = { showSearch = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                        )
                                    }
                                }

                                IconButton(onClick = { showMenu = true }) {
                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Copy as Text") },
                                        onClick = {
                                            showMenu = false
                                            copyToClipboard(context, "Transaction", generateTextSummary(transaction))
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Copy as cURL") },
                                        onClick = {
                                            showMenu = false
                                            copyToClipboard(context, "cURL", generateCurlCommand(transaction))
                                        },
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Share as JSON") },
                                        onClick = {
                                            showMenu = false
                                            val exportManager = com.azikar24.wormaceptor.feature.viewer.export.ExportManager(
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
                }
                // Tab row with click support (swipe is handled by the content pager)
                TabRow(
                    selectedTabIndex = tabPagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
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
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // HorizontalPager for tab content - swipe here switches between Overview/Request/Response
            HorizontalPager(
                state = tabPagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.ime),
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
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Search Navigation Controllers in Bottom Right - show immediately when search is active
            AnimatedVisibility(
                visible = showSearch && debouncedSearchQuery.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(150)) + scaleIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(100)) + scaleOut(animationSpec = tween(100)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .imePadding()
                    .padding(bottom = 32.dp, end = 16.dp),
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (matchCount > 0) {
                            Text(
                                text = "${currentMatchIndex + 1}/$matchCount",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                            IconButton(
                                onClick = {
                                    currentMatchIndex = (currentMatchIndex - 1 + matchCount) % matchCount
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, "Previous match")
                            }
                            IconButton(
                                onClick = {
                                    currentMatchIndex = (currentMatchIndex + 1) % matchCount
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, "Next match")
                            }
                        } else {
                            Text(
                                text = "No matches",
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
            title = "Timing",
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
            title = "Security",
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
            title = "Data Transfer",
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
                title = "Extensions",
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
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = WormaCeptorDesignSystem.Shapes.chip,
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
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
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
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    ),
            )

            // Response Phase
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        if (hasResponse) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
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
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            if (isSsl) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
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
                modifier = Modifier.size(16.dp),
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
    val gradientColors = listOf(
        iconTint.copy(alpha = 0.03f),
        iconTint.copy(alpha = 0.01f),
        Color.Transparent,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm),
        ),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(gradientColors),
                ),
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
                        modifier = Modifier.size(20.dp),
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
    var matches by remember { mutableStateOf<List<MatchInfo>>(emptyList()) }
    var isPrettyMode by remember { mutableStateOf(true) }
    var headersExpanded by remember { mutableStateOf(true) }
    var bodyExpanded by remember { mutableStateOf(true) }

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
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Only show Headers section if headers exist
            if (transaction.request.headers.isNotEmpty()) {
                CollapsibleSection(
                    title = "Headers",
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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Processing body...", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (requestBody != null || rawBody != null) {
                    // Get request content type
                    val requestContentType = transaction.request.headers.entries
                        .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
                        ?.value?.firstOrNull()

                    val detectedContentType = remember(rawBody, requestContentType) {
                        BodyParsingUtils.detectContentType(requestContentType, rawBody)
                    }
                    val colors = syntaxColors()

                    val bodyContent = if (isPrettyMode) (requestBody ?: rawBody!!) else (rawBody ?: requestBody!!)
                    val isLargeBody = isContentTooLargeForClipboard(bodyContent)

                    CollapsibleSection(
                        title = "Body",
                        isExpanded = bodyExpanded,
                        onToggle = { bodyExpanded = !bodyExpanded },
                        onCopy = {
                            copyToClipboardWithSizeCheck(context, "Request Body", bodyContent)
                        },
                        onShare = if (isLargeBody) {
                            {
                                val (ext, mime) = getFileInfoForContentType(requestContentType)
                                shareAsFile(
                                    context = context,
                                    content = bodyContent,
                                    fileName = "request_body.$ext",
                                    mimeType = mime,
                                    title = "Share Request Body",
                                )
                            }
                        } else {
                            null
                        },
                        trailingContent = {
                            BodyControlsRow(
                                contentType = detectedContentType,
                                isPrettyMode = isPrettyMode,
                                onPrettyModeToggle = { isPrettyMode = !isPrettyMode },
                            )
                        },
                    ) {
                        val displayBody = if (isPrettyMode) (requestBody ?: rawBody!!) else (rawBody ?: requestBody!!)
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
                        if (requestBody != null) {
                            appendLine("\n=== REQUEST BODY ===")
                            appendLine(if (isPrettyMode) requestBody!! else rawBody!!)
                        }
                    }
                    copyToClipboard(context, "Request Content", fullContent)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
    isSearchActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val blobId = transaction.response?.bodyRef
    var responseBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBody by remember(blobId) { mutableStateOf<String?>(null) }
    var rawBodyBytes by remember(blobId) { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember(blobId) { mutableStateOf(blobId != null) }
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

    // Determine if content is an image
    val isImageContent = remember(contentType, rawBodyBytes) {
        isImageContentType(contentType) || (rawBodyBytes != null && isImageData(rawBodyBytes!!))
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
                .padding(WormaCeptorDesignSystem.Spacing.lg),
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
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("Processing body...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (isImageContent && rawBodyBytes != null) {
                        // Image content - show Image preview card
                        CollapsibleSection(
                            title = "Body (Image)",
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = null,
                        ) {
                            ImagePreviewCard(
                                imageData = rawBodyBytes!!,
                                contentType = contentType,
                                onFullscreen = { showImageViewer = true },
                                onDownload = {
                                    val format = imageMetadata?.format ?: detectImageFormat(rawBodyBytes!!)
                                    saveImageToGallery(context, rawBodyBytes!!, format)
                                },
                                onShare = {
                                    val format = imageMetadata?.format ?: detectImageFormat(rawBodyBytes!!)
                                    shareImage(context, rawBodyBytes!!, format)
                                },
                            )
                        }
                    } else if (isPdfContentDetected && rawBodyBytes != null) {
                        // PDF content - show PDF preview card
                        CollapsibleSection(
                            title = "Body (PDF)",
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = null,
                        ) {
                            PdfPreviewCard(
                                pdfData = rawBodyBytes!!,
                                contentType = contentType,
                                onFullscreen = { showPdfViewer = true },
                                onDownload = {
                                    // Save PDF to downloads
                                    savePdfToDownloads(context, rawBodyBytes!!)
                                },
                            )
                        }
                    } else if (responseBody != null || rawBody != null) {
                        // Detect content type
                        val detectedContentType = remember(rawBody, contentType) {
                            BodyParsingUtils.detectContentType(contentType, rawBody)
                        }

                        val responseBodyContent = if (isPrettyMode) (responseBody ?: rawBody!!) else (rawBody ?: responseBody!!)
                        val isLargeResponseBody = isContentTooLargeForClipboard(responseBodyContent)

                        CollapsibleSection(
                            title = "Body",
                            isExpanded = bodyExpanded,
                            onToggle = { bodyExpanded = !bodyExpanded },
                            onCopy = {
                                copyToClipboardWithSizeCheck(context, "Response Body", responseBodyContent)
                            },
                            onShare = if (isLargeResponseBody) {
                                {
                                    val (ext, mime) = getFileInfoForContentType(contentType)
                                    shareAsFile(
                                        context = context,
                                        content = responseBodyContent,
                                        fileName = "response_body.$ext",
                                        mimeType = mime,
                                        title = "Share Response Body",
                                    )
                                }
                            } else {
                                null
                            },
                            trailingContent = {
                                BodyControlsRow(
                                    contentType = detectedContentType,
                                    isPrettyMode = isPrettyMode,
                                    onPrettyModeToggle = { isPrettyMode = !isPrettyMode },
                                )
                            },
                        ) {
                            val displayBody = if (isPrettyMode) (responseBody ?: rawBody!!) else (rawBody ?: responseBody!!)
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
                        if (responseBody != null) {
                            appendLine("\n=== RESPONSE BODY ===")
                            appendLine(if (isPrettyMode) responseBody!! else rawBody!!)
                        }
                    }
                    copyToClipboard(context, "Response Content", fullContent)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy All")
            }
        }

        // Fullscreen Image Viewer dialog
        if (showImageViewer && rawBodyBytes != null) {
            FullscreenImageViewer(
                imageData = rawBodyBytes!!,
                metadata = imageMetadata,
                onDismiss = { showImageViewer = false },
                onDownload = {
                    val format = imageMetadata?.format ?: detectImageFormat(rawBodyBytes!!)
                    saveImageToGallery(context, rawBodyBytes!!, format)
                },
                onShare = {
                    val format = imageMetadata?.format ?: detectImageFormat(rawBodyBytes!!)
                    shareImage(context, rawBodyBytes!!, format)
                },
            )
        }

        // Fullscreen PDF Viewer dialog
        if (showPdfViewer && rawBodyBytes != null) {
            PdfViewerScreen(
                pdfData = rawBodyBytes!!,
                onDismiss = { showPdfViewer = false },
                onDownload = {
                    savePdfToDownloads(context, rawBodyBytes!!)
                },
            )
        }
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
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
                    modifier = Modifier.size(18.dp),
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

                if (onShare != null) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share as File",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (onCopy != null) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
    Surface(
        onClick = onToggle,
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
        ) {
            Text(
                text = "Pretty",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isPretty) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                ),
                color = if (isPretty) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = "|",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Raw",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (!isPretty) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                ),
                color = if (!isPretty) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BodyControlsRow(
    contentType: ContentType,
    isPrettyMode: Boolean,
    onPrettyModeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        // Content type chip - informational, can wrap first
        ContentTypeChip(
            contentType = contentType,
            isAutoDetected = true,
        )
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
            background = Color.Yellow.copy(alpha = 0.4f),
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
        } catch (e: Exception) {
            null
        }
    }

    val highlightColor = Color.Cyan.copy(alpha = 0.7f)

    Box(modifier = modifier) {
        // Current match overlay using Canvas to draw the actual path shape
        // This correctly handles text that wraps across multiple lines
        currentMatchPath?.let { path ->
            androidx.compose.foundation.Canvas(
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

/**
 * Wrapper that returns AnnotatedString for compatibility with existing code.
 * For small-medium texts, this is fine. For huge texts, use HighlightedBodyText directly.
 */
@Composable
private fun rememberHighlightedText(
    text: String,
    query: String,
    @Suppress("UNUSED_PARAMETER") currentMatchGlobalPos: Int?,
): androidx.compose.ui.text.AnnotatedString {
    val matchRanges = remember(text, query) {
        findMatchRanges(text, query)
    }

    return remember(text, matchRanges) {
        buildBaseHighlightedText(text, matchRanges)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
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
 */
private fun savePdfToDownloads(context: Context, pdfData: ByteArray) {
    try {
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

            uri?.let { downloadUri ->
                context.contentResolver.openOutputStream(downloadUri)?.use { outputStream ->
                    outputStream.write(pdfData)
                }

                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(downloadUri, contentValues, null, null)

                Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Legacy approach for older Android versions
            @Suppress("DEPRECATION")
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS,
            )
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { it.write(pdfData) }
            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
