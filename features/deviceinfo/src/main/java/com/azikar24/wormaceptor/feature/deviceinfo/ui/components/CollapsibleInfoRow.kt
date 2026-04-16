package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.deviceinfo.R

@Composable
internal fun CollapsibleInfoRow(
    label: String,
    value: String,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val collapse = stringResource(R.string.deviceinfo_collapse)
    val expand = stringResource(R.string.deviceinfo_expand)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorTokens.Spacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) collapse else expand,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = WormaCeptorTokens.Animations.expandFadeIn,
            exit = WormaCeptorTokens.Animations.shrinkFadeOut,
        ) {
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WormaCeptorTokens.Spacing.xs)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = WormaCeptorTokens.Alpha.MODERATE,
                            ),
                            WormaCeptorTokens.Shapes.chip,
                        )
                        .padding(WormaCeptorTokens.Spacing.sm),
                )
            }
        }
    }
}
