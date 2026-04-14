package com.azikar24.wormaceptor.feature.logs.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorStatusBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
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
    val levelColor = logLevelColor(entry.level)
    val backgroundColor = logLevelBackground(entry.level, levelColor)

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
            WormaCeptorStatusBadge(
                text = entry.level.tag,
                containerColor = levelColor.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
                contentColor = levelColor,
                modifier = Modifier.padding(top = WormaCeptorTokens.Spacing.xxs),
            )

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

            LogEntryBody(
                tag = entry.tag,
                message = entry.message,
                formattedTime = formattedTime,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LogEntryBody(
    tag: String,
    message: String,
    formattedTime: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tag,
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

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
            lineHeight = WormaCeptorTokens.Typography.codeMedium.lineHeight,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun LogEntryItemPreview(@PreviewParameter(LogEntryPreviewProvider::class) entry: LogEntry) {
    WormaCeptorTheme {
        LogEntryItem(entry = entry)
    }
}

@Suppress("MagicNumber")
private class LogEntryPreviewProvider : PreviewParameterProvider<LogEntry> {
    override val values: Sequence<LogEntry> = sequenceOf(
        LogEntry(
            id = 1L,
            timestamp = System.currentTimeMillis(),
            level = LogLevel.VERBOSE,
            tag = "System",
            pid = 12_345,
            message = "GC freed 2048 objects",
        ),
        LogEntry(
            id = 2L,
            timestamp = System.currentTimeMillis(),
            level = LogLevel.DEBUG,
            tag = "OkHttp",
            pid = 12_345,
            message = "Sending request https://api.example.com/users",
        ),
        LogEntry(
            id = 3L,
            timestamp = System.currentTimeMillis(),
            level = LogLevel.INFO,
            tag = "MainActivity",
            pid = 12_345,
            message = "User authenticated successfully",
        ),
        LogEntry(
            id = 4L,
            timestamp = System.currentTimeMillis(),
            level = LogLevel.WARN,
            tag = "NetworkManager",
            pid = 12_345,
            message = "Retrying request after timeout (attempt 2/3)",
        ),
        LogEntry(
            id = 5L,
            timestamp = System.currentTimeMillis(),
            level = LogLevel.ERROR,
            tag = "CrashReporter",
            pid = 12_345,
            message = "Failed to upload crash report: timeout",
        ),
        LogEntry(
            id = 6L,
            timestamp = System.currentTimeMillis(),
            level = LogLevel.ASSERT,
            tag = "StrictMode",
            pid = 12_345,
            message = "Assertion failed: unexpected null context",
        ),
    )
}
