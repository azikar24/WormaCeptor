/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.R
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.IcGithubBuilder
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.WormaceptorLogo

/**
 * Welcome screen for the WormaCeptor demo app.
 *
 * Displays a hero section with logo, feature cards with descriptions,
 * primary CTA buttons, and footer with GitHub link.
 *
 * Design: Modern minimalist with subtle depth, inspired by Linear/Notion.
 * Features clear visual hierarchy and professional developer-tool aesthetic.
 */
@Composable
fun WelcomeScreen(
    onLaunchClick: () -> Unit,
    onTestToolsClick: () -> Unit,
    onGitHubClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFeatureClick: ((WelcomeFeature) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        // Hero Section with Logo and Branding
        HeroSection()

        Spacer(modifier = Modifier.height(40.dp))

        // Feature Cards Grid
        FeatureCardsGrid(onFeatureClick = onFeatureClick)

        Spacer(modifier = Modifier.height(40.dp))

        // Action Buttons
        ActionButtonsSection(
            onLaunchClick = onLaunchClick,
            onTestToolsClick = onTestToolsClick,
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxl))

        // Footer
        Footer(onGitHubClick = onGitHubClick)

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
    }
}

@Composable
private fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo with subtle glow effect container
        Box(
            contentAlignment = Alignment.Center,
        ) {
            // Subtle glow background
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    ),
            )
            // Logo
            Image(
                imageVector = WormaceptorLogo(),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

        // App Name with better typography
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Tagline
        Text(
            text = stringResource(id = R.string.app_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FeatureCardsGrid(onFeatureClick: ((WelcomeFeature) -> Unit)?) {
    val features = listOf(
        WelcomeFeature.Network,
        WelcomeFeature.Crashes,
        WelcomeFeature.Tools,
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        features.forEach { feature ->
            FeatureCard(
                feature = feature,
                onClick = onFeatureClick?.let { { it(feature) } },
            )
        }
    }
}

@Composable
private fun FeatureCard(feature: WelcomeFeature, onClick: (() -> Unit)?) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "feature_card_scale",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            FeatureIcon(feature = feature)
            FeatureTextContent(feature = feature, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FeatureIcon(feature: WelcomeFeature) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
            .background(feature.accentColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = feature.accentColor,
        )
    }
}

@Composable
private fun FeatureTextContent(feature: WelcomeFeature, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = feature.title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = feature.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ActionButtonsSection(onLaunchClick: () -> Unit, onTestToolsClick: () -> Unit) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        PrimaryActionButton(
            text = stringResource(id = R.string.action_launch_title),
            onClick = onLaunchClick,
            modifier = Modifier.fillMaxWidth(),
        )

        SecondaryActionButton(
            text = "Test Tools",
            onClick = onTestToolsClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PrimaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "primary_button_scale",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .scale(scale),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun SecondaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "secondary_button_scale",
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .scale(scale),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun Footer(onGitHubClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 1f,
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "footer_alpha",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        // Version badge
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ) {
            Text(
                text = stringResource(id = R.string.app_version),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.xs,
                ),
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        // GitHub link
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onGitHubClick,
                )
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.xs,
                )
                .alpha(alpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            Icon(
                imageVector = IcGithubBuilder.build(),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
            Text(
                text = stringResource(id = R.string.view_on_github),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}

/**
 * Feature types displayed on the welcome screen.
 * Each feature has an icon, title, description, and accent color.
 */
enum class WelcomeFeature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color,
) {
    Network(
        icon = Icons.Outlined.Wifi,
        title = "Network Inspector",
        description = "Monitor HTTP requests, responses, and headers in real-time",
        accentColor = Color(0xFF3B82F6), // Blue
    ),
    Crashes(
        icon = Icons.Outlined.BugReport,
        title = "Crash Reporter",
        description = "Capture and analyze crashes with detailed stack traces",
        accentColor = Color(0xFFEF4444), // Red
    ),
    Tools(
        icon = Icons.Outlined.Build,
        title = "Developer Tools",
        description = "Access shared preferences, databases, and more",
        accentColor = Color(0xFF8B5CF6), // Purple
    ),
}
