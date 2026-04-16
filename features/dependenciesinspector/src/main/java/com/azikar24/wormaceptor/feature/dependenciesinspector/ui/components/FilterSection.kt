package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.feature.dependenciesinspector.R
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util.categoryColor
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util.shortLabel

@Composable
@Suppress("LongParameterList")
internal fun FilterSection(
    selectedCategory: DependencyCategory?,
    summary: DependencySummary,
    showVersionedOnly: Boolean,
    onSelectCategory: (DependencyCategory?) -> Unit,
    onToggleVersionedOnly: (Boolean) -> Unit,
    colors: ToolColors.DependenciesInspector.Scheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier, Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        CategoryChipRow(selectedCategory, summary, onSelectCategory, colors)
        VersionedOnlyToggle(showVersionedOnly, onToggleVersionedOnly, colors)
    }
}

@Composable
private fun CategoryChipRow(
    selectedCategory: DependencyCategory?,
    summary: DependencySummary,
    onSelectCategory: (DependencyCategory?) -> Unit,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    val haptic = LocalHapticFeedback.current
    WormaCeptorScrollableRow(contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg)) {
        WormaCeptorChip(
            label = stringResource(R.string.dependenciesinspector_filter_all),
            selected = selectedCategory == null,
            onClick = {
                if (selectedCategory != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectCategory(null)
                }
            },
            accentColor = colors.primary,
        )

        summary.byCategory.entries
            .sortedByDescending { it.value }
            .forEach { (category, count) ->
                key(category) {
                    val color = category.categoryColor(colors)
                    WormaCeptorChip(
                        label = "${category.shortLabel()} ($count)",
                        selected = selectedCategory == category,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectCategory(if (selectedCategory == category) null else category)
                        },
                        accentColor = color,
                    )
                }
            }
    }
}

@Composable
private fun VersionedOnlyToggle(
    showVersionedOnly: Boolean,
    onToggleVersionedOnly: (Boolean) -> Unit,
    colors: ToolColors.DependenciesInspector.Scheme,
) {
    val haptic = LocalHapticFeedback.current
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
                onToggleVersionedOnly(newValue)
            },
        )
    }
}
