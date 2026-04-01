package com.azikar24.wormaceptor.feature.recomposition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.recomposition.R
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionItem
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionViewModel
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionViewState
import kotlinx.collections.immutable.persistentListOf

/**
 * Recomposition summary screen connected to the [RecompositionViewModel].
 */
@Composable
fun RecompositionSummaryScreen(
    viewModel: RecompositionViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()

    RecompositionSummaryContent(
        state = state,
        onReset = { viewModel.reset() },
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecompositionSummaryContent(
    state: RecompositionViewState,
    onReset: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recomposition_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.recomposition_back),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onReset) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.recomposition_reset),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        if (state.topRecomposers.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .navigationBarsPadding(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = WormaCeptorDesignSystem.Spacing.lg,
                    top = WormaCeptorDesignSystem.Spacing.lg,
                    end = WormaCeptorDesignSystem.Spacing.lg,
                    bottom = WormaCeptorDesignSystem.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                // Summary cards
                item(key = "summary") {
                    SummaryRow(
                        sessionDurationMs = state.sessionDurationMs,
                        totalRecompositions = state.totalRecompositions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Section header
                item(key = "header") {
                    Text(
                        text = stringResource(R.string.recomposition_top_recomposers),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Recomposer list
                itemsIndexed(
                    items = state.topRecomposers,
                    key = { _, item -> item.name },
                ) { index, item ->
                    RecomposerRow(
                        index = index + 1,
                        item = item,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    sessionDurationMs: Long,
    totalRecompositions: Long,
    modifier: Modifier = Modifier,
) {
    val totalSeconds = sessionDurationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val durationText = stringResource(
        R.string.recomposition_duration_format,
        minutes,
        seconds,
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        WormaCeptorSummaryCard(
            count = durationText,
            label = stringResource(R.string.recomposition_session_duration),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )

        WormaCeptorSummaryCard(
            count = formatCount(totalRecompositions),
            label = stringResource(R.string.recomposition_total_recompositions),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RecomposerRow(
    index: Int,
    item: RecompositionItem,
    modifier: Modifier = Modifier,
) {
    val rateColor = colorForRate(item.ratePerSecond)
    val statusText = statusLabelForRate(item.ratePerSecond)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.BOLD,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Rank badge
            Text(
                text = "#$index",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp),
            )

            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(rateColor),
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

            // Name and count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.recomposition_count_label,
                        item.count,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Rate + status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(
                        R.string.recomposition_rate_per_second,
                        item.ratePerSecond,
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = rateColor,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = rateColor,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.MODERATE,
            ),
            modifier = Modifier.size(
                WormaCeptorDesignSystem.IconSize.xxxl + WormaCeptorDesignSystem.Spacing.lg,
            ),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.INTENSE,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

        Text(
            text = stringResource(R.string.recomposition_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = stringResource(R.string.recomposition_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.HEAVY,
            ),
            modifier = Modifier.padding(horizontal = WormaCeptorDesignSystem.Spacing.xxl),
        )
    }
}

// ---- Helpers ----

private val RateGreen = Color(0xFF4CAF50)
private val RateYellow = Color(0xFFFFC107)
private val RateOrange = Color(0xFFFF9800)
private val RateRed = Color(0xFFF44336)

private fun colorForRate(ratePerSecond: Float): Color = when {
    ratePerSecond <= 2f -> RateGreen
    ratePerSecond <= 5f -> RateYellow
    ratePerSecond <= 10f -> RateOrange
    else -> RateRed
}

@Composable
private fun statusLabelForRate(ratePerSecond: Float): String = when {
    ratePerSecond <= 2f -> stringResource(R.string.recomposition_status_normal)
    ratePerSecond <= 5f -> stringResource(R.string.recomposition_status_elevated)
    ratePerSecond <= 10f -> stringResource(R.string.recomposition_status_excessive)
    else -> stringResource(R.string.recomposition_status_critical)
}

private fun formatCount(count: Long): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun RecompositionSummaryContentPreview() {
    WormaCeptorTheme {
        RecompositionSummaryContent(
            state = RecompositionViewState(
                sessionDurationMs = 135_000L,
                totalRecompositions = 1247L,
                topRecomposers = persistentListOf(
                    RecompositionItem("ProductCard", 342L, 11.4f),
                    RecompositionItem("SearchBar", 89L, 3.0f),
                    RecompositionItem("AppBar", 12L, 0.4f),
                    RecompositionItem("BottomNav", 6L, 0.2f),
                ),
            ),
            onReset = {},
            onBack = {},
        )
    }
}
