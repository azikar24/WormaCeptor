package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun CollapsibleSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: (() -> Unit)? = null,
    isCopyLoading: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = WormaCeptorTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    tint = WormaCeptorTokens.semantic().accent,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = WormaCeptorTokens.semantic().accent,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailingContent?.invoke()

                if (onCopy != null) {
                    IconButton(
                        onClick = onCopy,
                        enabled = !isCopyLoading,
                    ) {
                        if (isCopyLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                                strokeWidth = WormaCeptorTokens.BorderWidth.thick,
                                color = WormaCeptorTokens.semantic().accent,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.viewer_body_copy),
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                                tint = WormaCeptorTokens.semantic().textSecondary,
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.MEDIUM),
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.MEDIUM),
            ) + fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WormaCeptorTokens.Spacing.sm,
                        bottom = WormaCeptorTokens.Spacing.md,
                    ),
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun HeadersView(headers: Map<String, List<String>>) {
    Column {
        headers.forEach { (key, values) ->
            DetailRow(key, values.joinToString(", "))
        }
    }
}

@Composable
internal fun DetailRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.padding(vertical = WormaCeptorTokens.Spacing.xs)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = WormaCeptorTokens.semantic().accent,
        )
        SelectionContainer {
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollapsibleSectionPreview() {
    WormaCeptorTheme {
        CollapsibleSection(
            title = "Headers",
            isExpanded = true,
            onToggle = {},
            onCopy = {},
        ) {
            Text(
                text = "Content-Type: application/json",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
