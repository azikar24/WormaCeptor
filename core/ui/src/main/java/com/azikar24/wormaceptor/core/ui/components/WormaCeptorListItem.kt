package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme

/**
 * Standardized list item for WormaCeptor.
 *
 * Provides a consistent Row-based layout with optional leading icon, headline + supporting text,
 * and optional trailing content. Follows the common pattern found across all feature modules.
 *
 * @param headline Primary text (single line, medium weight)
 * @param modifier Modifier for the root composable
 * @param supporting Optional secondary text below the headline
 * @param leadingContent Optional composable displayed before the text (icon, badge, dot)
 * @param trailingContent Optional composable displayed after the text (icon, button, chevron)
 * @param onClick Optional click handler. When null, the item is not clickable.
 */
@Composable
fun WormaCeptorListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.md,
            ),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            leadingContent()
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Preview(name = "ListItem - Light")
@Composable
private fun WormaCeptorListItemPreview() {
    WormaCeptorTheme {
        Surface {
            Column {
                WormaCeptorListItem(
                    headline = "shared_prefs.xml",
                    supporting = "24 entries",
                    leadingContent = {
                        Icon(
                            Icons.Default.Folder,
                            null,
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {},
                )
                WormaCeptorListItem(
                    headline = "app_database.db",
                    supporting = "3 tables - 128 KB",
                    leadingContent = {
                        Icon(
                            Icons.Default.Storage,
                            null,
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    },
                    onClick = {},
                )
                WormaCeptorListItem(
                    headline = "Simple item without icon",
                )
            }
        }
    }
}

@Preview(name = "ListItem - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WormaCeptorListItemDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Column {
                WormaCeptorListItem(
                    headline = "shared_prefs.xml",
                    supporting = "24 entries",
                    leadingContent = {
                        Icon(
                            Icons.Default.Folder,
                            null,
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {},
                )
                WormaCeptorListItem(
                    headline = "Simple item",
                    supporting = "Dark mode variant",
                )
            }
        }
    }
}
