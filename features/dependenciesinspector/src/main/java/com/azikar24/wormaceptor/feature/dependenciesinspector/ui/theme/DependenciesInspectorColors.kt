/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.entities.DependencyCategory

/**
 * Colors for the Dependencies Inspector feature.
 *
 * Uses a consistent color scheme to represent different dependency categories:
 * - Networking: Blue - connectivity
 * - DI: Purple - framework infrastructure
 * - UI: Green - visual components
 * - Image: Cyan - media handling
 * - Serialization: Orange - data transformation
 * - Database: Amber - persistence
 * - Reactive: Pink - async patterns
 */
@Immutable
data class DependenciesInspectorColors(
    // Primary accent
    val primary: Color,

    // Category colors
    val networking: Color,
    val dependencyInjection: Color,
    val uiFramework: Color,
    val imageLoading: Color,
    val serialization: Color,
    val database: Color,
    val reactive: Color,
    val logging: Color,
    val analytics: Color,
    val testing: Color,
    val security: Color,
    val utility: Color,
    val androidx: Color,
    val kotlin: Color,
    val other: Color,

    // Version status colors
    val versionDetected: Color,
    val versionUnknown: Color,
    val highConfidence: Color,
    val mediumConfidence: Color,
    val lowConfidence: Color,

    // Background colors
    val cardBackground: Color,
    val chipBackground: Color,
    val selectedChipBackground: Color,
    val searchBackground: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
    val versionText: Color,

    // Accent colors
    val divider: Color,
    val link: Color,
) {
    /**
     * Returns the appropriate color for a dependency category.
     */
    fun colorForCategory(category: DependencyCategory): Color = when (category) {
        DependencyCategory.NETWORKING -> networking
        DependencyCategory.DEPENDENCY_INJECTION -> dependencyInjection
        DependencyCategory.UI_FRAMEWORK -> uiFramework
        DependencyCategory.IMAGE_LOADING -> imageLoading
        DependencyCategory.SERIALIZATION -> serialization
        DependencyCategory.DATABASE -> database
        DependencyCategory.REACTIVE -> reactive
        DependencyCategory.LOGGING -> logging
        DependencyCategory.ANALYTICS -> analytics
        DependencyCategory.TESTING -> testing
        DependencyCategory.SECURITY -> security
        DependencyCategory.UTILITY -> utility
        DependencyCategory.ANDROIDX -> androidx
        DependencyCategory.KOTLIN -> kotlin
        DependencyCategory.OTHER -> other
    }

    /**
     * Returns color based on confidence level.
     */
    fun colorForConfidence(confidence: String): Color = when (confidence) {
        "High" -> highConfidence
        "Medium" -> mediumConfidence
        else -> lowConfidence
    }
}

/**
 * Light theme colors for Dependencies Inspector feature.
 */
val LightDependenciesInspectorColors = DependenciesInspectorColors(
    // Primary accent
    primary = Color(0xFF6200EE), // Deep Purple

    // Category colors - vibrant for visibility
    networking = Color(0xFF2196F3), // Blue 500
    dependencyInjection = Color(0xFF9C27B0), // Purple 500
    uiFramework = Color(0xFF4CAF50), // Green 500
    imageLoading = Color(0xFF00BCD4), // Cyan 500
    serialization = Color(0xFFFF9800), // Orange 500
    database = Color(0xFFFFC107), // Amber 500
    reactive = Color(0xFFE91E63), // Pink 500
    logging = Color(0xFF795548), // Brown 500
    analytics = Color(0xFF3F51B5), // Indigo 500
    testing = Color(0xFF009688), // Teal 500
    security = Color(0xFFF44336), // Red 500
    utility = Color(0xFF607D8B), // Blue Gray 500
    androidx = Color(0xFF03A9F4), // Light Blue 500
    kotlin = Color(0xFF7C4DFF), // Deep Purple A200
    other = Color(0xFF9E9E9E), // Gray 500

    // Version status colors
    versionDetected = Color(0xFF4CAF50), // Green
    versionUnknown = Color(0xFFFF9800), // Orange
    highConfidence = Color(0xFF4CAF50), // Green
    mediumConfidence = Color(0xFFFFC107), // Amber
    lowConfidence = Color(0xFFFF9800), // Orange

    // Background colors
    cardBackground = Color(0xFFFAFAFA),
    chipBackground = Color(0xFFE0E0E0),
    selectedChipBackground = Color(0xFF6200EE),
    searchBackground = Color(0xFFF5F5F5),

    // Text colors
    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    valuePrimary = Color(0xFF424242),
    versionText = Color(0xFF1B5E20), // Green 800

    // Accent colors
    divider = Color(0xFFE0E0E0),
    link = Color(0xFF1976D2),
)

/**
 * Dark theme colors for Dependencies Inspector feature.
 */
val DarkDependenciesInspectorColors = DependenciesInspectorColors(
    // Primary accent
    primary = Color(0xFFBB86FC), // Purple 200

    // Category colors - slightly muted for dark backgrounds
    networking = Color(0xFF64B5F6), // Blue 300
    dependencyInjection = Color(0xFFBA68C8), // Purple 300
    uiFramework = Color(0xFF81C784), // Green 300
    imageLoading = Color(0xFF4DD0E1), // Cyan 300
    serialization = Color(0xFFFFB74D), // Orange 300
    database = Color(0xFFFFD54F), // Amber 300
    reactive = Color(0xFFF06292), // Pink 300
    logging = Color(0xFFA1887F), // Brown 300
    analytics = Color(0xFF7986CB), // Indigo 300
    testing = Color(0xFF4DB6AC), // Teal 300
    security = Color(0xFFE57373), // Red 300
    utility = Color(0xFF90A4AE), // Blue Gray 300
    androidx = Color(0xFF4FC3F7), // Light Blue 300
    kotlin = Color(0xFFB388FF), // Deep Purple A100
    other = Color(0xFFBDBDBD), // Gray 400

    // Version status colors
    versionDetected = Color(0xFF81C784), // Green 300
    versionUnknown = Color(0xFFFFB74D), // Orange 300
    highConfidence = Color(0xFF81C784), // Green 300
    mediumConfidence = Color(0xFFFFD54F), // Amber 300
    lowConfidence = Color(0xFFFFB74D), // Orange 300

    // Background colors
    cardBackground = Color(0xFF1E1E1E),
    chipBackground = Color(0xFF424242),
    selectedChipBackground = Color(0xFF7C4DFF),
    searchBackground = Color(0xFF2D2D2D),

    // Text colors
    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    valuePrimary = Color(0xFFBDBDBD),
    versionText = Color(0xFFA5D6A7), // Green 200

    // Accent colors
    divider = Color(0xFF424242),
    link = Color(0xFF64B5F6),
)

/**
 * Returns the appropriate colors based on the current theme.
 */
@Composable
fun dependenciesInspectorColors(darkTheme: Boolean = isSystemInDarkTheme()): DependenciesInspectorColors {
    return if (darkTheme) DarkDependenciesInspectorColors else LightDependenciesInspectorColors
}

/**
 * Returns a short label for category display in chips.
 */
fun DependencyCategory.shortLabel(): String = when (this) {
    DependencyCategory.NETWORKING -> "Network"
    DependencyCategory.DEPENDENCY_INJECTION -> "DI"
    DependencyCategory.UI_FRAMEWORK -> "UI"
    DependencyCategory.IMAGE_LOADING -> "Image"
    DependencyCategory.SERIALIZATION -> "Serial"
    DependencyCategory.DATABASE -> "DB"
    DependencyCategory.REACTIVE -> "Reactive"
    DependencyCategory.LOGGING -> "Log"
    DependencyCategory.ANALYTICS -> "Analytics"
    DependencyCategory.TESTING -> "Test"
    DependencyCategory.SECURITY -> "Security"
    DependencyCategory.UTILITY -> "Util"
    DependencyCategory.ANDROIDX -> "AndroidX"
    DependencyCategory.KOTLIN -> "Kotlin"
    DependencyCategory.OTHER -> "Other"
}
