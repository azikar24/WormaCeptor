package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoPreset
import com.azikar24.wormaceptor.feature.crypto.R

@Composable
internal fun PresetsSection(
    config: CryptoConfig,
    onApplyPreset: (CryptoPreset) -> Unit,
) {
    WormaCeptorContainer(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                stringResource(R.string.crypto_presets),
                style = WormaCeptorTokens.Typography.sectionHeader,
            )
            WormaCeptorScrollableRow(
                contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
            ) {
                CryptoPreset.entries.forEach { preset ->
                    WormaCeptorChip(
                        label = preset.displayName,
                        selected = config.algorithm == preset.config.algorithm &&
                            config.mode == preset.config.mode,
                        onClick = { onApplyPreset(preset) },
                    )
                }
            }
        }
    }
}
