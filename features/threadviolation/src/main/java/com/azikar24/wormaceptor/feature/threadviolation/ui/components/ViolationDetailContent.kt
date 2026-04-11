package com.azikar24.wormaceptor.feature.threadviolation.ui.components

import android.content.ClipData
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.DetailItem
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailHeader
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailSection
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatTimestampCompact
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.feature.threadviolation.R
import kotlinx.coroutines.launch

@Composable
internal fun ViolationDetailContent(
    violation: ThreadViolation,
    modifier: Modifier = Modifier,
) {
    val typeColor = violation.violationType.color

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        item {
            WormaCeptorDetailHeader(
                icon = Icons.Default.Warning,
                iconTint = typeColor,
                iconBackgroundColor = typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                title = violation.violationType.name.replace("_", " "),
                subtitle = {
                    Text(
                        violation.threadName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        item {
            WormaCeptorDetailSection(
                title = stringResource(R.string.threadviolation_detail_section_details),
                items = listOf(
                    DetailItem(
                        stringResource(R.string.threadviolation_detail_label_description),
                        violation.description,
                    ),
                    DetailItem(stringResource(R.string.threadviolation_detail_label_thread), violation.threadName),
                    DetailItem(
                        stringResource(R.string.threadviolation_detail_label_time),
                        formatTimestampCompact(violation.timestamp),
                    ),
                ) + (
                    violation.durationMs?.let {
                        listOf(DetailItem(stringResource(R.string.threadviolation_detail_label_duration), "${it}ms"))
                    } ?: emptyList()
                    ),
            )
        }

        if (violation.stackTrace.isNotEmpty()) {
            item {
                val clipboard = LocalClipboard.current
                val context = LocalContext.current
                val copiedMessage = stringResource(R.string.threadviolation_stack_copied)
                val scope = rememberCoroutineScope()
                Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.threadviolation_detail_section_stack_trace),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        IconButton(
                            onClick = {
                                val clipData = ClipData.newPlainText(
                                    "Stack Trace",
                                    violation.stackTrace.joinToString("\n"),
                                )
                                scope.launch {
                                    clipboard.setClipEntry(ClipEntry(clipData))
                                }
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.threadviolation_copy_stack),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                            )
                        }
                    }
                    Surface(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(WormaCeptorTokens.Radius.md),
                        MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(
                            Modifier.padding(WormaCeptorTokens.Spacing.md),
                            Arrangement.spacedBy(WormaCeptorTokens.Spacing.xxs),
                        ) {
                            violation.stackTrace.forEach {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = WormaCeptorTokens.Alpha.PROMINENT,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(WormaCeptorTokens.Spacing.lg)) }
    }
}
