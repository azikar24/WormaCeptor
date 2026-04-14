package com.azikar24.wormaceptor.feature.location.ui

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.feature.location.R

@Composable
internal fun MockLocationWarningBanner() {
    val context = LocalContext.current

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = WormaCeptorTokens.Colors.Location.warning.copy(alpha = TokenAlpha.SUBTLE),
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
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
    val containerColor = if (isMockEnabled) {
        WormaCeptorTokens.Colors.Location.enabled.copy(alpha = TokenAlpha.SUBTLE)
    } else {
        MaterialTheme.colorScheme.surface
    }

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = containerColor,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        style = CardStyle.Outlined,
        borderColor = if (isMockEnabled) {
            null
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorTokens.Alpha.MODERATE,
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MockLocationToggleRow(
                currentMockLocation = currentMockLocation,
                isMockEnabled = isMockEnabled,
                onToggle = onToggle,
                isEnabled = isEnabled,
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MockLocationIcon(isMockEnabled)
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.lg))
        MockLocationLabel(currentMockLocation, isMockEnabled, Modifier.weight(1f))
        Switch(
            checked = isMockEnabled,
            onCheckedChange = null,
            enabled = isEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = WormaCeptorTokens.Colors.Location.enabled,
            ),
        )
    }
}

@Composable
private fun MockLocationIcon(isMockEnabled: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isMockEnabled) {
            WormaCeptorTokens.Colors.Location.enabled
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.size(WormaCeptorTokens.Spacing.xxxl),
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
                modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
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
