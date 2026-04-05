package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.feature.dependenciesinspector.R
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util.shortLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FilterSection(
    selectedCategory: DependencyCategory?,
    summary: DependencySummary,
    showVersionedOnly: Boolean,
    onCategorySelected: (DependencyCategory?) -> Unit,
    onShowVersionedOnlyChanged: (Boolean) -> Unit,
    colors: ToolColors.DependenciesInspector.Scheme,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier, Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        // Category filter chips - scrollable horizontally
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.dependenciesinspector_filter_all)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(WormaCeptorTokens.Alpha.MEDIUM),
                ),
            )

            // Show categories that have detected dependencies, sorted by count
            summary.byCategory.entries
                .sortedByDescending { it.value }
                .forEach { (category, count) ->
                    val color = when (category) {
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
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            onCategorySelected(if (selectedCategory == category) null else category)
                        },
                        label = { Text("${category.shortLabel()} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(WormaCeptorTokens.Alpha.MEDIUM),
                            selectedLabelColor = color,
                        ),
                    )
                }
        }

        // Version filter toggle
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                stringResource(R.string.dependenciesinspector_filter_versioned_only),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.labelPrimary,
            )
            Switch(
                checked = showVersionedOnly,
                onCheckedChange = { newValue ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShowVersionedOnlyChanged(newValue)
                },
            )
        }
    }
}
