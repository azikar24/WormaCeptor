/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.gridoverlay

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.GridOverlayEngine
import com.azikar24.wormaceptor.domain.entities.GridConfig
import kotlinx.coroutines.flow.StateFlow

object GridOverlayFeature {
    fun createEngine(context: Context) = GridOverlayEngine(context.applicationContext)
    fun createViewModelFactory(engine: GridOverlayEngine) = GridOverlayViewModelFactory(engine)
}

class GridOverlayViewModel(private val engine: GridOverlayEngine) : ViewModel() {
    val config: StateFlow<GridConfig> = engine.config
    val isEnabled: StateFlow<Boolean> = engine.isEnabled
    private var activity: Activity? = null

    fun setActivity(activity: Activity) {
        this.activity = activity
    }
    fun toggleEnabled() {
        if (engine.isEnabled.value) engine.disable() else activity?.let { engine.enable(it) }
    }
    fun setGridSize(size: Int) = engine.setGridSize(size)
    fun toggleKeylines() = engine.toggleKeylines()
    fun toggleBaseline() = engine.toggleBaseline()
    fun setBaselineSize(size: Int) = engine.setBaselineSize(size)
    fun toggleSpacing() = engine.toggleSpacing()
    fun setGridAlpha(alpha: Float) = engine.setGridAlpha(alpha)
}

class GridOverlayViewModelFactory(private val engine: GridOverlayEngine) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = GridOverlayViewModel(engine) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridOverlayControl(activity: Activity, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine = remember { GridOverlayFeature.createEngine(activity) }
    val factory = remember { GridOverlayFeature.createViewModelFactory(engine) }
    val viewModel: GridOverlayViewModel = viewModel(factory = factory)

    LaunchedEffect(activity) { viewModel.setActivity(activity) }

    val config by viewModel.config.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.GridOn, null, tint = Color(0xFF2196F3))
                        Text("Grid Overlay", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(
                            onClick = it,
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), Arrangement.spacedBy(16.dp)) {
            // Enable toggle
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(Modifier.size(48.dp), Alignment.Center) {
                            Icon(
                                Icons.Default.GridOn,
                                null,
                                tint = if (isEnabled) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column {
                            Text("Grid Overlay", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (isEnabled) "Showing on screen" else "Hidden",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Switch(checked = isEnabled, onCheckedChange = { viewModel.toggleEnabled() })
                }
            }

            // Grid size
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Grid Size", fontWeight = FontWeight.SemiBold)
                        Text("${config.gridSize}dp", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        GridConfig.GRID_SIZE_PRESETS.forEach { size ->
                            FilterChip(
                                selected = config.gridSize == size,
                                onClick = { viewModel.setGridSize(size) },
                                label = { Text("${size}dp") },
                                enabled = isEnabled,
                            )
                        }
                    }
                }
            }

            // Options
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(8.dp)) {
                    Text("Options", fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Show Keylines", color = Color(0xFF212121))
                        Switch(
                            checked = config.showKeylines,
                            onCheckedChange = { viewModel.toggleKeylines() },
                            enabled = isEnabled,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Show Spacing", color = Color(0xFF212121))
                        Switch(
                            checked = config.showSpacing,
                            onCheckedChange = { viewModel.toggleSpacing() },
                            enabled = isEnabled,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Baseline Grid", color = Color(0xFF212121))
                        Switch(
                            checked = config.baselineGridEnabled,
                            onCheckedChange = { viewModel.toggleBaseline() },
                            enabled = isEnabled,
                        )
                    }
                }
            }

            // Opacity
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Opacity", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${(config.gridAlpha * 100).toInt()}%",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Slider(
                        value = config.gridAlpha,
                        onValueChange = { viewModel.setGridAlpha(it) },
                        valueRange = 0.1f..1f,
                        enabled = isEnabled,
                    )
                }
            }
        }
    }
}
