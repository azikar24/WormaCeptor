/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.engine.ComposeRenderEngine
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.ComposeRenderInfo
import com.azikar24.wormaceptor.domain.entities.ComposeRenderStats
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme

private val PurpleAccent = Color(0xFF9C27B0)

/**
 * Test activity for the Compose Render Tracker feature.
 * Provides test composables that trigger recompositions with inline tracking stats.
 */
class ComposeRenderTestActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start tracking with fresh data
        ComposeRenderEngine.getInstance().apply {
            clearStats()
            startTracking()
        }

        setContent {
            WormaCeptorMainTheme {
                val engine = remember { ComposeRenderEngine.getInstance() }
                val stats by engine.stats.collectAsState()
                val composables by engine.composables.collectAsState()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = PurpleAccent,
                                    )
                                    Column {
                                        Text(
                                            text = "Compose Render Test",
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        if (stats.totalRecompositions > 0) {
                                            Text(
                                                text = "${stats.totalRecompositions} recomposition${if (stats.totalRecompositions != 1) "s" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PurpleAccent,
                                            )
                                        }
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { engine.clearStats() },
                                    enabled = stats.totalRecompositions > 0,
                                ) {
                                    Icon(Icons.Default.Clear, "Clear stats")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        )
                    },
                ) { padding ->
                    RecompositionTestContent(
                        engine = engine,
                        stats = stats,
                        composables = composables,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ComposeRenderEngine.getInstance().stopTracking()
    }
}

/**
 * Test content with various composables that trigger recompositions.
 */
@Composable
private fun RecompositionTestContent(
    engine: ComposeRenderEngine,
    stats: ComposeRenderStats,
    composables: List<ComposeRenderInfo>,
    modifier: Modifier = Modifier,
) {
    var counter by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        Text(
            text = "Trigger recompositions to see them tracked below",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Counter section - triggers recomposition
        engine.trackRecomposition("CounterSection")
        CounterSection(
            count = counter,
            onIncrement = { counter++ },
            onDecrement = { counter-- },
            recomposeCount = composables.find { it.composableName == "CounterSection.Content" }?.recomposeCount ?: 0,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Animation section - triggers many recompositions
        engine.trackRecomposition("AnimationSection")
        AnimationSection(
            isAnimating = isAnimating,
            onToggle = { isAnimating = !isAnimating },
            recomposeCount = composables.find { it.composableName == "AnimationSection.Content" }?.recomposeCount ?: 0,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Color section - triggers recomposition on color change
        engine.trackRecomposition("ColorSection")
        ColorSection(
            count = counter,
            recomposeCount = composables.find { it.composableName == "ColorSection.Content" }?.recomposeCount ?: 0,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // Stats summary card
        TrackingStatsCard(stats = stats)

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Composable tracking details
        if (composables.isNotEmpty()) {
            Text(
                text = "Tracked Composables",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            composables.forEach { info ->
                ComposableInfoRow(info = info)
            }
        }
    }
}

@Composable
private fun CounterSection(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    recomposeCount: Int,
) {
    val engine = remember { ComposeRenderEngine.getInstance() }
    engine.trackRecomposition("CounterSection.Content", listOf("count=$count"))

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                )
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = "Counter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDecrement) {
                    Icon(Icons.Default.Remove, "Decrement")
                }

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Add, "Increment")
                }
            }
        }

        // Recomposition badge
        RecompositionBadge(
            count = recomposeCount,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp),
        )
    }
}

@Composable
private fun AnimationSection(
    isAnimating: Boolean,
    onToggle: () -> Unit,
    recomposeCount: Int,
) {
    val engine = remember { ComposeRenderEngine.getInstance() }
    engine.trackRecomposition("AnimationSection.Content", listOf("isAnimating=$isAnimating"))

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        label = "scale",
    )

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                )
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = "Animation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(scale)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                        ),
                )

                Button(onClick = onToggle) {
                    Text(if (isAnimating) "Stop" else "Animate")
                }
            }
        }

        // Recomposition badge
        RecompositionBadge(
            count = recomposeCount,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp),
        )
    }
}

@Composable
private fun ColorSection(count: Int, recomposeCount: Int) {
    val engine = remember { ComposeRenderEngine.getInstance() }
    engine.trackRecomposition("ColorSection.Content", listOf("count=$count"))

    val colors = listOf(
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
    )

    val targetColor = colors[count.mod(colors.size)]
    val animatedColor by animateColorAsState(targetColor, label = "color")

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                )
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = "Color Change",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            Text(
                text = "Change counter to see color animate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                colors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                if (index == count.mod(colors.size)) {
                                    animatedColor
                                } else {
                                    color.copy(alpha = 0.3f)
                                },
                                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                            ),
                    )
                }
            }
        }

        // Recomposition badge
        RecompositionBadge(
            count = recomposeCount,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp),
        )
    }
}

@Composable
private fun RecompositionBadge(count: Int, modifier: Modifier = Modifier) {
    if (count > 0) {
        Surface(
            modifier = modifier.size(28.dp),
            shape = CircleShape,
            color = PurpleAccent,
            shadowElevation = 2.dp,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun TrackingStatsCard(stats: ComposeRenderStats, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PurpleAccent.copy(alpha = 0.1f),
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = "Tracking Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "Composables",
                    value = stats.totalComposables.toString(),
                )
                StatItem(
                    label = "Recompositions",
                    value = stats.totalRecompositions.toString(),
                )
                StatItem(
                    label = "Avg Ratio",
                    value = "%.1f".format(stats.averageRecomposeRatio),
                )
            }

            if (stats.mostRecomposed != null) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
                Text(
                    text = "Most recomposed: ${stats.mostRecomposed}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PurpleAccent,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ComposableInfoRow(info: ComposeRenderInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.composableName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (info.parameters.isNotEmpty()) {
                    Text(
                        text = info.parameters.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${info.recomposeCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent,
                    )
                    Text(
                        text = "recomps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (info.averageRenderTimeNs > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "%.2f".format(info.averageRenderTimeNs / 1_000_000.0),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "ms avg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
