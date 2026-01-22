/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.measurement

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.MeasurementEngine
import com.azikar24.wormaceptor.domain.entities.MeasurementConfig
import com.azikar24.wormaceptor.domain.entities.MeasurementMode
import com.azikar24.wormaceptor.domain.entities.MeasurementResult
import com.azikar24.wormaceptor.domain.entities.ViewMeasurement
import kotlinx.coroutines.flow.StateFlow

object MeasurementFeature {
    fun createEngine() = MeasurementEngine()
    fun createViewModelFactory(engine: MeasurementEngine) = MeasurementViewModelFactory(engine)
}

class MeasurementViewModel(private val engine: MeasurementEngine) : ViewModel() {
    val isEnabled: StateFlow<Boolean> = engine.isEnabled
    val mode: StateFlow<MeasurementMode> = engine.mode
    val currentMeasurement: StateFlow<MeasurementResult?> = engine.currentMeasurement
    val selectedView: StateFlow<ViewMeasurement?> = engine.selectedView
    val config: StateFlow<MeasurementConfig> = engine.config
    private var activity: Activity? = null

    fun setActivity(activity: Activity) {
        this.activity = activity
    }
    fun toggleEnabled() {
        if (engine.isEnabled.value) engine.disable() else activity?.let { engine.enable(it) }
    }
    fun setMode(mode: MeasurementMode) = engine.setMode(mode)
    fun clear() = engine.clear()
    fun toggleSnapToGrid() = engine.updateConfig(engine.config.value.copy(snapToGrid = !engine.config.value.snapToGrid))
    fun toggleGuidelines() =
        engine.updateConfig(engine.config.value.copy(showGuidelines = !engine.config.value.showGuidelines))
}

class MeasurementViewModelFactory(private val engine: MeasurementEngine) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MeasurementViewModel(engine) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementTool(activity: Activity, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine = remember { MeasurementFeature.createEngine() }
    val factory = remember { MeasurementFeature.createViewModelFactory(engine) }
    val viewModel: MeasurementViewModel = viewModel(factory = factory)

    LaunchedEffect(activity) { viewModel.setActivity(activity) }

    val isEnabled by viewModel.isEnabled.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val measurement by viewModel.currentMeasurement.collectAsState()
    val selectedView by viewModel.selectedView.collectAsState()
    val config by viewModel.config.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Straighten, null, tint = Color(0xFF009688))
                        Text("Measurement", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(
                            onClick = it,
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clear() }) { Icon(Icons.Default.Delete, "Clear") }
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
                        Box(
                            Modifier.size(
                                48.dp,
                            ).clip(
                                RoundedCornerShape(12.dp),
                            ).background(
                                (
                                    if (isEnabled) {
                                        Color(
                                            0xFF009688,
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    ).copy(alpha = 0.15f),
                            ),
                            Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Straighten,
                                null,
                                tint = if (isEnabled) Color(0xFF009688) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column {
                            Text("Measurement Mode", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (isEnabled) "Tap to measure" else "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Switch(checked = isEnabled, onCheckedChange = { viewModel.toggleEnabled() })
                }
            }

            // Mode selection
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                    Text("Mode", fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = mode == MeasurementMode.DISTANCE,
                            onClick = { viewModel.setMode(MeasurementMode.DISTANCE) },
                            label = {
                                Text("Distance")
                            },
                            leadingIcon = { Icon(Icons.Default.Straighten, null, Modifier.size(18.dp)) },
                            enabled = isEnabled,
                        )
                        FilterChip(
                            selected = mode == MeasurementMode.VIEW_BOUNDS,
                            onClick = { viewModel.setMode(MeasurementMode.VIEW_BOUNDS) },
                            label = {
                                Text("View Bounds")
                            },
                            leadingIcon = { Icon(Icons.Default.CropSquare, null, Modifier.size(18.dp)) },
                            enabled = isEnabled,
                        )
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
                        Text("Snap to Grid")
                        Switch(
                            checked = config.snapToGrid,
                            onCheckedChange = { viewModel.toggleSnapToGrid() },
                            enabled = isEnabled,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Show Guidelines")
                        Switch(
                            checked = config.showGuidelines,
                            onCheckedChange = { viewModel.toggleGuidelines() },
                            enabled = isEnabled,
                        )
                    }
                }
            }

            // Result
            measurement?.let { m ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF009688).copy(alpha = 0.1f)),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(8.dp)) {
                        Text("Distance Measurement", fontWeight = FontWeight.SemiBold, color = Color(0xFF009688))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            MeasureValue("Distance", "${m.distanceDp.toInt()} dp", Color(0xFF009688))
                            MeasureValue("Pixels", "${m.distancePx.toInt()} px", Color(0xFF2196F3))
                            MeasureValue("Angle", "${m.angle.toInt()}°", Color(0xFFFF9800))
                        }
                    }
                }
            }

            selectedView?.let { v ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF009688).copy(alpha = 0.1f)),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(8.dp)) {
                        Text("View Measurement", fontWeight = FontWeight.SemiBold, color = Color(0xFF009688))
                        Text(v.viewClass, fontFamily = FontFamily.Monospace, color = Color(0xFF212121))
                        v.resourceId?.let {
                            Text(
                                "@id/$it",
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Divider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            MeasureValue("Width", "${v.widthDp.toInt()} dp", Color(0xFF009688))
                            MeasureValue("Height", "${v.heightDp.toInt()} dp", Color(0xFF009688))
                            MeasureValue("Position", "${v.x}, ${v.y}", Color(0xFF2196F3))
                        }
                        if (v.paddingLeft > 0 || v.paddingTop > 0 || v.paddingRight > 0 || v.paddingBottom > 0) {
                            Text(
                                "Padding: ${v.paddingLeft}, ${v.paddingTop}, ${v.paddingRight}, ${v.paddingBottom}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (v.marginLeft > 0 || v.marginTop > 0 || v.marginRight > 0 || v.marginBottom > 0) {
                            Text(
                                "Margin: ${v.marginLeft}, ${v.marginTop}, ${v.marginRight}, ${v.marginBottom}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasureValue(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
