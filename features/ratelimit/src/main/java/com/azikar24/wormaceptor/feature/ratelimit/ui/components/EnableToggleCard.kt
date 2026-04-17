package com.azikar24.wormaceptor.feature.ratelimit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.feature.ratelimit.R

@Composable
internal fun EnableToggleCard(
    enabled: Boolean,
    onToggle: () -> Unit,
    colors: ToolColors.RateLimit.Scheme,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val statusColor by animateColorAsState(
        targetValue = if (enabled) colors.enabled else colors.disabled,
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "ratelimit_toggle_status",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (enabled) {
            colors.enabled.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
        },
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "ratelimit_toggle_bg",
    )

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = backgroundColor,
        borderColor = if (enabled) {
            colors.enabled.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = enabled,
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
                Box(
                    modifier = Modifier
                        .size(WormaCeptorTokens.Spacing.xxxl)
                        .clip(WormaCeptorTokens.Shapes.cardLarge)
                        .background(statusColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = stringResource(R.string.ratelimit_title),
                        tint = statusColor,
                        modifier = Modifier.size(WormaCeptorTokens.Spacing.xl),
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
                            stringResource(R.string.ratelimit_toggle_status_active)
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
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.enabled,
                    checkedTrackColor = colors.enabled.copy(alpha = WormaCeptorTokens.Alpha.STRONG),
                ),
            )
        }
    }
}
