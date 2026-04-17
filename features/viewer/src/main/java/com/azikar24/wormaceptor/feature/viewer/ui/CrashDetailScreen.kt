package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashDetailPagerScreen(
    crashes: List<Crash>,
    initialCrashIndex: Int,
    onBack: () -> Unit,
) {
    val view = LocalView.current

    // Current crash index state with direction tracking
    var currentCrashIndex by remember {
        mutableIntStateOf(initialCrashIndex.coerceIn(0, (crashes.size - 1).coerceAtLeast(0)))
    }
    var navigationDirection by remember { mutableIntStateOf(0) } // -1 = prev, 1 = next, 0 = none

    // Current crash data
    val currentCrash = crashes.getOrNull(currentCrashIndex)

    // Navigation functions
    val canNavigatePrev = currentCrashIndex > 0
    val canNavigateNext = currentCrashIndex < crashes.size - 1

    fun navigateToPrevCrash() {
        if (canNavigatePrev) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            navigationDirection = -1
            currentCrashIndex--
        }
    }

    fun navigateToNextCrash() {
        if (canNavigateNext) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            navigationDirection = 1
            currentCrashIndex++
        }
    }

    // Smooth animation config
    val slideOffset = 100

    // Smooth directional slide transition
    AnimatedContent(
        targetState = currentCrashIndex to currentCrash,
        transitionSpec = {
            val slideSpec = tween<IntOffset>(
                durationMillis = WormaCeptorTokens.Animation.NORMAL,
                easing = WormaCeptorTokens.Easing.standard,
            )

            if (navigationDirection >= 0) {
                // Going forward (next) - content slides in from right
                slideInHorizontally(slideSpec) { slideOffset } togetherWith
                    slideOutHorizontally(slideSpec) { -slideOffset }
            } else {
                // Going backward (prev) - content slides in from left
                slideInHorizontally(slideSpec) { -slideOffset } togetherWith
                    slideOutHorizontally(slideSpec) { slideOffset }
            }
        },
        label = "crash_transition",
    ) { (_, crash) ->
        if (crash != null) {
            CrashDetailContent(
                crash = crash,
                onBack = onBack,
                onNavigatePrevCrash = ::navigateToPrevCrash,
                onNavigateNextCrash = ::navigateToNextCrash,
                canNavigatePrev = canNavigatePrev,
                canNavigateNext = canNavigateNext,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.viewer_crash_detail_not_found),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashDetailScreen(
    crash: Crash,
    onBack: () -> Unit,
) {
    CrashDetailContent(
        crash = crash,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
private fun CrashDetailContent(
    crash: Crash,
    onBack: () -> Unit,
    onNavigatePrevCrash: () -> Unit = {},
    onNavigateNextCrash: () -> Unit = {},
    canNavigatePrev: Boolean = false,
    canNavigateNext: Boolean = false,
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val stackFrames = remember(crash.stackTrace) {
        CrashUtils.parseStackTrace(crash.stackTrace)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            // Swipeable TopAppBar for crash navigation
            SwipeableTopBar(
                onSwipeLeft = onNavigateNextCrash,
                onSwipeRight = onNavigatePrevCrash,
                canSwipeLeft = canNavigateNext,
                canSwipeRight = canNavigatePrev,
            ) {
                WormaCeptorTopBar(
                    title = stringResource(R.string.viewer_crash_detail_title),
                    onBack = onBack,
                    backContentDescription = stringResource(R.string.viewer_crash_detail_back),
                    actions = {
                        IconButton(onClick = { shareCrash(context, crash) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.viewer_crash_detail_share),
                            )
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = WormaCeptorTokens.Spacing.lg,
                    top = WormaCeptorTokens.Spacing.lg,
                    end = WormaCeptorTokens.Spacing.lg,
                    bottom = WormaCeptorTokens.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
        ) {
            // Exception Info Card
            ExceptionInfoCard(crash, dateFormat)

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))

            // Message Card
            val message = crash.message
            if (!message.isNullOrBlank()) {
                MessageCard(message, context)
                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))
            }

            // Stack Trace Section
            StackTraceSection(stackFrames, crash.stackTrace, context)
        }
    }
}

@Composable
private fun ExceptionInfoCard(
    crash: Crash,
    dateFormat: SimpleDateFormat,
) {
    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        ),
        borderColor = MaterialTheme.colorScheme.error.copy(
            alpha = WormaCeptorTokens.Alpha.MODERATE,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
        ) {
            // Error indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorTokens.Spacing.sm)
                        .clip(WormaCeptorTokens.Shapes.pill)
                        .background(MaterialTheme.colorScheme.error),
                )
                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
                Text(
                    text = stringResource(R.string.viewer_crash_detail_crash_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            // Exception Type
            SelectionContainer {
                Text(
                    text = crash.exceptionType,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

            // Timestamp
            Text(
                text = dateFormat.format(Date(crash.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Crash Location
            val location = remember(crash.stackTrace) { CrashUtils.extractCrashLocation(crash.stackTrace) }
            if (location != null) {
                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.viewer_crash_detail_at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.xs))
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
private fun MessageCard(
    message: String,
    context: Context,
) {
    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.viewer_crash_detail_exception_message),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = { copyToClipboard(context, "Message", message) },
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.viewer_crash_detail_copy_message),
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

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
private fun StackTraceSection(
    stackFrames: List<CrashUtils.StackFrame>,
    fullStackTrace: String,
    context: Context,
) {
    var showAllFrames by remember { mutableStateOf(false) }

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.viewer_crash_detail_stack_trace),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = { copyToClipboard(context, "Stack Trace", fullStackTrace) },
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.viewer_crash_detail_copy_stack_trace),
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            // Show important frames (app code) first
            val appFrames = stackFrames.filter { it.isAppCode }
            val frameworkFrames = stackFrames.filter { !it.isAppCode }

            if (appFrames.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.viewer_crash_detail_app_code, appFrames.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.sm),
                )
                appFrames.forEachIndexed { index, frame ->
                    StackFrameItem(frame, isHighlighted = true)
                    if (index < appFrames.lastIndex) {
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
                    }
                }
            }

            if (frameworkFrames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

                // Collapsible framework section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAllFrames = !showAllFrames }
                        .padding(vertical = WormaCeptorTokens.Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.viewer_crash_detail_framework_system, frameworkFrames.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = if (showAllFrames) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showAllFrames) {
                            stringResource(
                                R.string.viewer_body_collapse,
                            )
                        } else {
                            stringResource(R.string.viewer_body_expand)
                        },
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
                        frameworkFrames.forEachIndexed { index, frame ->
                            StackFrameItem(frame, isHighlighted = false)
                            if (index < frameworkFrames.lastIndex) {
                                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StackFrameItem(
    frame: CrashUtils.StackFrame,
    isHighlighted: Boolean,
) {
    val backgroundColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
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
            .background(backgroundColor, WormaCeptorTokens.Shapes.chip)
            .padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xs,
            ),
    ) {
        if (frame.className != null && frame.methodName != null) {
            // Parsed frame with syntax highlighting
            SelectionContainer {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = textColor.copy(alpha = WormaCeptorTokens.Alpha.INTENSE))) {
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
                            withStyle(
                                SpanStyle(color = textColor.copy(alpha = WormaCeptorTokens.Alpha.INTENSE)),
                            ) {
                                append("(")
                            }
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                append("${frame.fileName}:${frame.lineNumber}")
                            }
                            withStyle(
                                SpanStyle(color = textColor.copy(alpha = WormaCeptorTokens.Alpha.INTENSE)),
                            ) {
                                append(")")
                            }
                        }
                    },
                    style = WormaCeptorTokens.Typography.codeMedium,
                )
            }
        } else {
            // Raw frame line
            SelectionContainer {
                Text(
                    text = frame.fullLine,
                    style = WormaCeptorTokens.Typography.codeMedium,
                    color = textColor,
                )
            }
        }
    }
}

private fun shareCrash(
    context: Context,
    crash: Crash,
) {
    val text = buildString {
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
    shareText(context, text, "Share Crash Report", "Crash Report: ${crash.exceptionType}")
}

@Preview(showBackground = true)
@Composable
private fun CrashDetailScreenPreview() {
    WormaCeptorTheme {
        CrashDetailScreen(
            crash = Crash(
                id = 1L,
                timestamp = System.currentTimeMillis(),
                exceptionType = "java.lang.NullPointerException",
                message = "Attempt to invoke virtual method on a null object reference",
                stackTrace = "java.lang.NullPointerException: Attempt to invoke virtual method\n" +
                    "\tat com.example.app.MainActivity.onCreate(MainActivity.kt:42)\n" +
                    "\tat android.app.Activity.performCreate(Activity.java:8051)",
            ),
            onBack = {},
        )
    }
}
