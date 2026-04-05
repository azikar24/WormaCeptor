package com.azikar24.wormaceptor.feature.logs.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun LogList(
    logs: ImmutableList<LogEntry>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(
            top = WormaCeptorTokens.Spacing.sm,
            bottom = WormaCeptorTokens.Spacing.sm +
                WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding(),
        ),
    ) {
        items(
            items = logs,
            key = { it.id },
        ) { entry ->
            LogEntryItem(
                entry = entry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun LogEntryItem(
    entry: LogEntry,
    modifier: Modifier = Modifier,
) {
    val logColors = WormaCeptorTokens.Colors.LogLevel
    val levelColor = when (entry.level) {
        LogLevel.VERBOSE -> logColors.verbose
        LogLevel.DEBUG -> logColors.debug
        LogLevel.INFO -> logColors.info
        LogLevel.WARN -> logColors.warn
        LogLevel.ERROR -> logColors.error
        LogLevel.ASSERT -> logColors.assert
    }
    val backgroundColor = when (entry.level) {
        LogLevel.VERBOSE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
        LogLevel.DEBUG -> logColors.debug.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
        LogLevel.INFO -> logColors.info.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
        LogLevel.WARN -> logColors.warn.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
        LogLevel.ERROR -> logColors.error.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
        LogLevel.ASSERT -> logColors.assert.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val formattedTime = remember(entry.timestamp) {
        timeFormat.format(Date(entry.timestamp))
    }

    Surface(
        modifier = modifier,
        color = backgroundColor.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.md,
                    vertical = WormaCeptorTokens.Radius.sm,
                ),
            verticalAlignment = Alignment.Top,
        ) {
            // Level badge
            Surface(
                shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                color = levelColor.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
                modifier = Modifier.padding(top = WormaCeptorTokens.Spacing.xxs),
            ) {
                Text(
                    text = entry.level.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Radius.sm,
                        vertical = WormaCeptorTokens.Spacing.xxs,
                    ),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                // Tag and timestamp row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.tag,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = WormaCeptorTokens.Alpha.HEAVY,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))

                // Message
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
                    lineHeight = WormaCeptorTokens.Typography.codeMedium.lineHeight,
                )
            }
        }
    }
}
