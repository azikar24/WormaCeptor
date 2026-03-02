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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.contracts.FormDataParser
import com.azikar24.wormaceptor.feature.viewer.R
import org.koin.java.KoinJavaComponent.get

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
        try {
            val parser: FormDataParser = get(FormDataParser::class.java)
            parser.parse(formData)
        } catch (_: RuntimeException) {
            emptyList()
        }
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
                    text = stringResource(R.string.viewer_form_no_data),
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
                            text = stringResource(R.string.viewer_form_key),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.4f),
                        )
                        Text(
                            text = stringResource(R.string.viewer_form_value),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.6f),
                        )
                    }

                    WormaCeptorDivider(style = DividerStyle.Subtle)

                    parsedData.forEachIndexed { index, param ->
                        FormDataRow(
                            key = param.key,
                            value = param.value,
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
