/*
 * Copyright AziKar24 2025.
 */

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Generic empty state component for WormaCeptor.
 * Displays a customizable empty state with icon, title, subtitle, and optional action.
 *
 * @param title Main heading text
 * @param modifier Modifier for the root composable
 * @param subtitle Optional descriptive text
 * @param icon Optional icon to display
 * @param actionLabel Optional action button label
 * @param onAction Optional action button callback
 */
@Suppress("LongMethod", "MagicNumber") // Composable UI - acceptable length, animation values are self-documenting
@Composable
fun WormaCeptorEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyState")
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
            .fillMaxSize()
            .padding(WormaCeptorDesignSystem.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Visual illustration with pulsing animation
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.2f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.3f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxl))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            OutlinedButton(
                onClick = onAction,
                shape = WormaCeptorDesignSystem.Shapes.button,
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    width = WormaCeptorDesignSystem.BorderWidth.regular,
                ),
            ) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }
        }
    }
}
