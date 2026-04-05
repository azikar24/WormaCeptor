package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.util

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
