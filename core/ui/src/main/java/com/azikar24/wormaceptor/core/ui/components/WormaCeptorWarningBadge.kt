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
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Animated pulsing warning icon badge for TopAppBar titles.
 * Fades between full opacity and [WormaCeptorTokens.Alpha.BOLD].
 *
 * @param contentDescription Accessibility description for the warning icon
 * @param modifier Modifier for the root composable
 */
@Composable
fun WormaCeptorWarningBadge(
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = WormaCeptorTokens.Alpha.BOLD,
        animationSpec = infiniteRepeatable(
            animation = tween(WormaCeptorTokens.Animation.VERY_SLOW, easing = LinearEasing),
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
                .padding(WormaCeptorTokens.Spacing.xs)
                .size(WormaCeptorTokens.Spacing.lg),
        )
    }
}

@Preview(name = "WarningBadge - Light")
@Composable
private fun WarningBadgeLightPreview() {
    WormaCeptorTheme {
        WormaCeptorWarningBadge(contentDescription = "Warning")
    }
}

@Preview(name = "WarningBadge - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WarningBadgeDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        WormaCeptorWarningBadge(contentDescription = "Warning")
    }
}
