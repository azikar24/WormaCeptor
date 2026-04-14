package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.PaddingScheme
import com.azikar24.wormaceptor.feature.crypto.R
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AlgorithmModeSection(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    WormaCeptorContainer(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            AlgorithmChips(config, onEvent)
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
            ModeChips(config, onEvent)
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
            PaddingChips(config, onEvent)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlgorithmChips(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Text(
        stringResource(R.string.crypto_algorithm),
        style = WormaCeptorTokens.Typography.sectionHeader,
    )
    WormaCeptorFlowRow(
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        CryptoAlgorithm.entries.filter { it != CryptoAlgorithm.RSA }.forEach { algorithm ->
            WormaCeptorChip(
                label = algorithm.displayName,
                selected = config.algorithm == algorithm,
                onClick = { onEvent(CryptoViewEvent.Config.SetAlgorithm(algorithm)) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModeChips(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Text(
        stringResource(R.string.crypto_mode),
        style = WormaCeptorTokens.Typography.sectionHeader,
    )
    WormaCeptorFlowRow(
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        CipherMode.entries.forEach { mode ->
            WormaCeptorChip(
                label = mode.displayName,
                selected = config.mode == mode,
                onClick = { onEvent(CryptoViewEvent.Config.SetMode(mode)) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaddingChips(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Text(
        stringResource(R.string.crypto_padding),
        style = WormaCeptorTokens.Typography.sectionHeader,
    )
    WormaCeptorFlowRow(
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        PaddingScheme.entries.forEach { padding ->
            WormaCeptorChip(
                label = padding.displayName,
                selected = config.padding == padding,
                onClick = { onEvent(CryptoViewEvent.Config.SetPadding(padding)) },
            )
        }
    }
}
