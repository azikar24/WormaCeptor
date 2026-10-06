package com.azikar24.wormaceptor.feature.preferences.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue

/**
 * Card showing one preference key, its value and type.
 *
 * @param item Preference to show
 * @param typeColors Colors keyed by value type
 * @param onClick Called on tap
 * @param onLongClick Called on long press; null disables it, used for read-only files
 * @param modifier Modifier for the card
 * @param expanded Shows the full value instead of two lines
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceItemCard(
    item: PreferenceItem,
    typeColors: ToolColors.Preferences.TypeScheme,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val typeColor = typeColors.forTypeName(item.value.typeName)

    WormaCeptorContainer(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick?.let { longClick ->
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        longClick()
                    }
                },
            ),
    ) {
        PreferenceItemRow(item = item, typeColor = typeColor, expanded = expanded)
    }
}

@Composable
private fun PreferenceItemRow(
    item: PreferenceItem,
    typeColor: Color,
    expanded: Boolean,
) {
    Row(
        modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                    tint = WormaCeptorTokens.semantic().textSecondary,
                )
                Text(
                    text = item.key,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = WormaCeptorTokens.semantic().textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

            Text(
                text = item.value.displayValue,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = WormaCeptorTokens.semantic().textSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

        Surface(
            color = typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
            contentColor = typeColor,
            shape = WormaCeptorTokens.Shapes.chip,
        ) {
            Text(
                text = item.value.typeName,
                style = WormaCeptorTokens.Typography.overline,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    horizontal = WormaCeptorTokens.Spacing.sm,
                    vertical = WormaCeptorTokens.Spacing.xxs,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreferenceItemCardPreview() {
    WormaCeptorTheme {
        PreferenceItemCard(
            item = PreferenceItem(
                key = "dark_mode",
                value = PreferenceValue.BooleanValue(true),
            ),
            typeColors = WormaCeptorTokens.Colors.Preferences.typeScheme(),
            onClick = {},
            onLongClick = {},
        )
    }
}
