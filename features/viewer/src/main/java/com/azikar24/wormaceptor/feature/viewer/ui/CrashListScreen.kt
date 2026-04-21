package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorPullToRefresh
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.feature.viewer.FormatCrashRelativeTimeUseCase
import com.azikar24.wormaceptor.feature.viewer.IsSevereExceptionUseCase
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.collections.immutable.ImmutableList

/** Crash list with skeleton loading, empty state, and optional pull-to-refresh. */
@Composable
fun CrashListScreen(
    crashes: ImmutableList<Crash>,
    onCrashClick: (Crash) -> Unit,
    modifier: Modifier = Modifier,
    isInitialLoading: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
) {
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val loadable: @Composable () -> Unit = {
        WormaCeptorLoadableContent(
            isLoading = isInitialLoading,
            isEmpty = crashes.isEmpty(),
            loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
            empty = {
                WormaCeptorEmptyState(
                    title = stringResource(R.string.viewer_crash_list_no_crashes_title),
                    subtitle = stringResource(R.string.viewer_crash_list_no_crashes_description),
                    icon = Icons.Default.BugReport,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = WormaCeptorTokens.Spacing.md,
                        top = WormaCeptorTokens.Spacing.md,
                        end = WormaCeptorTokens.Spacing.md,
                        bottom = WormaCeptorTokens.Spacing.md + navigationBarPadding,
                    ),
                ) {
                    items(crashes, key = { it.id }) { crash ->
                        CrashItem(
                            crash = crash,
                            onClick = { onCrashClick(crash) },
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    if (onRefresh != null) {
        WormaCeptorPullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
            indicatorColor = WormaCeptorTokens.semantic().error,
        ) {
            loadable()
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            loadable()
        }
    }
}

/** Renders a single crash entry card showing the exception type, message, and timestamp. */
@Composable
fun CrashItem(
    crash: Crash,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val formatRelativeTime = remember(context) { FormatCrashRelativeTimeUseCase(context) }
    val isSevereException = remember { IsSevereExceptionUseCase() }
    val location = remember(crash.stackTrace) { CrashUtils.extractCrashLocation(crash.stackTrace) }
    val relativeTime = remember(crash.timestamp) { formatRelativeTime(crash.timestamp) }
    val isSevere = remember(crash.exceptionType) { isSevereException(crash.exceptionType) }

    WormaCeptorCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.card,
        backgroundColor = WormaCeptorTokens.Colors.Status.red.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
        borderColor = WormaCeptorTokens.Colors.Status.red.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.Top,
        ) {
            // Icon badge
            Surface(
                shape = WormaCeptorTokens.Shapes.chip,
                color = WormaCeptorTokens.Colors.Status.red.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                contentColor = WormaCeptorTokens.Colors.Status.red,
                modifier = Modifier.size(WormaCeptorTokens.Spacing.xxl),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = if (isSevere) Icons.Default.BugReport else Icons.Default.Warning,
                        contentDescription = if (isSevere) {
                            stringResource(
                                R.string.viewer_crash_list_critical_crash,
                            )
                        } else {
                            stringResource(R.string.viewer_crash_list_warning)
                        },
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                // Exception type - prominent
                Text(
                    text = crash.exceptionType,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WormaCeptorTokens.semantic().error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

                // Error message
                val message = crash.message
                if (message != null && message.isNotBlank()) {
                    Text(
                        text = message,
                        style = WormaCeptorTokens.Typography.bodyMedium,
                        color = WormaCeptorTokens.semantic().textPrimary.copy(
                            alpha = WormaCeptorTokens.Alpha.PROMINENT,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
                }

                // Stack trace location in monospace
                if (location != null) {
                    Surface(
                        shape = WormaCeptorTokens.Shapes.chip,
                        color = WormaCeptorTokens.semantic().surfaceVariant.copy(
                            alpha = WormaCeptorTokens.Alpha.STRONG,
                        ),
                    ) {
                        Text(
                            text = location,
                            style = WormaCeptorTokens.Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = WormaCeptorTokens.semantic().textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.sm,
                                vertical = WormaCeptorTokens.Spacing.xxs,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
                }

                // Relative timestamp with better typography
                Text(
                    text = relativeTime,
                    style = WormaCeptorTokens.Typography.labelMedium,
                    color = WormaCeptorTokens.semantic().textSecondary.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CrashListScreenPreview() {
    WormaCeptorTheme {
        CrashListScreen(
            crashes = kotlinx.collections.immutable.persistentListOf(
                Crash(
                    id = 1L,
                    timestamp = System.currentTimeMillis() - 120_000,
                    exceptionType = "java.lang.NullPointerException",
                    message = "Attempt to invoke virtual method on a null object reference",
                    stackTrace = "java.lang.NullPointerException\n\tat " +
                        "com.example.app.MainActivity.onCreate(MainActivity.kt:42)",
                ),
                Crash(
                    id = 2L,
                    timestamp = System.currentTimeMillis() - 3_600_000,
                    exceptionType = "java.lang.IllegalStateException",
                    message = "Fragment not attached to a context",
                    stackTrace = "java.lang.IllegalStateException\n\tat " +
                        "com.example.app.HomeFragment.onResume(HomeFragment.kt:78)",
                ),
                Crash(
                    id = 3L,
                    timestamp = System.currentTimeMillis() - 86_400_000,
                    exceptionType = "java.lang.OutOfMemoryError",
                    message = "Failed to allocate a 12288012 byte allocation",
                    stackTrace = "java.lang.OutOfMemoryError\n\tat " +
                        "dalvik.system.VMRuntime.newNonMovableArray(VMRuntime.java)",
                ),
            ),
            onCrashClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
