package com.azikar24.wormaceptor.core.ui.components.list

import android.content.res.Configuration
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorMethodBadge
import com.azikar24.wormaceptor.core.ui.modifier.wormaceptorFocusRing
import com.azikar24.wormaceptor.core.ui.modifier.wormaceptorPressScale
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenDensity
import com.azikar24.wormaceptor.core.ui.theme.tokens.scaled

private val AccentStripeWidth = 3.dp

/**
 * Standardized list item for WormaCeptor.
 *
 * Provides a consistent Row-based layout with optional leading icon, headline + supporting text,
 * and optional trailing content. Follows the common pattern found across all feature modules.
 *
 * When clickable, the item gets a press-scale micro-interaction + focus ring driven by the
 * library's shared interactive-feedback modifiers, both of which honor reduce-motion.
 *
 * @param headline Primary text (single line, medium weight).
 * @param modifier Modifier for the root composable.
 * @param supporting Optional secondary text below the headline.
 * @param leadingContent Optional composable displayed before the text (icon, badge, dot).
 * @param trailingContent Optional composable displayed after the text (icon, button, chevron).
 * @param onClick Optional click handler. When null, the item is not clickable.
 * @param accentColor Optional left-edge context stripe color (e.g., HTTP method color).
 *                    When set, a 3dp stripe is drawn flush to the leading edge.
 */
@Suppress("LongMethod")
@Composable
fun WormaCeptorListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    accentColor: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickableModifier = if (onClick != null) {
        Modifier
            .wormaceptorPressScale(interactionSource)
            .wormaceptorFocusRing(interactionSource, RoundedCornerShape(0.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
    } else {
        Modifier
    }
    val stripeModifier = if (accentColor != null) {
        Modifier.drawWithContent {
            drawContent()
            drawRect(
                color = accentColor,
                topLeft = Offset.Zero,
                size = Size(AccentStripeWidth.toPx(), size.height),
            )
        }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .then(stripeModifier)
            .padding(
                horizontal = WormaCeptorTokens.Spacing.lg.scaled(),
                vertical = WormaCeptorTokens.Spacing.md.scaled(),
            ),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            leadingContent()
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xxs),
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

@Suppress("LongMethod")
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
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
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
                Spacer(Modifier.height(WormaCeptorTokens.Spacing.xs))
                WormaCeptorListItem(
                    headline = "GET /api/v1/users",
                    supporting = "200 OK · 128 ms",
                    accentColor = WormaCeptorTokens.Colors.HttpMethod.get,
                    leadingContent = {
                        Spacer(Modifier.width(WormaCeptorTokens.Spacing.xs))
                        WormaCeptorMethodBadge(method = "GET")
                    },
                    onClick = {},
                )
                WormaCeptorListItem(
                    headline = "POST /api/v1/auth",
                    supporting = "201 Created · 312 ms",
                    accentColor = WormaCeptorTokens.Colors.HttpMethod.post,
                    leadingContent = {
                        Spacer(Modifier.width(WormaCeptorTokens.Spacing.xs))
                        WormaCeptorMethodBadge(method = "POST")
                    },
                    onClick = {},
                )
                WormaCeptorListItem(
                    headline = "DELETE /api/v1/session",
                    supporting = "500 Server Error",
                    accentColor = WormaCeptorTokens.Colors.HttpMethod.delete,
                    leadingContent = {
                        Spacer(Modifier.width(WormaCeptorTokens.Spacing.xs))
                        WormaCeptorMethodBadge(method = "DELETE")
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
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
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
                    headline = "GET /api/v1/users",
                    supporting = "200 OK · 128 ms",
                    accentColor = WormaCeptorTokens.Colors.HttpMethod.get,
                    leadingContent = {
                        Spacer(Modifier.width(WormaCeptorTokens.Spacing.xs))
                        WormaCeptorMethodBadge(method = "GET")
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

@Preview(name = "ListItem - Density Triptych", heightDp = 420)
@Composable
private fun WormaCeptorListItemDensityPreview() {
    Column {
        listOf(
            TokenDensity.Compact to "Compact (85%)",
            TokenDensity.Default to "Default (100%)",
            TokenDensity.Expanded to "Expanded (115%)",
        ).forEach { (density, label) ->
            WormaCeptorTheme(density = density) {
                Surface {
                    Column {
                        WormaCeptorListItem(
                            headline = label,
                            supporting = "200 OK · 128 ms",
                            accentColor = WormaCeptorTokens.Colors.HttpMethod.get,
                            leadingContent = {
                                Spacer(Modifier.width(WormaCeptorTokens.Spacing.xs))
                                WormaCeptorMethodBadge(method = "GET")
                            },
                            onClick = {},
                        )
                        WormaCeptorListItem(
                            headline = "POST /api/v1/auth",
                            supporting = "312 ms",
                            accentColor = WormaCeptorTokens.Colors.HttpMethod.post,
                            leadingContent = {
                                Spacer(Modifier.width(WormaCeptorTokens.Spacing.xs))
                                WormaCeptorMethodBadge(method = "POST")
                            },
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}
