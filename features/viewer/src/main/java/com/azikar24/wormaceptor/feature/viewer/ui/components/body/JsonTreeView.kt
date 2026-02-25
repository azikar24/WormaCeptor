package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.ComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.syntaxColors
import org.json.JSONArray
import org.json.JSONObject

/**
 * A collapsible tree view for JSON content.
 * Supports syntax highlighting and expandable/collapsible nodes.
 */
@Composable
fun JsonTreeView(
    jsonString: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true,
    maxDepth: Int = 10,
    colors: ComposeSyntaxColors = syntaxColors(),
) {
    val json = remember(jsonString) {
        try {
            val trimmed = jsonString.trim()
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed)
                trimmed.startsWith("[") -> JSONArray(trimmed)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.codeBackground, WormaCeptorDesignSystem.Shapes.chip)
            .padding(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        if (json != null) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    when (json) {
                        is JSONObject -> JsonObjectNode(
                            obj = json,
                            depth = 0,
                            maxDepth = maxDepth,
                            initiallyExpanded = initiallyExpanded,
                            colors = colors,
                        )
                        is JSONArray -> JsonArrayNode(
                            array = json,
                            depth = 0,
                            maxDepth = maxDepth,
                            initiallyExpanded = initiallyExpanded,
                            colors = colors,
                        )
                    }
                }
            }
        } else {
            // Fallback to plain text if parsing fails
            Text(
                text = jsonString,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = colors.default,
            )
        }
    }
}

@Composable
private fun JsonObjectNode(
    obj: JSONObject,
    depth: Int,
    maxDepth: Int,
    initiallyExpanded: Boolean,
    colors: ComposeSyntaxColors,
    keyName: String? = null,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded && depth < maxDepth) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "chevron_rotation",
    )

    val keys = remember(obj) { obj.keys().asSequence().toList() }
    val isEmpty = keys.isEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier
                .clickable(enabled = !isEmpty) { expanded = !expanded }
                .padding(vertical = WormaCeptorDesignSystem.Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width((depth * 16).dp))

            if (!isEmpty) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotation),
                    tint = colors.punctuation,
                )
            } else {
                Spacer(modifier = Modifier.width(14.dp))
            }

            if (keyName != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.property)) {
                            append("\"$keyName\"")
                        }
                        withStyle(SpanStyle(color = colors.punctuation)) {
                            append(": ")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.punctuation)) {
                        append("{")
                    }
                    if (!expanded && !isEmpty) {
                        withStyle(SpanStyle(color = colors.comment)) {
                            append(" ${keys.size} ${if (keys.size == 1) "key" else "keys"} ")
                        }
                        withStyle(SpanStyle(color = colors.punctuation)) {
                            append("}")
                        }
                    } else if (isEmpty) {
                        withStyle(SpanStyle(color = colors.punctuation)) {
                            append("}")
                        }
                    }
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }

        AnimatedVisibility(
            visible = expanded && !isEmpty,
            enter = expandVertically(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
            exit = shrinkVertically(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
        ) {
            Column {
                keys.forEachIndexed { index, key ->
                    val value = obj.opt(key)
                    val isLast = index == keys.lastIndex

                    when (value) {
                        is JSONObject -> JsonObjectNode(
                            obj = value,
                            depth = depth + 1,
                            maxDepth = maxDepth,
                            initiallyExpanded = initiallyExpanded,
                            colors = colors,
                            keyName = key,
                        )
                        is JSONArray -> JsonArrayNode(
                            array = value,
                            depth = depth + 1,
                            maxDepth = maxDepth,
                            initiallyExpanded = initiallyExpanded,
                            colors = colors,
                            keyName = key,
                        )
                        else -> JsonValueNode(
                            key = key,
                            value = value,
                            depth = depth + 1,
                            isLast = isLast,
                            colors = colors,
                        )
                    }
                }

                Row {
                    Spacer(modifier = Modifier.width((depth * 16 + 14).dp))
                    Text(
                        text = "}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = colors.punctuation,
                    )
                }
            }
        }
    }
}

@Composable
private fun JsonArrayNode(
    array: JSONArray,
    depth: Int,
    maxDepth: Int,
    initiallyExpanded: Boolean,
    colors: ComposeSyntaxColors,
    keyName: String? = null,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded && depth < maxDepth) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "chevron_rotation",
    )

    val isEmpty = array.length() == 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(enabled = !isEmpty) { expanded = !expanded }
                .padding(vertical = WormaCeptorDesignSystem.Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width((depth * 16).dp))

            if (!isEmpty) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotation),
                    tint = colors.punctuation,
                )
            } else {
                Spacer(modifier = Modifier.width(14.dp))
            }

            if (keyName != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.property)) {
                            append("\"$keyName\"")
                        }
                        withStyle(SpanStyle(color = colors.punctuation)) {
                            append(": ")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.punctuation)) {
                        append("[")
                    }
                    if (!expanded && !isEmpty) {
                        withStyle(SpanStyle(color = colors.comment)) {
                            append(" ${array.length()} ${if (array.length() == 1) "item" else "items"} ")
                        }
                        withStyle(SpanStyle(color = colors.punctuation)) {
                            append("]")
                        }
                    } else if (isEmpty) {
                        withStyle(SpanStyle(color = colors.punctuation)) {
                            append("]")
                        }
                    }
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }

        AnimatedVisibility(
            visible = expanded && !isEmpty,
            enter = expandVertically(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
            exit = shrinkVertically(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
        ) {
            Column {
                for (i in 0 until array.length()) {
                    val value = array.opt(i)
                    val isLast = i == array.length() - 1

                    when (value) {
                        is JSONObject -> JsonObjectNode(
                            obj = value,
                            depth = depth + 1,
                            maxDepth = maxDepth,
                            initiallyExpanded = initiallyExpanded,
                            colors = colors,
                        )
                        is JSONArray -> JsonArrayNode(
                            array = value,
                            depth = depth + 1,
                            maxDepth = maxDepth,
                            initiallyExpanded = initiallyExpanded,
                            colors = colors,
                        )
                        else -> JsonArrayValueNode(
                            value = value,
                            depth = depth + 1,
                            isLast = isLast,
                            colors = colors,
                        )
                    }
                }

                Row {
                    Spacer(modifier = Modifier.width((depth * 16 + 14).dp))
                    Text(
                        text = "]",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = colors.punctuation,
                    )
                }
            }
        }
    }
}

@Composable
private fun JsonValueNode(
    key: String,
    value: Any?,
    depth: Int,
    isLast: Boolean,
    colors: ComposeSyntaxColors,
) {
    Row(
        modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width((depth * 16 + 14).dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.property)) {
                    append("\"$key\"")
                }
                withStyle(SpanStyle(color = colors.punctuation)) {
                    append(": ")
                }
                append(formatValue(value, colors))
                if (!isLast) {
                    withStyle(SpanStyle(color = colors.punctuation)) {
                        append(",")
                    }
                }
            },
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
            ),
        )
    }
}

@Composable
private fun JsonArrayValueNode(
    value: Any?,
    depth: Int,
    isLast: Boolean,
    colors: ComposeSyntaxColors,
) {
    Row(
        modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width((depth * 16 + 14).dp))
        Text(
            text = buildAnnotatedString {
                append(formatValue(value, colors))
                if (!isLast) {
                    withStyle(SpanStyle(color = colors.punctuation)) {
                        append(",")
                    }
                }
            },
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
            ),
        )
    }
}

@Composable
private fun formatValue(
    value: Any?,
    colors: ComposeSyntaxColors,
): AnnotatedString {
    return buildAnnotatedString {
        when (value) {
            null, JSONObject.NULL -> {
                withStyle(SpanStyle(color = colors.boolean)) {
                    append("null")
                }
            }
            is String -> {
                withStyle(SpanStyle(color = colors.string)) {
                    val escaped = value
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                    append("\"$escaped\"")
                }
            }
            is Number -> {
                withStyle(SpanStyle(color = colors.number)) {
                    append(value.toString())
                }
            }
            is Boolean -> {
                withStyle(SpanStyle(color = colors.boolean)) {
                    append(value.toString())
                }
            }
            else -> {
                withStyle(SpanStyle(color = colors.default)) {
                    append(value.toString())
                }
            }
        }
    }
}
