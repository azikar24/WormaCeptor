package com.azikar24.wormaceptor.feature.ratelimit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import com.azikar24.wormaceptor.feature.ratelimit.R
import com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors
import com.azikar24.wormaceptor.feature.ratelimit.ui.theme.rateLimitColors
import java.util.Locale

/**
 * Main screen for Network Rate Limiting.
 *
 * Features:
 * - Enable/disable toggle
 * - Network preset selection (WiFi, 3G, 2G, EDGE, Offline)
 * - Custom speed/latency/packet loss sliders
 * - Statistics display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateLimitScreen(
    config: RateLimitConfig,
    stats: ThrottleStats,
    selectedPreset: RateLimitConfig.NetworkPreset?,
    onEnableToggle: () -> Unit,
    onPresetSelected: (RateLimitConfig.NetworkPreset?) -> Unit,
    onDownloadSpeedChanged: (Long) -> Unit,
    onUploadSpeedChanged: (Long) -> Unit,
    onLatencyChanged: (Long) -> Unit,
    onPacketLossChanged: (Float) -> Unit,
    onClearStats: () -> Unit,
    onResetToDefaults: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = rateLimitColors()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.ratelimit_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.ratelimit_back),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onResetToDefaults) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.ratelimit_reset_defaults),
                        )
                    }
                    IconButton(onClick = onClearStats) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.ratelimit_clear_statistics),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Enable toggle card
            EnableToggleCard(
                enabled = config.enabled,
                onToggle = onEnableToggle,
                colors = colors,
            )

            AnimatedVisibility(visible = config.enabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
                ) {
                    // Statistics
                    StatisticsCard(
                        stats = stats,
                        colors = colors,
                    )

                    // Network presets
                    NetworkPresetsCard(
                        selectedPreset = selectedPreset,
                        enabled = config.enabled,
                        onPresetSelected = onPresetSelected,
                        colors = colors,
                    )

                    // Custom configuration
                    ConfigurationCard(
                        config = config,
                        enabled = config.enabled,
                        onDownloadSpeedChanged = onDownloadSpeedChanged,
                        onUploadSpeedChanged = onUploadSpeedChanged,
                        onLatencyChanged = onLatencyChanged,
                        onPacketLossChanged = onPacketLossChanged,
                        colors = colors,
                    )
                }
            }
        }
    }
}

@Composable
private fun EnableToggleCard(
    enabled: Boolean,
    onToggle: () -> Unit,
    colors: RateLimitColors,
    modifier: Modifier = Modifier,
) {
    val statusColor by animateColorAsState(
        targetValue = if (enabled) colors.enabled else colors.disabled,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
        label = "status",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onToggle,
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorDesignSystem.Spacing.xxxl)
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg))
                        .background(statusColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = stringResource(R.string.ratelimit_title),
                        tint = statusColor,
                        modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.ratelimit_toggle_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                    Text(
                        text = if (enabled) {
                            stringResource(
                                R.string.ratelimit_toggle_status_active,
                            )
                        } else {
                            stringResource(R.string.ratelimit_toggle_status_disabled)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.labelSecondary,
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.enabled,
                    checkedTrackColor = colors.enabled.copy(alpha = WormaCeptorDesignSystem.Alpha.strong),
                ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NetworkPresetsCard(
    selectedPreset: RateLimitConfig.NetworkPreset?,
    enabled: Boolean,
    onPresetSelected: (RateLimitConfig.NetworkPreset?) -> Unit,
    colors: RateLimitColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.ratelimit_presets_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
                modifier = Modifier.semantics { heading() },
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                RateLimitConfig.NetworkPreset.entries.forEach { preset ->
                    PresetChip(
                        preset = preset,
                        selected = selectedPreset == preset,
                        enabled = enabled,
                        onClick = { onPresetSelected(if (selectedPreset == preset) null else preset) },
                        colors = colors,
                    )
                }
            }

            // Preset info
            selectedPreset?.let { preset ->
                Surface(
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                    color = colors.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorDesignSystem.Spacing.md),
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
    colors: RateLimitColors,
    modifier: Modifier = Modifier,
) {
    val presetColor = when (preset) {
        RateLimitConfig.NetworkPreset.WIFI -> colors.presetWifi
        RateLimitConfig.NetworkPreset.GOOD_3G, RateLimitConfig.NetworkPreset.REGULAR_3G, RateLimitConfig.NetworkPreset.SLOW_3G -> colors.preset3G
        RateLimitConfig.NetworkPreset.GOOD_2G, RateLimitConfig.NetworkPreset.SLOW_2G -> colors.preset2G
        RateLimitConfig.NetworkPreset.EDGE -> colors.presetEdge
        RateLimitConfig.NetworkPreset.OFFLINE -> colors.presetOffline
    }

    val presetIcon = when (preset) {
        RateLimitConfig.NetworkPreset.WIFI -> Icons.Default.Wifi
        RateLimitConfig.NetworkPreset.GOOD_3G -> Icons.Default.SignalCellular4Bar
        RateLimitConfig.NetworkPreset.REGULAR_3G, RateLimitConfig.NetworkPreset.SLOW_3G -> Icons.Default.SignalCellularAlt
        RateLimitConfig.NetworkPreset.GOOD_2G, RateLimitConfig.NetworkPreset.SLOW_2G -> Icons.Default.SignalCellularAlt
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
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = presetColor.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
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
            modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.lg),
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

@Composable
private fun ConfigurationCard(
    config: RateLimitConfig,
    enabled: Boolean,
    onDownloadSpeedChanged: (Long) -> Unit,
    onUploadSpeedChanged: (Long) -> Unit,
    onLatencyChanged: (Long) -> Unit,
    onPacketLossChanged: (Float) -> Unit,
    colors: RateLimitColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.ratelimit_config_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
                modifier = Modifier.semantics { heading() },
            )

            // Download speed slider
            ConfigSlider(
                icon = Icons.Default.CloudDownload,
                label = stringResource(R.string.ratelimit_config_download_speed),
                value = config.downloadSpeedKbps.toFloat(),
                valueText = formatSpeed(config.downloadSpeedKbps),
                minValue = 1f,
                maxValue = 100000f,
                enabled = enabled,
                color = colors.download,
                onValueChange = { onDownloadSpeedChanged(it.toLong()) },
                colors = colors,
            )

            // Upload speed slider
            ConfigSlider(
                icon = Icons.Default.CloudUpload,
                label = stringResource(R.string.ratelimit_config_upload_speed),
                value = config.uploadSpeedKbps.toFloat(),
                valueText = formatSpeed(config.uploadSpeedKbps),
                minValue = 1f,
                maxValue = 100000f,
                enabled = enabled,
                color = colors.upload,
                onValueChange = { onUploadSpeedChanged(it.toLong()) },
                colors = colors,
            )

            // Latency slider
            ConfigSlider(
                icon = Icons.Default.Timer,
                label = stringResource(R.string.ratelimit_config_latency),
                value = config.latencyMs.toFloat(),
                valueText = "${config.latencyMs} ms",
                minValue = 0f,
                maxValue = 5000f,
                enabled = enabled,
                color = colors.latency,
                onValueChange = { onLatencyChanged(it.toLong()) },
                colors = colors,
            )

            // Packet loss slider
            ConfigSlider(
                icon = Icons.Default.Warning,
                label = stringResource(R.string.ratelimit_config_packet_loss),
                value = config.packetLossPercent,
                valueText = "${config.packetLossPercent.toInt()}%",
                minValue = 0f,
                maxValue = 100f,
                enabled = enabled,
                color = colors.packetLoss,
                onValueChange = { onPacketLossChanged(it) },
                colors = colors,
            )
        }
    }
}

@Composable
private fun ConfigSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    valueText: String,
    minValue: Float,
    maxValue: Float,
    enabled: Boolean,
    color: Color,
    onValueChange: (Float) -> Unit,
    colors: RateLimitColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) color else colors.disabled,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) colors.labelPrimary else colors.labelSecondary,
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = if (enabled) color else colors.disabled,
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = minValue..maxValue,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = colors.sliderTrack,
                disabledThumbColor = colors.disabled,
                disabledActiveTrackColor = colors.disabled,
                disabledInactiveTrackColor = colors.sliderTrack,
            ),
        )
    }
}

@Composable
private fun StatisticsCard(
    stats: ThrottleStats,
    colors: RateLimitColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ratelimit_stats_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                    modifier = Modifier.semantics { heading() },
                )
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = stringResource(R.string.ratelimit_stats_title),
                    tint = colors.primary,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_requests_throttled),
                    value = stats.requestsThrottled.toString(),
                    color = colors.primary,
                    colors = colors,
                )
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_packets_dropped),
                    value = stats.packetsDropped.toString(),
                    color = colors.packetLoss,
                    colors = colors,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_total_delay),
                    value = formatDuration(stats.totalDelayMs),
                    color = colors.latency,
                    colors = colors,
                )
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_bytes_throttled),
                    value = formatBytes(stats.bytesThrottled),
                    color = colors.download,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
    colors: RateLimitColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(WormaCeptorDesignSystem.Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.labelSecondary,
        )
    }
}

/**
 * Formats speed in Kbps to a human-readable string.
 */
private fun formatSpeed(kbps: Long): String {
    return if (kbps >= 1000) {
        String.format(Locale.US, "%.1f Mbps", kbps / 1000.0)
    } else {
        "$kbps Kbps"
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun RateLimitScreenPreview() {
    WormaCeptorTheme {
        RateLimitScreen(
            config = RateLimitConfig(
                enabled = true,
                downloadSpeedKbps = 2000L,
                uploadSpeedKbps = 500L,
                latencyMs = 100L,
                packetLossPercent = 1f,
                preset = RateLimitConfig.NetworkPreset.GOOD_3G,
            ),
            stats = ThrottleStats(
                requestsThrottled = 42,
                totalDelayMs = 8500L,
                packetsDropped = 3,
                bytesThrottled = 1_048_576L,
            ),
            selectedPreset = RateLimitConfig.NetworkPreset.GOOD_3G,
            onEnableToggle = {},
            onPresetSelected = {},
            onDownloadSpeedChanged = {},
            onUploadSpeedChanged = {},
            onLatencyChanged = {},
            onPacketLossChanged = {},
            onClearStats = {},
            onResetToDefaults = {},
            onBack = {},
        )
    }
}
