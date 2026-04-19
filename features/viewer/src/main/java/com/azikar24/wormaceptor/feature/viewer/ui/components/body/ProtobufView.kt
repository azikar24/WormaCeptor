package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
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
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        when (decodeResult) {
            is ProtobufDecodeResult.Success -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = WormaCeptorTokens.Spacing.xs),
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
                        color = WormaCeptorTokens.semantic().textSecondary,
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
                WormaCeptorCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = WormaCeptorTokens.semantic().error.copy(
                        alpha = WormaCeptorTokens.Alpha.SUBTLE,
                    ).copy(
                        alpha = WormaCeptorTokens.Alpha.LIGHT,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorTokens.Spacing.md),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WormaCeptorTokens.semantic().error,
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                            )
                            Text(
                                text = stringResource(R.string.viewer_protobuf_decode_error),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = WormaCeptorTokens.semantic().error,
                            )
                        }

                        Spacer(modifier = Modifier.padding(top = WormaCeptorTokens.Spacing.sm))

                        Text(
                            text = stringResource(R.string.viewer_protobuf_hex_dump),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = WormaCeptorTokens.semantic().textSecondary,
                            modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.xs),
                        )

                        SelectionContainer {
                            Text(
                                text = decodeResult.hexDump,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                color = WormaCeptorTokens.semantic().textPrimary,
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

private val protobufAccentColor = WormaCeptorTokens.Colors.Viewer.protobufAccent

@Composable
private fun ProtobufFieldCard(
    field: ProtobufField,
    initiallyExpanded: Boolean,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        label = "chevron_rotation",
    )

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        borderColor = if (expanded) {
            protobufAccentColor.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        } else {
            WormaCeptorTokens.semantic().surfaceVariant.copy(
                alpha = WormaCeptorTokens.Alpha.MEDIUM,
            )
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
                    color = protobufAccentColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(WormaCeptorTokens.Spacing.xs)
                            .size(WormaCeptorTokens.IconSize.sm),
                        tint = protobufAccentColor,
                    )
                }

                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.viewer_protobuf_field, field.fieldNumber),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = WormaCeptorTokens.semantic().textPrimary,
                    )
                    Text(
                        text = field.wireTypeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = WormaCeptorTokens.semantic().textSecondary,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            WormaCeptorTokens.semantic().surfaceVariant.copy(
                                alpha = WormaCeptorTokens.Alpha.MODERATE,
                            ),
                        )
                        .padding(WormaCeptorTokens.Spacing.md),
                ) {
                    SelectionContainer {
                        Text(
                            text = field.value,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = WormaCeptorTokens.semantic().textPrimary,
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
