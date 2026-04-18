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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.recomposition.R
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionItem
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionViewEvent
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionViewState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecompositionSummaryScreen(
    state: RecompositionViewState,
    onEvent: (RecompositionViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            WormaCeptorTopBar(
                title = stringResource(R.string.recomposition_title),
                onBack = { onEvent(RecompositionViewEvent.BackPressed) },
                backContentDescription = stringResource(R.string.recomposition_back),
                actions = {
                    IconButton(onClick = { onEvent(RecompositionViewEvent.Reset) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.recomposition_reset),
                        )
                    }
                },
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
                    start = WormaCeptorTokens.Spacing.lg,
                    top = WormaCeptorTokens.Spacing.lg,
                    end = WormaCeptorTokens.Spacing.lg,
                    bottom = WormaCeptorTokens.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
            ) {
                item(key = "summary") {
                    SummaryRow(
                        formattedDuration = state.formattedDuration,
                        formattedTotalRecompositions = state.formattedTotalRecompositions,
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
private fun RecompositionSummaryScreenPreview() {
    WormaCeptorTheme {
        RecompositionSummaryScreen(
            state = RecompositionViewState(
                sessionDurationMs = 135_000L,
                totalRecompositions = 1247L,
                formattedDuration = "02:15",
                formattedTotalRecompositions = "1.2K",
                topRecomposers = persistentListOf(
                    RecompositionItem("ProductCard", 342L, 11.4f),
                    RecompositionItem("SearchBar", 89L, 3.0f),
                    RecompositionItem("AppBar", 12L, 0.4f),
                    RecompositionItem("BottomNav", 6L, 0.2f),
                ),
            ),
            onEvent = {},
        )
    }
}
