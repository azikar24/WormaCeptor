package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.DetailItem
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailHeader
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailSection
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.feature.dependenciesinspector.R
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util.categoryColor

@Composable
internal fun DependencyDetailContent(
    dependency: DependencyInfo,
    colors: ToolColors.DependenciesInspector.Scheme,
    modifier: Modifier = Modifier,
) {
    val depCategoryColor = dependency.category.categoryColor(colors)
    val uriHandler = LocalUriHandler.current

    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg)) {
        DetailHeader(dependency, depCategoryColor, colors)
        DetailDescription(dependency, colors)
        DetailDetailsSection(dependency, colors)
        DetailDetectionSection(dependency, colors)

        dependency.website?.let { url ->
            WebsiteLink(url, colors) { uriHandler.openUri(url) }
        }

        Spacer(Modifier.height(WormaCeptorTokens.Spacing.lg))
    }
}

@Composable
private fun DetailHeader(
    dependency: DependencyInfo,
    depCategoryColor: Color,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    WormaCeptorDetailHeader(
        icon = Icons.Default.Code,
        iconTint = depCategoryColor,
        iconBackgroundColor = depCategoryColor.copy(WormaCeptorTokens.Alpha.SOFT),
        title = dependency.name,
        iconContentDescription = stringResource(R.string.dependenciesinspector_detail_dependency_icon),
        titleColor = colors.labelPrimary,
        subtitle = {
            val version = dependency.version
            if (version != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                ) {
                    Text(
                        dependency.category.displayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = depCategoryColor,
                    )
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                        color = colors.versionDetected.copy(WormaCeptorTokens.Alpha.MEDIUM),
                    ) {
                        Text(
                            stringResource(R.string.dependenciesinspector_detail_version_format, version),
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
            } else {
                Text(
                    dependency.category.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = depCategoryColor,
                )
            }
        },
    )
}

@Composable
private fun DetailDescription(
    dependency: DependencyInfo,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    Text(
        dependency.description,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.labelSecondary,
    )
}

@Composable
private fun DetailDetailsSection(
    dependency: DependencyInfo,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    val packageLabel = stringResource(R.string.dependenciesinspector_detail_label_package)
    val groupIdLabel = stringResource(R.string.dependenciesinspector_detail_label_group_id)
    val artifactIdLabel = stringResource(R.string.dependenciesinspector_detail_label_artifact_id)
    val mavenLabel = stringResource(R.string.dependenciesinspector_detail_label_maven)
    val items = listOfNotNull(
        DetailItem(packageLabel, dependency.packageName),
        dependency.groupId?.let { DetailItem(groupIdLabel, it) },
        dependency.artifactId?.let { DetailItem(artifactIdLabel, it) },
        dependency.mavenCoordinate?.let { DetailItem(mavenLabel, it) },
    )
    if (items.isEmpty()) return
    WormaCeptorDetailSection(
        title = stringResource(R.string.dependenciesinspector_detail_section_details),
        items = items,
        labelColor = colors.labelSecondary,
        valueColor = colors.valuePrimary,
        surfaceColor = colors.searchBackground,
    )
}

@Composable
private fun DetailDetectionSection(
    dependency: DependencyInfo,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    val methodLabel = stringResource(R.string.dependenciesinspector_detail_label_method)
    val confidenceLabel = stringResource(R.string.dependenciesinspector_detail_label_confidence)
    val versionStatusLabel = stringResource(R.string.dependenciesinspector_detail_label_version_status)
    val versionDetected = stringResource(R.string.dependenciesinspector_summary_detected)
    val versionUnknown = stringResource(R.string.dependenciesinspector_summary_unknown)
    WormaCeptorDetailSection(
        title = stringResource(R.string.dependenciesinspector_detail_section_detection),
        items = listOf(
            DetailItem(methodLabel, dependency.detectionMethod.displayName()),
            DetailItem(confidenceLabel, dependency.detectionMethod.confidence()),
            DetailItem(versionStatusLabel, if (dependency.version != null) versionDetected else versionUnknown),
        ),
        labelColor = colors.labelSecondary,
        valueColor = colors.valuePrimary,
        surfaceColor = colors.searchBackground,
    )
}

@Composable
private fun WebsiteLink(
    url: String,
    colors: ToolColors.DependenciesInspector.Scheme,
    onOpenUrl: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onOpenUrl() }
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
