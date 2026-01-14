/*
 * Copyright AziKar24 13/1/2026.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import com.azikar24.wormaceptorapp.wormaceptorui.theme.asSubtleBackground
import kotlinx.coroutines.delay

/**
 * Data class representing statistics to display
 */
data class DemoStats(
    val requestsIntercepted: Int = 0,
    val sessionTimeSeconds: Long = 0,
    val crashesCaptured: Int = 0
)

/**
 * A card component that displays live statistics for the demo app
 *
 * Features:
 * - Three-column layout showing requests, session time, and crashes
 * - Material 3 icons for each stat
 * - Smooth fade-in animation on appearance
 * - Animated counter for numbers
 * - Consistent with WormaCeptorDesignSystem
 */
@Composable
fun DemoStatsCard(
    stats: DemoStats,
    modifier: Modifier = Modifier
) {
    // Fade-in animation
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = WormaCeptorDesignSystem.AnimationDuration.slow,
            easing = LinearEasing
        ),
        label = "card_fade_in"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.xs
        )
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = WormaCeptorDesignSystem.BorderWidth.regular,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    shape = WormaCeptorDesignSystem.Shapes.card
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.xl),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Requests Intercepted
                StatItem(
                    icon = Icons.Outlined.NetworkCheck,
                    value = stats.requestsIntercepted,
                    label = "Requests",
                    modifier = Modifier.weight(1f)
                )

                // Vertical Divider
                Divider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(WormaCeptorDesignSystem.BorderWidth.regular),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )

                // Session Time
                StatItem(
                    icon = Icons.Outlined.NetworkCheck, // Using NetworkCheck as placeholder for Timer
                    value = formatSessionTime(stats.sessionTimeSeconds),
                    label = "Session",
                    modifier = Modifier.weight(1f)
                )

                // Vertical Divider
                Divider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(WormaCeptorDesignSystem.BorderWidth.regular),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )

                // Crashes Captured
                StatItem(
                    icon = Icons.Outlined.BugReport,
                    value = stats.crashesCaptured,
                    label = "Crashes",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual stat item with icon, animated value, and label
 */
@Composable
private fun StatItem(
    icon: ImageVector,
    value: Any,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
    ) {
        // Icon with subtle background
        Box(
            modifier = Modifier
                .size(40.dp)
                .wrapContentSize(Alignment.Center)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Animated Value
        AnimatedCounter(
            value = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Animated counter that smoothly transitions between values
 */
@Composable
private fun AnimatedCounter(
    value: Any,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast
                )
            ) togetherWith fadeOut(
                animationSpec = tween(
                    durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast
                )
            )
        },
        label = "counter_animation",
        modifier = modifier
    ) { targetValue ->
        Text(
            text = targetValue.toString(),
            style = style,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Format session time from seconds to human-readable format
 */
private fun formatSessionTime(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            if (remainingSeconds == 0L) "${minutes}m"
            else "${minutes}m ${remainingSeconds}s"
        }
        else -> {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            if (minutes == 0L) "${hours}h"
            else "${hours}h ${minutes}m"
        }
    }
}

// Preview composables
@Preview(showBackground = true)
@Composable
private fun DemoStatsCardPreview() {
    WormaCeptorMainTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            DemoStatsCard(
                stats = DemoStats(
                    requestsIntercepted = 142,
                    sessionTimeSeconds = 3665,
                    crashesCaptured = 3
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoStatsCardPreviewDark() {
    WormaCeptorMainTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            DemoStatsCard(
                stats = DemoStats(
                    requestsIntercepted = 0,
                    sessionTimeSeconds = 45,
                    crashesCaptured = 0
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoStatsCardPreviewLiveUpdate() {
    var stats by remember { mutableStateOf(DemoStats()) }

    LaunchedEffect(Unit) {
        var counter = 0
        while (true) {
            delay(1000)
            counter++
            stats = DemoStats(
                requestsIntercepted = counter * 5,
                sessionTimeSeconds = counter.toLong(),
                crashesCaptured = counter / 10
            )
        }
    }

    WormaCeptorMainTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            DemoStatsCard(stats = stats)
        }
    }
}
