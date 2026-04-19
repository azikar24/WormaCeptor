package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.DependencyCategory

internal fun DependencyCategory.shortLabel(): String = when (this) {
    DependencyCategory.NETWORKING -> "Net"
    DependencyCategory.DEPENDENCY_INJECTION -> "DI"
    DependencyCategory.UI_FRAMEWORK -> "UI"
    DependencyCategory.IMAGE_LOADING -> "Img"
    DependencyCategory.SERIALIZATION -> "Ser"
    DependencyCategory.DATABASE -> "DB"
    DependencyCategory.REACTIVE -> "Rx"
    DependencyCategory.LOGGING -> "Log"
    DependencyCategory.ANALYTICS -> "Ana"
    DependencyCategory.TESTING -> "Test"
    DependencyCategory.SECURITY -> "Sec"
    DependencyCategory.UTILITY -> "Util"
    DependencyCategory.ANDROIDX -> "AX"
    DependencyCategory.KOTLIN -> "KT"
    DependencyCategory.OTHER -> "Other"
}

internal fun DependencyCategory.categoryColor(colors: ToolColors.DependenciesInspector.Scheme): Color = when (this) {
    DependencyCategory.NETWORKING -> colors.networking
    DependencyCategory.DEPENDENCY_INJECTION -> colors.dependencyInjection
    DependencyCategory.UI_FRAMEWORK -> colors.uiFramework
    DependencyCategory.IMAGE_LOADING -> colors.imageLoading
    DependencyCategory.SERIALIZATION -> colors.serialization
    DependencyCategory.DATABASE -> colors.database
    DependencyCategory.REACTIVE -> colors.reactive
    DependencyCategory.LOGGING -> colors.logging
    DependencyCategory.ANALYTICS -> colors.analytics
    DependencyCategory.TESTING -> colors.testing
    DependencyCategory.SECURITY -> colors.security
    DependencyCategory.UTILITY -> colors.utility
    DependencyCategory.ANDROIDX -> colors.androidx
    DependencyCategory.KOTLIN -> colors.kotlin
    DependencyCategory.OTHER -> colors.other
}
