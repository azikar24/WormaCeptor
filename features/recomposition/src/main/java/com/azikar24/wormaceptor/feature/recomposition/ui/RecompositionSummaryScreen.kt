package com.azikar24.wormaceptor.feature.recomposition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.recomposition.R
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionEvent
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
    BaseScreen(viewModel) { state, onEvent ->
        RecompositionSummaryContent(
            state = state,
            onReset = { onEvent(RecompositionEvent.Reset) },
            onBack = onBack,
            modifier = modifier,
        )
    }
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
            EmptyRecompositionState(
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
                item(key = "summary") {
                    SummaryRow(
                        sessionDurationMs = state.sessionDurationMs,
                        totalRecompositions = state.totalRecompositions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item(key = "header") {
                    Text(
                        text = stringResource(R.string.recomposition_top_recomposers),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

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
