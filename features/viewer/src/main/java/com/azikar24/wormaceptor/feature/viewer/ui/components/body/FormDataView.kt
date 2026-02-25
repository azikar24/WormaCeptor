package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import java.net.URLDecoder

/**
 * A table view for URL-encoded form data (application/x-www-form-urlencoded).
 * Parses key-value pairs and displays them in a structured table format.
 */
@Composable
fun FormDataView(
    formData: String,
    modifier: Modifier = Modifier,
) {
    val parsedData = remember(formData) {
        parseFormData(formData)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        if (parsedData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No form data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            WormaCeptorContainer(
                style = ContainerStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = WormaCeptorDesignSystem.Alpha.bold,
                                ),
                            )
                            .padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.md,
                                vertical = WormaCeptorDesignSystem.Spacing.sm,
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Key",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.4f),
                        )
                        Text(
                            text = "Value",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.6f),
                        )
                    }

                    WormaCeptorDivider(style = DividerStyle.Subtle)

                    parsedData.forEachIndexed { index, (key, value) ->
                        FormDataRow(
                            key = key,
                            value = value,
                            isEven = index % 2 == 0,
                        )

                        if (index < parsedData.lastIndex) {
                            WormaCeptorDivider(
                                modifier = Modifier.padding(horizontal = WormaCeptorDesignSystem.Spacing.md),
                                style = DividerStyle.Subtle,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormDataRow(
    key: String,
    value: String,
    isEven: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isEven) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
                },
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.md,
                vertical = WormaCeptorDesignSystem.Spacing.sm,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        SelectionContainer(modifier = Modifier.weight(0.4f)) {
            Text(
                text = key,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        SelectionContainer(modifier = Modifier.weight(0.6f)) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Parses URL-encoded form data into key-value pairs.
 */
private fun parseFormData(formData: String): List<Pair<String, String>> {
    if (formData.isBlank()) return emptyList()

    return try {
        formData
            .split("&")
            .filter { it.isNotBlank() }
            .mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.isEmpty()) return@mapNotNull null

                val key = try {
                    URLDecoder.decode(parts[0], "UTF-8")
                } catch (e: Exception) {
                    parts[0]
                }

                val value = if (parts.size > 1) {
                    try {
                        URLDecoder.decode(parts[1], "UTF-8")
                    } catch (e: Exception) {
                        parts[1]
                    }
                } else {
                    ""
                }

                key to value
            }
    } catch (e: Exception) {
        listOf("raw" to formData)
    }
}
