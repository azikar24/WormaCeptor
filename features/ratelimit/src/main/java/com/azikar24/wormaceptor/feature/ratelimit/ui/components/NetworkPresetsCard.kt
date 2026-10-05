package com.azikar24.wormaceptor.feature.ratelimit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.feature.ratelimit.R

@Suppress("LongMethod", "LongParameterList")
@Composable
internal fun NetworkPresetsCard(
    selectedPreset: RateLimitConfig.NetworkPreset?,
    formattedPresetDownload: String,
    formattedPresetUpload: String,
    enabled: Boolean,
    onSelectPreset: (RateLimitConfig.NetworkPreset?) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.ratelimit_presets_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = WormaCeptorTokens.semantic().textPrimary,
                modifier = Modifier.semantics { heading() },
            )

            WormaCeptorScrollableRow(
                contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
            ) {
                RateLimitConfig.NetworkPreset.entries.forEach { preset ->
                    PresetChip(
                        preset = preset,
                        selected = selectedPreset == preset,
                        enabled = enabled,
                        onClick = { onSelectPreset(if (selectedPreset == preset) null else preset) },
                    )
                }
            }

            // Preset info
            selectedPreset?.let { preset ->
                Surface(
                    shape = WormaCeptorTokens.Shapes.card,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorTokens.Spacing.md),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        PresetInfoItem(
                            icon = Icons.Default.CloudDownload,
                            label = stringResource(R.string.ratelimit_preset_info_down),
                            value = formattedPresetDownload,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.CloudUpload,
                            label = stringResource(R.string.ratelimit_preset_info_up),
                            value = formattedPresetUpload,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.Timer,
                            label = stringResource(R.string.ratelimit_preset_info_latency),
                            value = "${preset.latencyMs}ms",
                        )
                        PresetInfoItem(
                            icon = Icons.Default.Warning,
                            label = stringResource(R.string.ratelimit_preset_info_loss),
                            value = "${preset.packetLoss.toInt()}%",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: RateLimitConfig.NetworkPreset,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presetIcon = when (preset) {
        RateLimitConfig.NetworkPreset.WIFI -> Icons.Default.Wifi
        RateLimitConfig.NetworkPreset.GOOD_3G -> Icons.Default.SignalCellular4Bar
        RateLimitConfig.NetworkPreset.REGULAR_3G,
        RateLimitConfig.NetworkPreset.SLOW_3G,
        -> Icons.Default.SignalCellularAlt
        RateLimitConfig.NetworkPreset.GOOD_2G,
        RateLimitConfig.NetworkPreset.SLOW_2G,
        -> Icons.Default.SignalCellularAlt
        RateLimitConfig.NetworkPreset.EDGE -> Icons.Default.SignalCellularAlt
        RateLimitConfig.NetworkPreset.OFFLINE -> Icons.Default.SignalCellularOff
    }

    WormaCeptorChip(
        label = preset.displayName,
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        leadingIcon = presetIcon,
        modifier = modifier,
    )
}

@Composable
private fun PresetInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = WormaCeptorTokens.semantic().textSecondary,
            modifier = Modifier.size(WormaCeptorTokens.Spacing.lg),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WormaCeptorTokens.semantic().textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = WormaCeptorTokens.semantic().textPrimary,
        )
    }
}
