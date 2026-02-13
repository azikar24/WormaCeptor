package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Animated pulsing warning icon badge for TopAppBar titles.
 * Fades between full opacity and [WormaCeptorDesignSystem.Alpha.bold].
 *
 * @param contentDescription Accessibility description for the warning icon
 * @param modifier Modifier for the root composable
 */
@Composable
fun WormaCeptorWarningBadge(contentDescription: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = WormaCeptorDesignSystem.Alpha.bold,
        animationSpec = infiniteRepeatable(
            animation = tween(WormaCeptorDesignSystem.AnimationDuration.verySlow, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "warning_alpha",
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error.copy(alpha = alpha),
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier
                .padding(WormaCeptorDesignSystem.Spacing.xs)
                .size(WormaCeptorDesignSystem.Spacing.lg),
        )
    }
}
