/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.composerender

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.azikar24.wormaceptor.core.engine.ComposeRenderEngine
import com.azikar24.wormaceptor.domain.entities.ComposeRenderInfo
import com.azikar24.wormaceptor.domain.entities.ComposeRenderStats
import kotlinx.coroutines.flow.StateFlow

object ComposeRenderFeature {
    fun createEngine() = ComposeRenderEngine()
    fun getGlobalEngine() = ComposeRenderEngine.getInstance()
    fun createViewModelFactory(engine: ComposeRenderEngine) = ComposeRenderViewModelFactory(engine)
}

class ComposeRenderViewModel(private val engine: ComposeRenderEngine) : ViewModel() {
    val composables: StateFlow<List<ComposeRenderInfo>> = engine.composables
    val stats: StateFlow<ComposeRenderStats> = engine.stats
    val isTracking: StateFlow<Boolean> = engine.isTracking

    fun toggleTracking() {
        if (engine.isTracking.value) engine.stopTracking() else engine.startTracking()
    }
    fun clearStats() = engine.clearStats()
}

class ComposeRenderViewModelFactory(private val engine: ComposeRenderEngine) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ComposeRenderViewModel(engine) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeRenderTracker(modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine = remember { ComposeRenderFeature.getGlobalEngine() }
    val factory = remember { ComposeRenderFeature.createViewModelFactory(engine) }
    val viewModel: ComposeRenderViewModel = viewModel(factory = factory)

    val composables by viewModel.composables.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()

    val statusColor by animateColorAsState(
        if (isTracking) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
        animationSpec = tween(300),
        label = "status",
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
                        Icon(Icons.Default.Speed, null, tint = Color(0xFF9C27B0))
                        Text("Compose Render", fontWeight = FontWeight.SemiBold)
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            1f,
                            if (isTracking) 0.5f else 1f,
                            infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                            label = "alpha",
                        )
                        Box(
                            Modifier.size(
                                8.dp,
                            ).clip(CircleShape).background(statusColor.copy(alpha = if (isTracking) alpha else 1f)),
                        )
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
                    IconButton(onClick = { viewModel.toggleTracking() }) {
                        Icon(
                            if (isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (isTracking) "Stop" else "Start",
                            tint = statusColor,
                        )
                    }
                    IconButton(onClick = { viewModel.clearStats() }) { Icon(Icons.Default.Delete, "Clear") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Stats card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            StatItem("Composables", stats.totalComposables.toString(), Color(0xFF9C27B0))
                            StatItem("Recompositions", stats.totalRecompositions.toString(), Color(0xFFFF9800))
                            StatItem(
                                "Avg Ratio",
                                String.format("%.0f%%", stats.averageRecomposeRatio * 100),
                                Color(0xFF2196F3),
                            )
                        }
                        stats.mostRecomposed?.let {
                            Surface(
                                Modifier.fillMaxWidth(),
                                RoundedCornerShape(8.dp),
                                Color(0xFFFF9800).copy(alpha = 0.1f),
                            ) {
                                Row(Modifier.padding(12.dp), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        "Most recomposed: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF9800),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Composables list
            item {
                Text(
                    "Tracked Composables",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (composables.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(150.dp), Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Default.Speed,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp),
                            )
                            Text(
                                if (isTracking) "Tracking..." else "No composables tracked",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "Use trackRecomposition() in your composables",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            } else {
                items(composables, key = { it.composableName }) { info ->
                    val isExcessive = info.recomposeRatio >= ComposeRenderInfo.EXCESSIVE_RECOMPOSE_RATIO
                    val isSlow = info.averageRenderTimeNs >= ComposeRenderInfo.SLOW_RENDER_THRESHOLD_NS

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(info.composableName, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (isExcessive) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                                        ) {
                                            Text(
                                                "HIGH",
                                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                Color(0xFFFF9800),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                    if (isSlow) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF44336).copy(alpha = 0.15f),
                                        ) {
                                            Text(
                                                "SLOW",
                                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                Color(0xFFF44336),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Column {
                                    Text(
                                        "Recompositions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        info.recomposeCount.toString(),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9800),
                                    )
                                }
                                Column {
                                    Text(
                                        "Skips",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        info.skipCount.toString(),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50),
                                    )
                                }
                                Column {
                                    Text(
                                        "Ratio",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        "${(info.recomposeRatio * 100).toInt()}%",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExcessive) Color(0xFFFF9800) else Color(0xFF2196F3),
                                    )
                                }
                                Column {
                                    Text(
                                        "Avg Time",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        "${info.averageRenderTimeNs / 1000}μs",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSlow) Color(0xFFF44336) else Color(0xFF4CAF50),
                                    )
                                }
                            }
                            if (info.parameters.isNotEmpty()) {
                                Text(
                                    "Params: ${info.parameters.joinToString()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
