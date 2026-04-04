package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Animated pulsing dot for TopAppBar titles showing monitoring state.
 * Pulses alpha when active, stays static when inactive.
 *
 * @param isActive Whether monitoring is currently active
 * @param modifier Modifier for the root composable
 * @param activeColor Color when active (defaults to primary)
 * @param inactiveColor Color when inactive (defaults to outline)
 */
@Composable
fun WormaCeptorMonitoringIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline,
) {
    val baseColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(WormaCeptorTokens.Animation.SLOW),
        label = "monitoring_indicator_color",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "monitoring_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) WormaCeptorTokens.Alpha.BOLD else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                WormaCeptorTokens.Animation.VERY_SLOW,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "monitoring_pulse_alpha",
    )

    Box(
        modifier = modifier
            .size(WormaCeptorTokens.Spacing.sm)
            .clip(CircleShape)
            .background(baseColor.copy(alpha = if (isActive) alpha else 1f)),
    )
}

@Preview(name = "MonitoringIndicator")
@Composable
private fun MonitoringIndicatorPreview() {
    WormaCeptorTheme {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WormaCeptorMonitoringIndicator(isActive = true)
                WormaCeptorMonitoringIndicator(isActive = false)
            }
        }
    }
}
