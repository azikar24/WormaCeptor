package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

private const val ExpandAnimationDurationMs = 200

/**
 * Clickable header with expand/collapse icon and animated content reveal.
 * Used for collapsible sections in detail and info screens.
 *
 * @param isExpanded Whether the content is currently visible
 * @param onToggle Callback when the header is clicked
 * @param header Composable content for the header row (excluding the expand icon)
 * @param modifier Modifier for the root composable
 * @param showDivider Whether to show a divider between header and content
 * @param content Composable content revealed when expanded
 */
@Composable
fun WormaCeptorExpandableCard(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    header: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                tint = MaterialTheme.colorScheme.primary,
            )
            header()
        }

        if (showDivider && isExpanded) {
            WormaCeptorDivider(style = DividerStyle.Subtle)
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(ExpandAnimationDurationMs),
            ) + fadeIn(animationSpec = tween(ExpandAnimationDurationMs)),
            exit = shrinkVertically(
                animationSpec = tween(ExpandAnimationDurationMs),
            ) + fadeOut(animationSpec = tween(ExpandAnimationDurationMs)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WormaCeptorDesignSystem.Spacing.sm),
                content = content,
            )
        }
    }
}
