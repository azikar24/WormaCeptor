package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

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
                .padding(vertical = WormaCeptorTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
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
                    .padding(top = WormaCeptorTokens.Spacing.sm),
                content = content,
            )
        }
    }
}

// region Previews

@Preview(name = "ExpandableCard Collapsed - Light")
@Composable
private fun ExpandableCardCollapsedLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorExpandableCard(
                isExpanded = false,
                onToggle = {},
                header = {
                    Text(
                        text = "Response Headers",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = WormaCeptorTokens.Spacing.xs),
                    )
                },
            ) {
                Text("Content-Type: application/json")
            }
        }
    }
}

@Preview(name = "ExpandableCard Collapsed - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExpandableCardCollapsedDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorExpandableCard(
                isExpanded = false,
                onToggle = {},
                header = {
                    Text(
                        text = "Response Headers",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = WormaCeptorTokens.Spacing.xs),
                    )
                },
            ) {
                Text("Content-Type: application/json")
            }
        }
    }
}

@Preview(name = "ExpandableCard Expanded - Light")
@Composable
private fun ExpandableCardExpandedLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorExpandableCard(
                isExpanded = true,
                onToggle = {},
                header = {
                    Text(
                        text = "Response Headers",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = WormaCeptorTokens.Spacing.xs),
                    )
                },
            ) {
                Text("Content-Type: application/json")
                Text("Cache-Control: no-cache")
                Text("X-Request-Id: abc-123-def")
            }
        }
    }
}

@Preview(name = "ExpandableCard Expanded - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExpandableCardExpandedDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorExpandableCard(
                isExpanded = true,
                onToggle = {},
                header = {
                    Text(
                        text = "Response Headers",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = WormaCeptorTokens.Spacing.xs),
                    )
                },
            ) {
                Text("Content-Type: application/json")
                Text("Cache-Control: no-cache")
                Text("X-Request-Id: abc-123-def")
            }
        }
    }
}

// endregion
