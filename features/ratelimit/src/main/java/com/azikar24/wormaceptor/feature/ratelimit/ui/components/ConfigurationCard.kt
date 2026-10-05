package com.azikar24.wormaceptor.feature.ratelimit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.feature.ratelimit.R

@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun ConfigurationCard(
    config: RateLimitConfig,
    formattedDownloadSpeed: String,
    formattedUploadSpeed: String,
    enabled: Boolean,
    onChangeDownloadSpeed: (Long) -> Unit,
    onChangeUploadSpeed: (Long) -> Unit,
    onChangeLatency: (Long) -> Unit,
    onChangePacketLoss: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.ratelimit_config_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = WormaCeptorTokens.semantic().textPrimary,
                modifier = Modifier.semantics { heading() },
            )

            // Download speed slider
            ConfigSlider(
                icon = Icons.Default.CloudDownload,
                label = stringResource(R.string.ratelimit_config_download_speed),
                value = config.downloadSpeedKbps.toFloat(),
                valueText = formattedDownloadSpeed,
                minValue = 1f,
                maxValue = 100_000f,
                enabled = enabled,
                onValueChange = { onChangeDownloadSpeed(it.toLong()) },
            )

            // Upload speed slider
            ConfigSlider(
                icon = Icons.Default.CloudUpload,
                label = stringResource(R.string.ratelimit_config_upload_speed),
                value = config.uploadSpeedKbps.toFloat(),
                valueText = formattedUploadSpeed,
                minValue = 1f,
                maxValue = 100_000f,
                enabled = enabled,
                onValueChange = { onChangeUploadSpeed(it.toLong()) },
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
                onValueChange = { onChangeLatency(it.toLong()) },
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
                onValueChange = { onChangePacketLoss(it) },
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ConfigSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    valueText: String,
    minValue: Float,
    maxValue: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelColor = if (enabled) {
        WormaCeptorTokens.semantic().textPrimary
    } else {
        WormaCeptorTokens.semantic().textSecondary
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = WormaCeptorTokens.semantic().textSecondary,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor,
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = labelColor,
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = minValue..maxValue,
            enabled = enabled,
        )
    }
}
