package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.feature.viewer.ui.theme.syntaxColors
import com.azikar24.wormaceptor.feature.viewer.ui.util.formatBytes
import kotlinx.coroutines.launch

/**
 * State class for managing paginated body content.
 */
@Stable
class PaginatedBodyState(
    initialContent: String = "",
    val totalSize: Long = 0L,
    val pageSize: Long = 50_000L, // 50KB default chunk size
) {
    var loadedContent by mutableStateOf(initialContent)
        private set

    var loadedBytes by mutableLongStateOf(initialContent.length.toLong())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    val hasMore: Boolean
        get() = loadedBytes < totalSize

    val progress: Float
        get() = if (totalSize > 0) loadedBytes.toFloat() / totalSize else 1f

    val remainingBytes: Long
        get() = (totalSize - loadedBytes).coerceAtLeast(0)

    fun appendContent(newContent: String) {
        loadedContent += newContent
        loadedBytes += newContent.length
        isLoading = false
        error = null
    }

    fun updateLoading(loading: Boolean) {
        isLoading = loading
        if (loading) error = null
    }

    fun updateError(message: String) {
        isLoading = false
        error = message
    }

    fun reset(initialContent: String) {
        loadedContent = initialContent
        loadedBytes = initialContent.length.toLong()
        isLoading = false
        error = null
    }
}

@Composable
fun rememberPaginatedBodyState(
    initialContent: String = "",
    totalSize: Long = 0L,
    pageSize: Long = 50_000L,
): PaginatedBodyState {
    return remember(totalSize, pageSize) {
        PaginatedBodyState(
            initialContent = initialContent,
            totalSize = totalSize,
            pageSize = pageSize,
        )
    }
}

/**
 * A composable that displays body content with lazy loading pagination.
 * Loads content in chunks to handle large response bodies efficiently.
 *
 * Features:
 * - Initial content loaded with optional syntax highlighting
 * - Load more button when content is truncated
 * - Progress indicator showing loaded vs total
 * - Error handling with retry
 * - Smooth animations for content expansion
 */
@Composable
fun PaginatedBodyView(
    state: PaginatedBodyState,
    onLoadMore: suspend () -> String?,
    modifier: Modifier = Modifier,
    highlightedContent: AnnotatedString? = null,
    showLineNumbers: Boolean = true,
    enableHighlighting: Boolean = true,
    searchQuery: String = "",
) {
    val scope = rememberCoroutineScope()
    val colors = syntaxColors()

    fun loadMoreContent() {
        state.updateLoading(true)
        scope.launch {
            try {
                val newContent = onLoadMore()
                if (newContent != null) {
                    state.appendContent(newContent)
                } else {
                    state.updateError("Failed to load content")
                }
            } catch (e: Exception) {
                state.updateError(e.message ?: "Unknown error")
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Progress indicator when content is truncated
        if (state.totalSize > 0 && state.hasMore) {
            BodyLoadingProgress(
                loadedBytes = state.loadedBytes,
                totalBytes = state.totalSize,
                modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
            )
        }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    colors.codeBackground,
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                ),
        ) {
            HighlightedText(
                text = state.loadedContent,
                highlightedText = if (enableHighlighting) highlightedContent else null,
                showLineNumbers = showLineNumbers,
                enableHighlighting = enableHighlighting,
                searchQuery = searchQuery,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Load more section
        AnimatedVisibility(
            visible = state.hasMore || state.error != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WormaCeptorDesignSystem.Spacing.md),
            ) {
                // Error state
                state.error?.let { errorMessage ->
                    InlineErrorRetry(
                        message = errorMessage,
                        onRetry = { loadMoreContent() },
                        modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
                    )
                }

                // Load more button
                if (state.hasMore && state.error == null) {
                    LoadMoreBodyButton(
                        remainingBytes = state.remainingBytes,
                        isLoading = state.isLoading,
                        onClick = { loadMoreContent() },
                    )
                }
            }
        }
    }
}

/**
 * Simplified version for plain text body content without syntax highlighting.
 */
@Composable
fun PaginatedPlainBodyView(
    content: String,
    totalSize: Long,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    onRetry: () -> Unit = onLoadMore,
) {
    val colors = syntaxColors()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        // Progress indicator
        if (totalSize > 0 && hasMore) {
            BodyLoadingProgress(
                loadedBytes = content.length.toLong(),
                totalBytes = totalSize,
                modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
            )
        }

        // Content area with scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .background(
                    colors.codeBackground,
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                )
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md)),
        ) {
            SelectionContainer {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = colors.default,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(WormaCeptorDesignSystem.Spacing.md),
                )
            }
        }

        // Error or load more
        AnimatedVisibility(
            visible = hasMore || error != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WormaCeptorDesignSystem.Spacing.md),
            ) {
                error?.let { errorMessage ->
                    InlineErrorRetry(
                        message = errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
                    )
                }

                if (hasMore && error == null) {
                    LoadMoreBodyButton(
                        remainingBytes = (totalSize - content.length).coerceAtLeast(0),
                        isLoading = isLoading,
                        onClick = onLoadMore,
                    )
                }
            }
        }
    }
}

/**
 * Body content with truncation indicator.
 * Shows when content has been truncated with option to load full content.
 */
@Composable
fun TruncatedBodyIndicator(totalSize: Long, loadedSize: Long, modifier: Modifier = Modifier) {
    val truncatedAmount = totalSize - loadedSize

    if (truncatedAmount <= 0) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = WormaCeptorColors.StatusAmber.asSubtleBackground(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Content truncated",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = WormaCeptorColors.StatusAmber,
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = "- ${formatBytes(truncatedAmount)} not shown",
                style = MaterialTheme.typography.bodySmall,
                color = WormaCeptorColors.StatusAmber.copy(alpha = 0.8f),
            )
        }
    }
}

/**
 * Skeleton placeholder for body content loading.
 */
@Composable
fun BodyContentSkeleton(modifier: Modifier = Modifier, lineCount: Int = 8) {
    val shimmerBrush = rememberShimmerBrush()
    val colors = syntaxColors()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                colors.codeBackground,
                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .padding(WormaCeptorDesignSystem.Spacing.md),
    ) {
        repeat(lineCount) { index ->
            // Vary line widths for realistic code appearance
            val widthFraction = when (index % 4) {
                0 -> 0.9f
                1 -> 0.6f
                2 -> 0.75f
                else -> 0.5f
            }

            SkeletonBox(
                modifier = Modifier.fillMaxWidth(widthFraction),
                height = 14.dp,
                brush = shimmerBrush,
            )

            if (index < lineCount - 1) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            }
        }
    }
}
