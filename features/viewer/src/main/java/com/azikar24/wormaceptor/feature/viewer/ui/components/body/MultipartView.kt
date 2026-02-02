/*
 * Copyright AziKar24 2025.
 */

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
import androidx.compose.material3.Divider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.R

/**
 * A data class representing a single part in multipart form data.
 */
data class MultipartPart(
    val name: String,
    val fileName: String? = null,
    val contentType: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val body: String,
    val size: Int = body.length,
)

/**
 * An accordion-style view for multipart form data.
 * Each part is displayed as an expandable section showing its headers and content.
 */
@Composable
fun MultipartView(multipartData: String, boundary: String? = null, modifier: Modifier = Modifier) {
    val parts = remember(multipartData, boundary) {
        parseMultipartData(multipartData, boundary)
    }

    if (parts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No multipart data or invalid format",
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
                text = "${parts.size} ${if (parts.size == 1) "part" else "parts"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (boundary != null) {
                Text(
                    text = "boundary: ${boundary.take(20)}${if (boundary.length > 20) "..." else ""}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
private fun MultipartPartCard(part: MultipartPart, index: Int, initiallyExpanded: Boolean) {
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
                accentColor.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
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
                    contentDescription = if (expanded) "Collapse" else "Expand",
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
                        text = part.name.ifEmpty { "Part ${index + 1}" },
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
                        text = formatSize(part.size),
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
                enter = expandVertically(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
                exit = shrinkVertically(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    )

                    if (part.headers.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                )
                                .padding(WormaCeptorDesignSystem.Spacing.md),
                        ) {
                            Text(
                                text = "Headers",
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

                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        )
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

private fun formatSize(bytes: Int): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

private fun parseMultipartData(data: String, providedBoundary: String?): List<MultipartPart> {
    if (data.isBlank()) return emptyList()

    val boundary = providedBoundary ?: detectBoundary(data) ?: return emptyList()

    val parts = mutableListOf<MultipartPart>()
    val delimiter = "--$boundary"

    val sections = data.split(delimiter)
        .drop(1)
        .filter { !it.trim().startsWith("--") && it.isNotBlank() }

    for (section in sections) {
        if (section.trim() == "--" || section.isBlank()) continue

        val part = parseMultipartPart(section.trim())
        if (part != null) {
            parts.add(part)
        }
    }

    return parts
}

private fun parseMultipartPart(section: String): MultipartPart? {
    val headerBodySplit = section.indexOf("\r\n\r\n").takeIf { it >= 0 }
        ?: section.indexOf("\n\n").takeIf { it >= 0 }
        ?: return null

    val headerSection = section.substring(0, headerBodySplit)
    val body = section.substring(
        headerBodySplit + (if (section.contains("\r\n\r\n")) 4 else 2),
    ).trimEnd()

    val headers = mutableMapOf<String, String>()
    headerSection.lines().forEach { line ->
        val colonIndex = line.indexOf(':')
        if (colonIndex > 0) {
            val key = line.substring(0, colonIndex).trim()
            val value = line.substring(colonIndex + 1).trim()
            headers[key] = value
        }
    }

    val contentDisposition = headers["Content-Disposition"] ?: ""
    val name = extractDispositionParam(contentDisposition, "name")
    val fileName = extractDispositionParam(contentDisposition, "filename")
    val contentType = headers["Content-Type"]

    val displayHeaders = headers.toMutableMap().apply {
        remove("Content-Disposition")
        remove("Content-Type")
    }

    return MultipartPart(
        name = name ?: "unnamed",
        fileName = fileName,
        contentType = contentType,
        headers = displayHeaders,
        body = body,
        size = body.length,
    )
}

private fun extractDispositionParam(disposition: String, param: String): String? {
    val regex = Regex("""$param\s*=\s*"?([^";]+)"?""", RegexOption.IGNORE_CASE)
    return regex.find(disposition)?.groupValues?.getOrNull(1)?.trim()
}

private fun detectBoundary(data: String): String? {
    val firstLine = data.lineSequence().firstOrNull { it.startsWith("--") } ?: return null
    return firstLine.removePrefix("--").trim().takeIf { it.isNotEmpty() }
}
