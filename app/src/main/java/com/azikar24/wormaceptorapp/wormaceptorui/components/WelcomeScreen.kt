package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptorapp.BuildConfig
import com.azikar24.wormaceptorapp.R
import com.azikar24.wormaceptorapp.wormaceptorui.drawables.IcGithubBuilder
import com.azikar24.wormaceptorapp.wormaceptorui.drawables.rememberWormaceptorLogo

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
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = WormaCeptorTokens.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(WormaCeptorTokens.TouchTarget.large))

        HeroSection()

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxxl))

        FeatureCardsGrid(onFeatureClick = onFeatureClick)

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxxl))

        ActionButtonsSection(
            onLaunchClick = onLaunchClick,
            onTestToolsClick = onTestToolsClick,
        )

        Spacer(modifier = Modifier.weight(1f))

        Footer(onGitHubClick = onGitHubClick)

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg + navigationBarPadding))
    }
}

@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier.padding(top = WormaCeptorTokens.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = rememberWormaceptorLogo(),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier.size(WormaCeptorTokens.TouchTarget.large),
        )

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
            color = WormaCeptorTokens.semantic().textPrimary,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        Text(
            text = stringResource(id = R.string.app_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = WormaCeptorTokens.semantic().textPrimary.copy(alpha = WormaCeptorTokens.Alpha.INTENSE),
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
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
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
private fun FeatureCard(
    feature: WelcomeFeature,
    onClick: (() -> Unit)?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.FAST),
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
        shape = WormaCeptorTokens.Shapes.cardLarge,
        color = WormaCeptorTokens.semantic().surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
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
            .size(WormaCeptorTokens.TouchTarget.minimum)
            .clip(WormaCeptorTokens.Shapes.card)
            .background(feature.accentColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
            tint = feature.accentColor,
        )
    }
}

@Composable
private fun FeatureTextContent(
    feature: WelcomeFeature,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xxs),
    ) {
        Text(
            text = stringResource(feature.titleRes),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = WormaCeptorTokens.semantic().textPrimary,
        )
        Text(
            text = stringResource(feature.descriptionRes),
            style = MaterialTheme.typography.bodySmall,
            color = WormaCeptorTokens.semantic().textPrimary.copy(alpha = WormaCeptorTokens.Alpha.INTENSE),
        )
    }
}

@Composable
private fun ActionButtonsSection(
    onLaunchClick: () -> Unit,
    onTestToolsClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        WormaCeptorButton(
            text = stringResource(id = R.string.action_launch_title),
            onClick = onLaunchClick,
            modifier = Modifier.fillMaxWidth(),
        )

        WormaCeptorButton(
            text = stringResource(R.string.action_test_tools),
            onClick = onTestToolsClick,
            variant = ButtonVariant.Outlined,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Footer(onGitHubClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) WormaCeptorTokens.Alpha.INTENSE else WormaCeptorTokens.Alpha.OPAQUE,
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.FAST),
        label = "footer_alpha",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        // Version badge
        Surface(
            shape = WormaCeptorTokens.Shapes.button,
            color = WormaCeptorTokens.semantic().surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.STRONG),
        ) {
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = WormaCeptorTokens.semantic().textPrimary.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
                modifier = Modifier.padding(
                    horizontal = WormaCeptorTokens.Spacing.sm,
                    vertical = WormaCeptorTokens.Spacing.xs,
                ),
            )
        }

        // GitHub link
        Row(
            modifier = Modifier
                .clip(WormaCeptorTokens.Shapes.button)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onGitHubClick,
                )
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.sm,
                    vertical = WormaCeptorTokens.Spacing.xs,
                )
                .alpha(alpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
        ) {
            Icon(
                imageVector = IcGithubBuilder.build(),
                contentDescription = null,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                tint = WormaCeptorTokens.semantic().textPrimary.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
            )
            Text(
                text = stringResource(id = R.string.view_on_github),
                style = MaterialTheme.typography.labelSmall,
                color = WormaCeptorTokens.semantic().textPrimary.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
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
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val accentColor: Color,
) {
    Network(
        icon = Icons.Outlined.Wifi,
        titleRes = R.string.feature_network_title,
        descriptionRes = R.string.feature_network_description,
        accentColor = WormaCeptorTokens.Colors.Status.blue,
    ),
    Crashes(
        icon = Icons.Outlined.BugReport,
        titleRes = R.string.feature_crashes_title,
        descriptionRes = R.string.feature_crashes_description,
        accentColor = WormaCeptorTokens.Colors.Status.red,
    ),
    Tools(
        icon = Icons.Outlined.Build,
        titleRes = R.string.feature_tools_title,
        descriptionRes = R.string.feature_tools_description,
        accentColor = WormaCeptorTokens.Colors.Category.simulation,
    ),
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    WormaCeptorTheme {
        WelcomeScreen(
            onLaunchClick = {},
            onTestToolsClick = {},
            onGitHubClick = {},
            onFeatureClick = {},
        )
    }
}
