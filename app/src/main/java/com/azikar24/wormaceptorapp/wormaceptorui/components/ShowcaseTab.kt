/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.R
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.IcGithubBuilder
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.WormaceptorLogo

/**
 * Showcase tab content for the demo app.
 * Displays a hero section with logo, feature grid, primary CTA, and footer.
 *
 * Design: Minimal, whitespace-driven layout inspired by Linear/Notion.
 * No card borders or backgrounds.
 */
@Composable
fun ShowcaseTab(
    onLaunchClick: () -> Unit,
    onTestToolsClick: () -> Unit,
    onGitHubClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFeatureClick: ((ShowcaseFeature) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxxl))

        // Hero Section
        HeroSection()

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxl))

        // Feature Grid
        FeatureGrid(onFeatureClick = onFeatureClick)

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxl))

        // Primary CTA
        LaunchInspectorButton(onClick = onLaunchClick)

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        // Secondary - Test Tools
        TestToolsButton(onClick = onTestToolsClick)

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxl))

        // Footer
        Footer(onGitHubClick = onGitHubClick)

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
    }
}

@Composable
private fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo - 64dp
        Image(
            imageVector = WormaceptorLogo(),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // App Name - 24sp, 600 weight
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Tagline - 14sp, textSecondary
        Text(
            text = stringResource(id = R.string.app_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun FeatureGrid(onFeatureClick: ((ShowcaseFeature) -> Unit)?) {
    val features = listOf(
        ShowcaseFeature.Network,
        ShowcaseFeature.Crashes,
        ShowcaseFeature.Tools,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        features.forEach { feature ->
            FeatureItem(
                feature = feature,
                onClick = if (onFeatureClick != null) {
                    { onFeatureClick(feature) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun FeatureItem(feature: ShowcaseFeature, onClick: (() -> Unit)?) {
    val modifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier.padding(WormaCeptorDesignSystem.Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Outlined icon - 20dp
        Icon(
            imageVector = feature.icon,
            contentDescription = feature.label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Label - 12sp
        Text(
            text = feature.label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun LaunchInspectorButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "launch_button_scale",
    )

    Button(
        onClick = onClick,
        modifier = Modifier.scale(scale),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        Text(
            text = stringResource(id = R.string.action_launch_title),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
        )
    }
}

@Composable
private fun TestToolsButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        ),
    ) {
        Text(
            text = "Test Tools",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.md,
            ),
        )
    }
}

@Composable
private fun Footer(onGitHubClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Version
        Text(
            text = stringResource(id = R.string.app_version),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // GitHub link
        Row(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onGitHubClick,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = IcGithubBuilder.build(),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))

            Text(
                text = stringResource(id = R.string.view_on_github),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            )
        }
    }
}

/**
 * Feature types displayed in the showcase grid.
 */
enum class ShowcaseFeature(
    val icon: ImageVector,
    val label: String,
) {
    Network(Icons.Outlined.Wifi, "Network"),
    Crashes(Icons.Outlined.BugReport, "Crashes"),
    Tools(Icons.Outlined.Build, "Tools"),
}
