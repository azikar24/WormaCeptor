package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
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
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.viewer_multipart_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WormaCeptorDesignSystem.Spacing.xs),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (boundary != null) {
                val truncated = "${boundary.take(20)}${if (boundary.length > 20) "..." else ""}"
                Text(
                    text = stringResource(R.string.viewer_multipart_boundary, truncated),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.heavy,
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
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "chevron_rotation",
    )

    val isFile = part.fileName != null
    val partIcon = when {
        part.contentType?.startsWith("image/") == true -> Icons.Default.Image
        isFile -> Icons.Default.AttachFile
        else -> Icons.Default.TextFields
    }

    val accentColor = when {
        part.contentType?.startsWith("image/") == true -> Color(0xFF14B8A6)
        isFile -> Color(0xFF6366F1)
        else -> Color(0xFF3B82F6)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                WormaCeptorDesignSystem.Elevation.sm,
            ),
        ),
        border = BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.thin,
            color = if (expanded) {
                accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
            },
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(WormaCeptorDesignSystem.Spacing.md),
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
                        .size(18.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.chip,
                    color = accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
                ) {
                    Icon(
                        imageVector = partIcon,
                        contentDescription = stringResource(R.string.viewer_multipart_content_type),
                        modifier = Modifier
                            .padding(WormaCeptorDesignSystem.Spacing.xs)
                            .size(16.dp),
                        tint = accentColor,
                    )
                }

                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = part.name.ifEmpty {
                            stringResource(R.string.viewer_multipart_part_label, index + 1)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.chip,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = formatBytes(part.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                            vertical = WormaCeptorDesignSystem.Spacing.xxs,
                        ),
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                ),
                exit = shrinkVertically(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                ),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    WormaCeptorDivider(style = DividerStyle.Subtle)

                    if (part.headers.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = WormaCeptorDesignSystem.Alpha.moderate,
                                    ),
                                )
                                .padding(WormaCeptorDesignSystem.Spacing.md),
                        ) {
                            Text(
                                text = stringResource(R.string.viewer_multipart_headers),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.xs),
                            )

                            part.headers.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = "$key: ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Medium,
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    SelectionContainer {
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
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
                            .padding(WormaCeptorDesignSystem.Spacing.md),
                    ) {
                        if (part.body.length > 1000) {
                            Column {
                                SelectionContainer {
                                    Text(
                                        text = part.body.take(1000),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Text(
                                    text = "... (${part.body.length - 1000} more characters)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = WormaCeptorDesignSystem.Spacing.xs),
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
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
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
