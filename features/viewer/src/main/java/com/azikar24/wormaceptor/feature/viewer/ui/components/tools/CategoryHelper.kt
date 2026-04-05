package com.azikar24.wormaceptor.feature.viewer.ui.components.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

internal object CategoryHelper {
    fun forCategory(name: String): Color = when (name.lowercase()) {
        "inspection" -> WormaCeptorTokens.Colors.Category.inspection
        "performance" -> WormaCeptorTokens.Colors.Category.performance
        "network" -> WormaCeptorTokens.Colors.Category.network
        "simulation" -> WormaCeptorTokens.Colors.Category.simulation
        "core" -> WormaCeptorTokens.Colors.Category.core
        "favorites" -> WormaCeptorTokens.Colors.Category.favorites
        else -> WormaCeptorTokens.Colors.Category.fallback
    }

    fun iconForCategory(name: String): ImageVector = when (name.lowercase()) {
        "inspection" -> Icons.Default.Explore
        "performance" -> Icons.Default.Speed
        "network" -> Icons.Default.Cable
        "simulation" -> Icons.Default.LocationOn
        "core" -> Icons.Default.Code
        else -> Icons.Default.BugReport
    }
}
