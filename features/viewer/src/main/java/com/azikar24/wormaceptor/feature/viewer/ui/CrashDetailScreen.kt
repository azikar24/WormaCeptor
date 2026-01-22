package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.util.copyToClipboard
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareText
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pager screen for crash details with swipe navigation between crashes.
 * Swipe on the top bar to navigate between crashes.
 *
 * @param crashes List of all crashes
 * @param initialCrashIndex Initial index to display
 * @param onBack Callback to navigate back
 */
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
    val animDuration = 250
    val slideOffset = 100

    // Smooth directional slide transition
    AnimatedContent(
        targetState = currentCrashIndex to currentCrash,
        transitionSpec = {
            val slideSpec = tween<IntOffset>(animDuration, easing = FastOutSlowInEasing)
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
                    "Crash not found",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/**
 * Original CrashDetailScreen - kept for backward compatibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashDetailScreen(crash: Crash, onBack: () -> Unit) {
    CrashDetailContent(
        crash = crash,
        onBack = onBack,
    )
}

/**
 * Crash detail content - the actual content
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            // Swipeable TopAppBar for crash navigation
            SwipeableTopBar(
                onSwipeLeft = onNavigateNextCrash,
                onSwipeRight = onNavigatePrevCrash,
                canSwipeLeft = canNavigateNext,
                canSwipeRight = canNavigatePrev,
            ) {
                TopAppBar(
                    title = { Text("Crash Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { shareCrash(context, crash) }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
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
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Exception Info Card
            ExceptionInfoCard(crash, dateFormat)

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

/**
 * Swipeable container for the TopAppBar that handles horizontal swipes
 * to navigate between crashes.
 */
@Composable
private fun SwipeableTopBar(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    canSwipeLeft: Boolean,
    canSwipeRight: Boolean,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(canSwipeLeft, canSwipeRight) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = size.width * 0.15f
                        when {
                            dragOffset < -threshold && canSwipeLeft -> {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeLeft()
                            }
                            dragOffset > threshold && canSwipeRight -> {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSwipeRight()
                            }
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    },
                )
            },
    ) {
        content()
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

private fun shareCrash(context: Context, crash: Crash) {
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
