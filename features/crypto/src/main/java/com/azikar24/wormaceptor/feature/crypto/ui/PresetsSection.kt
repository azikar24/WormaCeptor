package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
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
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                CryptoPreset.entries.forEach { preset ->
                    FilterChip(
                        selected = config.algorithm == preset.config.algorithm &&
                            config.mode == preset.config.mode,
                        onClick = { onApplyPreset(preset) },
                        label = { Text(preset.displayName) },
                    )
                }
            }
        }
    }
}
