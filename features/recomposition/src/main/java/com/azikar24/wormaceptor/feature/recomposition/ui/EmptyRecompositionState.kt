package com.azikar24.wormaceptor.feature.recomposition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.recomposition.R

@Composable
internal fun EmptyRecompositionState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = WormaCeptorTokens.Shapes.cardExtraLarge,
            color = WormaCeptorTokens.semantic().surfaceVariant.copy(
                alpha = WormaCeptorTokens.Alpha.MODERATE,
            ),
            modifier = Modifier.size(
                WormaCeptorTokens.IconSize.xxxl + WormaCeptorTokens.Spacing.lg,
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
                    color = WormaCeptorTokens.semantic().textSecondary.copy(
                        alpha = WormaCeptorTokens.Alpha.INTENSE,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))

        Text(
            text = stringResource(R.string.recomposition_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = WormaCeptorTokens.semantic().textPrimary,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        Text(
            text = stringResource(R.string.recomposition_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = WormaCeptorTokens.semantic().textSecondary.copy(
                alpha = WormaCeptorTokens.Alpha.HEAVY,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = WormaCeptorTokens.Spacing.xxl),
        )
    }
}

@Preview(name = "EmptyRecompositionState", showBackground = true)
@Composable
private fun EmptyRecompositionStatePreview() {
    WormaCeptorTheme {
        EmptyRecompositionState(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
