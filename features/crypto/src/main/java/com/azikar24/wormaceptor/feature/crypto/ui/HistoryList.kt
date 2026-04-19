package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CryptoResult

@Composable
internal fun HistoryList(
    history: List<CryptoResult>,
    onLoadResult: (CryptoResult) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = WormaCeptorTokens.Spacing.lg,
            top = WormaCeptorTokens.Spacing.lg,
            end = WormaCeptorTokens.Spacing.lg,
            bottom = WormaCeptorTokens.Spacing.lg +
                WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        itemsIndexed(
            items = history,
            key = { _, result -> result.id },
        ) { index, result ->
            HistoryItem(
                result = result,
                onLoad = { onLoadResult(result) },
                onRemove = { onRemove(result.id) },
            )
            if (index < history.lastIndex) {
                WormaCeptorDivider()
            }
        }
    }
}
