/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.ratelimit.ui

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import com.azikar24.wormaceptor.feature.ratelimit.R
import com.azikar24.wormaceptor.feature.ratelimit.ui.theme.rateLimitColors

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
    selectedPreset: NetworkPreset?,
    onEnableToggle: () -> Unit,
    onPresetSelected: (NetworkPreset?) -> Unit,
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
                            text = "Network Rate Limiter",
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

            // Statistics
            StatisticsCard(
                stats = stats,
                colors = colors,
            )
        }
    }
}

@Composable
private fun EnableToggleCard(
    enabled: Boolean,
    onToggle: () -> Unit,
    colors: com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors,
    modifier: Modifier = Modifier,
) {
    val statusColor by animateColorAsState(
        targetValue = if (enabled) colors.enabled else colors.disabled,
        animationSpec = tween(300),
        label = "status",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
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
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
                    )
                }

                Column {
                    Text(
                        text = "Rate Limiting",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                    Text(
                        text = if (enabled) "Active - throttling network" else "Disabled",
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
    selectedPreset: NetworkPreset?,
    enabled: Boolean,
    onPresetSelected: (NetworkPreset?) -> Unit,
    colors: com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors,
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
                text = "Network Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                NetworkPreset.entries.forEach { preset ->
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
                            label = "Down",
                            value = formatSpeed(preset.downloadKbps),
                            color = colors.download,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.CloudUpload,
                            label = "Up",
                            value = formatSpeed(preset.uploadKbps),
                            color = colors.upload,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.Timer,
                            label = "Latency",
                            value = "${preset.latencyMs}ms",
                            color = colors.latency,
                        )
                        PresetInfoItem(
                            icon = Icons.Default.Warning,
                            label = "Loss",
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
    preset: NetworkPreset,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    colors: com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors,
    modifier: Modifier = Modifier,
) {
    val presetColor = when (preset) {
        NetworkPreset.WIFI -> colors.presetWifi
        NetworkPreset.GOOD_3G, NetworkPreset.REGULAR_3G, NetworkPreset.SLOW_3G -> colors.preset3G
        NetworkPreset.GOOD_2G, NetworkPreset.SLOW_2G -> colors.preset2G
        NetworkPreset.EDGE -> colors.presetEdge
        NetworkPreset.OFFLINE -> colors.presetOffline
    }

    val presetIcon = when (preset) {
        NetworkPreset.WIFI -> Icons.Default.Wifi
        NetworkPreset.GOOD_3G -> Icons.Default.SignalCellular4Bar
        NetworkPreset.REGULAR_3G, NetworkPreset.SLOW_3G -> Icons.Default.SignalCellularAlt
        NetworkPreset.GOOD_2G, NetworkPreset.SLOW_2G -> Icons.Default.SignalCellularAlt
        NetworkPreset.EDGE -> Icons.Default.SignalCellularAlt
        NetworkPreset.OFFLINE -> Icons.Default.SignalCellularOff
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
                contentDescription = null,
                modifier = Modifier.size(18.dp), // Material spec for chip icons
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
            contentDescription = null,
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
    colors: com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors,
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
                text = "Custom Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
            )

            // Download speed slider
            ConfigSlider(
                icon = Icons.Default.CloudDownload,
                label = "Download Speed",
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
                label = "Upload Speed",
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
                label = "Latency",
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
                label = "Packet Loss",
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
    colors: com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors,
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
                    contentDescription = null,
                    tint = if (enabled) color else colors.disabled,
                    modifier = Modifier.size(20.dp), // Material spec for list icon
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
    colors: com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors,
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
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                )
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp), // Material spec for list icon
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "Requests Throttled",
                    value = stats.requestsThrottled.toString(),
                    color = colors.primary,
                    colors = colors,
                )
                StatItem(
                    label = "Packets Dropped",
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
                    label = "Total Delay",
                    value = formatDuration(stats.totalDelayMs),
                    color = colors.latency,
                    colors = colors,
                )
                StatItem(
                    label = "Bytes Throttled",
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
    colors: com.azikar24.wormaceptor.feature.ratelimit.ui.theme.RateLimitColors,
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
    return when {
        kbps >= 1000 -> String.format("%.1f Mbps", kbps / 1000.0)
        else -> "$kbps Kbps"
    }
}

/**
 * Formats bytes to a human-readable string.
 */
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}

/**
 * Formats duration in milliseconds to a human-readable string.
 */
private fun formatDuration(ms: Long): String {
    return when {
        ms >= 60000 -> String.format("%.1f min", ms / 60000.0)
        ms >= 1000 -> String.format("%.1f s", ms / 1000.0)
        else -> "$ms ms"
    }
}
