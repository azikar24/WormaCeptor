package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Generic error state component for WormaCeptor.
 * Mirrors [WormaCeptorEmptyState] API but uses error styling.
 *
 * Displays an animated error icon with title, optional detail message, and optional retry action.
 *
 * @param title Main error heading
 * @param modifier Modifier for the root composable
 * @param message Optional detailed error description
 * @param icon Error icon (defaults to [Icons.Filled.ErrorOutline])
 * @param retryLabel Optional retry button label
 * @param onRetry Optional retry callback
 */
@Suppress("LongMethod", "MagicNumber")
@Composable
fun WormaCeptorErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Filled.ErrorOutline,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "errorState")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(
        modifier = modifier
            .padding(WormaCeptorDesignSystem.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha * 0.2f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha * 0.3f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxl))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.HEAVY,
                ),
                textAlign = TextAlign.Center,
            )
        }

        if (retryLabel != null && onRetry != null) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            WormaCeptorButton(
                text = retryLabel,
                onClick = onRetry,
                variant = ButtonVariant.Outlined,
            )
        }
    }
}
