package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.feature.cpu.R
import com.azikar24.wormaceptor.feature.cpu.ui.theme.CpuColors
import java.util.Locale

@Composable
internal fun SystemInfoCard(
    currentCpu: CpuInfo,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.cpu_system_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
                modifier = Modifier.semantics { heading() },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // CPU Frequency
                SystemInfoItem(
                    icon = Icons.Default.Speed,
                    label = stringResource(R.string.cpu_frequency_label),
                    value = if (currentCpu.cpuFrequencyMHz > 0) {
                        stringResource(R.string.cpu_frequency_value, currentCpu.cpuFrequencyMHz)
                    } else {
                        stringResource(R.string.cpu_not_available)
                    },
                    iconTint = colors.cpuUsage,
                    colors = colors,
                )

                // Temperature
                val cpuTemp = currentCpu.cpuTemperature
                SystemInfoItem(
                    icon = Icons.Default.Thermostat,
                    label = stringResource(R.string.cpu_temperature_label),
                    value = cpuTemp?.let {
                        String.format(Locale.US, stringResource(R.string.cpu_temperature_value), it)
                    } ?: stringResource(R.string.cpu_not_available),
                    iconTint = when {
                        cpuTemp != null && cpuTemp > 70f -> colors.critical
                        cpuTemp != null && cpuTemp > 50f -> colors.warning
                        else -> colors.normal
                    },
                    colors = colors,
                )

                // Core count
                SystemInfoItem(
                    icon = Icons.Default.Memory,
                    label = stringResource(R.string.cpu_cores_label),
                    value = "${currentCpu.coreCount}",
                    iconTint = colors.cpuUsage,
                    colors = colors,
                )
            }

            // Uptime
            if (currentCpu.uptime > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.cpu_uptime, formatUptime(currentCpu.uptime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.labelSecondary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.labelSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = colors.labelPrimary,
        )
    }
}

/**
 * Formats uptime in milliseconds to a human-readable string.
 */
internal fun formatUptime(uptimeMs: Long): String {
    val seconds = uptimeMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
        hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
