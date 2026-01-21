/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.interception

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.InterceptionEngine
import com.azikar24.wormaceptor.core.engine.InterceptionStats
import com.azikar24.wormaceptor.domain.entities.InterceptionAction
import com.azikar24.wormaceptor.domain.entities.InterceptionConfig
import com.azikar24.wormaceptor.domain.entities.InterceptionEvent
import com.azikar24.wormaceptor.domain.entities.InterceptionRule
import com.azikar24.wormaceptor.domain.entities.InterceptionType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ================== Feature Object ==================

object InterceptionFeature {
    fun createEngine() = InterceptionEngine()
    fun createViewModelFactory(engine: InterceptionEngine) = InterceptionViewModelFactory(engine)
}

// ================== ViewModel ==================

class InterceptionViewModel(private val engine: InterceptionEngine) : ViewModel() {
    val config: StateFlow<InterceptionConfig> = engine.config
    val events: StateFlow<List<InterceptionEvent>> = engine.events
    val stats: StateFlow<InterceptionStats> = engine.stats

    fun toggleGlobalEnabled() {
        if (engine.isEnabled()) engine.disable() else engine.enable()
    }

    fun setTypeEnabled(type: InterceptionType, enabled: Boolean) = engine.setTypeEnabled(type, enabled)
    fun setLogEvents(enabled: Boolean) = engine.setLogEvents(enabled)

    fun addRule(rule: InterceptionRule) = engine.addRule(rule)
    fun removeRule(ruleId: String) = engine.removeRule(ruleId)
    fun updateRule(rule: InterceptionRule) = engine.updateRule(rule)
    fun setRuleEnabled(ruleId: String, enabled: Boolean) = engine.setRuleEnabled(ruleId, enabled)

    fun clearEvents() = engine.clearEvents()
    fun clearStats() = engine.clearStats()
    fun clearRules() = engine.clearRules()

    fun getEventsByType(type: InterceptionType) = engine.getEventsByType(type)
}

// ================== ViewModel Factory ==================

class InterceptionViewModelFactory(private val engine: InterceptionEngine) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = InterceptionViewModel(engine) as T
}

// ================== Main Composable ==================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterceptionFramework(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine = remember { InterceptionFeature.createEngine() }
    val factory = remember { InterceptionFeature.createViewModelFactory(engine) }
    val viewModel: InterceptionViewModel = viewModel(factory = factory)

    val config by viewModel.config.collectAsState()
    val events by viewModel.events.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddRuleSheet by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showEventLogSheet by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<InterceptionRule?>(null) }

    val tabs = listOf(
        TabInfo("Overview", Icons.Default.Layers),
        TabInfo("View", Icons.Default.Visibility),
        TabInfo("Touch", Icons.Default.TouchApp),
        TabInfo("Location", Icons.Default.LocationOn),
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.FilterList, null, tint = Color(0xFF9C27B0))
                        Text("Interception Framework", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (events.isNotEmpty()) {
                                Badge { Text("${events.size}") }
                            }
                        },
                    ) {
                        IconButton(onClick = { showEventLogSheet = true }) {
                            Icon(Icons.Default.ReceiptLong, "Event Log")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (selectedTabIndex > 0) {
                FloatingActionButton(
                    onClick = { showAddRuleSheet = true },
                    containerColor = Color(0xFF9C27B0),
                ) {
                    Icon(Icons.Default.Add, "Add Rule", tint = Color.White)
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, null, modifier = Modifier.size(20.dp)) },
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> OverviewTab(
                    config = config,
                    stats = stats,
                    onToggleGlobalEnabled = viewModel::toggleGlobalEnabled,
                    onToggleTypeEnabled = viewModel::setTypeEnabled,
                    onToggleLogEvents = viewModel::setLogEvents,
                    onShowTemplates = { showTemplatesSheet = true },
                    onClearStats = viewModel::clearStats,
                    onClearRules = viewModel::clearRules,
                )
                1 -> RulesTab(
                    type = InterceptionType.VIEW,
                    config = config,
                    onToggleRule = viewModel::setRuleEnabled,
                    onEditRule = { editingRule = it },
                    onDeleteRule = viewModel::removeRule,
                )
                2 -> RulesTab(
                    type = InterceptionType.TOUCH,
                    config = config,
                    onToggleRule = viewModel::setRuleEnabled,
                    onEditRule = { editingRule = it },
                    onDeleteRule = viewModel::removeRule,
                )
                3 -> RulesTab(
                    type = InterceptionType.LOCATION,
                    config = config,
                    onToggleRule = viewModel::setRuleEnabled,
                    onEditRule = { editingRule = it },
                    onDeleteRule = viewModel::removeRule,
                )
            }
        }
    }

    // Add Rule Bottom Sheet
    if (showAddRuleSheet) {
        val currentType = when (selectedTabIndex) {
            1 -> InterceptionType.VIEW
            2 -> InterceptionType.TOUCH
            3 -> InterceptionType.LOCATION
            else -> InterceptionType.VIEW
        }
        AddRuleBottomSheet(
            type = currentType,
            onDismiss = { showAddRuleSheet = false },
            onAddRule = { rule ->
                viewModel.addRule(rule)
                showAddRuleSheet = false
            },
        )
    }

    // Templates Bottom Sheet
    if (showTemplatesSheet) {
        TemplatesBottomSheet(
            onDismiss = { showTemplatesSheet = false },
            onSelectTemplate = { template ->
                viewModel.addRule(template.copy(id = java.util.UUID.randomUUID().toString()))
                showTemplatesSheet = false
            },
        )
    }

    // Event Log Bottom Sheet
    if (showEventLogSheet) {
        EventLogBottomSheet(
            events = events,
            onDismiss = { showEventLogSheet = false },
            onClearEvents = viewModel::clearEvents,
        )
    }

    // Edit Rule Dialog
    editingRule?.let { rule ->
        EditRuleDialog(
            rule = rule,
            onDismiss = { editingRule = null },
            onSave = { updatedRule ->
                viewModel.updateRule(updatedRule)
                editingRule = null
            },
        )
    }
}

// ================== Tab Components ==================

private data class TabInfo(val title: String, val icon: ImageVector)

@Composable
private fun OverviewTab(
    config: InterceptionConfig,
    stats: InterceptionStats,
    onToggleGlobalEnabled: () -> Unit,
    onToggleTypeEnabled: (InterceptionType, Boolean) -> Unit,
    onToggleLogEvents: (Boolean) -> Unit,
    onShowTemplates: () -> Unit,
    onClearStats: () -> Unit,
    onClearRules: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Global Enable Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (config.globalEnabled) Color(0xFF9C27B0).copy(alpha = 0.1f) else Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (config.globalEnabled) Icons.Default.PlayArrow else Icons.Default.Block,
                                null,
                                tint = if (config.globalEnabled) Color(0xFF9C27B0) else Color(0xFF9E9E9E),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column {
                            Text("Interception Framework", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (config.globalEnabled) "Active - intercepting events" else "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575),
                            )
                        }
                    }
                    Switch(
                        checked = config.globalEnabled,
                        onCheckedChange = { onToggleGlobalEnabled() },
                    )
                }
            }
        }

        // Type Toggles
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Interception Types", fontWeight = FontWeight.SemiBold)

                    InterceptionType.entries.forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    when (type) {
                                        InterceptionType.VIEW -> Icons.Default.Visibility
                                        InterceptionType.TOUCH -> Icons.Default.TouchApp
                                        InterceptionType.LOCATION -> Icons.Default.LocationOn
                                    },
                                    null,
                                    tint = Color(0xFF757575),
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    "${config.enabledRuleCountByType(type)}/${config.ruleCountByType(type)} rules",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575),
                                )
                                Switch(
                                    checked = config.isTypeEnabled(type),
                                    onCheckedChange = { onToggleTypeEnabled(type, it) },
                                    enabled = config.globalEnabled,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Statistics Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Statistics", fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = onClearStats) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatItem("Total", stats.totalInterceptions, Color(0xFF2196F3))
                        StatItem("Blocked", stats.totalBlocked, Color(0xFFF44336))
                        StatItem("View", stats.viewInterceptions, Color(0xFF4CAF50))
                        StatItem("Touch", stats.touchInterceptions, Color(0xFFFF9800))
                        StatItem("Location", stats.locationInterceptions, Color(0xFF9C27B0))
                    }
                }
            }
        }

        // Options Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Options", fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Log Events")
                        Switch(
                            checked = config.logEvents,
                            onCheckedChange = onToggleLogEvents,
                        )
                    }
                }
            }
        }

        // Quick Actions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Quick Actions", fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onShowTemplates,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Templates")
                        }

                        Button(
                            onClick = onClearRules,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear Rules")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$value",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = color,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575),
        )
    }
}

@Composable
private fun RulesTab(
    type: InterceptionType,
    config: InterceptionConfig,
    onToggleRule: (String, Boolean) -> Unit,
    onEditRule: (InterceptionRule) -> Unit,
    onDeleteRule: (String) -> Unit,
) {
    val rules = config.rulesByType(type)

    if (rules.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    when (type) {
                        InterceptionType.VIEW -> Icons.Default.Visibility
                        InterceptionType.TOUCH -> Icons.Default.TouchApp
                        InterceptionType.LOCATION -> Icons.Default.LocationOn
                    },
                    null,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No ${type.name.lowercase()} rules",
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "Tap + to add a rule",
                    color = Color(0xFF9E9E9E),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(rules, key = { it.id }) { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { onToggleRule(rule.id, it) },
                    onEdit = { onEditRule(rule) },
                    onDelete = { onDeleteRule(rule.id) },
                )
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: InterceptionRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.enabled) Color(0xFFFAFAFA) else Color(0xFFF5F5F5),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getActionColor(rule.action).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            getActionIcon(rule.action),
                            null,
                            tint = getActionColor(rule.action),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            rule.name,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            rule.action.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = getActionColor(rule.action),
                        )
                    }
                }
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = onToggle,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider()

                    Text(
                        "Pattern: ${rule.targetPattern}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF616161),
                    )

                    if (rule.parameters.isNotEmpty()) {
                        Text(
                            "Parameters:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                        rule.parameters.forEach { (key, value) ->
                            Text(
                                "  $key: $value",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF757575),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit")
                        }
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336)),
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Rule") },
            text = { Text("Are you sure you want to delete \"${rule.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336)),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

// ================== Bottom Sheets ==================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRuleBottomSheet(
    type: InterceptionType,
    onDismiss: () -> Unit,
    onAddRule: (InterceptionRule) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var targetPattern by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf(InterceptionAction.LOG_ONLY) }

    // Type-specific parameters
    var offsetX by remember { mutableStateOf("0") }
    var offsetY by remember { mutableStateOf("0") }
    var delayMs by remember { mutableStateOf("100") }
    var mockLat by remember { mutableStateOf("40.7128") }
    var mockLng by remember { mutableStateOf("-74.0060") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Add ${type.name.lowercase().replaceFirstChar { it.uppercase() }} Rule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Rule Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = targetPattern,
                onValueChange = { targetPattern = it },
                label = { Text("Target Pattern (regex or *)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("Action", fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                getActionsForType(type).forEach { action ->
                    FilterChip(
                        selected = selectedAction == action,
                        onClick = { selectedAction = action },
                        label = { Text(action.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            // Type-specific inputs
            when {
                type == InterceptionType.TOUCH && selectedAction == InterceptionAction.MODIFY -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = offsetX,
                            onValueChange = { offsetX = it },
                            label = { Text("Offset X") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        OutlinedTextField(
                            value = offsetY,
                            onValueChange = { offsetY = it },
                            label = { Text("Offset Y") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                }
                type == InterceptionType.TOUCH && selectedAction == InterceptionAction.DELAY -> {
                    OutlinedTextField(
                        value = delayMs,
                        onValueChange = { delayMs = it },
                        label = { Text("Delay (ms)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                type == InterceptionType.LOCATION && selectedAction == InterceptionAction.MOCK -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = mockLat,
                            onValueChange = { mockLat = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                        OutlinedTextField(
                            value = mockLng,
                            onValueChange = { mockLng = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val parameters = buildMap {
                        when {
                            type == InterceptionType.VIEW -> {
                                put(InterceptionRule.PARAM_VIEW_CLASS_PATTERN, targetPattern)
                            }
                            type == InterceptionType.TOUCH && selectedAction == InterceptionAction.MODIFY -> {
                                put(InterceptionRule.PARAM_TOUCH_OFFSET_X, offsetX)
                                put(InterceptionRule.PARAM_TOUCH_OFFSET_Y, offsetY)
                            }
                            type == InterceptionType.TOUCH && selectedAction == InterceptionAction.DELAY -> {
                                put(InterceptionRule.PARAM_TOUCH_DELAY_MS, delayMs)
                            }
                            type == InterceptionType.LOCATION && selectedAction == InterceptionAction.MOCK -> {
                                put(InterceptionRule.PARAM_MOCK_LATITUDE, mockLat)
                                put(InterceptionRule.PARAM_MOCK_LONGITUDE, mockLng)
                            }
                        }
                    }

                    val rule = InterceptionRule(
                        name = name.ifBlank { "Rule ${System.currentTimeMillis()}" },
                        type = type,
                        targetPattern = targetPattern.ifBlank { "*" },
                        action = selectedAction,
                        parameters = parameters,
                    )
                    onAddRule(rule)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() || targetPattern.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
            ) {
                Text("Add Rule")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatesBottomSheet(
    onDismiss: () -> Unit,
    onSelectTemplate: (InterceptionRule) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Rule Templates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InterceptionType.entries.forEach { type ->
                    val rules = InterceptionConfig.Companion.Templates.templatesForType(type)
                    if (rules.isNotEmpty()) {
                        item(key = "header_${type.name}") {
                            Text(
                                type.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575),
                            )
                        }
                        rules.forEach { template ->
                            item(key = "rule_${template.id}") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectTemplate(template) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(getActionColor(template.action).copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                getActionIcon(template.action),
                                                null,
                                                tint = getActionColor(template.action),
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(template.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                template.targetPattern,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF757575),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                        Icon(
                                            Icons.Default.Add,
                                            null,
                                            tint = Color(0xFF9C27B0),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventLogBottomSheet(
    events: List<InterceptionEvent>,
    onDismiss: () -> Unit,
    onClearEvents: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filterType by remember { mutableStateOf<InterceptionType?>(null) }

    val filteredEvents = remember(events, filterType) {
        if (filterType == null) events else events.filter { it.type == filterType }
    }.reversed() // Show newest first

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Event Log (${filteredEvents.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onClearEvents) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { filterType = null },
                    label = { Text("All") },
                )
                InterceptionType.entries.forEach { type ->
                    FilterChip(
                        selected = filterType == type,
                        onClick = { filterType = type },
                        label = { Text(type.name.take(1) + type.name.drop(1).lowercase()) },
                    )
                }
            }

            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No events logged", color = Color(0xFF757575))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventItem(event)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EventItem(event: InterceptionEvent) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(getActionColor(event.action).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when (event.type) {
                        InterceptionType.VIEW -> Icons.Default.Visibility
                        InterceptionType.TOUCH -> Icons.Default.TouchApp
                        InterceptionType.LOCATION -> Icons.Default.LocationOn
                    },
                    null,
                    tint = getActionColor(event.action),
                    modifier = Modifier.size(16.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.ruleName ?: event.type.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                )
                Text(
                    event.originalValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (event.wasModified && event.interceptedValue != null) {
                    Text(
                        "-> ${event.interceptedValue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                event.formattedTimestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
            )
        }
    }
}

// ================== Edit Rule Dialog ==================

@Composable
private fun EditRuleDialog(
    rule: InterceptionRule,
    onDismiss: () -> Unit,
    onSave: (InterceptionRule) -> Unit,
) {
    var name by remember { mutableStateOf(rule.name) }
    var targetPattern by remember { mutableStateOf(rule.targetPattern) }
    var priority by remember { mutableStateOf(rule.priority.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = targetPattern,
                    onValueChange = { targetPattern = it },
                    label = { Text("Target Pattern") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        rule.copy(
                            name = name,
                            targetPattern = targetPattern,
                            priority = priority.toIntOrNull() ?: 0,
                        ),
                    )
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ================== Helper Functions ==================

private fun getActionColor(action: InterceptionAction): Color = when (action) {
    InterceptionAction.BLOCK -> Color(0xFFF44336)
    InterceptionAction.LOG_ONLY -> Color(0xFF2196F3)
    InterceptionAction.MODIFY -> Color(0xFF4CAF50)
    InterceptionAction.DELAY -> Color(0xFFFF9800)
    InterceptionAction.MOCK -> Color(0xFF9C27B0)
}

private fun getActionIcon(action: InterceptionAction): ImageVector = when (action) {
    InterceptionAction.BLOCK -> Icons.Default.Block
    InterceptionAction.LOG_ONLY -> Icons.Default.ReceiptLong
    InterceptionAction.MODIFY -> Icons.Default.Edit
    InterceptionAction.DELAY -> Icons.Default.ReceiptLong
    InterceptionAction.MOCK -> Icons.Default.LocationOn
}

private fun getActionsForType(type: InterceptionType): List<InterceptionAction> = when (type) {
    InterceptionType.VIEW -> listOf(InterceptionAction.BLOCK, InterceptionAction.LOG_ONLY)
    InterceptionType.TOUCH -> listOf(InterceptionAction.BLOCK, InterceptionAction.MODIFY, InterceptionAction.DELAY, InterceptionAction.LOG_ONLY)
    InterceptionType.LOCATION -> listOf(InterceptionAction.BLOCK, InterceptionAction.MOCK, InterceptionAction.MODIFY, InterceptionAction.LOG_ONLY)
}

// ================== External Engine Composable ==================

/**
 * Main composable with an externally provided engine.
 * Use this when you need to share the engine across the application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterceptionFramework(
    engine: InterceptionEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val factory = remember(engine) { InterceptionFeature.createViewModelFactory(engine) }
    val viewModel: InterceptionViewModel = viewModel(factory = factory)

    val config by viewModel.config.collectAsState()
    val events by viewModel.events.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddRuleSheet by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showEventLogSheet by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<InterceptionRule?>(null) }

    val tabs = listOf(
        TabInfo("Overview", Icons.Default.Layers),
        TabInfo("View", Icons.Default.Visibility),
        TabInfo("Touch", Icons.Default.TouchApp),
        TabInfo("Location", Icons.Default.LocationOn),
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.FilterList, null, tint = Color(0xFF9C27B0))
                        Text("Interception Framework", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (events.isNotEmpty()) {
                                Badge { Text("${events.size}") }
                            }
                        },
                    ) {
                        IconButton(onClick = { showEventLogSheet = true }) {
                            Icon(Icons.Default.ReceiptLong, "Event Log")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (selectedTabIndex > 0) {
                FloatingActionButton(
                    onClick = { showAddRuleSheet = true },
                    containerColor = Color(0xFF9C27B0),
                ) {
                    Icon(Icons.Default.Add, "Add Rule", tint = Color.White)
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, null, modifier = Modifier.size(20.dp)) },
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> OverviewTab(
                    config = config,
                    stats = stats,
                    onToggleGlobalEnabled = viewModel::toggleGlobalEnabled,
                    onToggleTypeEnabled = viewModel::setTypeEnabled,
                    onToggleLogEvents = viewModel::setLogEvents,
                    onShowTemplates = { showTemplatesSheet = true },
                    onClearStats = viewModel::clearStats,
                    onClearRules = viewModel::clearRules,
                )
                1 -> RulesTab(
                    type = InterceptionType.VIEW,
                    config = config,
                    onToggleRule = viewModel::setRuleEnabled,
                    onEditRule = { editingRule = it },
                    onDeleteRule = viewModel::removeRule,
                )
                2 -> RulesTab(
                    type = InterceptionType.TOUCH,
                    config = config,
                    onToggleRule = viewModel::setRuleEnabled,
                    onEditRule = { editingRule = it },
                    onDeleteRule = viewModel::removeRule,
                )
                3 -> RulesTab(
                    type = InterceptionType.LOCATION,
                    config = config,
                    onToggleRule = viewModel::setRuleEnabled,
                    onEditRule = { editingRule = it },
                    onDeleteRule = viewModel::removeRule,
                )
            }
        }
    }

    if (showAddRuleSheet) {
        val currentType = when (selectedTabIndex) {
            1 -> InterceptionType.VIEW
            2 -> InterceptionType.TOUCH
            3 -> InterceptionType.LOCATION
            else -> InterceptionType.VIEW
        }
        AddRuleBottomSheet(
            type = currentType,
            onDismiss = { showAddRuleSheet = false },
            onAddRule = { rule ->
                viewModel.addRule(rule)
                showAddRuleSheet = false
            },
        )
    }

    if (showTemplatesSheet) {
        TemplatesBottomSheet(
            onDismiss = { showTemplatesSheet = false },
            onSelectTemplate = { template ->
                viewModel.addRule(template.copy(id = java.util.UUID.randomUUID().toString()))
                showTemplatesSheet = false
            },
        )
    }

    if (showEventLogSheet) {
        EventLogBottomSheet(
            events = events,
            onDismiss = { showEventLogSheet = false },
            onClearEvents = viewModel::clearEvents,
        )
    }

    editingRule?.let { rule ->
        EditRuleDialog(
            rule = rule,
            onDismiss = { editingRule = null },
            onSave = { updatedRule ->
                viewModel.updateRule(updatedRule)
                editingRule = null
            },
        )
    }
}
