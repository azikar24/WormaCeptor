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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.azikar24.wormaceptor.feature.composerender.ComposeRenderTracker
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme

/**
 * Test activity for the Compose Render Tracker feature.
 * Provides test composables that trigger recompositions.
 */
class ComposeRenderTestActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start tracking
        ComposeRenderEngine.getInstance().startTracking()

        setContent {
            WormaCeptorMainTheme {
                var showTracker by remember { mutableStateOf(false) }

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
                                        tint = Color(0xFF9C27B0),
                                    )
                                    Text(
                                        text = if (showTracker) "Compose Tracker" else "Compose Test",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            },
                            actions = {
                                OutlinedButton(
                                    onClick = { showTracker = !showTracker },
                                ) {
                                    Text(if (showTracker) "Test UI" else "Tracker")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        )
                    },
                ) { padding ->
                    if (showTracker) {
                        ComposeRenderTracker(
                            onNavigateBack = { showTracker = false },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                        )
                    } else {
                        RecompositionTestContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                        )
                    }
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
private fun RecompositionTestContent(modifier: Modifier = Modifier) {
    val engine = remember { ComposeRenderEngine.getInstance() }

    var counter by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        Text(
            text = "Trigger recompositions to see them tracked",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        // Counter section - triggers recomposition
        engine.trackRecomposition("CounterSection")
        CounterSection(
            count = counter,
            onIncrement = { counter++ },
            onDecrement = { counter-- },
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        // Animation section - triggers many recompositions
        engine.trackRecomposition("AnimationSection")
        AnimationSection(
            isAnimating = isAnimating,
            onToggle = { isAnimating = !isAnimating },
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        // Color section - triggers recomposition on color change
        engine.trackRecomposition("ColorSection")
        ColorSection(count = counter)

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        // Info text
        Text(
            text = "Tap 'Tracker' in the top right to see tracked recompositions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun CounterSection(count: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    val engine = remember { ComposeRenderEngine.getInstance() }
    engine.trackRecomposition("CounterSection.Content", listOf("count=$count"))

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
}

@Composable
private fun AnimationSection(isAnimating: Boolean, onToggle: () -> Unit) {
    val engine = remember { ComposeRenderEngine.getInstance() }
    engine.trackRecomposition("AnimationSection.Content", listOf("isAnimating=$isAnimating"))

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        label = "scale",
    )

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
}

@Composable
private fun ColorSection(count: Int) {
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
                                color.copy(
                                    alpha = 0.3f,
                                )
                            },
                            RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                        ),
                )
            }
        }
    }
}
