package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.feature.dependenciesinspector.R
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util.shortLabel

@Composable
internal fun DependencyCard(
    dependency: DependencyInfo,
    onClick: () -> Unit,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    val categoryColor = when (dependency.category) {
        DependencyCategory.NETWORKING -> colors.networking
        DependencyCategory.DEPENDENCY_INJECTION -> colors.dependencyInjection
        DependencyCategory.UI_FRAMEWORK -> colors.uiFramework
        DependencyCategory.IMAGE_LOADING -> colors.imageLoading
        DependencyCategory.SERIALIZATION -> colors.serialization
        DependencyCategory.DATABASE -> colors.database
        DependencyCategory.REACTIVE -> colors.reactive
        DependencyCategory.LOGGING -> colors.logging
        DependencyCategory.ANALYTICS -> colors.analytics
        DependencyCategory.TESTING -> colors.testing
        DependencyCategory.SECURITY -> colors.security
        DependencyCategory.UTILITY -> colors.utility
        DependencyCategory.ANDROIDX -> colors.androidx
        DependencyCategory.KOTLIN -> colors.kotlin
        DependencyCategory.OTHER -> colors.other
    }
    val hasVersion = dependency.version != null

    WormaCeptorCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = colors.cardBackground,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category icon
            Box(
                Modifier.size(
                    WormaCeptorTokens.IconSize.xxl,
                ).clip(
                    RoundedCornerShape(WormaCeptorTokens.Radius.md),
                ).background(categoryColor.copy(0.15f)),
                Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Code,
                    null,
                    tint = categoryColor,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
            }

            Spacer(Modifier.width(WormaCeptorTokens.Spacing.md))

            // Info
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dependency.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (hasVersion) {
                        Spacer(Modifier.width(WormaCeptorTokens.Spacing.sm))
                        Icon(
                            Icons.Default.CheckCircle,
                            stringResource(R.string.dependenciesinspector_status_version_detected),
                            tint = colors.versionDetected,
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                        )
                    }
                }

                Text(
                    dependency.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = colors.labelSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
                    // Version badge
                    if (hasVersion) {
                        Surface(
                            shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                            color = colors.versionDetected.copy(0.15f),
                        ) {
                            Text(
                                "v${dependency.version}",
                                Modifier.padding(
                                    horizontal = WormaCeptorTokens.Spacing.sm,
                                    vertical = WormaCeptorTokens.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.versionText,
                            )
                        }
                    }

                    // Detection method badge
                    Text(
                        dependency.detectionMethod.displayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.labelSecondary,
                    )
                }
            }

            // Category badge
            Surface(
                shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                color = categoryColor.copy(0.15f),
            ) {
                Text(
                    dependency.category.shortLabel(),
                    Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.sm,
                        vertical = WormaCeptorTokens.Spacing.xxs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor,
                )
            }
        }
    }
}
