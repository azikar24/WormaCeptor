package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Suppress("LongParameterList", "LongMethod", "ParameterNaming")
@Composable
fun TestToolsSheetContent(
    onRunApiTests: () -> Unit,
    onWebSocketTest: () -> Unit,
    onTriggerCrash: () -> Unit,
    onTriggerLeak: () -> Unit,
    onThreadViolation: () -> Unit,
    onSeedDatabase: () -> Unit,
    onSeedPreferences: () -> Unit,
    onWriteSampleFiles: () -> Unit,
    onEmitSampleLogs: () -> Unit,
    onBurnCpu: () -> Unit,
    onAllocateMemory: () -> Unit,
    onDropFrames: () -> Unit,
    onRecompositionStorm: () -> Unit,
    onLocationClick: () -> Unit,
    onWebViewClick: () -> Unit,
    onSecureStorageClick: () -> Unit,
    modifier: Modifier = Modifier,
    apiTestStatus: ToolStatus = ToolStatus.Idle,
    webSocketStatus: ToolStatus = ToolStatus.Idle,
    leakStatus: ToolStatus = ToolStatus.Idle,
    threadViolationStatus: ToolStatus = ToolStatus.Idle,
    seedDatabaseStatus: ToolStatus = ToolStatus.Idle,
    seedPreferencesStatus: ToolStatus = ToolStatus.Idle,
    writeFilesStatus: ToolStatus = ToolStatus.Idle,
    logsStatus: ToolStatus = ToolStatus.Idle,
    cpuStressStatus: ToolStatus = ToolStatus.Idle,
    memoryStressStatus: ToolStatus = ToolStatus.Idle,
    frameDropStatus: ToolStatus = ToolStatus.Idle,
    recompositionStormActive: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        SectionHeader(title = "NETWORK")

        ToolListItem(
            icon = Icons.Outlined.PlayArrow,
            label = "Run API Tests",
            onClick = onRunApiTests,
            status = apiTestStatus,
        )
        ToolListItem(
            icon = Icons.Outlined.Sync,
            label = "WebSocket Test",
            onClick = onWebSocketTest,
            status = webSocketStatus,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        SectionHeader(title = "STORAGE")

        ToolListItem(
            icon = Icons.Outlined.Storage,
            label = "Seed Database",
            onClick = onSeedDatabase,
            status = seedDatabaseStatus,
        )
        ToolListItem(
            icon = Icons.Outlined.Tune,
            label = "Seed Preferences",
            onClick = onSeedPreferences,
            status = seedPreferencesStatus,
        )
        ToolListItem(
            icon = Icons.Outlined.Folder,
            label = "Write Sample Files",
            onClick = onWriteSampleFiles,
            status = writeFilesStatus,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        SectionHeader(title = "SECURITY")

        ToolListItem(
            icon = Icons.Outlined.Security,
            label = "Secure Storage",
            onClick = onSecureStorageClick,
            showChevron = true,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        SectionHeader(title = "OBSERVABILITY")

        ToolListItem(
            icon = Icons.AutoMirrored.Outlined.Article,
            label = "Emit Sample Logs",
            onClick = onEmitSampleLogs,
            status = logsStatus,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        SectionHeader(title = "DEBUG TRIGGERS")

        ToolListItem(
            icon = Icons.Outlined.BugReport,
            label = "Trigger Crash",
            onClick = onTriggerCrash,
            isDestructive = true,
        )
        ToolListItem(
            icon = Icons.Outlined.Memory,
            label = "Trigger Memory Leak",
            description = "Rotate screen to detect",
            onClick = onTriggerLeak,
            isDestructive = true,
            status = leakStatus,
        )
        ToolListItem(
            icon = Icons.Outlined.Storage,
            label = "Thread Violation",
            onClick = onThreadViolation,
            isDestructive = true,
            status = threadViolationStatus,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        SectionHeader(title = "PERFORMANCE STRESS")

        ToolListItem(
            icon = Icons.Outlined.Whatshot,
            label = "Burn CPU (3s)",
            onClick = onBurnCpu,
            status = cpuStressStatus,
        )
        ToolListItem(
            icon = Icons.Outlined.Memory,
            label = "Allocate 50 MB (3s)",
            onClick = onAllocateMemory,
            status = memoryStressStatus,
        )
        ToolListItem(
            icon = Icons.Outlined.Speed,
            label = "Drop Frames (1.5s burst)",
            onClick = onDropFrames,
            status = frameDropStatus,
        )
        ToolListItem(
            icon = Icons.Outlined.Refresh,
            label = "Recomposition Storm (3s)",
            onClick = onRecompositionStorm,
            status = if (recompositionStormActive) ToolStatus.Running else ToolStatus.Idle,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

        SectionHeader(title = "FEATURE TESTS")

        ToolListItem(
            icon = Icons.Outlined.LocationOn,
            label = "Location Simulator",
            onClick = onLocationClick,
            showChevron = true,
        )
        ToolListItem(
            icon = Icons.Outlined.Language,
            label = "WebView Monitor",
            onClick = onWebViewClick,
            showChevron = true,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = WormaCeptorTokens.semantic().textSecondary,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorTokens.Spacing.xl,
                vertical = WormaCeptorTokens.Spacing.sm,
            ),
    )
}

@Suppress("LongParameterList")
@Composable
private fun ToolListItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    isDestructive: Boolean = false,
    showChevron: Boolean = false,
    status: ToolStatus = ToolStatus.Idle,
) {
    val textColor = if (isDestructive) {
        WormaCeptorTokens.semantic().error
    } else {
        WormaCeptorTokens.semantic().textPrimary
    }

    val iconTint = if (isDestructive) {
        WormaCeptorTokens.semantic().error.copy(alpha = WormaCeptorTokens.Alpha.HEAVY)
    } else {
        WormaCeptorTokens.semantic().textSecondary
    }

    val showDescription = description != null && status == ToolStatus.WaitingForAction

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(
                min = if (showDescription) {
                    WormaCeptorTokens.TouchTarget.large
                } else {
                    WormaCeptorTokens.TouchTarget.comfortable
                },
            )
            .clickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            )
            .padding(horizontal = WormaCeptorTokens.Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
            tint = iconTint,
        )
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))
        ToolListItemLabel(label, description, textColor, showDescription, Modifier.weight(1f))
        ToolListItemTrailing(status, showChevron)
    }
}

@Composable
private fun ToolListItemLabel(
    label: String,
    description: String?,
    textColor: Color,
    showDescription: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
        if (showDescription && description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = WormaCeptorTokens.semantic().textSecondary.copy(
                    alpha = WormaCeptorTokens.Alpha.HEAVY,
                ),
            )
        }
    }
}

@Composable
private fun ToolListItemTrailing(
    status: ToolStatus,
    showChevron: Boolean,
) {
    AnimatedContent(
        targetState = status,
        transitionSpec = {
            fadeIn(animationSpec = tween(WormaCeptorTokens.Animation.FAST)) togetherWith
                fadeOut(animationSpec = tween(WormaCeptorTokens.Animation.FAST))
        },
        label = "status",
    ) { currentStatus ->
        when (currentStatus) {
            ToolStatus.Running -> CircularProgressIndicator(
                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                strokeWidth = WormaCeptorTokens.BorderWidth.thick,
                color = WormaCeptorTokens.semantic().accent,
            )
            ToolStatus.Done -> Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Done",
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                tint = WormaCeptorTokens.semantic().accent,
            )
            else -> if (showChevron) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                    tint = WormaCeptorTokens.semantic().textSecondary.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
                )
            } else {
                Spacer(modifier = Modifier.size(WormaCeptorTokens.IconSize.md))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TestToolsSheetContentPreview() {
    WormaCeptorTheme {
        TestToolsSheetContent(
            onRunApiTests = {},
            onWebSocketTest = {},
            onTriggerCrash = {},
            onTriggerLeak = {},
            onThreadViolation = {},
            onSeedDatabase = {},
            onSeedPreferences = {},
            onWriteSampleFiles = {},
            onEmitSampleLogs = {},
            onBurnCpu = {},
            onAllocateMemory = {},
            onDropFrames = {},
            onRecompositionStorm = {},
            onLocationClick = {},
            onWebViewClick = {},
            onSecureStorageClick = {},
            apiTestStatus = ToolStatus.Done,
            webSocketStatus = ToolStatus.Running,
            leakStatus = ToolStatus.WaitingForAction,
            threadViolationStatus = ToolStatus.Idle,
            recompositionStormActive = true,
        )
    }
}
