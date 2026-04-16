package com.azikar24.wormaceptor.feature.mockrules.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorMethodBadge
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorStatusBadge
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import com.azikar24.wormaceptor.feature.mockrules.R
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewEvent
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockRulesScreen(
    state: MockRulesViewState,
    onEvent: (MockRulesViewEvent) -> Unit,
    onNavigateToEditor: (String?) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var ruleToDelete by remember { mutableStateOf<MockRule?>(null) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            MockRulesTopBar(
                hasRules = state.rules.isNotEmpty(),
                onDeleteAll = { showDeleteAllDialog = true },
                onBack = onBack,
            )
        },
        floatingActionButton = {
            WormaCeptorFAB(
                onClick = { onNavigateToEditor(null) },
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.mock_rules_add_rule),
            )
        },
    ) { padding ->
        MockRulesContent(
            state = state,
            onEvent = onEvent,
            onNavigateToEditor = onNavigateToEditor,
            onDeleteRule = { rule -> ruleToDelete = rule },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }

    if (showDeleteAllDialog) {
        WormaCeptorAlertDialog(
            title = stringResource(R.string.mock_rules_dialog_delete_all_title),
            message = stringResource(R.string.mock_rules_dialog_delete_all_message, state.rules.size),
            confirmLabel = stringResource(R.string.mock_rules_dialog_delete_all_confirm),
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEvent(MockRulesViewEvent.List.DeleteAllRules)
                showDeleteAllDialog = false
            },
            dismissLabel = stringResource(R.string.mock_rules_dialog_delete_all_cancel),
            onDismiss = { showDeleteAllDialog = false },
            destructive = true,
        )
    }

    ruleToDelete?.let { rule ->
        WormaCeptorAlertDialog(
            title = stringResource(R.string.mock_rules_dialog_delete_title),
            message = stringResource(R.string.mock_rules_dialog_delete_message, rule.name),
            confirmLabel = stringResource(R.string.mock_rules_dialog_delete_confirm),
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEvent(MockRulesViewEvent.List.DeleteRule(rule.id))
                ruleToDelete = null
            },
            dismissLabel = stringResource(R.string.mock_rules_dialog_delete_all_cancel),
            onDismiss = { ruleToDelete = null },
            destructive = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockRulesTopBar(
    hasRules: Boolean,
    onDeleteAll: () -> Unit,
    onBack: (() -> Unit)?,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.mock_rules_title)) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.mock_rules_back),
                    )
                }
            }
        },
        actions = {
            if (hasRules) {
                IconButton(onClick = onDeleteAll) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = stringResource(R.string.mock_rules_delete_all),
                    )
                }
            }
        },
    )
}

@Composable
private fun MockRulesContent(
    state: MockRulesViewState,
    onEvent: (MockRulesViewEvent) -> Unit,
    onNavigateToEditor: (String?) -> Unit,
    onDeleteRule: (MockRule) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = WormaCeptorTokens.Spacing.lg,
            vertical = WormaCeptorTokens.Spacing.sm,
        ),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        item(key = "master_toggle") {
            MasterToggleCard(
                enabled = state.mockingEnabled,
                ruleCount = state.rules.size,
                onToggle = { onEvent(MockRulesViewEvent.List.ToggleMocking) },
            )
        }

        if (state.rules.isEmpty()) {
            item(key = "empty_state") {
                EmptyRulesState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WormaCeptorTokens.Spacing.xxl),
                )
            }
        }

        items(
            items = state.rules,
            key = { it.id },
        ) { rule ->
            MockRuleItem(
                rule = rule,
                onToggle = { onEvent(MockRulesViewEvent.List.ToggleRule(rule.id)) },
                onDelete = { onDeleteRule(rule) },
                onClick = { onNavigateToEditor(rule.id) },
            )
        }
    }
}

@Composable
private fun MasterToggleCard(
    enabled: Boolean,
    ruleCount: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val statusColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "master_toggle_status",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD)
        },
        animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        label = "master_toggle_bg",
    )

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = backgroundColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = enabled,
                    role = Role.Switch,
                    onValueChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggle()
                    },
                )
                .padding(WormaCeptorTokens.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorTokens.Spacing.xxxl)
                        .clip(RoundedCornerShape(WormaCeptorTokens.Radius.lg))
                        .background(statusColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(WormaCeptorTokens.Spacing.xl),
                    )
                }

                Column {
                    val mockingStatusRes = if (enabled) {
                        R.string.mock_rules_mocking_enabled
                    } else {
                        R.string.mock_rules_mocking_disabled
                    }
                    Text(
                        text = stringResource(mockingStatusRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val pluralSuffix = if (ruleCount != 1) {
                        stringResource(R.string.mock_rules_count_plural)
                    } else {
                        stringResource(R.string.mock_rules_count_singular)
                    }
                    Text(
                        text = stringResource(R.string.mock_rules_count, ruleCount, pluralSuffix),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun MockRuleItem(
    rule: MockRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val ruleStatusColor = statusColor(rule.response.statusCode)
    val contentAlpha = if (rule.enabled) 1f else WormaCeptorTokens.Alpha.MODERATE

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(WormaCeptorTokens.Shapes.card)
            .background(
                color = if (rule.enabled) {
                    ruleStatusColor.copy(alpha = TokenAlpha.SUBTLE)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.SOFT)
                },
                shape = WormaCeptorTokens.Shapes.card,
            )
            .clickable(onClick = onClick)
            .padding(WormaCeptorTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left content
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha),
        ) {
            // Top row: method badge + rule name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                val ruleMethod = rule.matcher.method
                if (ruleMethod != null) {
                    WormaCeptorMethodBadge(ruleMethod)
                }
                Text(
                    text = rule.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

            // URL pattern + match type chip row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
            ) {
                MatchTypeChip(matchType = rule.matcher.matchType)
                Text(
                    text = rule.matcher.urlPattern,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

        // Right side: status code + switch + delete
        Column(horizontalAlignment = Alignment.End) {
            // Status code chip
            WormaCeptorStatusBadge(
                text = rule.response.statusCode.toString(),
                containerColor = ruleStatusColor.copy(alpha = TokenAlpha.SUBTLE),
                contentColor = ruleStatusColor,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))

            // Switch + delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.xl),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.mock_rules_delete_rule),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                }
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggle()
                    },
                )
            }
        }
    }
}

@Composable
private fun statusColor(code: Int): Color = when (code) {
    in 200..299 -> MaterialTheme.colorScheme.primary
    in 300..399 -> MaterialTheme.colorScheme.secondary
    in 400..499 -> MaterialTheme.colorScheme.error
    in 500..599 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun MatchTypeChip(
    matchType: UrlMatchType,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(
        when (matchType) {
            UrlMatchType.EXACT -> R.string.mock_rules_match_exact
            UrlMatchType.PREFIX -> R.string.mock_rules_match_prefix
            UrlMatchType.REGEX -> R.string.mock_rules_match_regex
        },
    )

    Surface(
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.chip,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = WormaCeptorTokens.Spacing.xxs),
        )
    }
}

@Composable
private fun EmptyRulesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.xxxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

        Text(
            text = stringResource(R.string.mock_rules_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

        Text(
            text = stringResource(R.string.mock_rules_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
            textAlign = TextAlign.Center,
        )
    }
}
