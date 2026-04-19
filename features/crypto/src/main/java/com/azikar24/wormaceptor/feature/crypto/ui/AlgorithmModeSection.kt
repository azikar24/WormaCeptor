package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorExpandableCard
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.PaddingScheme
import com.azikar24.wormaceptor.feature.crypto.R
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent

@Composable
internal fun AlgorithmModeSection(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    WormaCeptorContainer(
        modifier = Modifier.fillMaxWidth(),
    ) {
        WormaCeptorExpandableCard(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            isExpanded = expanded,
            onToggle = { expanded = !expanded },
            showDivider = false,
            header = {
                Text(
                    text = stringResource(R.string.crypto_advanced),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WormaCeptorTokens.semantic().textPrimary,
                )
            },
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
}

@Composable
private fun AlgorithmChips(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Text(
        stringResource(R.string.crypto_algorithm),
        style = WormaCeptorTokens.Typography.sectionHeader,
    )
    WormaCeptorScrollableRow(
        contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
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

@Composable
private fun ModeChips(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Text(
        stringResource(R.string.crypto_mode),
        style = WormaCeptorTokens.Typography.sectionHeader,
    )
    WormaCeptorScrollableRow(
        contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
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

@Composable
private fun PaddingChips(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Text(
        stringResource(R.string.crypto_padding),
        style = WormaCeptorTokens.Typography.sectionHeader,
    )
    WormaCeptorScrollableRow(
        contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
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

@Preview(showBackground = true)
@Composable
private fun AlgorithmModeSectionPreview() {
    WormaCeptorTheme {
        AlgorithmModeSection(
            config = CryptoConfig.default(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlgorithmModeSectionAlternatePreview() {
    WormaCeptorTheme {
        AlgorithmModeSection(
            config = CryptoConfig.default().copy(
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.GCM,
                padding = PaddingScheme.NO_PADDING,
            ),
            onEvent = {},
        )
    }
}
