package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
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
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.feature.cpu.R

private const val TempCriticalThreshold = 70f
private const val TempWarningThreshold = 50f

@Composable
internal fun SystemInfoCard(
    currentCpu: CpuInfo,
    formattedUptime: String,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardExtraLarge,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.cpu_system_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            SystemInfoMetrics(currentCpu = currentCpu)

            UptimeRow(formattedUptime = formattedUptime)
        }
    }
}

@Composable
private fun SystemInfoMetrics(currentCpu: CpuInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SystemInfoItem(
            icon = Icons.Default.Speed,
            label = stringResource(R.string.cpu_frequency_label),
            value = if (currentCpu.cpuFrequencyMHz > 0) {
                stringResource(R.string.cpu_frequency_value, currentCpu.cpuFrequencyMHz)
            } else {
                stringResource(R.string.cpu_not_available)
            },
            iconTint = WormaCeptorTokens.Colors.Cpu.usage,
        )

        val cpuTemp = currentCpu.cpuTemperature
        SystemInfoItem(
            icon = Icons.Default.Thermostat,
            label = stringResource(R.string.cpu_temperature_label),
            value = cpuTemp?.let {
                stringResource(R.string.cpu_temperature_value, it)
            } ?: stringResource(R.string.cpu_not_available),
            iconTint = when {
                cpuTemp == null -> WormaCeptorTokens.Colors.Status.green
                cpuTemp > TempCriticalThreshold -> WormaCeptorTokens.Colors.Status.red
                cpuTemp > TempWarningThreshold -> WormaCeptorTokens.Colors.Status.amber
                else -> WormaCeptorTokens.Colors.Status.green
            },
        )

        SystemInfoItem(
            icon = Icons.Default.Memory,
            label = stringResource(R.string.cpu_cores_label),
            value = currentCpu.coreCount.toString(),
            iconTint = WormaCeptorTokens.Colors.Cpu.usage,
        )
    }
}

@Composable
private fun UptimeRow(formattedUptime: String) {
    if (formattedUptime.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.cpu_uptime, formattedUptime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun SystemInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(WormaCeptorTokens.Spacing.xl),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SystemInfoCardPreview() {
    WormaCeptorTheme {
        SystemInfoCard(
            currentCpu = CpuInfo(
                timestamp = System.currentTimeMillis(),
                overallUsagePercent = 42f,
                perCoreUsage = listOf(30f, 55f, 20f, 65f),
                coreCount = 8,
                cpuFrequencyMHz = 2400L,
                cpuTemperature = 42.5f,
                uptime = 3_600_000L,
            ),
            formattedUptime = "1h 0m 0s",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SystemInfoCardHighTempPreview() {
    WormaCeptorTheme {
        SystemInfoCard(
            currentCpu = CpuInfo(
                timestamp = System.currentTimeMillis(),
                overallUsagePercent = 88f,
                perCoreUsage = listOf(85f, 90f, 82f, 95f),
                coreCount = 4,
                cpuFrequencyMHz = 3200L,
                cpuTemperature = 75f,
            ),
            formattedUptime = "",
        )
    }
}
