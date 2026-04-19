package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.contracts.MultipartParser
import com.azikar24.wormaceptor.domain.entities.MultipartPart
import com.azikar24.wormaceptor.feature.viewer.R
import org.koin.java.KoinJavaComponent.get

/**
 * An accordion-style view for multipart form data.
 * Each part is displayed as an expandable section showing its headers and content.
 */
@Composable
fun MultipartView(
    multipartData: String,
    modifier: Modifier = Modifier,
    boundary: String? = null,
) {
    val parts = remember(multipartData, boundary) {
        try {
            val parser: MultipartParser = get(MultipartParser::class.java)
            parser.parse(multipartData, boundary)
        } catch (_: RuntimeException) {
            emptyList()
        }
    }

    if (parts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.viewer_multipart_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = WormaCeptorTokens.semantic().textSecondary,
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WormaCeptorTokens.Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(
                    id = R.plurals.viewer_multipart_part_count,
                    count = parts.size,
                    parts.size,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = WormaCeptorTokens.semantic().textSecondary,
            )

            if (boundary != null) {
                val truncated = "${boundary.take(20)}${if (boundary.length > 20) "..." else ""}"
                Text(
                    text = stringResource(R.string.viewer_multipart_boundary, truncated),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = WormaCeptorTokens.semantic().textSecondary.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    ),
                )
            }
        }

        parts.forEachIndexed { index, part ->
            MultipartPartCard(
                part = part,
                index = index,
                initiallyExpanded = parts.size <= 3,
            )
        }
    }
}

@Composable
private fun MultipartPartCard(
    part: MultipartPart,
    index: Int,
    initiallyExpanded: Boolean,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        label = "chevron_rotation",
    )

    val isFile = part.fileName != null
    val partIcon = when {
        part.contentType?.startsWith("image/") == true -> Icons.Default.Image
        isFile -> Icons.Default.AttachFile
        else -> Icons.Default.TextFields
    }

    val accentColor = when {
        part.contentType?.startsWith("image/") == true -> WormaCeptorTokens.Colors.ContentType.image
        isFile -> WormaCeptorTokens.Colors.ContentType.multipart
        else -> WormaCeptorTokens.Colors.ContentType.formData
    }

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        borderColor = if (expanded) {
            accentColor.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        } else {
            WormaCeptorTokens.semantic().surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(WormaCeptorTokens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (expanded) {
                        stringResource(R.string.viewer_body_collapse)
                    } else {
                        stringResource(R.string.viewer_body_expand)
                    },
                    modifier = Modifier
                        .size(WormaCeptorTokens.IconSize.md)
                        .rotate(rotation),
                    tint = WormaCeptorTokens.semantic().textSecondary,
                )

                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                Surface(
                    shape = WormaCeptorTokens.Shapes.chip,
                    color = accentColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                ) {
                    Icon(
                        imageVector = partIcon,
                        contentDescription = stringResource(R.string.viewer_multipart_content_type),
                        modifier = Modifier
                            .padding(WormaCeptorTokens.Spacing.xs)
                            .size(WormaCeptorTokens.IconSize.sm),
                        tint = accentColor,
                    )
                }

                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = part.name.ifEmpty {
                            stringResource(R.string.viewer_multipart_part_label, index + 1)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = WormaCeptorTokens.semantic().textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (part.fileName != null || part.contentType != null) {
                        Text(
                            text = buildString {
                                part.fileName?.let { append(it) }
                                if (part.fileName != null && part.contentType != null) append(" - ")
                                part.contentType?.let { append(it) }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = WormaCeptorTokens.semantic().textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Surface(
                    shape = WormaCeptorTokens.Shapes.chip,
                    color = WormaCeptorTokens.semantic().surfaceVariant,
                ) {
                    Text(
                        text = formatBytes(part.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = WormaCeptorTokens.semantic().textSecondary,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorTokens.Spacing.sm,
                            vertical = WormaCeptorTokens.Spacing.xxs,
                        ),
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
                ),
                exit = shrinkVertically(
                    animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
                ),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    WormaCeptorDivider(style = DividerStyle.Subtle)

                    if (part.headers.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    WormaCeptorTokens.semantic().surfaceVariant.copy(
                                        alpha = WormaCeptorTokens.Alpha.MODERATE,
                                    ),
                                )
                                .padding(WormaCeptorTokens.Spacing.md),
                        ) {
                            Text(
                                text = stringResource(R.string.viewer_multipart_headers),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = WormaCeptorTokens.semantic().textSecondary,
                                modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.xs),
                            )

                            part.headers.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.padding(vertical = WormaCeptorTokens.Spacing.xxs),
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = "$key: ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                            ),
                                            color = WormaCeptorTokens.semantic().accent,
                                        )
                                    }
                                    SelectionContainer {
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                            ),
                                            color = WormaCeptorTokens.semantic().textPrimary,
                                        )
                                    }
                                }
                            }
                        }

                        WormaCeptorDivider(style = DividerStyle.Subtle)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorTokens.Spacing.md),
                    ) {
                        if (part.body.length > 1000) {
                            Column {
                                SelectionContainer {
                                    Text(
                                        text = part.body.take(1000),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                        color = WormaCeptorTokens.semantic().textPrimary,
                                    )
                                }
                                Text(
                                    text = "... (${part.body.length - 1000} more characters)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WormaCeptorTokens.semantic().textSecondary,
                                    modifier = Modifier.padding(top = WormaCeptorTokens.Spacing.xs),
                                )
                            }
                        } else {
                            SelectionContainer {
                                Text(
                                    text = part.body.ifEmpty { "(empty)" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                    ),
                                    color = if (part.body.isEmpty()) {
                                        WormaCeptorTokens.semantic().textSecondary
                                    } else {
                                        WormaCeptorTokens.semantic().textPrimary
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
