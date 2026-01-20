package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashDetailScreen(crash: Crash, onBack: () -> Unit) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val stackFrames = remember(crash.stackTrace) {
        CrashUtils.parseStackTrace(crash.stackTrace)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crash Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { searchStackOverflow(context, crash) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Stack Overflow")
                    }
                    IconButton(onClick = { shareCrash(context, crash) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Exception Info Card
            ExceptionInfoCard(crash, dateFormat)

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // Quick Actions
            QuickActionsRow(context, crash)

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            // Message Card
            val message = crash.message
            if (!message.isNullOrBlank()) {
                MessageCard(message, context)
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
            }

            // Stack Trace Section
            StackTraceSection(stackFrames, crash.stackTrace, context)
        }
    }
}

@Composable
private fun ExceptionInfoCard(crash: Crash, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Error indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill))
                        .background(MaterialTheme.colorScheme.error),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text(
                    text = "CRASH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Exception Type
            SelectionContainer {
                Text(
                    text = crash.exceptionType,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Timestamp
            Text(
                text = dateFormat.format(Date(crash.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Crash Location
            val location = remember(crash.stackTrace) { CrashUtils.extractCrashLocation(crash.stackTrace) }
            if (location != null) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "at ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SelectionContainer {
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(context: Context, crash: Crash) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        // Copy stack trace
        OutlinedButton(
            onClick = { copyToClipboard(context, "Stack Trace", crash.stackTrace) },
            modifier = Modifier.weight(1f),
            shape = WormaCeptorDesignSystem.Shapes.button,
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
            Text("Copy", fontSize = 13.sp)
        }

        // Search on Stack Overflow
        OutlinedButton(
            onClick = { searchStackOverflow(context, crash) },
            modifier = Modifier.weight(1f),
            shape = WormaCeptorDesignSystem.Shapes.button,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
            Text("Search", fontSize = 13.sp)
        }
    }
}

@Composable
private fun MessageCard(message: String, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Exception Message",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = { copyToClipboard(context, "Message", message) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            SelectionContainer {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun StackTraceSection(stackFrames: List<CrashUtils.StackFrame>, fullStackTrace: String, context: Context) {
    var showAllFrames by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Stack Trace",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = { copyToClipboard(context, "Stack Trace", fullStackTrace) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy stack trace",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Show important frames (app code) first
            val appFrames = stackFrames.filter { it.isAppCode }
            val frameworkFrames = stackFrames.filter { !it.isAppCode }

            if (appFrames.isNotEmpty()) {
                Text(
                    text = "App Code (${appFrames.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
                )
                appFrames.forEach { frame ->
                    StackFrameItem(frame, isHighlighted = true)
                }
            }

            if (frameworkFrames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                // Collapsible framework section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAllFrames = !showAllFrames }
                        .padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Framework & System (${frameworkFrames.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = if (showAllFrames) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showAllFrames) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                AnimatedVisibility(
                    visible = showAllFrames,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    ) + fadeIn(),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    ) + fadeOut(),
                ) {
                    Column {
                        frameworkFrames.forEach { frame ->
                            StackFrameItem(frame, isHighlighted = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StackFrameItem(frame: CrashUtils.StackFrame, isHighlighted: Boolean) {
    val backgroundColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }

    val textColor = if (isHighlighted) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs))
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
    ) {
        if (frame.className != null && frame.methodName != null) {
            // Parsed frame with syntax highlighting
            SelectionContainer {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = textColor.copy(alpha = 0.6f))) {
                            append("at ")
                        }
                        withStyle(
                            SpanStyle(
                                color = textColor,
                                fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal,
                            ),
                        ) {
                            append("${frame.className}.")
                        }
                        withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Bold)) {
                            append(frame.methodName)
                        }
                        if (frame.fileName != null && frame.lineNumber != null) {
                            withStyle(SpanStyle(color = textColor.copy(alpha = 0.6f))) {
                                append("(")
                            }
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                append("${frame.fileName}:${frame.lineNumber}")
                            }
                            withStyle(SpanStyle(color = textColor.copy(alpha = 0.6f))) {
                                append(")")
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                )
            }
        } else {
            // Raw frame line
            SelectionContainer {
                Text(
                    text = frame.fullLine,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun searchStackOverflow(context: Context, crash: Crash) {
    val query = CrashUtils.generateStackOverflowQuery(crash.exceptionType, crash.message)
    val url = "https://stackoverflow.com/search?q=${Uri.encode(query)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

private fun shareCrash(context: Context, crash: Crash) {
    val shareText = buildString {
        appendLine("WormaCeptor Crash Report")
        appendLine("=======================")
        appendLine()
        appendLine("Exception: ${crash.exceptionType}")
        appendLine(
            "Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(crash.timestamp))}",
        )
        if (!crash.message.isNullOrBlank()) {
            appendLine()
            appendLine("Message:")
            appendLine(crash.message)
        }
        appendLine()
        appendLine("Stack Trace:")
        appendLine(crash.stackTrace)
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Crash Report: ${crash.exceptionType}")
    }
    context.startActivity(Intent.createChooser(intent, "Share Crash Report"))
}
