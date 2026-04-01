package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.engine.ParserRegistry
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.ContentTypeChip

/**
 * Data class holding information about a single search match.
 */
internal data class MatchInfo(
    val globalPosition: Int,
    val lineIndex: Int,
)

/** Maximum body size (in chars) for which syntax highlighting is attempted. */
internal const val MAX_SYNTAX_HIGHLIGHT_SIZE = 200_000

internal const val MaxParseBodySize = 500_000
internal const val TruncatedDisplaySize = 100_000

@Composable
internal fun PrettyRawToggle(
    isPretty: Boolean,
    onToggle: () -> Unit,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val radius = WormaCeptorDesignSystem.CornerRadius.xs
    val shape = RoundedCornerShape(radius)

    val borderColor by animateColorAsState(
        targetValue = activeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE),
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
        label = "segment_border",
    )

    Row(
        modifier = Modifier
            .clip(shape)
            .border(WormaCeptorDesignSystem.BorderWidth.thin, borderColor, shape),
    ) {
        SegmentOption(
            text = stringResource(R.string.viewer_body_pretty),
            isSelected = isPretty,
            activeColor = activeColor,
            onClick = { if (!isPretty) onToggle() },
        )
        SegmentOption(
            text = stringResource(R.string.viewer_body_raw),
            isSelected = !isPretty,
            activeColor = activeColor,
            onClick = { if (isPretty) onToggle() },
        )
    }
}

@Composable
private fun SegmentOption(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            activeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.LIGHT)
        } else {
            Color.Transparent
        },
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
        label = "segment_bg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
        label = "segment_text",
    )

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = textColor,
        modifier = Modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
    )
}

/**
 * Responsive row of body controls that wraps on small screens.
 * Uses FlowRow to allow badges to flow to multiple lines when space is constrained.
 *
 * Layout priority (from most to least important):
 * 1. Pretty/Raw toggle - primary interaction for viewing mode
 * 2. Zoom button - quick access to fullscreen view
 * 3. Content type chip - informational, can wrap to next row
 */
@Composable
internal fun BodyControlsRow(
    contentType: ContentType,
    isPrettyMode: Boolean,
    onPrettyModeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Content type chip - informational, can wrap first
        ContentTypeChip(contentType = contentType)
        // Pretty/Raw toggle - most important, always visible
        PrettyRawToggle(
            isPretty = isPrettyMode,
            onToggle = onPrettyModeToggle,
        )
    }
}

/**
 * Find all match positions using indexOf - faster than regex for simple substring matching.
 * O(n) where n = text length.
 */
internal fun findMatchRanges(
    text: String,
    query: String,
): List<IntRange> {
    if (query.isEmpty()) return emptyList()
    val matches = ArrayList<IntRange>(64) // Pre-size for typical case
    var index = 0
    while (true) {
        index = text.indexOf(query, index, ignoreCase = true)
        if (index < 0) break
        matches.add(index until index + query.length)
        index++
    }
    return matches
}

/**
 * Build base AnnotatedString with ALL matches highlighted (yellow).
 * This is cached and reused - only rebuilt when text/query changes.
 * Current match is NOT highlighted here - it uses an overlay for true O(1).
 */
internal fun buildBaseHighlightedText(
    text: String,
    matchRanges: List<IntRange>,
    syntaxHighlighted: AnnotatedString? = null,
): AnnotatedString {
    val base = syntaxHighlighted ?: AnnotatedString(text)
    if (matchRanges.isEmpty()) return base

    return buildAnnotatedString {
        append(base)
        val defaultStyle = SpanStyle(
            background = WormaCeptorColors.Accent.Highlight.copy(alpha = WormaCeptorDesignSystem.Alpha.STRONG),
        )
        matchRanges.forEach { range ->
            addStyle(defaultStyle, range.first, range.last + 1)
        }
    }
}

/**
 * True O(1) highlighted text component.
 * Uses overlay for current match instead of rebuilding AnnotatedString.
 */
@Composable
internal fun HighlightedBodyText(
    text: String,
    query: String,
    currentMatchGlobalPos: Int?,
    modifier: Modifier = Modifier,
    syntaxHighlighted: AnnotatedString? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    // Level 1: Cache match ranges - only when text/query changes
    val matchRanges = remember(text, query) {
        findMatchRanges(text, query)
    }

    // Level 2: Cache base text with ALL matches in yellow - only when text/query changes
    val baseHighlighted = remember(text, matchRanges, syntaxHighlighted) {
        buildBaseHighlightedText(text, matchRanges, syntaxHighlighted)
    }

    // Track layout for overlay positioning
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Current match range for overlay - O(1) lookup
    val currentMatchRange = remember(currentMatchGlobalPos, matchRanges) {
        if (currentMatchGlobalPos == null) {
            null
        } else {
            matchRanges.find { it.first == currentMatchGlobalPos }
        }
    }

    // Current match PATH (not bounds) - handles wrapped text correctly
    val currentMatchPath = remember(textLayoutResult, currentMatchRange) {
        val layout = textLayoutResult ?: return@remember null
        val range = currentMatchRange ?: return@remember null

        try {
            layout.getPathForRange(range.first, range.last + 1)
        } catch (_: Exception) {
            null
        }
    }

    val highlightColor = WormaCeptorColors.StatusBlue.copy(alpha = WormaCeptorDesignSystem.Alpha.HEAVY)

    Box(modifier = modifier) {
        // Current match overlay using Canvas to draw the actual path shape
        // This correctly handles text that wraps across multiple lines
        currentMatchPath?.let { path ->
            Canvas(
                modifier = Modifier.matchParentSize(),
            ) {
                drawPath(path, highlightColor)
            }
        }

        // Base text with yellow highlights (cached, never changes on "next")
        Text(
            text = baseHighlighted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = {
                textLayoutResult = it
                onTextLayout(it)
            },
        )
    }
}

internal fun isProtobufContentType(contentType: String?): Boolean {
    return detectContentTypeViaRegistry(contentType, null) == ContentType.PROTOBUF
}

internal fun detectContentTypeViaRegistry(
    contentTypeHeader: String?,
    body: String?,
): ContentType {
    return try {
        val registry: ParserRegistry = org.koin.java.KoinJavaComponent.get(ParserRegistry::class.java)
        registry.detectContentType(contentTypeHeader, body)
    } catch (_: RuntimeException) {
        ContentType.UNKNOWN
    }
}

internal fun extractMultipartBoundaryViaRegistry(contentType: String): String? {
    return try {
        val registry: ParserRegistry = org.koin.java.KoinJavaComponent.get(ParserRegistry::class.java)
        registry.extractMultipartBoundary(contentType)
    } catch (_: RuntimeException) {
        null
    }
}

internal fun parseBodyViaRegistry(
    contentType: String?,
    bytes: ByteArray,
    rawFallback: String,
): Pair<String, ContentType> {
    if (bytes.size > MaxParseBodySize) {
        val truncated = rawFallback.take(TruncatedDisplaySize) +
            "\n\n... (Rest of content truncated for performance) ..."
        return truncated to ContentType.PLAIN_TEXT
    }
    return try {
        val registry: ParserRegistry =
            org.koin.java.KoinJavaComponent.get(ParserRegistry::class.java)
        val parsed = registry.parseBody(contentType, bytes)
        parsed.formatted to parsed.contentType
    } catch (_: RuntimeException) {
        rawFallback to ContentType.UNKNOWN
    }
}

/**
 * Saves PDF data to the device's Downloads directory.
 *
 * @return Message describing the result of the operation
 */
internal fun savePdfToDownloads(
    context: Context,
    pdfData: ByteArray,
): String {
    return try {
        val fileName = "wormaceptor_${System.currentTimeMillis()}.pdf"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ use MediaStore
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues,
            )

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pdfData)
                }

                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                "PDF saved to Downloads"
            } else {
                "Failed to save PDF"
            }
        } else {
            // Legacy approach for older Android versions
            @Suppress("DEPRECATION")
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS,
            )
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { it.write(pdfData) }
            "PDF saved to Downloads"
        }
    } catch (e: Exception) {
        "Failed to save PDF: ${e.message}"
    }
}

internal fun generateTextSummary(
    transaction: com.azikar24.wormaceptor.domain.entities.NetworkTransaction,
    requestBody: String? = null,
    responseBody: String? = null,
): String = buildString {
    appendLine("--- WormaCeptor Transaction ---")
    appendLine("URL: ${transaction.request.url}")
    appendLine("Method: ${transaction.request.method}")
    appendLine("Status: ${transaction.status.name}")
    transaction.response?.let { res ->
        append("Code: ${res.code}")
        if (res.message.isNotBlank()) append(" ${res.message}")
        appendLine()
        res.protocol?.let { appendLine("Protocol: $it") }
        res.tlsVersion?.let { appendLine("TLS: $it") }
        res.error?.let { appendLine("Error: $it") }
    } ?: appendLine("Code: -")
    appendLine("Duration: ${com.azikar24.wormaceptor.core.ui.util.formatDuration(transaction.durationMs)}")

    appendLine("\n[Request Headers]")
    appendLine(formatHeaders(transaction.request.headers))

    if (!requestBody.isNullOrBlank()) {
        appendLine("\n[Request Body]")
        appendLine(requestBody)
    }

    transaction.response?.let { res ->
        appendLine("\n[Response Headers]")
        appendLine(formatHeaders(res.headers))

        if (!responseBody.isNullOrBlank()) {
            appendLine("\n[Response Body]")
            appendLine(responseBody)
        }
    }
}
