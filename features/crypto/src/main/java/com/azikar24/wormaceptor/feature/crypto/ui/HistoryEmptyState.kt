package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.crypto.R

@Composable
internal fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.xxxl),
                tint = WormaCeptorTokens.semantic().textSecondary.copy(
                    alpha = WormaCeptorTokens.Alpha.BOLD,
                ),
            )
            Text(
                text = stringResource(R.string.crypto_no_history),
                style = MaterialTheme.typography.bodyLarge,
                color = WormaCeptorTokens.semantic().textSecondary,
            )
            Text(
                text = stringResource(R.string.crypto_empty_history_description),
                style = MaterialTheme.typography.bodyMedium,
                color = WormaCeptorTokens.semantic().textSecondary.copy(
                    alpha = WormaCeptorTokens.Alpha.HEAVY,
                ),
            )
        }
    }
}
