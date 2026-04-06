package com.azikar24.wormaceptor.feature.ratelimit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.feature.ratelimit.R
import com.azikar24.wormaceptor.feature.ratelimit.ui.util.formatSpeed

@Suppress("LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NetworkPresetsCard(
    selectedPreset: RateLimitConfig.NetworkPreset?,
    enabled: Boolean,
    onSelectPreset: (RateLimitConfig.NetworkPreset?) -> Unit,
    colors: ToolColors.RateLimit.Scheme,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.ratelimit_presets_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
                modifier = Modifier.semantics { heading() },
            )

            WormaCeptorFlowRow(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                RateLimitConfig.NetworkPreset.entries.forEach { preset ->
                    PresetChip(
                        preset = preset,
                        selected = selectedPreset == preset,
                        enabled = enabled,
                        onClick = { onSelectPreset(if (selectedPreset == preset) null else preset) },
                        colors = colors,
                    )
                }
            }

            // Preset info
            selectedPreset?.let { preset ->
                Surface(
                    shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
                    color = colors.primary.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
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
                            value = formatSpeed(preset.downloadKbps),
                            color = colors.download,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.CloudUpload,
                            label = stringResource(R.string.ratelimit_preset_info_up),
                            value = formatSpeed(preset.uploadKbps),
                            color = colors.upload,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.Timer,
                            label = stringResource(R.string.ratelimit_preset_info_latency),
                            value = "${preset.latencyMs}ms",
                            color = colors.latency,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.Warning,
                            label = stringResource(R.string.ratelimit_preset_info_loss),
                            value = "${preset.packetLoss.toInt()}%",
                            color = colors.packetLoss,
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
    colors: ToolColors.RateLimit.Scheme,
    modifier: Modifier = Modifier,
) {
    val presetColor = when (preset) {
        RateLimitConfig.NetworkPreset.WIFI -> colors.presetWifi
        RateLimitConfig.NetworkPreset.GOOD_3G,
        RateLimitConfig.NetworkPreset.REGULAR_3G,
        RateLimitConfig.NetworkPreset.SLOW_3G,
        -> colors.preset3G
        RateLimitConfig.NetworkPreset.GOOD_2G,
        RateLimitConfig.NetworkPreset.SLOW_2G,
        -> colors.preset2G
        RateLimitConfig.NetworkPreset.EDGE -> colors.presetEdge
        RateLimitConfig.NetworkPreset.OFFLINE -> colors.presetOffline
    }

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

    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = preset.displayName,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = presetIcon,
                contentDescription = preset.displayName,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = presetColor.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
            selectedLabelColor = presetColor,
            selectedLeadingIconColor = presetColor,
        ),
        modifier = modifier,
    )
}

@Composable
private fun PresetInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(WormaCeptorTokens.Spacing.lg),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = color,
        )
    }
}
