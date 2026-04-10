package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailSection
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.feature.leakdetection.R

@Composable
internal fun LeakDetailContent(
    leak: LeakInfo,
    modifier: Modifier = Modifier,
) {
    val color = severityColor(leak.severity)
    val background = color.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(bottom = WormaCeptorTokens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        item {
            LeakDetailHeader(
                leak = leak,
                severityColor = color,
                severityBackground = background,
            )
        }

        item {
            WormaCeptorDetailSection(
                title = stringResource(R.string.leakdetection_section_details),
                items = listOf(
                    stringResource(R.string.leakdetection_detail_class) to leak.objectClass,
                    stringResource(R.string.leakdetection_detail_description) to leak.leakDescription,
                    stringResource(R.string.leakdetection_detail_retained_size) to formatBytes(leak.retainedSize),
                    stringResource(R.string.leakdetection_detail_detected) to formatTimestampFull(leak.timestamp),
                ),
            )
        }

        if (leak.referencePath.isNotEmpty()) {
            item {
                ReferencePathSection(referencePath = leak.referencePath)
            }
        }
    }
}

@Composable
private fun LeakDetailHeader(
    leak: LeakInfo,
    severityColor: Color,
    severityBackground: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(WormaCeptorTokens.Spacing.xxxl)
                .clip(RoundedCornerShape(WormaCeptorTokens.Radius.lg))
                .background(severityBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = stringResource(
                    R.string.leakdetection_leak_icon_desc,
                    leak.severity.name,
                    leak.objectClass.substringAfterLast('.'),
                ),
                tint = severityColor,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
            )
        }
        Column {
            Text(
                text = leak.objectClass.substringAfterLast('.'),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                    color = severityBackground,
                ) {
                    Text(
                        text = leak.severity.name,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorTokens.Spacing.sm,
                            vertical = WormaCeptorTokens.Spacing.xxs,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                    )
                }
                Text(
                    text = formatBytes(leak.retainedSize),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = severityColor,
                )
            }
        }
    }
}

@Composable
private fun ReferencePathSection(referencePath: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.leakdetection_section_reference_path),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
            ) {
                referencePath.forEach { step ->
                    Text(
                        text = step,
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

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(name = "LeakDetail - Light")
@Composable
private fun LeakDetailContentPreview() {
    WormaCeptorTheme {
        Surface {
            LeakDetailContent(
                leak = LeakInfo(
                    timestamp = System.currentTimeMillis(),
                    objectClass = "com.example.app.ui.HomeActivity",
                    leakDescription = "Activity retained after onDestroy",
                    retainedSize = 2 * 1_048_576L,
                    referencePath = listOf(
                        "GC Root -> static field",
                        "AppManager.instance -> activity",
                    ),
                    severity = LeakInfo.LeakSeverity.CRITICAL,
                ),
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            )
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(name = "LeakDetail - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LeakDetailContentDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            LeakDetailContent(
                leak = LeakInfo(
                    timestamp = System.currentTimeMillis(),
                    objectClass = "com.example.app.data.CacheManager",
                    leakDescription = "Cache not cleared on low memory",
                    retainedSize = 512 * 1024L,
                    referencePath = emptyList(),
                    severity = LeakInfo.LeakSeverity.HIGH,
                ),
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            )
        }
    }
}
