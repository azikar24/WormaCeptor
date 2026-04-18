package com.azikar24.wormaceptor.feature.location.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.toggle.SwitchVariant
import com.azikar24.wormaceptor.core.ui.components.toggle.WormaCeptorSwitch
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.feature.location.R

@Composable
internal fun MockLocationWarningBanner() {
    val context = LocalContext.current

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        backgroundColor = WormaCeptorTokens.Colors.Location.warning.copy(alpha = TokenAlpha.SUBTLE),
        borderColor = WormaCeptorTokens.Colors.Location.warning.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        shape = WormaCeptorTokens.Shapes.card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.location_warning_title),
                    tint = WormaCeptorTokens.Colors.Location.warning,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                )
                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.location_warning_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))
                    Text(
                        text = stringResource(R.string.location_warning_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            WormaCeptorButton(
                text = stringResource(R.string.location_open_dev_options),
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
                variant = ButtonVariant.Primary,
                containerColor = WormaCeptorTokens.Colors.Location.warning,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
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
    val statusColor by animateColorAsState(
        targetValue = if (isMockEnabled) {
            WormaCeptorTokens.Colors.Location.enabled
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "location_toggle_status",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isMockEnabled) {
            WormaCeptorTokens.Colors.Location.enabled.copy(alpha = TokenAlpha.SUBTLE)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD)
        },
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "location_toggle_bg",
    )

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        backgroundColor = containerColor,
        borderColor = if (isMockEnabled) {
            WormaCeptorTokens.Colors.Location.enabled.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        } else {
            null
        },
        shape = WormaCeptorTokens.Shapes.cardLarge,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MockLocationToggleRow(
                currentMockLocation = currentMockLocation,
                isMockEnabled = isMockEnabled,
                onToggle = onToggle,
                isEnabled = isEnabled,
                statusColor = statusColor,
            )

            val showHint = isEnabled && !isMockEnabled && !isInputValid
            if (showHint) {
                Text(
                    text = stringResource(R.string.location_enable_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = WormaCeptorTokens.Spacing.lg,
                        end = WormaCeptorTokens.Spacing.lg,
                        bottom = WormaCeptorTokens.Spacing.md,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MockLocationToggleRow(
    currentMockLocation: MockLocation?,
    isMockEnabled: Boolean,
    onToggle: () -> Unit,
    isEnabled: Boolean,
    statusColor: Color,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isMockEnabled,
                enabled = isEnabled,
                role = Role.Switch,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle()
                },
            )
            .padding(WormaCeptorTokens.Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            MockLocationIcon(isMockEnabled = isMockEnabled, statusColor = statusColor)
            MockLocationLabel(currentMockLocation, isMockEnabled, Modifier)
        }
        WormaCeptorSwitch(
            checked = isMockEnabled,
            onCheckedChange = null,
            enabled = isEnabled,
            variant = SwitchVariant.Accent(
                color = WormaCeptorTokens.Colors.Location.enabled,
            ),
        )
    }
}

@Composable
private fun MockLocationIcon(
    isMockEnabled: Boolean,
    statusColor: Color,
) {
    Box(
        modifier = Modifier
            .size(WormaCeptorTokens.Spacing.xxxl)
            .clip(WormaCeptorTokens.Shapes.cardLarge)
            .background(statusColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = stringResource(
                if (isMockEnabled) R.string.location_mock_active else R.string.location_mock_disabled,
            ),
            tint = statusColor,
            modifier = Modifier.size(WormaCeptorTokens.Spacing.xl),
        )
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
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))
            Text(
                text = currentMockLocation.formatCoordinates(),
                style = MaterialTheme.typography.bodyMedium,
                color = WormaCeptorTokens.Colors.Location.coordinate,
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
