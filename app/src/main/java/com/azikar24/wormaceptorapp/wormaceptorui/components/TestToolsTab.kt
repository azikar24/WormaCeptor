/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Test Tools tab content for the demo app.
 * Displays grouped lists of testing features for developers.
 *
 * Sections:
 * - NETWORK: API and WebSocket testing
 * - DEBUG TRIGGERS: Crash, leak, and violation triggers (destructive)
 * - FEATURE TESTS: Navigation to feature test screens
 */
@Composable
fun TestToolsTab(
    onRunApiTests: () -> Unit,
    onWebSocketTest: () -> Unit,
    onTriggerCrash: () -> Unit,
    onTriggerLeak: () -> Unit,
    onThreadViolation: () -> Unit,
    onLocationClick: () -> Unit,
    onCookiesClick: () -> Unit,
    onWebViewClick: () -> Unit,
    onSecureStorageClick: () -> Unit,
    onComposeRenderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // NETWORK Section
        SectionHeader(title = "NETWORK")

        ToolListItem(
            icon = Icons.Outlined.PlayArrow,
            label = "Run API Tests",
            onClick = onRunApiTests,
        )

        ToolListItem(
            icon = Icons.Outlined.Sync,
            label = "WebSocket Test",
            onClick = onWebSocketTest,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // DEBUG TRIGGERS Section
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
            onClick = onTriggerLeak,
            isDestructive = true,
        )

        ToolListItem(
            icon = Icons.Outlined.Storage,
            label = "Thread Violation",
            onClick = onThreadViolation,
            isDestructive = true,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // FEATURE TESTS Section
        SectionHeader(title = "FEATURE TESTS")

        ToolListItem(
            icon = Icons.Outlined.LocationOn,
            label = "Location Simulator",
            onClick = onLocationClick,
            showChevron = true,
        )

        ToolListItem(
            icon = Icons.Outlined.Cookie,
            label = "Cookie Inspector",
            onClick = onCookiesClick,
            showChevron = true,
        )

        ToolListItem(
            icon = Icons.Outlined.Language,
            label = "WebView Monitor",
            onClick = onWebViewClick,
            showChevron = true,
        )

        ToolListItem(
            icon = Icons.Outlined.Security,
            label = "Secure Storage",
            onClick = onSecureStorageClick,
            showChevron = true,
        )

        ToolListItem(
            icon = Icons.Outlined.Speed,
            label = "Compose Render",
            onClick = onComposeRenderClick,
            showChevron = true,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
    }
}

/**
 * Section header with uppercase styling.
 */
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xl,
                vertical = WormaCeptorDesignSystem.Spacing.sm,
            ),
    )
}

/**
 * Tool list item with icon, label, and optional chevron.
 *
 * @param icon The leading icon
 * @param label The item label text
 * @param onClick Action when tapped
 * @param isDestructive If true, uses error color for text
 * @param showChevron If true, shows a chevron arrow indicating navigation
 */
@Composable
private fun ToolListItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    showChevron: Boolean = false,
) {
    val textColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val iconTint = if (isDestructive) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            )
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        // Leading icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint,
        )

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

        // Label
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f),
        )

        // Optional chevron
        if (showChevron) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}
