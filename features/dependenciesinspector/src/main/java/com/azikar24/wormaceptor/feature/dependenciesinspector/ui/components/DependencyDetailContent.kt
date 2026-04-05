package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.feature.dependenciesinspector.R

@Composable
internal fun DependencyDetailContent(
    dependency: DependencyInfo,
    colors: ToolColors.DependenciesInspector.Scheme,
    modifier: Modifier = Modifier,
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
    val uriHandler = LocalUriHandler.current

    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Box(
                Modifier.size(
                    WormaCeptorTokens.IconSize.xxxl,
                ).clip(
                    RoundedCornerShape(WormaCeptorTokens.Radius.lg),
                ).background(categoryColor.copy(0.15f)),
                Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Code,
                    null,
                    tint = categoryColor,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                )
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dependency.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                    if (dependency.version != null) {
                        Spacer(Modifier.width(WormaCeptorTokens.Spacing.sm))
                        Surface(
                            shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                            color = colors.versionDetected.copy(0.2f),
                        ) {
                            Text(
                                "v${dependency.version}",
                                Modifier.padding(
                                    horizontal = WormaCeptorTokens.Spacing.sm,
                                    vertical = WormaCeptorTokens.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.versionText,
                            )
                        }
                    }
                }
                Text(
                    dependency.category.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = categoryColor,
                )
            }
        }

        // Description
        Text(
            dependency.description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.labelSecondary,
        )

        // Details section
        val packageLabel = stringResource(R.string.dependenciesinspector_detail_label_package)
        val groupIdLabel = stringResource(R.string.dependenciesinspector_detail_label_group_id)
        val artifactIdLabel = stringResource(R.string.dependenciesinspector_detail_label_artifact_id)
        val mavenLabel = stringResource(R.string.dependenciesinspector_detail_label_maven)
        DetailSection(
            stringResource(R.string.dependenciesinspector_detail_section_details),
            listOfNotNull(
                packageLabel to dependency.packageName,
                dependency.groupId?.let { groupIdLabel to it },
                dependency.artifactId?.let { artifactIdLabel to it },
                dependency.mavenCoordinate?.let { mavenLabel to it },
            ),
            colors,
        )

        // Detection info
        val methodLabel = stringResource(R.string.dependenciesinspector_detail_label_method)
        val confidenceLabel = stringResource(R.string.dependenciesinspector_detail_label_confidence)
        val versionStatusLabel = stringResource(R.string.dependenciesinspector_detail_label_version_status)
        val versionDetected = stringResource(R.string.dependenciesinspector_summary_detected)
        val versionUnknown = stringResource(R.string.dependenciesinspector_summary_unknown)
        DetailSection(
            stringResource(R.string.dependenciesinspector_detail_section_detection),
            listOf(
                methodLabel to dependency.detectionMethod.displayName(),
                confidenceLabel to dependency.detectionMethod.confidence(),
                versionStatusLabel to if (dependency.version != null) versionDetected else versionUnknown,
            ),
            colors,
        )

        // Website link
        dependency.website?.let { url ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(url) }
                    .padding(vertical = WormaCeptorTokens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Icon(
                    Icons.Default.Language,
                    stringResource(R.string.dependenciesinspector_detail_section_details),
                    tint = colors.link,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
                Text(
                    url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.link,
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(WormaCeptorTokens.Spacing.lg))
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Pair<String, String>>,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    if (items.isEmpty()) return

    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        Text(
            title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
        )
        Surface(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(WormaCeptorTokens.Radius.md),
            colors.searchBackground,
        ) {
            Column(
                Modifier.padding(WormaCeptorTokens.Spacing.md),
                Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                items.forEach { (label, value) ->
                    Column {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.labelSecondary)
                        Text(
                            value,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = colors.valuePrimary,
                        )
                    }
                }
            }
        }
    }
}
