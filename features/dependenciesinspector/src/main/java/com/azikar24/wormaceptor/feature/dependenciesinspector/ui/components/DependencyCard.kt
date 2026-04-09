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
import androidx.compose.ui.graphics.Color
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
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util.categoryColor
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util.shortLabel

private const val CategoryIconBackgroundAlpha = 0.15f

@Composable
internal fun DependencyCard(
    dependency: DependencyInfo,
    onClick: () -> Unit,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    val categoryColor = dependency.category.categoryColor(colors)

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
            CategoryIcon(categoryColor)
            Spacer(Modifier.width(WormaCeptorTokens.Spacing.md))
            DependencyContent(
                dependency = dependency,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            CategoryBadge(dependency.category, categoryColor)
        }
    }
}

@Composable
private fun CategoryIcon(categoryColor: Color) {
    Box(
        Modifier
            .size(WormaCeptorTokens.IconSize.xxl)
            .clip(RoundedCornerShape(WormaCeptorTokens.Radius.md))
            .background(categoryColor.copy(CategoryIconBackgroundAlpha)),
        Alignment.Center,
    ) {
        Icon(
            Icons.Default.Code,
            null,
            tint = categoryColor,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
        )
    }
}

@Composable
private fun DependencyContent(
    dependency: DependencyInfo,
    colors: ToolColors.DependenciesInspector.Scheme,
    modifier: Modifier = Modifier,
) {
    val hasVersion = dependency.version != null

    Column(modifier) {
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
            if (hasVersion) {
                VersionBadge(checkNotNull(dependency.version), colors)
            }
            Text(
                dependency.detectionMethod.displayName(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.labelSecondary,
            )
        }
    }
}

@Composable
private fun VersionBadge(
    version: String,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    Surface(
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
        color = colors.versionDetected.copy(CategoryIconBackgroundAlpha),
    ) {
        Text(
            "v$version",
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

@Composable
private fun CategoryBadge(
    category: DependencyCategory,
    categoryColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
        color = categoryColor.copy(CategoryIconBackgroundAlpha),
    ) {
        Text(
            category.shortLabel(),
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
