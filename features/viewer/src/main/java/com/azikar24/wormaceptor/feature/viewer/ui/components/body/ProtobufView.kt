package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.contracts.ProtobufDecoder
import com.azikar24.wormaceptor.domain.entities.ProtobufDecodeResult
import com.azikar24.wormaceptor.domain.entities.ProtobufField
import com.azikar24.wormaceptor.feature.viewer.R
import org.koin.java.KoinJavaComponent.get

/**
 * Composable that decodes and displays protobuf wire format data without a schema.
 *
 * Shows each field as an expandable card with field number, wire type, and decoded value.
 * Falls back to a hex dump when decoding fails.
 */
@Composable
fun ProtobufView(
    data: ByteArray,
    modifier: Modifier = Modifier,
) {
    val decodeResult = remember(data) {
        try {
            val decoder: ProtobufDecoder = get(ProtobufDecoder::class.java)
            decoder.decode(data)
        } catch (_: RuntimeException) {
            ProtobufDecodeResult.Failure("Decoder not available")
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        when (decodeResult) {
            is ProtobufDecodeResult.Success -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = WormaCeptorDesignSystem.Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.viewer_protobuf_fields_count,
                            decodeResult.fields.size,
                            decodeResult.fields.size,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                decodeResult.fields.forEachIndexed { _, field ->
                    ProtobufFieldCard(
                        field = field,
                        initiallyExpanded = decodeResult.fields.size <= 5,
                    )
                }
            }

            is ProtobufDecodeResult.Failure -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.LIGHT,
                        ),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorDesignSystem.Spacing.md),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.viewer_protobuf_decode_error),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        Spacer(modifier = Modifier.padding(top = WormaCeptorDesignSystem.Spacing.sm))

                        Text(
                            text = stringResource(R.string.viewer_protobuf_hex_dump),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.xs),
                        )

                        SelectionContainer {
                            Text(
                                text = decodeResult.hexDump,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            )
                        }
                    }
                }
            }
        }
    }
}

private val protobufAccentColor = Color(0xFF8B5CF6)

@Composable
private fun ProtobufFieldCard(
    field: ProtobufField,
    initiallyExpanded: Boolean,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.FAST),
        label = "chevron_rotation",
    )

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
                protobufAccentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = WormaCeptorDesignSystem.Alpha.MEDIUM,
                )
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
                    color = protobufAccentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.LIGHT),
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(WormaCeptorDesignSystem.Spacing.xs)
                            .size(16.dp),
                        tint = protobufAccentColor,
                    )
                }

                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.viewer_protobuf_field, field.fieldNumber),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = field.wireTypeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
                ),
                exit = shrinkVertically(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = WormaCeptorDesignSystem.Alpha.MODERATE,
                            ),
                        )
                        .padding(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    SelectionContainer {
                        Text(
                            text = field.value,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                        )
                    }
                }
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun ProtobufViewPreview() {
    MaterialTheme {
        ProtobufView(
            data = byteArrayOf(
                0x08, 0x96.toByte(), 0x01,
                0x12, 0x07, 0x74, 0x65, 0x73, 0x74, 0x69, 0x6E, 0x67,
            ),
        )
    }
}
