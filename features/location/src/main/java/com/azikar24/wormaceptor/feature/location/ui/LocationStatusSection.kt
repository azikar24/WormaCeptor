package com.azikar24.wormaceptor.feature.location.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.feature.location.R
import com.azikar24.wormaceptor.feature.location.ui.theme.LocationColors

@Composable
internal fun MockLocationWarningBanner() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LocationColors.warning.asSubtleBackground(),
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.location_warning_title),
                    tint = LocationColors.warning,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.location_warning_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                    Text(
                        text = stringResource(R.string.location_warning_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            Button(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            },
                        )
                    } catch (_: Exception) {
                        context.startActivity(
                            Intent(Settings.ACTION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocationColors.warning,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text(stringResource(R.string.location_open_dev_options))
            }
        }
    }
}

@Composable
internal fun MockLocationStatusCard(
    currentMockLocation: MockLocation?,
    isMockEnabled: Boolean,
    onToggle: () -> Unit,
    isEnabled: Boolean,
    isInputValid: Boolean,
) {
    val containerColor =
        if (isMockEnabled) LocationColors.enabled.asSubtleBackground() else MaterialTheme.colorScheme.surface
    val cardBorder = if (isMockEnabled) {
        null
    } else {
        BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        border = cardBorder,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isMockEnabled,
                        enabled = isEnabled,
                        role = Role.Switch,
                        onValueChange = { onToggle() },
                    )
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MockLocationIcon(isMockEnabled)
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.lg))
                MockLocationLabel(currentMockLocation, isMockEnabled, Modifier.weight(1f))
                Switch(
                    checked = isMockEnabled,
                    onCheckedChange = null,
                    enabled = isEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = LocationColors.enabled,
                    ),
                )
            }

            if (isEnabled && !isMockEnabled && !isInputValid) {
                Text(
                    text = stringResource(R.string.location_enable_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = WormaCeptorDesignSystem.Spacing.lg,
                        end = WormaCeptorDesignSystem.Spacing.lg,
                        bottom = WormaCeptorDesignSystem.Spacing.md,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MockLocationIcon(isMockEnabled: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isMockEnabled) LocationColors.enabled else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xxxl),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = stringResource(
                    if (isMockEnabled) R.string.location_mock_active else R.string.location_mock_disabled,
                ),
                tint = if (isMockEnabled) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
            )
        }
    }
}

@Composable
private fun MockLocationLabel(
    currentMockLocation: MockLocation?,
    isMockEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(
                if (isMockEnabled) R.string.location_mock_active else R.string.location_mock_disabled,
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (currentMockLocation != null && isMockEnabled) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
            Text(
                text = currentMockLocation.formatCoordinates(),
                style = MaterialTheme.typography.bodyMedium,
                color = LocationColors.coordinate,
                fontWeight = FontWeight.Medium,
            )
            currentMockLocation.name?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
